package com.campus.model;

import java.util.Objects;

/**
 * Generic Node representing a physical location, intersection, or point of interest
 * on campus. Coordinates x and y are normalized to the campus map canvas.
 * Geographic coordinates (lat, lng) store the real-world GPS position.
 */
public class Node {
    private String id;
    private String name;
    private String type;
    private double x;
    private double y;
    private boolean accessible;
    private Double lat;
    private Double lng;

    public Node() {
    }

    public Node(String id, String name, String type, double x, double y, boolean accessible) {
        this(id, name, type, x, y, accessible, null, null);
    }

    public Node(String id, String name, String type, double x, double y, boolean accessible, Double lat, Double lng) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.accessible = accessible;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", accessible=" + accessible +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
