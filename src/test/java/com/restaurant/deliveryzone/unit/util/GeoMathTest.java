package com.restaurant.deliveryzone.unit.util;

import com.restaurant.deliveryzone.util.GeoMath;
import com.restaurant.deliveryzone.util.TestDataFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GeoMathTest {

    private static final String TEST_DATA_PATH = "unit/util/geo_math/geo_math_scenarios.json";

    @ParameterizedTest
    @MethodSource("distanceScenarios")
    void shouldReturnExpectedDistanceWithinTolerance(DistanceScenario scenario) {
        double actual = GeoMath.haversineMeters(
                scenario.latitude1(),
                scenario.longitude1(),
                scenario.latitude2(),
                scenario.longitude2()
        );

        assertThat(actual).isCloseTo(scenario.expectedMeters(), org.assertj.core.data.Offset.offset(scenario.toleranceMeters()));
    }

    @ParameterizedTest
    @MethodSource("distanceScenarios")
    void shouldBeSymmetric(DistanceScenario scenario) {
        double forward = GeoMath.haversineMeters(
                scenario.latitude1(),
                scenario.longitude1(),
                scenario.latitude2(),
                scenario.longitude2()
        );
        double reverse = GeoMath.haversineMeters(
                scenario.latitude2(),
                scenario.longitude2(),
                scenario.latitude1(),
                scenario.longitude1()
        );

        assertThat(forward).isCloseTo(reverse, org.assertj.core.data.Offset.offset(scenario.toleranceMeters()));
    }

    private static Stream<DistanceScenario> distanceScenarios() {
        ScenarioFile file = TestDataFactory.retreiveObject(ScenarioFile.class, TEST_DATA_PATH);
        return file.scenarios().stream();
    }

    private record ScenarioFile(List<DistanceScenario> scenarios) {
    }

    private record DistanceScenario(
            String scenario,
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2,
            double expectedMeters,
            double toleranceMeters
    ) {
    }
}

