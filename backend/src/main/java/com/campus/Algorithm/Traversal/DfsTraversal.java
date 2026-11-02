package com.campus.algorithm.traversal;

import com.campus.algorithm.GraphFilter;
import com.campus.dto.AlgorithmResult;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Depth-First Search (DFS) implementation for the Smart Campus graph.
 * - Supports both Recursive and Iterative DFS traversal.
 * - Checks connectivity and reconstructs a path between two locations.
 * - Identifies all connected components across the campus (including disconnected vertices).
 * - Detects cycles in the undirected (or directed) campus graph.
 */
@Component
public class DfsTraversal {

    /**
     * Executes Recursive DFS (default) and computes connected components + cycle detection.
     */
    public AlgorithmResult executeDfs(
            Graph graph,
            String startNodeId,
            String endNodeId,
            Set<String> blockedEdgeIds,
            boolean wheelchairAccessibleOnly) {
        return executeDfs(graph, startNodeId, endNodeId, blockedEdgeIds, wheelchairAccessibleOnly, false);
    }

    /**
     * Executes DFS with choice of recursive or iterative stack-based exploration.
     */
    public AlgorithmResult executeDfs(
            Graph graph,
            String startNodeId,
            String endNodeId,
            Set<String> blockedEdgeIds,
            boolean wheelchairAccessibleOnly,
            boolean useIterative) {

        long startTime = System.nanoTime();
        AlgorithmResult result = new AlgorithmResult();
        result.setAlgorithm(useIterative ? "DFS_ITERATIVE" : "DFS");
        result.setStartNodeId(startNodeId);
        result.setEndNodeId(endNodeId);

        if (graph == null || startNodeId == null || startNodeId.isBlank()) {
            result.setReachable(false);
            result.setExecutionTimeMicros((System.nanoTime() - startTime) / 1_000);
            return result;
        }

        Map<String, Node> nodeMap = GraphFilter.buildNodeMap(graph);
        Map<String, List<Edge>> adj = graph.getAdjacencyList();

        // Always compute campus-wide connected components and cycle status
        ComponentAndCycleAnalysis analysis = analyzeComponentsAndCycles(
                graph, nodeMap, adj, blockedEdgeIds, wheelchairAccessibleOnly);
        result.setConnectedComponents(analysis.components);
        result.setConnectedComponentsCount(analysis.components.size());
        result.setCycleDetected(analysis.hasCycle);

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

        Set<String> visited = new LinkedHashSet<>();
        Map<String, String> parentNodeMap = new HashMap<>();
        Map<String, Edge> parentEdgeMap = new HashMap<>();

        if (useIterative) {
            dfsIterative(
                    startNodeId, endNodeId, hasTarget, graph.isDirected(),
                    nodeMap, adj, blockedEdgeIds, wheelchairAccessibleOnly,
                    visited, parentNodeMap, parentEdgeMap);
        } else {
            dfsRecursive(
                    startNodeId, endNodeId, hasTarget, graph.isDirected(),
                    nodeMap, adj, blockedEdgeIds, wheelchairAccessibleOnly,
                    visited, parentNodeMap, parentEdgeMap);
        }

        result.setVisitedOrder(new ArrayList<>(visited));

        if (hasTarget) {
            boolean targetFound = visited.contains(endNodeId);
            result.setReachable(targetFound);
            if (targetFound) {
                GraphFilter.populatePath(result, startNodeId, endNodeId, nodeMap, parentNodeMap, parentEdgeMap);
            }
        } else {
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

    /**
     * Recursive DFS helper. Returns true immediately if the target node is reached.
     */
    private boolean dfsRecursive(
            String currentId,
            String endNodeId,
            boolean hasTarget,
            boolean isDirected,
            Map<String, Node> nodeMap,
            Map<String, List<Edge>> adj,
            Set<String> blockedEdgeIds,
            boolean wheelchairAccessibleOnly,
            Set<String> visited,
            Map<String, String> parentNodeMap,
            Map<String, Edge> parentEdgeMap) {

        visited.add(currentId);
        if (hasTarget && currentId.equals(endNodeId)) {
            return true;
        }

        List<Edge> incidentEdges = adj.getOrDefault(currentId, Collections.emptyList());
        for (Edge edge : incidentEdges) {
            if (!GraphFilter.isEdgeAllowed(edge, blockedEdgeIds)) {
                continue;
            }

            String neighborId = GraphFilter.getNeighborId(edge, currentId, isDirected);
            if (neighborId == null || visited.contains(neighborId)) {
                continue;
            }

            Node neighborNode = nodeMap.get(neighborId);
            if (!GraphFilter.isNodeAllowed(neighborNode, wheelchairAccessibleOnly)) {
                continue;
            }

            parentNodeMap.put(neighborId, currentId);
            parentEdgeMap.put(neighborId, edge);

            if (dfsRecursive(
                    neighborId, endNodeId, hasTarget, isDirected,
                    nodeMap, adj, blockedEdgeIds, wheelchairAccessibleOnly,
                    visited, parentNodeMap, parentEdgeMap)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Iterative DFS using an explicit LIFO stack (ArrayDeque).
     */
    private void dfsIterative(
            String startNodeId,
            String endNodeId,
            boolean hasTarget,
            boolean isDirected,
            Map<String, Node> nodeMap,
            Map<String, List<Edge>> adj,
            Set<String> blockedEdgeIds,
            boolean wheelchairAccessibleOnly,
            Set<String> visited,
            Map<String, String> parentNodeMap,
            Map<String, Edge> parentEdgeMap) {

        Deque<String> stack = new ArrayDeque<>();
        stack.push(startNodeId);

        while (!stack.isEmpty()) {
            String currentId = stack.pop();
            if (visited.contains(currentId)) {
                continue;
            }

            visited.add(currentId);
            if (hasTarget && currentId.equals(endNodeId)) {
                return;
            }

            List<Edge> incidentEdges = adj.getOrDefault(currentId, Collections.emptyList());
            // Iterate in reverse so the first neighbor in adjacency list is popped first from the stack
            for (int i = incidentEdges.size() - 1; i >= 0; i--) {
                Edge edge = incidentEdges.get(i);
                if (!GraphFilter.isEdgeAllowed(edge, blockedEdgeIds)) {
                    continue;
                }

                String neighborId = GraphFilter.getNeighborId(edge, currentId, isDirected);
                if (neighborId == null || visited.contains(neighborId)) {
                    continue;
                }

                Node neighborNode = nodeMap.get(neighborId);
                if (!GraphFilter.isNodeAllowed(neighborNode, wheelchairAccessibleOnly)) {
                    continue;
                }

                // Update parent so path reconstruction matches the deepest branch taken
                parentNodeMap.put(neighborId, currentId);
                parentEdgeMap.put(neighborId, edge);
                stack.push(neighborId);
            }
        }
    }

    /**
     * Finds all connected components in the graph (handling disconnected vertices)
     * and detects whether any cycle exists.
     */
    private ComponentAndCycleAnalysis analyzeComponentsAndCycles(
            Graph graph,
            Map<String, Node> nodeMap,
            Map<String, List<Edge>> adj,
            Set<String> blockedEdgeIds,
            boolean wheelchairAccessibleOnly) {

        List<List<String>> components = new ArrayList<>();
        Set<String> globalVisited = new HashSet<>();
        boolean[] cycleFound = {false};

        for (Node node : graph.getNodes()) {
            if (!GraphFilter.isNodeAllowed(node, wheelchairAccessibleOnly)) {
                continue;
            }
            String nodeId = node.getId();
            if (!globalVisited.contains(nodeId)) {
                List<String> currentComponent = new ArrayList<>();
                exploreComponentAndDetectCycle(
                        nodeId, null, null, graph.isDirected(),
                        nodeMap, adj, blockedEdgeIds, wheelchairAccessibleOnly,
                        globalVisited, currentComponent, cycleFound);
                components.add(currentComponent);
            }
        }

        return new ComponentAndCycleAnalysis(components, cycleFound[0]);
    }

    /**
     * Recursive helper for connected component grouping and undirected cycle detection.
     * In an undirected graph, a cycle exists if we encounter an already-visited neighbor
     * via an edge that is NOT the exact parent edge we just arrived on.
     */
    private void exploreComponentAndDetectCycle(
            String currentId,
            String parentNodeId,
            String parentEdgeId,
            boolean isDirected,
            Map<String, Node> nodeMap,
            Map<String, List<Edge>> adj,
            Set<String> blockedEdgeIds,
            boolean wheelchairAccessibleOnly,
            Set<String> globalVisited,
            List<String> currentComponent,
            boolean[] cycleFound) {

        globalVisited.add(currentId);
        currentComponent.add(currentId);

        List<Edge> incidentEdges = adj.getOrDefault(currentId, Collections.emptyList());
        for (Edge edge : incidentEdges) {
            if (!GraphFilter.isEdgeAllowed(edge, blockedEdgeIds)) {
                continue;
            }

            String neighborId = GraphFilter.getNeighborId(edge, currentId, isDirected);
            if (neighborId == null) {
                continue;
            }

            Node neighborNode = nodeMap.get(neighborId);
            if (!GraphFilter.isNodeAllowed(neighborNode, wheelchairAccessibleOnly)) {
                continue;
            }

            if (!globalVisited.contains(neighborId)) {
                exploreComponentAndDetectCycle(
                        neighborId, currentId, edge.getId(), isDirected,
                        nodeMap, adj, blockedEdgeIds, wheelchairAccessibleOnly,
                        globalVisited, currentComponent, cycleFound);
            } else if (!neighborId.equals(parentNodeId) || !Objects.equals(edge.getId(), parentEdgeId)) {
                // Visited neighbor that is not our immediate parent -> back-edge (cycle) detected!
                cycleFound[0] = true;
            }
        }
    }

    private static class ComponentAndCycleAnalysis {
        final List<List<String>> components;
        final boolean hasCycle;

        ComponentAndCycleAnalysis(List<List<String>> components, boolean hasCycle) {
            this.components = components;
            this.hasCycle = hasCycle;
        }
    }
}