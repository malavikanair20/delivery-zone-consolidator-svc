package com.restaurant.deliveryzone.integration.api.controller;

import com.restaurant.deliveryzone.api.model.GroupDetails;
import com.restaurant.deliveryzone.api.model.GroupSummary;
import com.restaurant.deliveryzone.api.model.GroupSummaryResponse;
import com.restaurant.deliveryzone.api.model.RestaurantRequest;
import com.restaurant.deliveryzone.api.model.RestaurantResponse;
import com.restaurant.deliveryzone.domain.Restaurant;
import com.restaurant.deliveryzone.integration.ITBase;
import com.restaurant.deliveryzone.util.TestDataFactory;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

class DeliveryGroupingControllerIT extends ITBase {

    private static final String TEST_DATA_PATH = "integration/api/delivery_grouping_controller/delivery_grouping_controller_it_scenarios.json";
    private static final String BASE_PATH = "/v1/delivery";

    @Nested
    class PositiveScenarios {

        @Test
        void shouldReplaceRestaurantsAndReturnDeterministicGroups() {
            ScenarioFile scenarioFile = TestDataFactory.retreiveObject(ScenarioFile.class, TEST_DATA_PATH);
            HappyPathScenario scenario = scenarioFile.happyPath();

            RestaurantResponse replaceResponse = given().port(port)
                    .contentType(ContentType.JSON)
                    .body(scenario.restaurants())
                    .when()
                    .post(BASE_PATH + "/restaurants")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(RestaurantResponse.class);

            assertThat(replaceResponse).usingRecursiveComparison().isEqualTo(scenario.expectedReplaceResponse());

            GroupSummaryResponse groupSummaryResponse = given().port(port)
                    .accept(ContentType.JSON)
                    .when()
                    .get(BASE_PATH + "/groups")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(GroupSummaryResponse.class);

            assertThat(groupSummaryResponse.groupCount()).isEqualTo(scenario.expectedGroupCount());
            assertThat(normalize(groupSummaryResponse.groups()))
                    .usingRecursiveComparison()
                    .isEqualTo(scenario.expectedNormalizedGroups());

            String firstGroupId = groupSummaryResponse.groups().stream()
                    .min(Comparator.comparing(GroupSummary::groupId))
                    .orElseThrow()
                    .groupId();

            GroupDetails details = given().port(port)
                    .accept(ContentType.JSON)
                    .when()
                    .get(BASE_PATH + "/groups/" + firstGroupId)
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(GroupDetails.class);

            assertThat(details.groupId()).isEqualTo(firstGroupId);
            assertThat(details.restaurantIds()).containsExactlyElementsOf(details.restaurants().stream().map(Restaurant::id).toList());
            assertThat(details.restaurantCount()).isEqualTo(details.restaurants().size());
        }
    }

    @Nested
    class NegativeScenarios {

        @ParameterizedTest
        @MethodSource("com.restaurant.deliveryzone.integration.api.controller.DeliveryGroupingControllerIT#invalidRestaurantPayloadScenarios")
        void shouldReturnBadRequestForInvalidRestaurantPayload(InvalidPayloadScenario scenario) {
            Response response = given().port(port)
                    .contentType(ContentType.JSON)
                    .body(scenario.payload())
                    .when()
                    .post(BASE_PATH + "/restaurants")
                    .then()
                    .statusCode(400)
                    .extract()
                    .response();

            Map<String, Object> body = response.jsonPath().getMap("$");
            assertThat(body).containsEntry("status", 400);
            assertThat(body).containsEntry("error", "Validation Failed");
            assertThat(body.get("messages")).isInstanceOf(Map.class);
            assertThat((Map<?, ?>) body.get("messages")).isNotEmpty();
        }

        @Test
        void shouldReturnBadRequestForDuplicateRestaurantIds() {
            ScenarioFile scenarioFile = TestDataFactory.retreiveObject(ScenarioFile.class, TEST_DATA_PATH);
            DuplicateIdScenario scenario = scenarioFile.duplicateIdScenario();

            Response response = given().port(port)
                    .contentType(ContentType.JSON)
                    .body(scenario.payload())
                    .when()
                    .post(BASE_PATH + "/restaurants")
                    .then()
                    .statusCode(400)
                    .extract()
                    .response();

            Map<String, Object> body = response.jsonPath().getMap("$");
            assertThat(body).containsEntry("status", 400);
            assertThat(body).containsEntry("error", scenario.expectedError());
        }

        @Test
        void shouldReturnNotFoundForUnknownGroupId() {
            ScenarioFile scenarioFile = TestDataFactory.retreiveObject(ScenarioFile.class, TEST_DATA_PATH);
            MissingGroupScenario scenario = scenarioFile.missingGroupScenario();

            given().port(port)
                    .contentType(ContentType.JSON)
                    .body(scenario.dataset())
                    .when()
                    .post(BASE_PATH + "/restaurants")
                    .then()
                    .statusCode(200);

            Response response = given().port(port)
                    .accept(ContentType.JSON)
                    .when()
                    .get(BASE_PATH + "/groups/" + scenario.groupId())
                    .then()
                    .statusCode(404)
                    .extract()
                    .response();

            Map<String, Object> body = response.jsonPath().getMap("$");
            assertThat(body).containsEntry("status", 404);
            assertThat(body).containsEntry("error", scenario.expectedError());
        }
    }

    static Stream<InvalidPayloadScenario> invalidRestaurantPayloadScenarios() {
        ScenarioFile scenarioFile = TestDataFactory.retreiveObject(ScenarioFile.class, TEST_DATA_PATH);
        return scenarioFile.invalidPayloadScenarios().stream();
    }

    private List<NormalizedGroup> normalize(List<GroupSummary> groups) {
        return groups.stream()
                .map(group -> new NormalizedGroup(group.restaurantIds(), group.restaurantCount()))
                .sorted(Comparator.comparing(group -> group.restaurantIds().getFirst()))
                .toList();
    }


    private record ScenarioFile(
            HappyPathScenario happyPath,
            List<InvalidPayloadScenario> invalidPayloadScenarios,
            DuplicateIdScenario duplicateIdScenario,
            MissingGroupScenario missingGroupScenario
    ) {
    }

    private record HappyPathScenario(
            List<RestaurantRequest> restaurants,
            RestaurantResponse expectedReplaceResponse,
            int expectedGroupCount,
            List<NormalizedGroup> expectedNormalizedGroups
    ) {
    }

    private record InvalidPayloadScenario(String scenario, List<Map<String, Object>> payload) {
    }

    private record DuplicateIdScenario(List<RestaurantRequest> payload, String expectedError) {
    }

    private record MissingGroupScenario(List<RestaurantRequest> dataset, String groupId, String expectedError) {
    }

    private record NormalizedGroup(List<String> restaurantIds, int restaurantCount) {
    }
}

