package com.restaurant.deliveryzone.api.controller;

import com.restaurant.deliveryzone.api.document.DeliveryGroupingApi;
import com.restaurant.deliveryzone.api.model.*;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.mapper.DeliveryMapper;
import com.restaurant.deliveryzone.service.GroupingService;
import com.restaurant.deliveryzone.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DeliveryGroupingController implements DeliveryGroupingApi {
    private final RestaurantService restaurantService;
    private final DeliveryMapper deliveryMapper;
    private final GroupingService groupingService;

    @Override
    public ResponseEntity<RestaurantResponse> replaceRestaurants(List<RestaurantRequest> restaurantRequestList) {
        List<Restaurant> restaurants = restaurantRequestList.stream().map(deliveryMapper::toRestaurant).toList();
        return ResponseEntity.ok(restaurantService.replaceAllAndBuildResponse(restaurants));
    }

    @Override
    public ResponseEntity<GroupSummaryResponse> getGroups() {
        return ResponseEntity.ok(groupingService.getGroupsResponse());
    }

    @Override
    public ResponseEntity<GroupDetails> getGroupById(String groupId) {
       return ResponseEntity.ok(groupingService.getGroup(groupId));
    }
}
