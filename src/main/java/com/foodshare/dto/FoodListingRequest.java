package com.foodshare.dto;

import com.foodshare.model.FoodType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodListingRequest {

    @NotNull(message = "Donor ID is required")
    private Long donorId;

    @NotBlank(message = "Food name is required")
    private String foodName;

    @NotNull(message = "Food type is required (VEG, NON_VEG, COOKED, RAW)")
    private FoodType foodType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Double quantity;

    @NotBlank(message = "Unit is required (e.g. kg, plates, boxes)")
    private String unit;

    @NotNull(message = "Safe-to-eat-until time is required")
    @Future(message = "Safe-to-eat-until time must be in the future")
    private LocalDateTime safeToEatUntil;
}
