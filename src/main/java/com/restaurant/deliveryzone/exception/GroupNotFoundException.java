package com.restaurant.deliveryzone.exception;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(String groupId) {
        super("No group found with id '" + groupId + "'");
    }
}
