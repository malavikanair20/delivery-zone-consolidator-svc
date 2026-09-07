package com.restaurant.deliveryzone.service;

import com.restaurant.deliveryzone.api.model.RestaurantRequestDto;
import com.restaurant.deliveryzone.api.model.RestaurantResponseDto;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.mapper.DeliveryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryGroupingService {

    private final RestaurantService restaurantService;
    private final DeliveryMapper deliveryMapper;

    public RestaurantResponseDto replaceRestaurants(List<RestaurantRequestDto> restaurantRequestDtoList) {
        List<Restaurant> restaurants = restaurantRequestDtoList.stream().map(deliveryMapper::toRestaurant).toList();
        int loadedCount = restaurantService.replaceAll(restaurants);
        return new RestaurantResponseDto(
                "success",
                loadedCount,
                "Restaurants successfully stored"
        );
    }
}
