package com.restaurant.deliveryzone.service;

import com.restaurant.deliveryzone.api.model.GroupDetails;
import com.restaurant.deliveryzone.api.model.GroupSummary;
import com.restaurant.deliveryzone.domain.ClusterResult;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.exception.GroupNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class GroupingService {

    private final RestaurantService restaurantService;
    private final GroupingEngine groupingEngine;
    private volatile Cache cache = new Cache(-1, List.of(), List.of(), Map.of());
    public List<GroupSummary> getGroups() {
        return getCurrentCache().groups;
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
        cache = new Cache(-1, List.of(), List.of(), Map.of());
    }

    private Cache getCurrentCache() {
        long version = restaurantService.getVersion();

        Cache current = cache;
        if (current.version == version) {
            log.debug("Cache hit at version {}", version);
            return current;
        }

        synchronized (this) {
            current = cache;
            if (current.version == version) {
                return current;
            }

            long startNanos = System.nanoTime();

            List<Restaurant> restaurants = restaurantService.findAll();

            // Single pass: clustering + target-center/radius computed together,
            // so we don't walk the restaurant list twice like the old split did.
            List<ClusterResult> clusters = groupingEngine.calculateGroups(restaurants);

            List<GroupSummary> summaries = clusters.stream()
                    .map(this::toSummary)
                    .sorted(Comparator.comparing(GroupSummary::groupId))
                    .toList();

            Map<String, Restaurant> byId = restaurants.stream()
                    .collect(Collectors.toMap(Restaurant::id, Function.identity()));

            Cache updated = new Cache(version, restaurants, summaries, byId);
            cache = updated;

            long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
            log.info("Rebuilt group cache at version {}: {} restaurants -> {} groups in {} ms",
                    version, restaurants.size(), summaries.size(), elapsedMillis);

            return updated;
        }
    }

    private GroupSummary toSummary(ClusterResult cluster) {
        List<String> ids = cluster.restaurantList().stream()
                .map(Restaurant::id)
                .sorted()
                .toList();

        String groupId = "g-" + stableHash(ids).substring(0, 12);

        return new GroupSummary(
                groupId,
                cluster.restaurantList().size(),
                ids,
                cluster.latitude(),
                cluster.longitude(),
                cluster.radiusMeters());
    }

    private String stableHash(List<String> ids) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = String.join("\u0000", ids);
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private record Cache(
            long version,
            List<Restaurant> restaurants,
            List<GroupSummary> groups,
            Map<String, Restaurant> restaurantsById) {
    }
}