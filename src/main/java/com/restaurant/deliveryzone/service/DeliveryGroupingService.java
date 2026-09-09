package com.restaurant.deliveryzone.service;


import com.restaurant.deliveryzone.api.model.*;
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
    private final GroupingService groupingService;

    public RestaurantResponse replaceRestaurants(List<RestaurantRequest> restaurantRequestList) {
        List<Restaurant> restaurants = restaurantRequestList.stream().map(deliveryMapper::toRestaurant).toList();
        int loadedCount = restaurantService.replaceAll(restaurants);
        return new RestaurantResponse(
                "success",
                loadedCount,
                "Restaurants successfully stored"
        );
    }

    public GroupSummaryResponse getGroups() {
        List<GroupSummary> summaries = groupingService.getGroups();
        return new GroupSummaryResponse(summaries.size(), summaries);

    }


    public GroupDetails getGroupById(String groupId) {
        return groupingService.getGroup(groupId);
    }
}
