package com.restaurant.deliveryzone.unit.service;

import com.restaurant.deliveryzone.api.model.GroupDetails;
import com.restaurant.deliveryzone.api.model.GroupSummary;
import com.restaurant.deliveryzone.api.model.GroupSummaryResponse;
import com.restaurant.deliveryzone.domain.ClusterResult;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.exception.GroupNotFoundException;
import com.restaurant.deliveryzone.service.GroupingEngine;
import com.restaurant.deliveryzone.service.GroupingService;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupingServiceTest {

    private static final String TEST_DATA_PATH = "unit/service/grouping_service/grouping_service_scenarios.json";

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private GroupingEngine groupingEngine;

    private GroupingService groupingService;
    private GroupingServiceScenario scenario;

    @BeforeEach
    void setup() {
        groupingService = new GroupingService(restaurantService, groupingEngine);
        scenario = TestDataFactory.retreiveObject(GroupingServiceScenario.class, TEST_DATA_PATH);
    }

    @Nested
    class GetGroupsResponse {

        @Test
        void shouldBuildAndReturnGroupSummaryResponseFromClusters() {
            when(restaurantService.getVersion()).thenReturn(scenario.getGroupsResponse().version());
            when(restaurantService.findAll()).thenReturn(scenario.getGroupsResponse().restaurants());
            when(groupingEngine.calculateGroups(scenario.getGroupsResponse().restaurants())).thenReturn(scenario.getGroupsResponse().clusters());

            GroupSummaryResponse actual = groupingService.getGroupsResponse();

            assertThat(actual).usingRecursiveComparison().isEqualTo(scenario.getGroupsResponse().expectedResponse());
            verify(restaurantService, times(1)).getVersion();
            verify(restaurantService, times(1)).findAll();
            verify(groupingEngine, times(1)).calculateGroups(scenario.getGroupsResponse().restaurants());
            verifyNoMoreInteractions(restaurantService, groupingEngine);
        }

        @Test
        void shouldUseCacheWhenVersionUnchanged() {
            when(restaurantService.getVersion()).thenReturn(scenario.cacheHit().version(), scenario.cacheHit().version());
            when(restaurantService.findAll()).thenReturn(scenario.cacheHit().restaurants());
            when(groupingEngine.calculateGroups(scenario.cacheHit().restaurants())).thenReturn(scenario.cacheHit().clusters());

            GroupSummaryResponse first = groupingService.getGroupsResponse();
            GroupSummaryResponse second = groupingService.getGroupsResponse();

            assertThat(second).usingRecursiveComparison().isEqualTo(first);
            verify(restaurantService, times(2)).getVersion();
            verify(restaurantService, times(1)).findAll();
            verify(groupingEngine, times(1)).calculateGroups(scenario.cacheHit().restaurants());
            verifyNoMoreInteractions(restaurantService, groupingEngine);
        }
    }

    @Nested
    class GetGroupById {

        @Test
        void shouldReturnGroupDetailsForExistingGroupId() {
            when(restaurantService.getVersion()).thenReturn(scenario.getGroupById().version());
            when(restaurantService.findAll()).thenReturn(scenario.getGroupById().restaurants());
            when(groupingEngine.calculateGroups(scenario.getGroupById().restaurants())).thenReturn(scenario.getGroupById().clusters());

            GroupDetails actual = groupingService.getGroup(scenario.getGroupById().groupId());

            assertThat(actual).usingRecursiveComparison().isEqualTo(scenario.getGroupById().expectedDetails());
            verify(restaurantService, times(1)).getVersion();
            verify(restaurantService, times(1)).findAll();
            verify(groupingEngine, times(1)).calculateGroups(scenario.getGroupById().restaurants());
            verifyNoMoreInteractions(restaurantService, groupingEngine);
        }

        @Test
        void shouldThrowWhenGroupIdDoesNotExist() {
            when(restaurantService.getVersion()).thenReturn(scenario.groupNotFound().version());
            when(restaurantService.findAll()).thenReturn(scenario.groupNotFound().restaurants());
            when(groupingEngine.calculateGroups(scenario.groupNotFound().restaurants())).thenReturn(scenario.groupNotFound().clusters());

            assertThatThrownBy(() -> groupingService.getGroup(scenario.groupNotFound().groupId()))
                    .isInstanceOf(GroupNotFoundException.class)
                    .hasMessage(scenario.groupNotFound().expectedMessage());

            verify(restaurantService, times(1)).getVersion();
            verify(restaurantService, times(1)).findAll();
            verify(groupingEngine, times(1)).calculateGroups(scenario.groupNotFound().restaurants());
            verifyNoMoreInteractions(restaurantService, groupingEngine);
        }
    }

    @Nested
    class InvalidateCache {

        @Test
        void shouldForceRebuildEvenWhenVersionIsSame() {
            when(restaurantService.getVersion()).thenReturn(scenario.invalidateCache().version(), scenario.invalidateCache().version());
            when(restaurantService.findAll()).thenAnswer(ignored -> scenario.invalidateCache().restaurants());
            when(groupingEngine.calculateGroups(scenario.invalidateCache().restaurants()))
                    .thenAnswer(ignored -> scenario.invalidateCache().clusters());

            GroupSummaryResponse first = groupingService.getGroupsResponse();
            groupingService.invalidateCache();
            GroupSummaryResponse second = groupingService.getGroupsResponse();

            assertThat(second).usingRecursiveComparison().isEqualTo(first);
            verify(restaurantService, times(2)).getVersion();
            verify(restaurantService, times(2)).findAll();
            verify(groupingEngine, times(2)).calculateGroups(scenario.invalidateCache().restaurants());
            verifyNoMoreInteractions(restaurantService, groupingEngine);
        }
    }

    private record GroupingServiceScenario(
            GetGroupsScenario getGroupsResponse,
            CacheHitScenario cacheHit,
            GetGroupByIdScenario getGroupById,
            GroupNotFoundScenario groupNotFound,
            InvalidateCacheScenario invalidateCache
    ) {
    }

    private record GetGroupsScenario(
            long version,
            List<Restaurant> restaurants,
            List<ClusterResult> clusters,
            GroupSummaryResponse expectedResponse
    ) {
    }

    private record CacheHitScenario(long version, List<Restaurant> restaurants, List<ClusterResult> clusters) {
    }

    private record GetGroupByIdScenario(long version, String groupId, List<Restaurant> restaurants, List<ClusterResult> clusters, GroupDetails expectedDetails) {
    }

    private record GroupNotFoundScenario(long version, String groupId, String expectedMessage, List<Restaurant> restaurants, List<ClusterResult> clusters) {
    }

    private record InvalidateCacheScenario(long version, List<Restaurant> restaurants, List<ClusterResult> clusters) {
    }
}

