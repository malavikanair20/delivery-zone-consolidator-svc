package com.restaurant.deliveryzone.domain;

public record Restaurant(
        String id,
        String name,
        double latitude,
        double longitude,
        int deliveryRadiusMeters
) {
}
