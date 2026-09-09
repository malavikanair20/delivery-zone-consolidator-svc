package com.restaurant.deliveryzone.service;

import com.restaurant.deliveryzone.domain.ClusterResult;
import com.restaurant.deliveryzone.domain.Restaurant;

import java.util.List;

public interface GroupingEngine {

    List<ClusterResult> calculateGroups(List<Restaurant> restaurants);
}
