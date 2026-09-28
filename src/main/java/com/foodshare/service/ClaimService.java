package com.foodshare.service;

import com.foodshare.dto.ClaimRequest;
import com.foodshare.dto.ClaimResponse;
import com.foodshare.exception.InvalidOperationException;
import com.foodshare.exception.ListingAlreadyClaimedException;
import com.foodshare.exception.ListingExpiredException;
import com.foodshare.exception.ResourceNotFoundException;
import com.foodshare.model.*;
import com.foodshare.repository.ClaimRepository;
import com.foodshare.repository.FoodListingRepository;
import com.foodshare.repository.NGORepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);

    private final ClaimRepository claimRepository;
    private final FoodListingRepository foodListingRepository;
    private final NGORepository ngoRepository;

    @Transactional
    public ClaimResponse claimListing(ClaimRequest request) {
        // 1. Fetch Food Listing
        FoodListing listing = foodListingRepository.findById(request.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Food listing not found with ID: " + request.getListingId()));

        // 2. Fetch NGO
        NGO ngo = ngoRepository.findById(request.getNgoId())
                .orElseThrow(() -> new ResourceNotFoundException("NGO not found with ID: " + request.getNgoId()));

        LocalDateTime now = LocalDateTime.now();

        // BUSINESS RULE 1: A listing past its safe-to-eat-until time can no longer be claimed
        if (listing.getSafeToEatUntil().isBefore(now)) {
            listing.setStatus(ListingStatus.EXPIRED);
            foodListingRepository.save(listing);
            log.warn("CLAIM REJECTED: Listing ID {} is past safe-to-eat-until time ({}). Marked as EXPIRED.",
                    listing.getId(), listing.getSafeToEatUntil());
            throw new ListingExpiredException("Cannot claim listing: The safe-to-eat-until time ("
                    + listing.getSafeToEatUntil() + ") has already passed.");
        }

        // BUSINESS RULE 2: Only one NGO can hold an active claim on a listing at a time
        if (listing.getStatus() != ListingStatus.AVAILABLE ||
                claimRepository.existsByFoodListingIdAndStatus(listing.getId(), ClaimStatus.ACTIVE)) {
            log.warn("CLAIM REJECTED: Listing ID {} is already claimed or not available. Current status: {}",
                    listing.getId(), listing.getStatus());
            throw new ListingAlreadyClaimedException("This food listing has already been claimed or is unavailable.");
        }

        // 3. Create Claim
        Claim claim = Claim.builder()
                .foodListing(listing)
                .ngo(ngo)
                .claimedAt(now)
                .status(ClaimStatus.ACTIVE)
                .build();

        // Update listing status to CLAIMED
        listing.setStatus(ListingStatus.CLAIMED);
        foodListingRepository.save(listing);

        Claim savedClaim = claimRepository.save(claim);
        log.info("NOTIFICATION: Food listing ID {} successfully claimed by NGO '{}' (ID: {}). Status: CLAIMED.",
                listing.getId(), ngo.getName(), ngo.getId());

        return mapToResponse(savedClaim);
    }

    @Transactional
    public ClaimResponse markListingCollected(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        if (claim.getStatus() == ClaimStatus.COLLECTED) {
            throw new InvalidOperationException("This listing has already been collected on " + claim.getCollectedAt());
        }

        if (claim.getStatus() != ClaimStatus.ACTIVE) {
            throw new InvalidOperationException("Cannot collect listing. Current claim status is: " + claim.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        claim.setStatus(ClaimStatus.COLLECTED);
        claim.setCollectedAt(now);

        // Update linked FoodListing status to COLLECTED
        FoodListing listing = claim.getFoodListing();
        listing.setStatus(ListingStatus.COLLECTED);
        foodListingRepository.save(listing);

        Claim updatedClaim = claimRepository.save(claim);
        log.info("NOTIFICATION: Claim ID {} for food '{}' marked as COLLECTED at {} by NGO '{}'. Status: COLLECTED.",
                updatedClaim.getId(), listing.getFoodName(), now, claim.getNgo().getName());

        return mapToResponse(updatedClaim);
    }

    public List<ClaimResponse> getAllClaims() {
        return claimRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ClaimResponse> getClaimsByNgo(Long ngoId) {
        return claimRepository.findByNgoId(ngoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClaimResponse getClaimById(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + id));
        return mapToResponse(claim);
    }

    private ClaimResponse mapToResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .listingId(claim.getFoodListing().getId())
                .foodName(claim.getFoodListing().getFoodName())
                .quantity(claim.getFoodListing().getQuantity())
                .unit(claim.getFoodListing().getUnit())
                .ngoId(claim.getNgo().getId())
                .ngoName(claim.getNgo().getName())
                .ngoPhone(claim.getNgo().getPhone())
                .claimedAt(claim.getClaimedAt())
                .status(claim.getStatus())
                .collectedAt(claim.getCollectedAt())
                .build();
    }
}
