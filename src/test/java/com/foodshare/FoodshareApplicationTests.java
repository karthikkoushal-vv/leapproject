package com.foodshare;

import com.foodshare.dto.*;
import com.foodshare.exception.ListingAlreadyClaimedException;
import com.foodshare.exception.ListingExpiredException;
import com.foodshare.model.*;
import com.foodshare.repository.ClaimRepository;
import com.foodshare.repository.DonorRepository;
import com.foodshare.repository.FoodListingRepository;
import com.foodshare.repository.NGORepository;
import com.foodshare.scheduler.ExpiryScheduler;
import com.foodshare.service.AnalyticsService;
import com.foodshare.service.ClaimService;
import com.foodshare.service.FoodListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FoodshareApplicationTests {

    @Autowired
    private FoodListingService foodListingService;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private ExpiryScheduler expiryScheduler;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private NGORepository ngoRepository;

    @Autowired
    private FoodListingRepository foodListingRepository;

    @Autowired
    private ClaimRepository claimRepository;

    private Donor testDonor;
    private NGO testNgo1;
    private NGO testNgo2;

    @BeforeEach
    void setUp() {
        claimRepository.deleteAll();
        foodListingRepository.deleteAll();

        testDonor = donorRepository.findAll().stream().findFirst().orElseGet(() ->
                donorRepository.save(Donor.builder()
                        .name("Test Canteen")
                        .email("testcanteen@campus.edu")
                        .phone("9999999999")
                        .address("Campus Block A")
                        .build())
        );

        List<NGO> ngos = ngoRepository.findAll();
        if (ngos.size() >= 2) {
            testNgo1 = ngos.get(0);
            testNgo2 = ngos.get(1);
        } else {
            testNgo1 = ngoRepository.save(NGO.builder()
                    .name("Test NGO 1")
                    .contactPerson("Person 1")
                    .email("ngo1@test.org")
                    .phone("8888888881")
                    .address("Shelter 1")
                    .build());
            testNgo2 = ngoRepository.save(NGO.builder()
                    .name("Test NGO 2")
                    .contactPerson("Person 2")
                    .email("ngo2@test.org")
                    .phone("8888888882")
                    .address("Shelter 2")
                    .build());
        }
    }

    @Test
    @DisplayName("Feature 1: Donor lists surplus food with quantity, type and safe-to-eat-until time")
    void testCreateFoodListing() {
        FoodListingRequest request = FoodListingRequest.builder()
                .donorId(testDonor.getId())
                .foodName("Surplus Biryani")
                .foodType(FoodType.NON_VEG)
                .quantity(25.0)
                .unit("kg")
                .safeToEatUntil(LocalDateTime.now().plusHours(3))
                .build();

        FoodListingResponse response = foodListingService.createListing(request);

        assertNotNull(response.getId());
        assertEquals("Surplus Biryani", response.getFoodName());
        assertEquals(25.0, response.getQuantity());
        assertEquals(ListingStatus.AVAILABLE, response.getStatus());
    }

    @Test
    @DisplayName("Feature 2: NGO browses and claims an available listing")
    void testClaimAvailableListing() {
        FoodListingRequest listingRequest = FoodListingRequest.builder()
                .donorId(testDonor.getId())
                .foodName("Veg Pulao")
                .foodType(FoodType.VEG)
                .quantity(30.0)
                .unit("plates")
                .safeToEatUntil(LocalDateTime.now().plusHours(4))
                .build();

        FoodListingResponse listing = foodListingService.createListing(listingRequest);

        // Browse available listings
        List<FoodListingResponse> available = foodListingService.getAvailableListings();
        assertTrue(available.stream().anyMatch(l -> l.getId().equals(listing.getId())));

        // Claim it
        ClaimRequest claimRequest = ClaimRequest.builder()
                .listingId(listing.getId())
                .ngoId(testNgo1.getId())
                .build();

        ClaimResponse claimResponse = claimService.claimListing(claimRequest);

        assertNotNull(claimResponse.getId());
        assertEquals(ClaimStatus.ACTIVE, claimResponse.getStatus());
        assertEquals(testNgo1.getId(), claimResponse.getNgoId());

        // Listing should now be CLAIMED
        FoodListing updatedListing = foodListingRepository.findById(listing.getId()).orElseThrow();
        assertEquals(ListingStatus.CLAIMED, updatedListing.getStatus());
    }

    @Test
    @DisplayName("Business Rule 1: A listing past its safe-to-eat-until time can no longer be claimed")
    void testCannotClaimExpiredListing() {
        // Create an expired listing directly in repository
        FoodListing expiredListing = FoodListing.builder()
                .donor(testDonor)
                .foodName("Expired Sandwiches")
                .foodType(FoodType.VEG)
                .quantity(10.0)
                .unit("packets")
                .safeToEatUntil(LocalDateTime.now().minusMinutes(10)) // past time!
                .status(ListingStatus.AVAILABLE)
                .build();
        expiredListing = foodListingRepository.save(expiredListing);

        ClaimRequest claimRequest = ClaimRequest.builder()
                .listingId(expiredListing.getId())
                .ngoId(testNgo1.getId())
                .build();

        assertThrows(ListingExpiredException.class, () -> {
            claimService.claimListing(claimRequest);
        });
    }

    @Test
    @DisplayName("Business Rule 2: Only one NGO can hold an active claim on a listing at a time")
    void testOnlyOneNgoCanClaimListing() {
        FoodListingRequest listingRequest = FoodListingRequest.builder()
                .donorId(testDonor.getId())
                .foodName("Idli and Sambar")
                .foodType(FoodType.VEG)
                .quantity(50.0)
                .unit("servings")
                .safeToEatUntil(LocalDateTime.now().plusHours(2))
                .build();

        FoodListingResponse listing = foodListingService.createListing(listingRequest);

        // First NGO claims
        ClaimRequest claim1 = ClaimRequest.builder()
                .listingId(listing.getId())
                .ngoId(testNgo1.getId())
                .build();
        claimService.claimListing(claim1);

        // Second NGO attempts to claim the same listing -> MUST THROW EXCEPTION
        ClaimRequest claim2 = ClaimRequest.builder()
                .listingId(listing.getId())
                .ngoId(testNgo2.getId())
                .build();

        assertThrows(ListingAlreadyClaimedException.class, () -> {
            claimService.claimListing(claim2);
        });
    }

    @Test
    @DisplayName("Feature 3: Mark a listing collected once picked up")
    void testMarkListingCollected() {
        FoodListingRequest listingRequest = FoodListingRequest.builder()
                .donorId(testDonor.getId())
                .foodName("Fruit Salad")
                .foodType(FoodType.VEG)
                .quantity(15.0)
                .unit("kg")
                .safeToEatUntil(LocalDateTime.now().plusHours(3))
                .build();

        FoodListingResponse listing = foodListingService.createListing(listingRequest);

        ClaimResponse claim = claimService.claimListing(ClaimRequest.builder()
                .listingId(listing.getId())
                .ngoId(testNgo1.getId())
                .build());

        ClaimResponse collectedClaim = claimService.markListingCollected(claim.getId());

        assertEquals(ClaimStatus.COLLECTED, collectedClaim.getStatus());
        assertNotNull(collectedClaim.getCollectedAt());

        // Verify listing also marked COLLECTED
        FoodListing updatedListing = foodListingRepository.findById(listing.getId()).orElseThrow();
        assertEquals(ListingStatus.COLLECTED, updatedListing.getStatus());
    }

    @Test
    @DisplayName("Feature 4: Auto-expire a listing that passes safe-to-eat-until time unclaimed")
    void testAutoExpireScheduler() {
        FoodListing pastListing = FoodListing.builder()
                .donor(testDonor)
                .foodName("Past Samosas")
                .foodType(FoodType.VEG)
                .quantity(40.0)
                .unit("pieces")
                .safeToEatUntil(LocalDateTime.now().minusMinutes(5)) // passed
                .status(ListingStatus.AVAILABLE)
                .build();
        pastListing = foodListingRepository.save(pastListing);

        // Trigger scheduler
        expiryScheduler.autoExpireUnclaimedListings();

        FoodListing reloaded = foodListingRepository.findById(pastListing.getId()).orElseThrow();
        assertEquals(ListingStatus.EXPIRED, reloaded.getStatus());
    }

    @Test
    @DisplayName("Feature 5: View total food diverted from waste per month")
    void testMonthlyDivertedReport() {
        LocalDateTime now = LocalDateTime.now();

        // Create, claim, and collect a food listing
        FoodListingRequest req = FoodListingRequest.builder()
                .donorId(testDonor.getId())
                .foodName("Chapathi Rolls")
                .foodType(FoodType.VEG)
                .quantity(60.0)
                .unit("rolls")
                .safeToEatUntil(now.plusHours(2))
                .build();

        FoodListingResponse listing = foodListingService.createListing(req);

        ClaimResponse claim = claimService.claimListing(ClaimRequest.builder()
                .listingId(listing.getId())
                .ngoId(testNgo1.getId())
                .build());

        claimService.markListingCollected(claim.getId());

        // Query report
        MonthlyReportResponse report = analyticsService.getMonthlyWasteDiversion(now.getYear(), now.getMonthValue());

        assertTrue(report.getTotalListingsDiverted() >= 1);
        assertTrue(report.getTotalQuantityDiverted() >= 60.0);
        assertTrue(report.getQuantityByUnit().containsKey("rolls"));
    }
}
