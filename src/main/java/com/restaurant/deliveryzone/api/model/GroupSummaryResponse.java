package com.restaurant.deliveryzone.api.model;

import java.util.List;

    public record GroupSummaryResponse(int groupCount, List<GroupSummary> groups) {

}
