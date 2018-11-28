package org.emstrack.models;

public class Location extends Address {

    public static final String TYPE_BASE = "b";
    public static final String TYPE_AED = "a";
    public static final String TYPE_INCIDENT = "i";
    public static final String TYPE_HOSPITAL = "h";
    public static final String TYPE_WAYPOINT = "w";
    public static final String TYPE_OTHER = "o";

    private String name;
    private String type;

    public Location(String name, String type, GPSLocation location) {
        super(location);
        this.name = name;
        this.type = type;
    }
    public Location(String name, String type,
                    String number, String street, String unit, String neighborhood, String city,
                    String state, String zipcode, String country,
                    GPSLocation location) {
        super(number, street, unit, neighborhood, city, state, zipcode, country, location);
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
