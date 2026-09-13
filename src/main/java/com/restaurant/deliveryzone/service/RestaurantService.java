package com.restaurant.deliveryzone.service;

import com.restaurant.deliveryzone.api.model.RestaurantResponse;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.exception.DuplicateRestaurantIdException;
import com.restaurant.deliveryzone.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Log4j2
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    private final AtomicLong version = new AtomicLong(0);

    private int replaceAll(List<Restaurant> restaurantsList) {
        List<Restaurant> normalizedRestaurants = normalizeRestaurants(restaurantsList);
        validateUniqueIds(normalizedRestaurants);
        restaurantRepository.replaceAll(normalizedRestaurants);
        long newVersion = version.incrementAndGet();
        log.info("Replaced restaurant dataset: {} restaurants, version={}",
                normalizedRestaurants.size(), newVersion);
        return normalizedRestaurants.size();
    }

    public RestaurantResponse replaceAllAndBuildResponse(List<Restaurant> restaurantsList) {
        int loadedCount = replaceAll(restaurantsList);
        return new RestaurantResponse(
                "success",
                loadedCount,
                "Restaurants successfully stored"
        );
    }

    private static List<Restaurant> normalizeRestaurants(List<Restaurant> restaurantsList) {
        return restaurantsList.stream()
                .map(restaurant -> new Restaurant(
                        requireNonBlank(restaurant.id(), "id"),
                        requireNonBlank(restaurant.name(), "name"),
                        restaurant.latitude(),
                        restaurant.longitude(),
                        restaurant.deliveryRadiusMeters()
                ))
                .toList();
    }

    private static String requireNonBlank(String value, String fieldName) {
        String trimmed = Objects.requireNonNull(value, fieldName + " must not be null").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    private static void validateUniqueIds(List<Restaurant> restaurantsList) {
        Set<String> uniqueIds = new HashSet<>();
        for (Restaurant restaurant : restaurantsList) {
            if (!uniqueIds.add(restaurant.id().trim())) {
                throw new DuplicateRestaurantIdException("Duplicate restaurant ID :" + restaurant.id());
            }
        }
    }

    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    public long getVersion() {
        return version.get();
    }
}
