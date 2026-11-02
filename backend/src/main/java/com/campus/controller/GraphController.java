package com.campus.controller;

import com.campus.dto.AlgorithmResult;
import com.campus.dto.ApiResponse;
import com.campus.dto.GraphRequest;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import com.campus.service.GraphService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * REST Controller exposing graph topology (vertices and edges)
 * and DAA pathfinding and optimization algorithms.
 */
@RestController
@RequestMapping("/api/graph")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    /**
     * Retrieves the entire campus graph representation.
     */
    @GetMapping
    public ResponseEntity<Graph> getGraph() {
        return ResponseEntity.ok(graphService.getGraph());
    }

    /**
     * Retrieves all vertices (nodes) in the graph.
     */
    @GetMapping("/nodes")
    public ResponseEntity<List<Node>> getAllNodes() {
        return ResponseEntity.ok(graphService.getAllNodes());
    }

    /**
     * Retrieves a single vertex by node ID.
     */
    @GetMapping("/nodes/{id}")
    public ResponseEntity<?> getNodeById(@PathVariable String id) {
        return graphService.getNodeById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Node not found with id: " + id)));
    }

    /**
     * Retrieves all weighted edges (pathways) in the graph.
     */
    @GetMapping("/edges")
    public ResponseEntity<List<Edge>> getAllEdges() {
        return ResponseEntity.ok(graphService.getAllEdges());
    }

    /**
     * Runs Breadth-First Search (BFS) between startNodeId and optional endNodeId.
     * Example: GET /api/graph/bfs?startNodeId=N001&endNodeId=N004&accessibleOnly=false
     */
    @GetMapping("/bfs")
    public ResponseEntity<ApiResponse<AlgorithmResult>> runBfs(
            @RequestParam String startNodeId,
            @RequestParam(required = false) String endNodeId,
            @RequestParam(defaultValue = "false") boolean accessibleOnly,
            @RequestParam(required = false) List<String> blockedEdges) {

        Set<String> blocked = blockedEdges != null ? new HashSet<>(blockedEdges) : Collections.emptySet();
        AlgorithmResult result = graphService.runBfs(startNodeId, endNodeId, blocked, accessibleOnly);
        return ResponseEntity.ok(ApiResponse.success(result, "BFS executed successfully"));
    }

    /**
     * Runs Depth-First Search (DFS) for traversal, connectivity, connected components, and cycle detection.
     * Example: GET /api/graph/dfs?startNodeId=N001&endNodeId=N009&iterative=false
     */
    @GetMapping("/dfs")
    public ResponseEntity<ApiResponse<AlgorithmResult>> runDfs(
            @RequestParam String startNodeId,
            @RequestParam(required = false) String endNodeId,
            @RequestParam(defaultValue = "false") boolean accessibleOnly,
            @RequestParam(defaultValue = "false") boolean iterative,
            @RequestParam(required = false) List<String> blockedEdges) {

        Set<String> blocked = blockedEdges != null ? new HashSet<>(blockedEdges) : Collections.emptySet();
        AlgorithmResult result = graphService.runDfs(startNodeId, endNodeId, blocked, accessibleOnly, iterative);
        return ResponseEntity.ok(ApiResponse.success(result, "DFS executed successfully"));
    }

    /**
     * POST endpoint accepting a GraphRequest body for BFS / DFS / DFS_ITERATIVE.
     */
    @PostMapping("/traverse")
    public ResponseEntity<ApiResponse<AlgorithmResult>> traverseGraph(@RequestBody GraphRequest request) {
        try {
            AlgorithmResult result = graphService.executeTraversalRequest(request);
            return ResponseEntity.ok(ApiResponse.success(result, result.getAlgorithm() + " executed successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }
}