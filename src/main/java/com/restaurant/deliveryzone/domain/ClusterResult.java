package com.restaurant.deliveryzone.domain;

import java.util.List;

public record ClusterResult(
        List<Restaurant> restaurantList,
        double latitude,
        double longitude,
        int radiusMeters
) {
}
