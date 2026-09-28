package com.foodshare.scheduler;

import com.foodshare.model.FoodListing;
import com.foodshare.model.ListingStatus;
import com.foodshare.repository.FoodListingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiryScheduler.class);

    private final FoodListingRepository foodListingRepository;

    // Runs every 60 seconds (1 minute) to check for expired listings
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoExpireUnclaimedListings() {
        LocalDateTime now = LocalDateTime.now();
        List<FoodListing> expiredListings = foodListingRepository.findByStatusAndSafeToEatUntilBefore(
                ListingStatus.AVAILABLE,
                now
        );

        if (!expiredListings.isEmpty()) {
            for (FoodListing listing : expiredListings) {
                listing.setStatus(ListingStatus.EXPIRED);
                log.warn("NOTIFICATION [AUTO-EXPIRE]: Listing ID {} ('{}') passed safe-to-eat time ({}). Marked as EXPIRED.",
                        listing.getId(), listing.getFoodName(), listing.getSafeToEatUntil());
            }
            foodListingRepository.saveAll(expiredListings);
            log.info("AUTO-EXPIRE JOB: Successfully marked {} unclaimed listings as EXPIRED.", expiredListings.size());
        }
    }
}
