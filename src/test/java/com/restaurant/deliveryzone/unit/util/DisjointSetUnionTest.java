package com.restaurant.deliveryzone.unit.util;

import com.restaurant.deliveryzone.util.DisjointSetUnion;
import com.restaurant.deliveryzone.util.TestDataFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DisjointSetUnionTest {

    private static final String TEST_DATA_PATH = "unit/util/disjoint_set_union/disjoint_set_union_scenarios.json";

    @ParameterizedTest
    @MethodSource("scenarios")
    void shouldBuildExpectedConnectedComponents(Scenario scenario) {
        DisjointSetUnion dsu = new DisjointSetUnion(scenario.size());

        for (UnionStep step : scenario.unionSteps()) {
            dsu.union(step.first(), step.second());
        }

        for (ConnectionExpectation expectation : scenario.expectations()) {
            int leftRoot = dsu.find(expectation.left());
            int rightRoot = dsu.find(expectation.right());
            assertThat(leftRoot == rightRoot).isEqualTo(expectation.connected());
        }
    }

    private static Stream<Scenario> scenarios() {
        ScenarioFile file = TestDataFactory.retreiveObject(ScenarioFile.class, TEST_DATA_PATH);
        return file.scenarios().stream();
    }

    private record ScenarioFile(List<Scenario> scenarios) {
    }

    private record Scenario(
            String scenario,
            int size,
            List<UnionStep> unionSteps,
            List<ConnectionExpectation> expectations
    ) {
    }

    private record UnionStep(int first, int second) {
    }

    private record ConnectionExpectation(int left, int right, boolean connected) {
    }
}

