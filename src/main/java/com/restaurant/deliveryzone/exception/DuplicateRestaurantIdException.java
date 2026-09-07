package com.restaurant.deliveryzone.exception;

public class DuplicateRestaurantIdException extends RuntimeException {
    public DuplicateRestaurantIdException(String message) {
        super(message);
    }
}
