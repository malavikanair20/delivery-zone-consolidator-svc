package com.restaurant.deliveryzone.api.controller;

import com.restaurant.deliveryzone.api.document.DeliveryGroupingApi;
import com.restaurant.deliveryzone.api.model.RestaurantRequestDto;
import com.restaurant.deliveryzone.api.model.RestaurantResponseDto;
import com.restaurant.deliveryzone.service.DeliveryGroupingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DeliveryGroupingController implements DeliveryGroupingApi {
    private final DeliveryGroupingService deliveryGroupingService;

    @Override
    public ResponseEntity<RestaurantResponseDto> replaceRestaurants(List<@Valid RestaurantRequestDto> restaurantRequestList) {
        return ResponseEntity.ok(deliveryGroupingService.replaceRestaurants(restaurantRequestList));
    }
}
