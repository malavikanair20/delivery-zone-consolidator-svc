package com.restaurant.deliveryzone.repository;


import com.restaurant.deliveryzone.domain.Restaurant;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Repository
public class InMemoryRestaurantRepository implements RestaurantRepository {

    private final AtomicReference<List<Restaurant>> restaurants =
            new AtomicReference<>(List.of());

    @Override
    public void replaceAll(List<Restaurant> newRestaurants) {
        restaurants.set(List.copyOf(newRestaurants));
    }

    @Override
    public List<Restaurant> findAll() {
        return restaurants.get();
    }
}
