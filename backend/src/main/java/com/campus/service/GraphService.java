package com.campus.service;

import com.campus.algorithm.shortestpath.DijkstraPathfinder;
import com.campus.algorithm.mst.KruskalsMst;
import com.campus.algorithm.mst.PrimsMst;
import com.campus.algorithm.traversal.BfsTraversal;
import com.campus.algorithm.traversal.DfsTraversal;
import com.campus.dto.AlgorithmResult;
import com.campus.dto.GraphRequest;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import com.campus.repository.JsonDataRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service Layer: Provides graph queries and operations.
 * Hosts DAA graph algorithms (BFS, DFS, Dijkstra, Prim's, Kruskal's).
 */
@Service
public class GraphService {

    private final JsonDataRepository repository;
    private final BfsTraversal bfsTraversal;
    private final DfsTraversal dfsTraversal;
    private final DijkstraPathfinder dijkstraPathfinder;
    private final PrimsMst primsMst;
    private final KruskalsMst kruskalsMst;

    public GraphService(
            JsonDataRepository repository,
            BfsTraversal bfsTraversal,
            DfsTraversal dfsTraversal,
            DijkstraPathfinder dijkstraPathfinder,
            PrimsMst primsMst,
            KruskalsMst kruskalsMst) {
        this.repository = repository;
        this.bfsTraversal = bfsTraversal;
        this.dfsTraversal = dfsTraversal;
        this.dijkstraPathfinder = dijkstraPathfinder;
        this.primsMst = primsMst;
        this.kruskalsMst = kruskalsMst;
    }

    public Graph getGraph() {
        return repository.getGraph();
    }

    public List<Node> getAllNodes() {
        return repository.findAllNodes();
    }

    public Optional<Node> getNodeById(String id) {
        return repository.findNodeById(id);
    }

    public List<Edge> getAllEdges() {
        return repository.findAllEdges();
    }

    /**
     * Runs Breadth-First Search (BFS) for reachability and minimum-hop pathfinding.
     */
    public AlgorithmResult runBfs(
            String startNodeId,
            String endNodeId,
            Set<String> blockedEdges,
            boolean wheelchairAccessibleOnly) {
        return bfsTraversal.executeBfs(
                repository.getGraph(),
                startNodeId,
                endNodeId,
                blockedEdges != null ? blockedEdges : Collections.emptySet(),
                wheelchairAccessibleOnly);
    }

    /**
     * Runs Depth-First Search (DFS) for traversal, connectivity, connected components, and cycle detection.
     */
    public AlgorithmResult runDfs(
            String startNodeId,
            String endNodeId,
            Set<String> blockedEdges,
            boolean wheelchairAccessibleOnly,
            boolean iterative) {
        return dfsTraversal.executeDfs(
                repository.getGraph(),
                startNodeId,
                endNodeId,
                blockedEdges != null ? blockedEdges : Collections.emptySet(),
                wheelchairAccessibleOnly,
                iterative);
    }

    /**
     * Runs Dijkstra's Shortest Path Algorithm for minimum walking distance.
     */
    public AlgorithmResult runDijkstra(
            String startNodeId,
            String endNodeId,
            Set<String> blockedEdges,
            boolean wheelchairAccessibleOnly) {
        return dijkstraPathfinder.findShortestPath(
                repository.getGraph(),
                startNodeId,
                endNodeId,
                blockedEdges != null ? blockedEdges : Collections.emptySet(),
                wheelchairAccessibleOnly);
    }

    /**
     * Runs Prim's Minimum Spanning Tree algorithm.
     */
    public AlgorithmResult runPrimsMst(
            String startNodeId,
            Set<String> blockedEdges,
            boolean wheelchairAccessibleOnly) {
        return primsMst.computeMst(
                repository.getGraph(),
                startNodeId,
                blockedEdges != null ? blockedEdges : Collections.emptySet(),
                wheelchairAccessibleOnly);
    }

    /**
     * Runs Kruskal's Minimum Spanning Tree algorithm.
     */
    public AlgorithmResult runKruskalsMst(
            Set<String> blockedEdges,
            boolean wheelchairAccessibleOnly) {
        return kruskalsMst.computeMst(
                repository.getGraph(),
                blockedEdges != null ? blockedEdges : Collections.emptySet(),
                wheelchairAccessibleOnly);
    }

    /**
     * Unified dispatcher for GraphRequest payloads.
     */
    public AlgorithmResult executeTraversalRequest(GraphRequest request) {
        Set<String> blocked = request.getBlockedEdgeIds() != null
                ? new HashSet<>(request.getBlockedEdgeIds())
                : Collections.emptySet();
        String algo = request.getAlgorithm() != null ? request.getAlgorithm().trim().toUpperCase() : "BFS";

        return switch (algo) {
            case "BFS" -> runBfs(
                    request.getStartNodeId(),
                    request.getEndNodeId(),
                    blocked,
                    request.isWheelchairAccessibleOnly());
            case "DFS" -> runDfs(
                    request.getStartNodeId(),
                    request.getEndNodeId(),
                    blocked,
                    request.isWheelchairAccessibleOnly(),
                    false);
            case "DFS_ITERATIVE" -> runDfs(
                    request.getStartNodeId(),
                    request.getEndNodeId(),
                    blocked,
                    request.isWheelchairAccessibleOnly(),
                    true);
            case "DIJKSTRA" -> runDijkstra(
                    request.getStartNodeId(),
                    request.getEndNodeId(),
                    blocked,
                    request.isWheelchairAccessibleOnly());
            case "PRIMS", "PRIMS_MST" -> runPrimsMst(
                    request.getStartNodeId(),
                    blocked,
                    request.isWheelchairAccessibleOnly());
            case "KRUSKALS", "KRUSKALS_MST" -> runKruskalsMst(
                    blocked,
                    request.isWheelchairAccessibleOnly());
            default -> throw new IllegalArgumentException("Unsupported algorithm: " + algo);
        };
    }
}