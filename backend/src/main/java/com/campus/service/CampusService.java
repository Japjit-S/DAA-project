package com.campus.service;

import com.campus.algorithm.traversal.BfsTraversal;
import com.campus.algorithm.traversal.DfsTraversal;
import com.campus.dto.AlgorithmResult;
import com.campus.dto.GraphRequest;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import com.campus.repository.JsonDataRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service Layer: Provides graph queries and operations.
 * Hosts DAA graph algorithms (BFS, DFS, Dijkstra, Prim's, Kruskal's).
 */
@Service
public class GraphService {

    private final JsonDataRepository repository;
    private final BfsTraversal bfsTraversal;
    private final DfsTraversal dfsTraversal;

    public GraphService(
            JsonDataRepository repository,
            BfsTraversal bfsTraversal,
            DfsTraversal dfsTraversal) {
        this.repository = repository;
        this.bfsTraversal = bfsTraversal;
        this.dfsTraversal = dfsTraversal;
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
            default -> throw new IllegalArgumentException("Unsupported traversal algorithm: " + algo);
        };
    }
}