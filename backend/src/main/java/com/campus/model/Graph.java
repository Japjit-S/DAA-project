package com.campus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

/**
 * Generic Graph data structure holding vertices (Nodes) and weighted edges (Edges).
 * Provides foundation methods for upcoming DAA algorithms (BFS, DFS, Dijkstra, Prim's, Kruskal's).
 */
public class Graph {
    private String graphId = "DEFAULT_GRAPH";
    private boolean directed = false;
    private int nodeCount;
    private int edgeCount;
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    public Graph() {
    }

    public Graph(String graphId, boolean directed, List<Node> nodes, List<Edge> edges) {
        this.graphId = graphId;
        this.directed = directed;
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.edges = edges != null ? edges : new ArrayList<>();
        this.nodeCount = this.nodes.size();
        this.edgeCount = this.edges.size();
    }

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public int getNodeCount() {
        return nodes != null ? nodes.size() : nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getEdgeCount() {
        return edges != null ? edges.size() : edgeCount;
    }

    public void setEdgeCount(int edgeCount) {
        this.edgeCount = edgeCount;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.nodeCount = this.nodes.size();
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges != null ? edges : new ArrayList<>();
        this.edgeCount = this.edges.size();
    }

    public void addNode(Node node) {
        if (this.nodes == null) {
            this.nodes = new ArrayList<>();
        }
        this.nodes.add(node);
        this.nodeCount = this.nodes.size();
    }

    public void addEdge(Edge edge) {
        if (this.edges == null) {
            this.edges = new ArrayList<>();
        }
        this.edges.add(edge);
        this.edgeCount = this.edges.size();
    }

    @JsonIgnore
    public Node getNodeById(String nodeId) {
        if (nodes == null || nodeId == null) return null;
        for (Node n : nodes) {
            if (nodeId.equals(n.getId())) {
                return n;
            }
        }
        return null;
    }

    /**
     * Builds an adjacency list mapping Node ID -> List of incident Edges.
     * Ready for consumption by BFS, DFS, Dijkstra, and MST algorithms.
     */
    @JsonIgnore
    public Map<String, List<Edge>> getAdjacencyList() {
        Map<String, List<Edge>> adj = new HashMap<>();
        if (nodes != null) {
            for (Node n : nodes) {
                adj.put(n.getId(), new ArrayList<>());
            }
        }
        if (edges != null) {
            for (Edge e : edges) {
                adj.computeIfAbsent(e.getSource(), k -> new ArrayList<>()).add(e);
                if (e.isBidirectional() || !directed) {
                    adj.computeIfAbsent(e.getTarget(), k -> new ArrayList<>()).add(e);
                }
            }
        }
        return adj;
    }

    /**
     * Returns incident edges for a given node.
     */
    @JsonIgnore
    public List<Edge> getNeighbors(String nodeId) {
        return getAdjacencyList().getOrDefault(nodeId, Collections.emptyList());
    }

    @Override
    public String toString() {
        return "Graph{" +
                "graphId='" + graphId + '\'' +
                ", directed=" + directed +
                ", nodeCount=" + getNodeCount() +
                ", edgeCount=" + getEdgeCount() +
                '}';
    }
}
