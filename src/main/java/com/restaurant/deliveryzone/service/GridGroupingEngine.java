package com.restaurant.deliveryzone.service;

import com.restaurant.deliveryzone.domain.ClusterResult;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.util.DisjointSetUnion;
import com.restaurant.deliveryzone.util.GeoMath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@Component
public class GridGroupingEngine implements GroupingEngine {

    private static final double METERS_PER_DEGREE_LATITUDE = 111_320.0;
    private static final double MIN_CELL_SIZE_METERS = 1.0;
    private static final double EPSILON = 1e-12;

    private final double maxCellSizeMeters;

    public GridGroupingEngine(
            @Value("${grouping.max-cell-size-meters:50000}")
            double maxCellSizeMeters) {

        this.maxCellSizeMeters = maxCellSizeMeters;
    }

    @Override
    public List<ClusterResult> calculateGroups(
            List<Restaurant> restaurants) {

        if (restaurants == null || restaurants.isEmpty()) {
            return List.of();
        }

        var startNanos = System.nanoTime();

        var groupingContext = createGroupingContext(restaurants);

        mergeOverlappingRestaurants(
                restaurants,
                groupingContext
        );

        var results = buildClusterResults(
                restaurants,
                groupingContext.dsu()
        );

        logPerformance(
                startNanos,
                restaurants.size(),
                results.size(),
                groupingContext.cellSizeMeters()
        );

        return results;
    }

    // GROUPING SETUP
    private GroupingContext createGroupingContext(
            List<Restaurant> restaurants) {

        var referenceLatitudeRadians =
                calculateReferenceLatitude(restaurants);

        var maxRadiusMeters =
                calculateMaximumRadius(restaurants);

        var cellSizeMeters =
                calculateCellSize(maxRadiusMeters);

        var grid =
                createGrid(
                        restaurants,
                        referenceLatitudeRadians,
                        cellSizeMeters
                );

        return new GroupingContext(
                new DisjointSetUnion(restaurants.size()),
                grid,
                referenceLatitudeRadians,
                cellSizeMeters,
                maxRadiusMeters
        );
    }

    private double calculateReferenceLatitude(
            List<Restaurant> restaurants) {

        return Math.toRadians(
                restaurants.stream()
                        .mapToDouble(Restaurant::latitude)
                        .average()
                        .orElseThrow()
        );
    }

    private double calculateMaximumRadius(
            List<Restaurant> restaurants) {

        return restaurants.stream()
                .mapToDouble(Restaurant::deliveryRadiusMeters)
                .max()
                .orElse(0.0);
    }

    private double calculateCellSize(
            double maxRadiusMeters) {

        return Math.clamp(
                2.0 * maxRadiusMeters,
                MIN_CELL_SIZE_METERS
                ,
                maxCellSizeMeters
        );
    }


    // GRID CREATION
    private Map<GridCell, List<Integer>> createGrid(
            List<Restaurant> restaurants,
            double referenceLatitudeRadians,
            double cellSizeMeters) {

        var grid = new HashMap<GridCell, List<Integer>>();

        IntStream.range(0, restaurants.size())
                .forEach(index -> {

                    var restaurant = restaurants.get(index);

                    var cell = toGridCell(
                            restaurant,
                            referenceLatitudeRadians,
                            cellSizeMeters
                    );

                    grid.computeIfAbsent(
                                    cell,
                                    ignored -> new ArrayList<>()
                            )
                            .add(index);
                });

        return grid;
    }

    private GridCell toGridCell(
            Restaurant restaurant,
            double referenceLatitudeRadians,
            double cellSizeMeters) {

        var point = project(
                restaurant,
                referenceLatitudeRadians
        );

        return new GridCell(
                Math.floorDiv(
                        (long) Math.floor(point.xMeters()),
                        (long) cellSizeMeters
                ),
                Math.floorDiv(
                        (long) Math.floor(point.yMeters()),
                        (long) cellSizeMeters
                )
        );
    }


    // OVERLAP DETECTION
    private void mergeOverlappingRestaurants(
            List<Restaurant> restaurants,
            GroupingContext context) {

        IntStream.range(0, restaurants.size())
                .forEach(currentIndex -> {

                    var restaurant =
                            restaurants.get(currentIndex);

                    findCandidateIndexes(
                            restaurant,
                            currentIndex,
                            context
                    ).forEach(candidateIndex -> {

                        var candidate =
                                restaurants.get(candidateIndex);

                        if (overlaps(restaurant, candidate)) {

                            context.dsu().union(
                                    currentIndex,
                                    candidateIndex
                            );
                        }
                    });
                });
    }

    private List<Integer> findCandidateIndexes(
            Restaurant restaurant,
            int restaurantIndex,
            GroupingContext context) {

        var currentCell =
                toGridCell(
                        restaurant,
                        context.referenceLatitudeRadians(),
                        context.cellSizeMeters()
                );

        var cellRange =
                calculateCellRange(
                        restaurant,
                        context.maxRadiusMeters(),
                        context.cellSizeMeters()
                );

        return candidateCells(
                currentCell,
                cellRange
        ).stream()
                .map(context.grid()::get)
                .filter(list -> list != null && !list.isEmpty())
                .flatMap(List::stream)
                .filter(candidateIndex ->
                        candidateIndex > restaurantIndex
                )
                .toList();
    }

    private int calculateCellRange(
            Restaurant restaurant,
            double maxRadiusMeters,
            double cellSizeMeters) {

        var maximumPossibleDistance =
                restaurant.deliveryRadiusMeters()
                        + maxRadiusMeters;

        return (int) Math.ceil(
                maximumPossibleDistance / cellSizeMeters
        );
    }

    private List<GridCell> candidateCells(
            GridCell centre,
            int range) {

        var cells = new ArrayList<GridCell>(
                (2 * range + 1) * (2 * range + 1)
        );

        for (var x = -range; x <= range; x++) {
            for (var y = -range; y <= range; y++) {

                cells.add(
                        new GridCell(
                                centre.x() + x,
                                centre.y() + y
                        )
                );
            }
        }

        return cells;
    }

    private boolean overlaps(
            Restaurant left,
            Restaurant right) {

        var distanceMeters =
                GeoMath.haversineMeters(
                        left.latitude(),
                        left.longitude(),
                        right.latitude(),
                        right.longitude()
                );

        var maximumAllowedDistance =
                left.deliveryRadiusMeters()
                        + right.deliveryRadiusMeters();

        return distanceMeters <= maximumAllowedDistance;
    }


    // CONNECTED COMPONENTS
    private List<ClusterResult> buildClusterResults(
            List<Restaurant> restaurants,
            DisjointSetUnion dsu) {

        var components =
                buildComponents(restaurants, dsu);

        return components.values()
                .stream()
                .map(this::toClusterResult)
                .toList();
    }

    private Map<Integer, List<Restaurant>> buildComponents(
            List<Restaurant> restaurants,
            DisjointSetUnion dsu) {

        var components =
                new HashMap<Integer, List<Restaurant>>();

        IntStream.range(0, restaurants.size())
                .forEach(index -> {

                    var root = dsu.find(index);

                    components
                            .computeIfAbsent(
                                    root,
                                    ignored -> new ArrayList<>()
                            )
                            .add(restaurants.get(index));
                });

        return components;
    }

    // CLUSTER RESULT
    private ClusterResult toClusterResult(
            List<Restaurant> restaurants) {

        var sortedRestaurants =
                restaurants.stream()
                        .sorted(
                                java.util.Comparator.comparing(
                                        Restaurant::id
                                )
                        )
                        .toList();

        var target =
                calculateGroupTargetCenter(
                        sortedRestaurants
                );

        var targetRadius =
                calculateTargetRadius(
                        sortedRestaurants,
                        target
                );

        return new ClusterResult(
                sortedRestaurants,
                target.latitude(),
                target.longitude(),
                targetRadius
        );
    }

    private int calculateTargetRadius(
            List<Restaurant> restaurants,
            GeoPoint target) {

        return (int) Math.ceil(
                restaurants.stream()
                        .mapToDouble(restaurant ->
                                GeoMath.haversineMeters(
                                        target.latitude(),
                                        target.longitude(),
                                        restaurant.latitude(),
                                        restaurant.longitude()
                                )
                                        + restaurant.deliveryRadiusMeters()
                        )
                        .max()
                        .orElse(0.0)
        );
    }

    // SPHERICAL CENTRE CALCULATION

    private GeoPoint calculateGroupTargetCenter(
            List<Restaurant> restaurants) {

        var vector =
                restaurants.stream()
                        .map(this::toUnitVector)
                        .reduce(
                                UnitVector.ZERO,
                                UnitVector::add
                        );

        return vector.toGeoPoint();
    }

    private UnitVector toUnitVector(
            Restaurant restaurant) {

        var latitude =
                Math.toRadians(
                        restaurant.latitude()
                );

        var longitude =
                Math.toRadians(
                        restaurant.longitude()
                );

        return new UnitVector(
                Math.cos(latitude)
                        * Math.cos(longitude),

                Math.cos(latitude)
                        * Math.sin(longitude),

                Math.sin(latitude)
        );
    }

    // GEO PROJECTION

    private ProjectedPoint project(
            Restaurant restaurant,
            double referenceLatitudeRadians) {

        var metersPerDegreeLongitude =
                METERS_PER_DEGREE_LATITUDE
                        * Math.cos(
                        referenceLatitudeRadians
                );

        /*
         * Protect against division/scaling problems near the poles.
         */
        metersPerDegreeLongitude =
                Math.max(
                        metersPerDegreeLongitude,
                        MIN_CELL_SIZE_METERS
                );

        var xMeters =
                restaurant.longitude()
                        * metersPerDegreeLongitude;

        var yMeters =
                restaurant.latitude()
                        * METERS_PER_DEGREE_LATITUDE;

        return new ProjectedPoint(
                xMeters,
                yMeters
        );
    }

    private void logPerformance(
            long startNanos,
            int restaurantCount,
            int groupCount,
            double cellSizeMeters) {

        var elapsedMillis =
                (System.nanoTime() - startNanos)
                        / 1_000_000;

        log.info(
                "Computed {} groups from {} restaurants " +
                        "in {} ms (cellSizeMeters={})",
                groupCount,
                restaurantCount,
                elapsedMillis,
                cellSizeMeters
        );
    }

    private record GroupingContext(
            DisjointSetUnion dsu,
            Map<GridCell, List<Integer>> grid,
            double referenceLatitudeRadians,
            double cellSizeMeters,
            double maxRadiusMeters) {
    }

    private record GridCell(
            long x,
            long y) {
    }

    private record ProjectedPoint(
            double xMeters,
            double yMeters) {
    }

    private record GeoPoint(
            double latitude,
            double longitude) {
    }

    private record UnitVector(
            double x,
            double y,
            double z) {

        private static final UnitVector ZERO =
                new UnitVector(0.0, 0.0, 0.0);

        private UnitVector add(UnitVector other) {
            return new UnitVector(
                    x + other.x,
                    y + other.y,
                    z + other.z
            );
        }

        private GeoPoint toGeoPoint() {

            var magnitude =
                    Math.sqrt(
                            x * x
                                    + y * y
                                    + z * z
                    );

            if (magnitude < EPSILON) {
                throw new IllegalStateException(
                        "Cannot calculate target centre " +
                                "from a degenerate vector"
                );
            }

            var normalizedX = x / magnitude;
            var normalizedY = y / magnitude;
            var normalizedZ = z / magnitude;

            var latitude =
                    Math.toDegrees(
                            Math.asin(normalizedZ)
                    );

            var longitude =
                    Math.toDegrees(
                            Math.atan2(
                                    normalizedY,
                                    normalizedX
                            )
                    );

            return new GeoPoint(
                    latitude,
                    longitude
            );
        }
    }
}