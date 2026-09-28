package com.foodshare.dto;

import com.foodshare.model.FoodType;
import com.foodshare.model.ListingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodListingResponse {
    private Long id;
    private Long donorId;
    private String donorName;
    private String donorPhone;
    private String donorAddress;
    private String foodName;
    private FoodType foodType;
    private Double quantity;
    private String unit;
    private LocalDateTime safeToEatUntil;
    private ListingStatus status;
    private LocalDateTime createdAt;
}
