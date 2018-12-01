package org.emstrack.models;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Waypoint {

    static class SortByOrder implements Comparator<Waypoint>
    {
        public int compare(Waypoint a, Waypoint b) {
            return a.order - b.order;
        }
    }

    public static final String STATUS_NOT_VISITED = "N";
    public static final String STATUS_VISITING = "V";
    public static final String STATUS_VISITED = "D";

    public static final Map<String, String> statusLabel;
    static {

        Map<String, String> map = new HashMap<>();

        map.put(STATUS_NOT_VISITED, "Not yet visited");
        map.put(STATUS_VISITED, "Visited");
        map.put(STATUS_VISITING, "Visiting");

        statusLabel = Collections.unmodifiableMap(map);
    }

    private int id;
    private int ambulanceCallId;
    private int order;
    private String status;
    private boolean active;
    private Location location;
    private String comment;
    private int updatedBy;
    private Date updatedOn;

    public Waypoint(int id, int ambulanceCallId, int order, String status,
                    boolean active, Location location,
                    String comment, int updatedBy, Date updatedOn) {
        this.id = id;
        this.ambulanceCallId = ambulanceCallId;
        this.order = order;
        this.status = status;
        this.active = active;
        this.location = location;
        this.comment = comment;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
    }

    public Waypoint(int order, String status, Location location) {
        this(-1, -1, order, status, true, location,
                "", -1, new Date());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAmbulanceCallId() {
        return ambulanceCallId;
    }

    public void setAmbulanceCallId(int ambulanceCallId) {
        this.ambulanceCallId = ambulanceCallId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isNotVisited() {
        return status.equals(STATUS_NOT_VISITED);
    }

    public boolean isVisited() {
        return status.equals(STATUS_VISITED);
    }

    public boolean isVisiting() {
        return status.equals(STATUS_VISITING);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

}
