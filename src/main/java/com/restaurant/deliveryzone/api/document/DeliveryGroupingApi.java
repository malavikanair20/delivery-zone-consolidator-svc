package com.restaurant.deliveryzone.api.document;

import com.restaurant.deliveryzone.api.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("v1/delivery")
@Validated
@Tag(name = "Delivery Grouping Api", description = "API for restaurant delivery zone consolidation")
public interface DeliveryGroupingApi {

    @Operation(summary = "Replace restaurant dataset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "restaurants successfully stored"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/restaurants")
    ResponseEntity<RestaurantResponse> replaceRestaurants(
            @RequestBody List<@Valid RestaurantRequest> restaurantRequestList
            );

    @Operation(summary = "Get consolidated delivery groups")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups generated successfully")
    })
    @GetMapping("/groups")
    ResponseEntity<GroupSummaryResponse> getGroups();

    @Operation(summary = "Get individual delivery groups by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups generated successfully")
    })
    @GetMapping("/groups/{groupId}")
    ResponseEntity<GroupDetails> getGroupById(@PathVariable String groupId);
}
