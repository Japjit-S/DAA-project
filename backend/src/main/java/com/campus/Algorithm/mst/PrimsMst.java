package com.campus.algorithm.mst;

import com.campus.algorithm.GraphFilter;
import com.campus.algorithm.datastructure.MinHeap;
import com.campus.dto.AlgorithmResult;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Prim's Minimum Spanning Tree (MST) Algorithm using a Min-Heap.
 */
@Component
public class PrimsMst {

    private static class EdgeWeightWrapper implements Comparable<EdgeWeightWrapper> {
        final Edge edge;
        final double weight;

        EdgeWeightWrapper(Edge edge) {
            this.edge = edge;
            this.weight = edge.getWeight();
        }

        @Override
        public int compareTo(EdgeWeightWrapper other) {
            return Double.compare(this.weight, other.weight);
        }
    }

    public AlgorithmResult computeMst(
            Graph graph,
            String startNodeId,
            Set<String> blockedEdgeIds,
            boolean wheelchairAccessibleOnly) {

        long startTime = System.nanoTime();
        AlgorithmResult result = new AlgorithmResult();
        result.setAlgorithm("PRIMS_MST");

        if (graph == null || graph.getNodes().isEmpty()) {
            result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
            return result;
        }

        Map<String, Node> nodeMap = GraphFilter.buildNodeMap(graph);
        Map<String, List<Edge>> adj = graph.getAdjacencyList();

        List<Node> allowedNodes = new ArrayList<>();
        for (Node node : graph.getNodes()) {
            if (GraphFilter.isNodeAllowed(node, wheelchairAccessibleOnly)) {
                allowedNodes.add(node);
            }
        }

        if (allowedNodes.isEmpty()) {
            result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
            return result;
        }

        String rootId = (startNodeId != null && nodeMap.containsKey(startNodeId))
                ? startNodeId
                : allowedNodes.get(0).getId();

        Set<String> visited = new HashSet<>();
        List<Edge> mstEdges = new ArrayList<>();
        double totalWeight = 0.0;
        MinHeap<EdgeWeightWrapper> minHeap = new MinHeap<>();

        visited.add(rootId);
        pushIncidentEdges(rootId, adj, visited, blockedEdgeIds, nodeMap, wheelchairAccessibleOnly, graph.isDirected(), minHeap);

        while (!minHeap.isEmpty() && visited.size() < allowedNodes.size()) {
            EdgeWeightWrapper wrapper = minHeap.extractMin();
            Edge edge = wrapper.edge;

            String u = edge.getSource();
            String v = edge.getTarget();

            boolean uVisited = visited.contains(u);
            boolean vVisited = visited.contains(v);

            if (uVisited && vVisited) {
                continue;
            }

            String newNodeId = uVisited ? v : u;
            visited.add(newNodeId);
            mstEdges.add(edge);
            totalWeight += edge.getWeight();

            pushIncidentEdges(newNodeId, adj, visited, blockedEdgeIds, nodeMap, wheelchairAccessibleOnly, graph.isDirected(), minHeap);
        }

        result.setPathEdges(mstEdges);
        result.setTotalWeight(totalWeight);
        result.setReachable(visited.size() == allowedNodes.size());
        result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
        return result;
    }

    private void pushIncidentEdges(
            String nodeId,
            Map<String, List<Edge>> adj,
            Set<String> visited,
            Set<String> blockedEdgeIds,
            Map<String, Node> nodeMap,
            boolean wheelchairAccessibleOnly,
            boolean isDirected,
            MinHeap<EdgeWeightWrapper> minHeap) {

        for (Edge edge : adj.getOrDefault(nodeId, Collections.emptyList())) {
            if (!GraphFilter.isEdgeAllowed(edge, blockedEdgeIds)) {
                continue;
            }
            String neighborId = GraphFilter.getNeighborId(edge, nodeId, isDirected);
            if (neighborId == null || visited.contains(neighborId)) {
                continue;
            }
            Node neighborNode = nodeMap.get(neighborId);
            if (!GraphFilter.isNodeAllowed(neighborNode, wheelchairAccessibleOnly)) {
                continue;
            }
            minHeap.insert(new EdgeWeightWrapper(edge));
        }
    }
}