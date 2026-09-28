package com.foodshare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRequest {

    @NotNull(message = "Food Listing ID is required")
    private Long listingId;

    @NotNull(message = "NGO ID is required")
    private Long ngoId;
}
