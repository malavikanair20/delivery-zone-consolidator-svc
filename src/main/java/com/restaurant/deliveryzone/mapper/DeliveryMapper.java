package com.restaurant.deliveryzone.mapper;

import com.restaurant.deliveryzone.api.model.RestaurantRequest;
import com.restaurant.deliveryzone.domain.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "latitude", source = "latitude")
    @Mapping(target = "longitude", source = "longitude")
    @Mapping(target = "deliveryRadiusMeters", source = "deliveryRadiusMeters")
    Restaurant toRestaurant(RestaurantRequest restaurantRequest);
}
