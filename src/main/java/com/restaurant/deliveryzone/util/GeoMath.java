package com.restaurant.deliveryzone.util;

public final class GeoMath {

    private GeoMath() {
    }

    public static final double EARTH_RADIUS_METERS = 6_371_000.0;

    public static double haversineMeters(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2) {

        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);

        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(longitude2 - longitude1);

        double sinLat = Math.sin(deltaLat / 2.0);
        double sinLon = Math.sin(deltaLon / 2.0);

        double a = sinLat * sinLat
                + Math.cos(lat1) * Math.cos(lat2) * sinLon * sinLon;

        // Protect against tiny floating point errors.
        a = Math.clamp(a, 0.0, 1.0);

        return 2.0 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
    }
}
