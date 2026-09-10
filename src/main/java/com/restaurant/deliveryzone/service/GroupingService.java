package com.restaurant.deliveryzone.service;

import com.restaurant.deliveryzone.api.model.GroupDetails;
import com.restaurant.deliveryzone.api.model.GroupSummary;
import com.restaurant.deliveryzone.api.model.GroupSummaryResponse;
import com.restaurant.deliveryzone.domain.ClusterResult;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.exception.GroupNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Log4j2
@RequiredArgsConstructor
public class GroupingService {

    private final RestaurantService restaurantService;
    private final GroupingEngine groupingEngine;
    private final AtomicReference<Cache> cache = new AtomicReference<>(new Cache(-1, List.of(), List.of(), Map.of()));

    public GroupSummaryResponse getGroupsResponse() {
        List<GroupSummary> groups = getCurrentCache().groups;
        return new GroupSummaryResponse(groups.size(), groups);
    }

    public GroupDetails getGroup(String groupId) {
        Cache current = getCurrentCache();

        GroupSummary summary = current.groups.stream()
                .filter(group -> group.groupId().equals(groupId))
                .findFirst()
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        List<Restaurant> restaurants = summary.restaurantIds().stream()
                .map(current.restaurantsById::get)
                .toList();

        return new GroupDetails(
                summary.groupId(),
                summary.restaurantCount(),
                summary.restaurantIds(),
                summary.recommendedTargetLatitude(),
                summary.recommendedTargetLongitude(),
                summary.recommendedTargetRadiusMeters(),
                restaurants);
    }

    /** Kept for callers that want to force a rebuild without waiting on version drift. */
    public synchronized void invalidateCache() {
        cache.set(new Cache(-1, List.of(), List.of(), Map.of()));
    }

    private Cache getCurrentCache() {
        long version = restaurantService.getVersion();

        Cache current = cache.get();
        if (current.version == version) {
            log.debug("Cache hit at version {}", version);
            return current;
        }

        synchronized (this) {
            current = cache.get();
            if (current.version == version) {
                return current;
            }

            long startNanos = System.nanoTime();

            List<Restaurant> restaurants = restaurantService.findAll();

            List<ClusterResult> clusters = groupingEngine.calculateGroups(restaurants);

            List<ClusterWithIds> sortedClusters = clusters.stream()
                    .map(clusterResult ->
                            new ClusterWithIds(sortedRestaurantIds(clusterResult), clusterResult))
                    .sorted(Comparator.comparing(cluster ->
                            String.join(",", cluster.restaurantIds)))
                    .toList();

            List<GroupSummary> summaries = IntStream.range(0, sortedClusters.size())
                    .mapToObj(index -> {
                        ClusterWithIds clusterWithIds = sortedClusters.get(index);
                        return toSummary(clusterWithIds.cluster(), "g"+ (index+1), clusterWithIds.restaurantIds());
                    }).toList();

            Map<String, Restaurant> byId = restaurants.stream()
                    .collect(Collectors.toMap(Restaurant::id, Function.identity()));

            Cache updated = new Cache(version, restaurants, summaries, byId);
            cache.set(updated);

            long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
            log.info("Rebuilt group cache at version {}: {} restaurants -> {} groups in {} ms",
                    version, restaurants.size(), summaries.size(), elapsedMillis);

            return updated;
        }
    }

    private GroupSummary toSummary(ClusterResult cluster, String groupId, List<String> ids) {

        return new GroupSummary(
                groupId,
                cluster.restaurantList().size(),
                ids,
                cluster.latitude(),
                cluster.longitude(),
                cluster.radiusMeters());
    }

    private List<String> sortedRestaurantIds(ClusterResult clusterResult) {
        return clusterResult.restaurantList().stream().map(Restaurant::id)
                .sorted().toList();
    }

    private record Cache(
            long version,
            List<Restaurant> restaurants,
            List<GroupSummary> groups,
            Map<String, Restaurant> restaurantsById) {
    }

    private record ClusterWithIds(List<String> restaurantIds, ClusterResult cluster) {}
}