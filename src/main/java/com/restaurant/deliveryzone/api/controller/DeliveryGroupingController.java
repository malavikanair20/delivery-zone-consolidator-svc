package com.restaurant.deliveryzone.api.controller;

import com.restaurant.deliveryzone.api.document.DeliveryGroupingApi;
import com.restaurant.deliveryzone.api.model.*;
import com.restaurant.deliveryzone.service.DeliveryGroupingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DeliveryGroupingController implements DeliveryGroupingApi {
    private final DeliveryGroupingService deliveryGroupingService;

    @Override
    public ResponseEntity<RestaurantResponse> replaceRestaurants(List<RestaurantRequest> restaurantRequestList) {
        return ResponseEntity.ok(deliveryGroupingService.replaceRestaurants(restaurantRequestList));
    }

    @Override
    public ResponseEntity<GroupSummaryResponse> getGroups() {
        return ResponseEntity.ok(deliveryGroupingService.getGroups());
    }

    @Override
    public ResponseEntity<GroupDetails> getGroupById(String groupId) {
       return ResponseEntity.ok(deliveryGroupingService.getGroupById(groupId));
    }
}
