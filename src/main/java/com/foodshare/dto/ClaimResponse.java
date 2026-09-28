package com.foodshare.dto;

import com.foodshare.model.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
    private Long id;
    private Long listingId;
    private String foodName;
    private Double quantity;
    private String unit;
    private Long ngoId;
    private String ngoName;
    private String ngoPhone;
    private LocalDateTime claimedAt;
    private ClaimStatus status;
    private LocalDateTime collectedAt;
}
