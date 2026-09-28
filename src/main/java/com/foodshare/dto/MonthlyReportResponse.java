package com.foodshare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportResponse {
    private int year;
    private int month;
    private long totalListingsDiverted;
    private double totalQuantityDiverted;
    private Map<String, Double> quantityByUnit;
    private String summary;
}
