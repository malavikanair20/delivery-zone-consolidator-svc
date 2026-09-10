package com.restaurant.deliveryzone.unit.api.controller;

import com.restaurant.deliveryzone.api.controller.DeliveryGroupingController;
import com.restaurant.deliveryzone.api.model.GroupDetails;
import com.restaurant.deliveryzone.api.model.GroupSummaryResponse;
import com.restaurant.deliveryzone.api.model.RestaurantRequest;
import com.restaurant.deliveryzone.api.model.RestaurantResponse;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.mapper.DeliveryMapper;
import com.restaurant.deliveryzone.service.GroupingService;
import com.restaurant.deliveryzone.service.RestaurantService;
import com.restaurant.deliveryzone.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryGroupingControllerTest {

    private static final String TEST_DATA_PATH = "unit/controller/delivery_grouping_controller/delivery_grouping_controller_scenarios.json";

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private DeliveryMapper deliveryMapper;

    @Mock
    private GroupingService groupingService;

    private DeliveryGroupingController controller;

    private ControllerScenario scenario;

    @BeforeEach
    void setup() {
        controller = new DeliveryGroupingController(restaurantService, deliveryMapper, groupingService);
        scenario = TestDataFactory.retreiveObject(ControllerScenario.class, TEST_DATA_PATH);
    }

    @Nested
    class ReplaceRestaurants {

        @Test
        void shouldReturnOkAndResponseWhenReplacingRestaurants() {
            List<RestaurantRequest> requests = scenario.replaceRestaurants().requests();
            List<Restaurant> mappedRestaurants = scenario.replaceRestaurants().mappedRestaurants();
            RestaurantResponse expectedBody = scenario.replaceRestaurants().expectedResponse();

            when(deliveryMapper.toRestaurant(requests.getFirst())).thenReturn(mappedRestaurants.getFirst());
            when(deliveryMapper.toRestaurant(requests.get(1))).thenReturn(mappedRestaurants.get(1));
            when(restaurantService.replaceAllAndBuildResponse(mappedRestaurants)).thenReturn(expectedBody);

            ResponseEntity<RestaurantResponse> actual = controller.replaceRestaurants(requests);

            assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(actual.getBody()).usingRecursiveComparison().isEqualTo(expectedBody);
            verify(deliveryMapper).toRestaurant(requests.getFirst());
            verify(deliveryMapper).toRestaurant(requests.get(1));
            verify(restaurantService).replaceAllAndBuildResponse(mappedRestaurants);
            verifyNoMoreInteractions(restaurantService, deliveryMapper, groupingService);
        }
    }

    @Nested
    class GetGroups {

        @Test
        void shouldReturnOkAndGroupSummaryResponse() {
            GroupSummaryResponse expectedBody = scenario.getGroups().serviceResponse();
            when(groupingService.getGroupsResponse()).thenReturn(expectedBody);

            ResponseEntity<GroupSummaryResponse> actual = controller.getGroups();

            assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(actual.getBody()).usingRecursiveComparison().isEqualTo(expectedBody);
            verify(groupingService).getGroupsResponse();
            verifyNoMoreInteractions(restaurantService, deliveryMapper, groupingService);
        }
    }

    @Nested
    class GetGroupById {

        @Test
        void shouldReturnOkAndGroupDetailsForProvidedGroupId() {
            String groupId = scenario.getGroupById().groupId();
            GroupDetails expectedBody = scenario.getGroupById().serviceResponse();

            when(groupingService.getGroup(groupId)).thenReturn(expectedBody);

            ResponseEntity<GroupDetails> actual = controller.getGroupById(groupId);

            assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(actual.getBody()).usingRecursiveComparison().isEqualTo(expectedBody);
            verify(groupingService).getGroup(groupId);
            verifyNoMoreInteractions(restaurantService, deliveryMapper, groupingService);
        }
    }

    private record ControllerScenario(
            ReplaceRestaurantsScenario replaceRestaurants,
            GetGroupsScenario getGroups,
            GetGroupByIdScenario getGroupById
    ) {
    }

    private record ReplaceRestaurantsScenario(
            List<RestaurantRequest> requests,
            List<Restaurant> mappedRestaurants,
            RestaurantResponse expectedResponse
    ) {
    }

    private record GetGroupsScenario(
            GroupSummaryResponse serviceResponse
    ) {
    }

    private record GetGroupByIdScenario(
            String groupId,
            GroupDetails serviceResponse
    ) {
    }
}

