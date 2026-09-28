package com.foodshare.controller;

import com.foodshare.dto.MonthlyReportResponse;
import com.foodshare.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Feature 5: View total food (by weight/quantity) diverted from waste per month
    @GetMapping("/monthly-diverted")
    public ResponseEntity<MonthlyReportResponse> getMonthlyWasteDiversion(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        MonthlyReportResponse report = analyticsService.getMonthlyWasteDiversion(targetYear, targetMonth);
        return ResponseEntity.ok(report);
    }
}
