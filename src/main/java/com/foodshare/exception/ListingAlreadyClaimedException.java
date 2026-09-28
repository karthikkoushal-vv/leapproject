package com.foodshare.exception;

public class ListingAlreadyClaimedException extends RuntimeException {
    public ListingAlreadyClaimedException(String message) {
        super(message);
    }
}
