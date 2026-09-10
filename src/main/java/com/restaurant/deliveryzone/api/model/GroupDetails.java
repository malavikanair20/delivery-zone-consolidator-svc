package com.restaurant.deliveryzone.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.restaurant.deliveryzone.domain.Restaurant;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "DeliveryGroupResponseDTO", description = "Consolidated delivery target group")
public record GroupDetails(

    @JsonProperty("groupId")
    String groupId,

    @JsonProperty("restaurantCount")
    int restaurantCount,

    @JsonProperty("restaurantIds")
    List<String> restaurantIds,

    @JsonProperty("recommendedTargetLatitude")
    double recommendedTargetLatitude,

    @JsonProperty("recommendedTargetLongitude")
    double recommendedTargetLongitude,

    @JsonProperty("recommendedTargetRadiusMeters")
    int recommendedTargetRadiusMeters,

    @JsonProperty("restaurants")
    List<Restaurant> restaurants
) {
    public GroupDetails {
        restaurantIds = List.copyOf(restaurantIds);
        restaurants = List.copyOf(restaurants);
    }
}

