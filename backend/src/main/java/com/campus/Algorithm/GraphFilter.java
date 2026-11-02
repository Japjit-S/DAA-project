package com.campus.algorithm;

import com.campus.dto.AlgorithmResult;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;

import java.util.*;

/**
 * Shared graph utility for resolving undirected neighbors, filtering blocked edges
 * and wheelchair-inaccessible nodes, and reconstructing paths from parent maps.
 */
public final class GraphFilter {

    private GraphFilter() {
    }

    /**
     * Builds a fast O(1) lookup map from Node ID -> Node object.
     */
    public static Map<String, Node> buildNodeMap(Graph graph) {
        Map<String, Node> map = new HashMap<>();
        if (graph != null && graph.getNodes() != null) {
            for (Node node : graph.getNodes()) {
                map.put(node.getId(), node);
            }
        }
        return map;
    }

    /**
     * Given an edge incident to currentNodeId, returns the Node ID at the opposite end.
     * Returns null if the edge cannot be traversed in that direction.
     */
    public static String getNeighborId(Edge edge, String currentNodeId, boolean isGraphDirected) {
        if (edge == null || currentNodeId == null) {
            return null;
        }
        if (currentNodeId.equals(edge.getSource())) {
            return edge.getTarget();
        }
        if (currentNodeId.equals(edge.getTarget()) && (edge.isBidirectional() || !isGraphDirected)) {
            return edge.getSource();
        }
        return null;
    }

    /**
     * Checks if a node exists and satisfies the wheelchair accessibility constraint.
     */
    public static boolean isNodeAllowed(Node node, boolean wheelchairAccessibleOnly) {
        if (node == null) {
            return false;
        }
        return !wheelchairAccessibleOnly || node.isAccessible();
    }

    /**
     * Checks if an edge is not in the blocked set.
     */
    public static boolean isEdgeAllowed(Edge edge, Set<String> blockedEdgeIds) {
        if (edge == null) {
            return false;
        }
        return blockedEdgeIds == null || !blockedEdgeIds.contains(edge.getId());
    }

    /**
     * Reconstructs the forward path of Nodes and Edges from startNodeId to endNodeId
     * using the parentNodeMap and parentEdgeMap recorded during BFS/DFS/Dijkstra/A*.
     */
    public static void populatePath(
            AlgorithmResult result,
            String startNodeId,
            String endNodeId,
            Map<String, Node> nodeMap,
            Map<String, String> parentNodeMap,
            Map<String, Edge> parentEdgeMap) {

        List<Node> pathNodes = new ArrayList<>();
        List<Edge> pathEdges = new ArrayList<>();
        double totalWeight = 0.0;

        String curr = endNodeId;
        while (curr != null) {
            Node node = nodeMap.get(curr);
            if (node != null) {
                pathNodes.add(node);
            }
            if (curr.equals(startNodeId)) {
                break;
            }
            Edge edge = parentEdgeMap.get(curr);
            if (edge != null) {
                pathEdges.add(edge);
                totalWeight += edge.getWeight();
            }
            curr = parentNodeMap.get(curr);
        }

        // Reverse because we backtracked from endNodeId -> startNodeId
        Collections.reverse(pathNodes);
        Collections.reverse(pathEdges);

        result.setPathNodes(pathNodes);
        result.setPathEdges(pathEdges);
        result.setHopCount(pathEdges.size());
        result.setTotalWeight(totalWeight);
    }
}