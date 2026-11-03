package com.campus.model;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic Edge representing a pathway, road, or corridor connecting two nodes.
 * Supports weighted graph representations and undirected (bidirectional) traversals.
 */
public class Edge {
    private String id;
    private String source;
    private String target;
    private double weight;
    private boolean bidirectional;
    private String type;
    private List<GeoPoint> geometry = new ArrayList<>();
    private String confidence;

    public Edge() {
        this.bidirectional = true;
    }

    public Edge(String id, String source, String target, double weight, boolean bidirectional, String type) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.bidirectional = bidirectional;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /** Ordered GPS points describing the visible road or walkway between the graph endpoints. */
    public List<GeoPoint> getGeometry() {
        return geometry;
    }

    public void setGeometry(List<GeoPoint> geometry) {
        this.geometry = geometry == null ? new ArrayList<>() : geometry;
    }

    /** Confidence of the map trace, especially for paths and facility access points. */
    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(id, edge.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "id='" + id + '\'' +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", weight=" + weight +
                ", bidirectional=" + bidirectional +
                ", type='" + type + '\'' +
                ", geometry=" + geometry +
                ", confidence='" + confidence + '\'' +
                '}';
    }
}
