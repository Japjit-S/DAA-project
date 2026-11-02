package com.campus.algorithm.shortestpath;

import com.campus.algorithm.GraphFilter;
import com.campus.algorithm.datastructure.MinHeap;
import com.campus.dto.AlgorithmResult;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Dijkstra's Shortest Path Algorithm implementation for the Smart Campus graph.
 */
@Component
public class DijkstraPathfinder {

    private static class NodeDistancePair implements Comparable<NodeDistancePair> {
        final String nodeId;
        final double distance;

        NodeDistancePair(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistancePair other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    public AlgorithmResult findShortestPath(
            Graph graph,
            String startNodeId,
            String endNodeId,
            Set<String> blockedEdgeIds,
            boolean wheelchairAccessibleOnly) {

        long startTime = System.nanoTime();
        AlgorithmResult result = new AlgorithmResult();
        result.setAlgorithm("DIJKSTRA");
        result.setStartNodeId(startNodeId);
        result.setEndNodeId(endNodeId);

        if (graph == null || startNodeId == null || endNodeId == null) {
            result.setReachable(false);
            result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
            return result;
        }

        Map<String, Node> nodeMap = GraphFilter.buildNodeMap(graph);
        Map<String, List<Edge>> adj = graph.getAdjacencyList();

        Node startNode = nodeMap.get(startNodeId);
        Node endNode = nodeMap.get(endNodeId);

        if (!GraphFilter.isNodeAllowed(startNode, wheelchairAccessibleOnly) ||
            !GraphFilter.isNodeAllowed(endNode, wheelchairAccessibleOnly)) {
            result.setReachable(false);
            result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
            return result;
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, String> parentNodeMap = new HashMap<>();
        Map<String, Edge> parentEdgeMap = new HashMap<>();
        Set<String> visited = new LinkedHashSet<>();
        MinHeap<NodeDistancePair> minHeap = new MinHeap<>();

        for (Node node : graph.getNodes()) {
            distances.put(node.getId(), Double.POSITIVE_INFINITY);
        }

        distances.put(startNodeId, 0.0);
        minHeap.insert(new NodeDistancePair(startNodeId, 0.0));

        boolean found = false;

        while (!minHeap.isEmpty()) {
            NodeDistancePair current = minHeap.extractMin();
            String u = current.nodeId;

            if (visited.contains(u)) {
                continue;
            }
            visited.add(u);

            if (u.equals(endNodeId)) {
                found = true;
                break;
            }

            if (current.distance > distances.get(u)) {
                continue;
            }

            for (Edge edge : adj.getOrDefault(u, Collections.emptyList())) {
                if (!GraphFilter.isEdgeAllowed(edge, blockedEdgeIds)) {
                    continue;
                }

                String v = GraphFilter.getNeighborId(edge, u, graph.isDirected());
                if (v == null || visited.contains(v)) {
                    continue;
                }

                Node neighborNode = nodeMap.get(v);
                if (!GraphFilter.isNodeAllowed(neighborNode, wheelchairAccessibleOnly)) {
                    continue;
                }

                double newDist = distances.get(u) + edge.getWeight();
                if (newDist < distances.get(v)) {
                    distances.put(v, newDist);
                    parentNodeMap.put(v, u);
                    parentEdgeMap.put(v, edge);
                    minHeap.insert(new NodeDistancePair(v, newDist));
                }
            }
        }

        result.setVisitedOrder(new ArrayList<>(visited));
        result.setReachable(found);

        if (found) {
            GraphFilter.populatePath(result, startNodeId, endNodeId, nodeMap, parentNodeMap, parentEdgeMap);
        } else {
            result.setTotalWeight(Double.POSITIVE_INFINITY);
        }

        result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
        return result;
    }
}