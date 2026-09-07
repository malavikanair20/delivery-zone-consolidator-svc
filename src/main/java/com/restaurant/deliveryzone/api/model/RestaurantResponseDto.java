package com.restaurant.deliveryzone.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "RestaurantResponseDto", description = "Restaurant details after replacement")
public record RestaurantResponseDto (

    @JsonProperty("status")
    String status,

    @JsonProperty("restaurantsLoaded")
    int restaurantsLoaded,

    @JsonProperty("message")
    String message
){

}
