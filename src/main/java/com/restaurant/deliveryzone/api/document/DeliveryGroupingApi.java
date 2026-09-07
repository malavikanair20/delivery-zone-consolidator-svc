package com.restaurant.deliveryzone.api.document;

import com.restaurant.deliveryzone.api.model.RestaurantRequestDto;
import com.restaurant.deliveryzone.api.model.RestaurantResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("v1/delivery")
@Tag(name = "Delivery Grouping Api", description = "API for restaurant delivery zone consolidation")
public interface DeliveryGroupingApi {

    @Operation(summary = "Replace restaurant dataset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "restaurants successfully stored"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/restaurants")
    ResponseEntity<RestaurantResponseDto> replaceRestaurants(
            @RequestBody @Valid List<@Valid RestaurantRequestDto> restaurantRequestList
            );
}
