package com.restaurant.deliveryzone.unit.repository;

import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.repository.InMemoryRestaurantRepository;
import com.restaurant.deliveryzone.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryRestaurantRepositoryTest {

    private static final String TEST_DATA_PATH = "unit/repository/in_memory_restaurant_repository/in_memory_restaurant_repository_scenarios.json";

    private InMemoryRestaurantRepository repository;
    private RepositoryScenario scenario;

    @BeforeEach
    void setup() {
        repository = new InMemoryRestaurantRepository();
        scenario = TestDataFactory.retreiveObject(RepositoryScenario.class, TEST_DATA_PATH);
    }

    @Nested
    class ReplaceAndFindAll {

        @Test
        void shouldKeepStoredSnapshotIndependentFromInputListMutations() {
            List<Restaurant> mutableInput = new ArrayList<>(scenario.replaceAndFindAll().input());

            repository.replaceAll(mutableInput);
            mutableInput.removeFirst();

            List<Restaurant> actual = repository.findAll();

            assertThat(actual).usingRecursiveComparison().isEqualTo(scenario.replaceAndFindAll().expectedStored());
        }

        @Test
        void shouldReturnImmutableStoredList() {
            repository.replaceAll(new ArrayList<>(scenario.replaceAndFindAll().input()));

            List<Restaurant> actual = repository.findAll();
            Restaurant firstRestaurant = scenario.replaceAndFindAll().input().getFirst();

            assertThatThrownBy(() -> actual.add(firstRestaurant))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    private record RepositoryScenario(ReplaceAndFindAllScenario replaceAndFindAll) {
    }

    private record ReplaceAndFindAllScenario(List<Restaurant> input, List<Restaurant> expectedStored) {
    }
}

