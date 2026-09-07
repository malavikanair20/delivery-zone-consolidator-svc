package com.restaurant.deliveryzone.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "RestaurantRequestDto", description = "Restaurant payload with delivery zone")
public class RestaurantRequestDto {

    @NotBlank
    @Schema(name = "id", example = "r1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("id")
    String id;

    @NotBlank
    @Schema(name = "name", example = "Restaurant A", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("name")
    String name;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    @Schema(name = "latitude", example = "51.5074", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("latitude")
    double latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    @Schema(name = "longitude", example = "-0.1278", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("longitude")
    double longitude;

    @NotNull
    @PositiveOrZero
    @Schema(name = "deliveryRadiusMeters", example = "3000", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("deliveryRadiusMeters")
    int deliveryRadiusMeters;
}
