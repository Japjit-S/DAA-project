package com.campus.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Building entity representing a campus facility, department, or hostel.
 * Links to a primary Node on the campus graph via nodeId.
 */
public class Building {
    private String id;
    private String name;
    private String code;
    private String category;
    private String nodeId;
    private String referenceNodeId;
    private String accessConfidence;
    private String accessNote;
    private String description;
    private int floors;
    private List<String> facilities = new ArrayList<>();

    public Building() {
    }

    public Building(String id, String name, String code, String category, String nodeId,
                    String description, int floors, List<String> facilities) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.category = category;
        this.nodeId = nodeId;
        this.description = description;
        this.floors = floors;
        this.facilities = facilities != null ? facilities : new ArrayList<>();
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getReferenceNodeId() { return referenceNodeId; }
    public void setReferenceNodeId(String referenceNodeId) { this.referenceNodeId = referenceNodeId; }
    public String getAccessConfidence() { return accessConfidence; }
    public void setAccessConfidence(String accessConfidence) { this.accessConfidence = accessConfidence; }
    public String getAccessNote() { return accessNote; }
    public void setAccessNote(String accessNote) { this.accessNote = accessNote; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFloors() {
        return floors;
    }

    public void setFloors(int floors) {
        this.floors = floors;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities != null ? facilities : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Building building = (Building) o;
        return Objects.equals(id, building.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Building{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", category='" + category + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", floors=" + floors +
                '}';
    }
}
