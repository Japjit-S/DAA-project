package com.campus.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for querying graph algorithms (routing, MST, accessibility).
 * Serves as an interface contract for upcoming DAA implementations.
 */
public class GraphRequest {
    private String startNodeId;
    private String endNodeId;
    private String algorithm; // e.g., "DIJKSTRA", "BFS", "DFS", "PRIMS", "KRUSKALS"
    private boolean wheelchairAccessibleOnly;
    private List<String> blockedEdgeIds = new ArrayList<>();

    public GraphRequest() {
    }

    public GraphRequest(String startNodeId, String endNodeId, String algorithm, boolean wheelchairAccessibleOnly) {
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.algorithm = algorithm;
        this.wheelchairAccessibleOnly = wheelchairAccessibleOnly;
    }

    public String getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(String startNodeId) {
        this.startNodeId = startNodeId;
    }

    public String getEndNodeId() {
        return endNodeId;
    }

    public void setEndNodeId(String endNodeId) {
        this.endNodeId = endNodeId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public boolean isWheelchairAccessibleOnly() {
        return wheelchairAccessibleOnly;
    }

    public void setWheelchairAccessibleOnly(boolean wheelchairAccessibleOnly) {
        this.wheelchairAccessibleOnly = wheelchairAccessibleOnly;
    }

    public List<String> getBlockedEdgeIds() {
        return blockedEdgeIds;
    }

    public void setBlockedEdgeIds(List<String> blockedEdgeIds) {
        this.blockedEdgeIds = blockedEdgeIds != null ? blockedEdgeIds : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "GraphRequest{" +
                "startNodeId='" + startNodeId + '\'' +
                ", endNodeId='" + endNodeId + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", wheelchairAccessibleOnly=" + wheelchairAccessibleOnly +
                ", blockedEdgeIds=" + blockedEdgeIds +
                '}';
    }
}