package com.restaurant.deliveryzone.mapper;

import com.restaurant.deliveryzone.api.model.RestaurantRequestDto;
import com.restaurant.deliveryzone.domain.Restaurant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {

    Restaurant toRestaurant(RestaurantRequestDto restaurantRequestDto);
}
