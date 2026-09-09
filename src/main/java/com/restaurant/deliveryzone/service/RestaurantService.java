package com.restaurant.deliveryzone.service;

import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.exception.DuplicateRestaurantIdException;
import com.restaurant.deliveryzone.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Log4j2
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    private final AtomicLong version = new AtomicLong(0);
    private final Object writeLock = new Object();

    public int replaceAll(List<Restaurant> restaurantsList) {
        validateUniqueIds(restaurantsList);
        synchronized (writeLock) {
            restaurantRepository.replaceAll(restaurantsList);
            long newVersion = version.incrementAndGet();
            log.info("Replaced restaurant dataset: {} restaurants, version={}",
                    restaurantsList.size(), newVersion);
        }
        return restaurantsList.size();
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
