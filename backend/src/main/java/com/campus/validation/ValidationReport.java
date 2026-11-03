package com.campus.validation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the results of validating campus graph data, buildings, geometry, and connectivity.
 */
public class ValidationReport {

    private final String timestamp;
    private boolean valid;
    private int nodeCount;
    private int edgeCount;
    private int buildingCount;
    private int connectedComponentCount;
    private List<List<String>> connectedComponents = new ArrayList<>();
    private int lowConfidenceEdgeCount;
    private double maxEndpointDeviationMeters;
    private double maxWeightDeviationMeters;
    private final List<ValidationIssue> issues = new ArrayList<>();

    public ValidationReport() {
        this.timestamp = Instant.now().toString();
        this.valid = true;
    }

    public void addIssue(ValidationIssue issue) {
        if (issue != null) {
            issues.add(issue);
            if (issue.getSeverity() == ValidationIssue.Severity.ERROR) {
                this.valid = false;
            }
        }
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public void setEdgeCount(int edgeCount) {
        this.edgeCount = edgeCount;
    }

    public int getBuildingCount() {
        return buildingCount;
    }

    public void setBuildingCount(int buildingCount) {
        this.buildingCount = buildingCount;
    }

    public int getConnectedComponentCount() {
        return connectedComponentCount;
    }

    public void setConnectedComponentCount(int connectedComponentCount) {
        this.connectedComponentCount = connectedComponentCount;
    }

    public List<List<String>> getConnectedComponents() {
        return Collections.unmodifiableList(connectedComponents);
    }

    public void setConnectedComponents(List<List<String>> connectedComponents) {
        this.connectedComponents = connectedComponents != null ? new ArrayList<>(connectedComponents) : new ArrayList<>();
    }

    public int getLowConfidenceEdgeCount() {
        return lowConfidenceEdgeCount;
    }

    public void setLowConfidenceEdgeCount(int lowConfidenceEdgeCount) {
        this.lowConfidenceEdgeCount = lowConfidenceEdgeCount;
    }

    public double getMaxEndpointDeviationMeters() {
        return maxEndpointDeviationMeters;
    }

    public void setMaxEndpointDeviationMeters(double maxEndpointDeviationMeters) {
        this.maxEndpointDeviationMeters = maxEndpointDeviationMeters;
    }

    public double getMaxWeightDeviationMeters() {
        return maxWeightDeviationMeters;
    }

    public void setMaxWeightDeviationMeters(double maxWeightDeviationMeters) {
        this.maxWeightDeviationMeters = maxWeightDeviationMeters;
    }

    public List<ValidationIssue> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    public int getErrorCount() {
        return (int) issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.ERROR).count();
    }

    public int getWarningCount() {
        return (int) issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.WARNING).count();
    }

    public int getInfoCount() {
        return (int) issues.stream().filter(i -> i.getSeverity() == ValidationIssue.Severity.INFO).count();
    }

    public String toSummaryString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Validation Report [%s]: %s\n", timestamp, valid ? "VALID" : "INVALID"));
        sb.append(String.format("  Counts: Nodes=%d, Edges=%d, Buildings=%d, Components=%d\n",
                nodeCount, edgeCount, buildingCount, connectedComponentCount));
        sb.append(String.format("  Metrics: MaxEndpointDeviation=%.4fm, MaxWeightDeviation=%.4fm, LowConfidenceEdges=%d\n",
                maxEndpointDeviationMeters, maxWeightDeviationMeters, lowConfidenceEdgeCount));
        sb.append(String.format("  Issues: Errors=%d, Warnings=%d, Info=%d\n",
                getErrorCount(), getWarningCount(), getInfoCount()));
        for (ValidationIssue issue : issues) {
            sb.append("    ").append(issue.toString()).append("\n");
        }
        return sb.toString();
    }
}
