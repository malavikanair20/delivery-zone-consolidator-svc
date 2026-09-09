package com.restaurant.deliveryzone.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.restaurant.deliveryzone.domain.Restaurant;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "DeliveryGroupsResponseDTO", description = "Collection of consolidated delivery groups")
public record GroupSummary(
        String groupId,
        int restaurantCount,
        List<String> restaurantIds,
        double recommendedTargetLatitude,
        double recommendedTargetLongitude,
        int recommendedTargetRadiusMeters
) {
    public GroupSummary {
        restaurantIds = List.copyOf(restaurantIds); // defensive copy: never expose a mutable list
    }
}

