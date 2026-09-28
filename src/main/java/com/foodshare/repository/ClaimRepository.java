package com.foodshare.repository;

import com.foodshare.model.Claim;
import com.foodshare.model.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    // Find claim by FoodListing ID
    Optional<Claim> findByFoodListingId(Long foodListingId);

    // Check if an active claim exists for this listing
    boolean existsByFoodListingIdAndStatus(Long foodListingId, ClaimStatus status);

    // Find claims by NGO ID
    List<Claim> findByNgoId(Long ngoId);

    // Find claims collected within a date range for monthly waste diversion reporting
    List<Claim> findByStatusAndCollectedAtBetween(
            ClaimStatus status,
            LocalDateTime start,
            LocalDateTime end
    );
}
