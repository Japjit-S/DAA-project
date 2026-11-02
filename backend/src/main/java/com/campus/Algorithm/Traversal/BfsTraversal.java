package com.campus.algorithm.traversal;

import com.campus.algorithm.GraphFilter;
import com.campus.dto.AlgorithmResult;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Breadth-First Search (BFS) implementation for the Smart Campus graph.
 * - Finds whether a destination is reachable.
 * - Guarantees the minimum-hop path between startNodeId and endNodeId.
 * - Reconstructs both the Node sequence and Edge sequence of the path.
 * - Handles disconnected vertices and wheelchair/blocked-edge constraints.
 */
@Component
public class BfsTraversal {

    /**
     * Executes BFS from startNodeId.
     * If endNodeId is provided, stops early once endNodeId is reached and reconstructs the min-hop path.
     * If endNodeId is null or blank, performs a complete level-order traversal of all reachable nodes.
     */
    public AlgorithmResult executeBfs(
            Graph graph,
            String startNodeId,
            String endNodeId,
            Set<String> blockedEdgeIds,
            boolean wheelchairAccessibleOnly) {

        long startTime = System.nanoTime();
        AlgorithmResult result = new AlgorithmResult();
        result.setAlgorithm("BFS");
        result.setStartNodeId(startNodeId);
        result.setEndNodeId(endNodeId);

        if (graph == null || startNodeId == null || startNodeId.isBlank()) {
            result.setReachable(false);
            result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
            return result;
        }

        Map<String, Node> nodeMap = GraphFilter.buildNodeMap(graph);
        Map<String, List<Edge>> adj = graph.getAdjacencyList();

        Node startNode = nodeMap.get(startNodeId);
        if (!GraphFilter.isNodeAllowed(startNode, wheelchairAccessibleOnly)) {
            result.setReachable(false);
            result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
            return result;
        }

        boolean hasTarget = (endNodeId != null && !endNodeId.isBlank());
        if (hasTarget) {
            Node targetNode = nodeMap.get(endNodeId);
            if (!GraphFilter.isNodeAllowed(targetNode, wheelchairAccessibleOnly)) {
                result.setReachable(false);
                result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
                return result;
            }
        }

        // Core BFS Data Structures
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new LinkedHashSet<>(); // Preserves discovery order
        Map<String, String> parentNodeMap = new HashMap<>();
        Map<String, Edge> parentEdgeMap = new HashMap<>();

        // Initialize with start node
        queue.offer(startNodeId);
        visited.add(startNodeId);

        boolean targetFound = startNodeId.equals(endNodeId);

        while (!queue.isEmpty() && !targetFound) {
            String currentId = queue.poll();
            List<Edge> incidentEdges = adj.getOrDefault(currentId, Collections.emptyList());

            for (Edge edge : incidentEdges) {
                if (!GraphFilter.isEdgeAllowed(edge, blockedEdgeIds)) {
                    continue;
                }

                String neighborId = GraphFilter.getNeighborId(edge, currentId, graph.isDirected());
                if (neighborId == null || visited.contains(neighborId)) {
                    continue;
                }

                Node neighborNode = nodeMap.get(neighborId);
                if (!GraphFilter.isNodeAllowed(neighborNode, wheelchairAccessibleOnly)) {
                    continue;
                }

                // Mark visited immediately upon enqueueing to prevent duplicate entries
                visited.add(neighborId);
                parentNodeMap.put(neighborId, currentId);
                parentEdgeMap.put(neighborId, edge);
                queue.offer(neighborId);

                if (hasTarget && neighborId.equals(endNodeId)) {
                    targetFound = true;
                    break;
                }
            }
        }

        result.setVisitedOrder(new ArrayList<>(visited));

        if (hasTarget) {
            result.setReachable(targetFound);
            if (targetFound) {
                GraphFilter.populatePath(result, startNodeId, endNodeId, nodeMap, parentNodeMap, parentEdgeMap);
            }
        } else {
            // Full traversal mode (no specific endNodeId requested)
            result.setReachable(!visited.isEmpty());
            List<Node> visitedNodes = new ArrayList<>();
            for (String id : visited) {
                visitedNodes.add(nodeMap.get(id));
            }
            result.setPathNodes(visitedNodes);
            result.setHopCount(Math.max(0, visitedNodes.size() - 1));
        }

        result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
        return result;
    }
}