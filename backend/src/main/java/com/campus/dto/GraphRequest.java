package com.campus.dto;

/**
 * DTO for querying graph algorithms (routing, MST, accessibility).
 * Serves as an interface contract for upcoming DAA implementations.
 */
public class GraphRequest {
    private String startNodeId;
    private String endNodeId;
    private String algorithm; // e.g., "DIJKSTRA", "BFS", "DFS", "PRIMS", "KRUSKALS"
    private boolean wheelchairAccessibleOnly;

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

    @Override
    public String toString() {
        return "GraphRequest{" +
                "startNodeId='" + startNodeId + '\'' +
                ", endNodeId='" + endNodeId + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", wheelchairAccessibleOnly=" + wheelchairAccessibleOnly +
                '}';
    }
}
