package com.restaurant.deliveryzone.service;

import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.exception.DuplicateRestaurantIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final AtomicReference<List<Restaurant>> restaurants = new AtomicReference<>(List.of());
    private final AtomicLong datasetVersion = new AtomicLong(0);

    public int replaceAll(List<Restaurant> restaurantsList) {
        validateUniqueIds(restaurantsList);
        List<Restaurant> normalized = restaurantsList.stream()
                .map(this :: normalizeRestaurant)
                .sorted(Comparator.comparing(Restaurant::id))
                .toList();
        restaurants.set(normalized);
        datasetVersion.incrementAndGet();
        return normalized.size();

    }

    private static void validateUniqueIds(List<Restaurant> restaurantsList) {
        Set<String> uniqueIds = new HashSet<>();
        for (Restaurant restaurant : restaurantsList) {
            if (!uniqueIds.add(restaurant.id().trim())) {
                throw new DuplicateRestaurantIdException("Duplicate restaurant ID :" + restaurant.id());
            }
        }
    }

    private Restaurant normalizeRestaurant(Restaurant restaurant) {
        return new Restaurant(
                restaurant.id().trim(),
                restaurant.name().trim(),
                restaurant.latitude(),
                restaurant.longitude(),
                restaurant.deliveryRadiusMeters());
    }
}
