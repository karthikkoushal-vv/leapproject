package com.foodshare.controller;

import com.foodshare.dto.FoodListingRequest;
import com.foodshare.dto.FoodListingResponse;
import com.foodshare.service.FoodListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FoodListingController {

    private final FoodListingService foodListingService;

    // Feature 1: Donor lists surplus food with quantity, type and safe-to-eat-until time
    @PostMapping
    public ResponseEntity<FoodListingResponse> createListing(@Valid @RequestBody FoodListingRequest request) {
        FoodListingResponse response = foodListingService.createListing(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Feature 2: NGO browses available listings
    @GetMapping("/available")
    public ResponseEntity<List<FoodListingResponse>> getAvailableListings() {
        return ResponseEntity.ok(foodListingService.getAvailableListings());
    }

    // Get all listings (regardless of status)
    @GetMapping
    public ResponseEntity<List<FoodListingResponse>> getAllListings() {
        return ResponseEntity.ok(foodListingService.getAllListings());
    }

    // Get a single listing by ID
    @GetMapping("/{id}")
    public ResponseEntity<FoodListingResponse> getListingById(@PathVariable Long id) {
        return ResponseEntity.ok(foodListingService.getListingById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodListingResponse> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody FoodListingRequest request
    ) {
        return ResponseEntity.ok(foodListingService.updateListing(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable Long id) {
        foodListingService.deleteListing(id);
        return ResponseEntity.noContent().build();
    }
}
