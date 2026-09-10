package com.restaurant.deliveryzone.unit.service;

import com.restaurant.deliveryzone.domain.ClusterResult;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.service.GridGroupingEngine;
import com.restaurant.deliveryzone.util.GeoMath;
import com.restaurant.deliveryzone.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GridGroupingEngineTest {

    private static final String TEST_DATA_PATH = "unit/service/grid_grouping_engine/grid_grouping_engine_scenarios.json";

    private GridGroupingEngine engine;
    private EngineScenario scenario;

    @BeforeEach
    void setup() {
        engine = new GridGroupingEngine(50_000);
        scenario = TestDataFactory.retreiveObject(EngineScenario.class, TEST_DATA_PATH);
    }

    @Nested
    class CalculateGroups {

        @ParameterizedTest
        @MethodSource("com.restaurant.deliveryzone.unit.service.GridGroupingEngineTest#nullOrEmptyInputs")
        void shouldReturnEmptyForNullOrEmptyInput(List<Restaurant> input) {
            List<ClusterResult> actual = engine.calculateGroups(input);

            assertThat(actual).isEmpty();
        }

        @Test
        void shouldGroupTransitiveOverlapsAndIsolatedRestaurant() {
            List<ClusterResult> actual = engine.calculateGroups(scenario.transitiveAndIsolated().restaurants());

            assertThat(normalize(actual)).usingRecursiveComparison().isEqualTo(scenario.transitiveAndIsolated().expectedGroups());
            assertEachClusterTargetCoversAllRestaurants(actual);
        }

        @Test
        void shouldBeDeterministicForSameInput() {
            List<Restaurant> restaurants = scenario.determinism().restaurants();

            List<ClusterResult> first = engine.calculateGroups(restaurants);
            List<ClusterResult> second = engine.calculateGroups(restaurants);

            assertThat(normalize(first)).usingRecursiveComparison().isEqualTo(normalize(second));
        }
    }

    private static Stream<List<Restaurant>> nullOrEmptyInputs() {
        return Stream.of(null, List.of());
    }

    private List<NormalizedGroup> normalize(List<ClusterResult> clusters) {
        return clusters.stream()
                .map(cluster -> new NormalizedGroup(
                        cluster.restaurantList().stream().map(Restaurant::id).sorted().toList(),
                        cluster.restaurantList().size()
                ))
                .sorted(Comparator.comparing(group -> group.restaurantIds().getFirst()))
                .toList();
    }

    private void assertEachClusterTargetCoversAllRestaurants(List<ClusterResult> clusters) {
        for (ClusterResult cluster : clusters) {
            for (Restaurant restaurant : cluster.restaurantList()) {
                double distance = GeoMath.haversineMeters(
                        cluster.latitude(),
                        cluster.longitude(),
                        restaurant.latitude(),
                        restaurant.longitude()
                );
                assertThat(distance + restaurant.deliveryRadiusMeters())
                        .isLessThanOrEqualTo(cluster.radiusMeters());
            }
        }
    }

    private record EngineScenario(
            TransitiveAndIsolatedScenario transitiveAndIsolated,
            DeterminismScenario determinism
    ) {
    }

    private record TransitiveAndIsolatedScenario(List<Restaurant> restaurants, List<NormalizedGroup> expectedGroups) {
    }

    private record DeterminismScenario(List<Restaurant> restaurants) {
    }

    private record NormalizedGroup(List<String> restaurantIds, int restaurantCount) {
    }
}


