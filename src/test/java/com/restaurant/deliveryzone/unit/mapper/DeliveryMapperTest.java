package com.restaurant.deliveryzone.unit.mapper;

import com.restaurant.deliveryzone.api.model.RestaurantRequest;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.mapper.DeliveryMapper;
import com.restaurant.deliveryzone.util.TestDataFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryMapperTest {

    private static final String TEST_DATA_PATH = "unit/mapper/delivery_mapper/delivery_mapper_scenarios.json";

    private final DeliveryMapper deliveryMapper = Mappers.getMapper(DeliveryMapper.class);

    @ParameterizedTest
    @MethodSource("mappingScenarios")
    void shouldMapRestaurantRequestToRestaurant(MappingScenario scenario) {
        Restaurant actual = deliveryMapper.toRestaurant(scenario.input());

        assertThat(actual).usingRecursiveComparison().isEqualTo(scenario.expected());
    }

    private static Stream<MappingScenario> mappingScenarios() {
        DeliveryMapperScenarioFile scenarioFile = TestDataFactory.retreiveObject(DeliveryMapperScenarioFile.class, TEST_DATA_PATH);
        return scenarioFile.scenarios().stream();
    }

    private record DeliveryMapperScenarioFile(List<MappingScenario> scenarios) {
    }

    private record MappingScenario(
            String scenario,
            RestaurantRequest input,
            Restaurant expected
    ) {
    }
}

