package com.restaurant.deliveryzone.unit.service;

import com.restaurant.deliveryzone.api.model.RestaurantResponse;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.exception.DuplicateRestaurantIdException;
import com.restaurant.deliveryzone.repository.RestaurantRepository;
import com.restaurant.deliveryzone.service.RestaurantService;
import com.restaurant.deliveryzone.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    private static final String TEST_DATA_PATH = "unit/service/restaurant_service/restaurant_service_scenarios.json";

    @Mock
    private RestaurantRepository restaurantRepository;

    private RestaurantService restaurantService;
    private ServiceScenario scenario;

    @BeforeEach
    void setup() {
        restaurantService = new RestaurantService(restaurantRepository);
        scenario = TestDataFactory.retreiveObject(ServiceScenario.class, TEST_DATA_PATH);
    }

    @Nested
    class ReplaceAll {

        @Test
        void shouldReplaceAllAndBuildResponseAndIncrementVersion() {
            List<Restaurant> restaurants = scenario.replaceAllSuccess().input();

            RestaurantResponse actual = restaurantService.replaceAllAndBuildResponse(restaurants);

            assertThat(actual).usingRecursiveComparison().isEqualTo(scenario.replaceAllSuccess().expectedResponse());
            assertThat(actual.restaurantsLoaded()).isEqualTo(scenario.replaceAllSuccess().expectedResponse().restaurantsLoaded());
            assertThat(restaurantService.getVersion()).isEqualTo(scenario.replaceAllSuccess().expectedVersionAfterCall());
            verify(restaurantRepository, times(1)).replaceAll(restaurants);
        }

        @Test
        void shouldThrowWhenDuplicateIdsArePresent() {
            List<Restaurant> inputWithDuplicateIds = scenario.replaceAllDuplicateIds().input();

            assertThatThrownBy(() -> restaurantService.replaceAllAndBuildResponse(inputWithDuplicateIds))
                    .isInstanceOf(DuplicateRestaurantIdException.class)
                    .hasMessage(scenario.replaceAllDuplicateIds().expectedMessage());

            verifyNoInteractions(restaurantRepository);
            assertThat(restaurantService.getVersion()).isZero();
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldDelegateToRepositoryAndReturnRestaurants() {
            List<Restaurant> expected = scenario.findAll().repositoryResult();
            when(restaurantRepository.findAll()).thenReturn(expected);

            List<Restaurant> actual = restaurantService.findAll();

            assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
            verify(restaurantRepository, times(1)).findAll();
        }
    }

    private record ServiceScenario(
            ReplaceAllSuccessScenario replaceAllSuccess,
            ReplaceAllDuplicateIdsScenario replaceAllDuplicateIds,
            FindAllScenario findAll
    ) {
    }

    private record ReplaceAllSuccessScenario(
            List<Restaurant> input,
            long expectedVersionAfterCall,
            RestaurantResponse expectedResponse
    ) {
    }

    private record ReplaceAllDuplicateIdsScenario(List<Restaurant> input, String expectedMessage) {
    }

    private record FindAllScenario(List<Restaurant> repositoryResult) {
    }
}

