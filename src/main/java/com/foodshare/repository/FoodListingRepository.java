package com.foodshare.repository;

import com.foodshare.model.FoodListing;
import com.foodshare.model.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodListingRepository extends JpaRepository<FoodListing, Long> {

    // Find all active available listings that have not expired yet
    List<FoodListing> findByStatusAndSafeToEatUntilAfterOrderBySafeToEatUntilAsc(
            ListingStatus status,
            LocalDateTime currentTime
    );

    // Find all available listings that passed safe-to-eat-until time
    List<FoodListing> findByStatusAndSafeToEatUntilBefore(
            ListingStatus status,
            LocalDateTime currentTime
    );

    // Find listings by donor
    List<FoodListing> findByDonorId(Long donorId);
}
