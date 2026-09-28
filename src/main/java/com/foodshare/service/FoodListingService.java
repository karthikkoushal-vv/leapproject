package com.foodshare.service;

import com.foodshare.dto.FoodListingRequest;
import com.foodshare.dto.FoodListingResponse;
import com.foodshare.exception.ResourceNotFoundException;
import com.foodshare.model.Donor;
import com.foodshare.model.FoodListing;
import com.foodshare.model.ListingStatus;
import com.foodshare.repository.DonorRepository;
import com.foodshare.repository.FoodListingRepository;
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
public class FoodListingService {

    private static final Logger log = LoggerFactory.getLogger(FoodListingService.class);

    private final FoodListingRepository foodListingRepository;
    private final DonorRepository donorRepository;

    @Transactional
    public FoodListingResponse createListing(FoodListingRequest request) {
        Donor donor = donorRepository.findById(request.getDonorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with ID: " + request.getDonorId()));

        FoodListing listing = FoodListing.builder()
                .donor(donor)
                .foodName(request.getFoodName())
                .foodType(request.getFoodType())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .safeToEatUntil(request.getSafeToEatUntil())
                .status(ListingStatus.AVAILABLE)
                .build();

        FoodListing saved = foodListingRepository.save(listing);
        log.info("NOTIFICATION: New food listing created - ID: {}, Food: {}, Quantity: {} {}, Safe until: {}, Donor: {}",
                saved.getId(), saved.getFoodName(), saved.getQuantity(), saved.getUnit(), saved.getSafeToEatUntil(), donor.getName());

        return mapToResponse(saved);
    }

    public List<FoodListingResponse> getAvailableListings() {
        return foodListingRepository
                .findByStatusAndSafeToEatUntilAfterOrderBySafeToEatUntilAsc(
                        ListingStatus.AVAILABLE,
                        LocalDateTime.now()
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FoodListingResponse> getAllListings() {
        return foodListingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FoodListingResponse getListingById(Long id) {
        FoodListing listing = foodListingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food listing not found with ID: " + id));
        return mapToResponse(listing);
    }

    public FoodListingResponse mapToResponse(FoodListing listing) {
        return FoodListingResponse.builder()
                .id(listing.getId())
                .donorId(listing.getDonor().getId())
                .donorName(listing.getDonor().getName())
                .donorPhone(listing.getDonor().getPhone())
                .donorAddress(listing.getDonor().getAddress())
                .foodName(listing.getFoodName())
                .foodType(listing.getFoodType())
                .quantity(listing.getQuantity())
                .unit(listing.getUnit())
                .safeToEatUntil(listing.getSafeToEatUntil())
                .status(listing.getStatus())
                .createdAt(listing.getCreatedAt())
                .build();
    }
}
