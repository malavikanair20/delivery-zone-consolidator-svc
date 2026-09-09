package com.restaurant.deliveryzone.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RestaurantResponseDto", description = "Restaurant details after replacement")
public record RestaurantResponse(

    @JsonProperty("status")
    String status,

    @JsonProperty("restaurantsLoaded")
    int restaurantsLoaded,

    @JsonProperty("message")
    String message
){

}
