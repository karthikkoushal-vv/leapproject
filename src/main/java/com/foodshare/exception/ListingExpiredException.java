package com.foodshare.exception;

public class ListingExpiredException extends RuntimeException {
    public ListingExpiredException(String message) {
        super(message);
    }
}
