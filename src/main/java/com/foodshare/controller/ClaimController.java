package com.foodshare.controller;

import com.foodshare.dto.ClaimRequest;
import com.foodshare.dto.ClaimResponse;
import com.foodshare.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    // Feature 2: NGO claims an available listing
    @PostMapping
    public ResponseEntity<ClaimResponse> claimListing(@Valid @RequestBody ClaimRequest request) {
        ClaimResponse response = claimService.claimListing(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Feature 3: Mark a listing collected once picked up
    @PutMapping("/{id}/collect")
    public ResponseEntity<ClaimResponse> markCollected(@PathVariable Long id) {
        ClaimResponse response = claimService.markListingCollected(id);
        return ResponseEntity.ok(response);
    }

    // Get all claims
    @GetMapping
    public ResponseEntity<List<ClaimResponse>> getAllClaims() {
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    // Get claim by ID
    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.getClaimById(id));
    }

    // Get claims by NGO ID
    @GetMapping("/ngo/{ngoId}")
    public ResponseEntity<List<ClaimResponse>> getClaimsByNgo(@PathVariable Long ngoId) {
        return ResponseEntity.ok(claimService.getClaimsByNgo(ngoId));
    }
}
