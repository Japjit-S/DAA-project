package com.campus.dto;

import com.campus.model.Edge;
import com.campus.model.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Standardized response payload for all DAA graph algorithms
 * (BFS, DFS, Dijkstra, A*, Prim's, Kruskal's).
 */
public class AlgorithmResult {
    private String algorithm;
    private String startNodeId;
    private String endNodeId;
    private boolean reachable;
    private int hopCount;
    private double totalWeight;
    private List<Node> pathNodes = new ArrayList<>();
    private List<Edge> pathEdges = new ArrayList<>();
    private List<String> visitedOrder = new ArrayList<>();
    private boolean cycleDetected;
    private int connectedComponentsCount;
    private List<List<String>> connectedComponents = new ArrayList<>();
    private long executionTimeMicros;

    public AlgorithmResult() {
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
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

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public List<Node> getPathNodes() {
        return pathNodes;
    }

    public void setPathNodes(List<Node> pathNodes) {
        this.pathNodes = pathNodes != null ? pathNodes : new ArrayList<>();
    }

    public List<Edge> getPathEdges() {
        return pathEdges;
    }

    public void setPathEdges(List<Edge> pathEdges) {
        this.pathEdges = pathEdges != null ? pathEdges : new ArrayList<>();
    }

    public List<String> getVisitedOrder() {
        return visitedOrder;
    }

    public void setVisitedOrder(List<String> visitedOrder) {
        this.visitedOrder = visitedOrder != null ? visitedOrder : new ArrayList<>();
    }

    public boolean isCycleDetected() {
        return cycleDetected;
    }

    public void setCycleDetected(boolean cycleDetected) {
        this.cycleDetected = cycleDetected;
    }

    public int getConnectedComponentsCount() {
        return connectedComponentsCount;
    }

    public void setConnectedComponentsCount(int connectedComponentsCount) {
        this.connectedComponentsCount = connectedComponentsCount;
    }

    public List<List<String>> getConnectedComponents() {
        return connectedComponents;
    }

    public void setConnectedComponents(List<List<String>> connectedComponents) {
        this.connectedComponents = connectedComponents != null ? connectedComponents : new ArrayList<>();
    }

    public long getExecutionTimeMicros() {
        return executionTimeMicros;
    }

    public void setExecutionTimeMicros(long executionTimeMicros) {
        this.executionTimeMicros = executionTimeMicros;
    }
}