package org.emstrack.ambulance.util;

import android.location.Location;

/**
 * Created by mauricio on 3/14/2018.
 */

public class LatLon {

    public final static double earthRadius = 6371e3; // in meters
    public static double stationaryRadius = 10.; // in meters
    public static double stationaryVelocity= .1; // in meters/s

    public static double calculateDistanceHaversine(Location location1, Location location2) {

        // convert latitude and longitude to radians first
        double lat1 = Math.PI * location1.getLatitude() / 180;
        double lat2 = Math.PI * location2.getLatitude() / 180;
        double d_phi = lat2 - lat1;
        double d_lambda = Math.PI * (location2.getLongitude() - location1.getLongitude()) / 180;

        double a = Math.sin(d_phi / 2) * Math.sin(d_phi / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(d_lambda / 2) * Math.sin(d_lambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return LatLon.earthRadius * c;

    }

    public static double calculateBearing(Location location1, Location location2) {

        // convert latitude and longitude to radians first
        double lat1 = Math.PI * location1.getLatitude() / 180;
        double lat2 = Math.PI * location2.getLatitude() / 180;
        double d_lambda = Math.PI * (location2.getLongitude() - location1.getLongitude()) / 180;

        // calculate bearing and convert to degrees
        double bearing = (180 / Math.PI) * Math.atan2(Math.sin(d_lambda) * Math.cos(lat2),
                Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(d_lambda));

        return (bearing < 0 ? bearing + 360 : bearing);

    }

    public static double[] calculateDistanceAndBearing(Location location1, Location location2) {

        // convert latitude and longitude to radians first
        double lat1 = Math.PI * location1.getLatitude() / 180;
        double lat2 = Math.PI * location2.getLatitude() / 180;
        double d_phi = lat2 - lat1;
        double d_lambda = Math.PI * (location2.getLongitude() - location1.getLongitude()) / 180;

        // calculate distance
        double a = Math.sin(d_phi / 2) * Math.sin(d_phi / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(d_lambda / 2) * Math.sin(d_lambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = LatLon.earthRadius * c;

        // calculate bearing and convert to degrees
        double bearing = (180 / Math.PI) * Math.atan2(Math.sin(d_lambda) * Math.cos(lat2),
                Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(d_lambda));

        return new double[] {distance, (bearing < 0 ? bearing + 360 : bearing)};

    }

    public static Location updateLocation(Location start, double bearing, double distance) {

        // convert latitude, longitude, and bearing to radians first
        double lat1 = Math.PI * start.getLatitude() / 180;
        double lon1 = Math.PI * start.getLatitude() / 180;
        double brng = Math.PI * bearing / 180;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / earthRadius) +
                        Math.cos(lat1) * Math.sin(distance / earthRadius) * Math.cos(brng));
        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(distance / earthRadius) * Math.cos(lat1),
                Math.cos(distance / earthRadius) - Math.sin(lat1) * Math.sin(lat2));

        Location location = new Location(start);
        location.setLatitude(lat2);
        location.setLongitude(lon2);

        return location;

    }
}
