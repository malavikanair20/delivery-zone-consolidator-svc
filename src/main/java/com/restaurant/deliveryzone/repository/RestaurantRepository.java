package com.restaurant.deliveryzone.repository;


import com.restaurant.deliveryzone.domain.Restaurant;

import java.util.List;

public interface RestaurantRepository {

    void replaceAll(List<Restaurant> restaurants);

    List<Restaurant> findAll();
}
