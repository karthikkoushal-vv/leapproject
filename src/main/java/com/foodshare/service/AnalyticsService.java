package com.foodshare.service;

import com.foodshare.dto.MonthlyReportResponse;
import com.foodshare.model.Claim;
import com.foodshare.model.ClaimStatus;
import com.foodshare.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClaimRepository claimRepository;

    public MonthlyReportResponse getMonthlyWasteDiversion(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);

        List<Claim> collectedClaims = claimRepository.findByStatusAndCollectedAtBetween(
                ClaimStatus.COLLECTED,
                startOfMonth,
                endOfMonth
        );

        double totalQuantity = 0.0;
        Map<String, Double> quantityByUnit = new HashMap<>();

        for (Claim claim : collectedClaims) {
            Double qty = claim.getFoodListing().getQuantity();
            String unit = claim.getFoodListing().getUnit().toLowerCase();

            totalQuantity += (qty != null ? qty : 0.0);
            quantityByUnit.put(unit, quantityByUnit.getOrDefault(unit, 0.0) + (qty != null ? qty : 0.0));
        }

        String summary = String.format(
                "In %s %d, FoodShare successfully diverted %d food listings from waste, saving a total of %.1f units of food.",
                yearMonth.getMonth().name(),
                year,
                collectedClaims.size(),
                totalQuantity
        );

        return MonthlyReportResponse.builder()
                .year(year)
                .month(month)
                .totalListingsDiverted(collectedClaims.size())
                .totalQuantityDiverted(totalQuantity)
                .quantityByUnit(quantityByUnit)
                .summary(summary)
                .build();
    }
}
