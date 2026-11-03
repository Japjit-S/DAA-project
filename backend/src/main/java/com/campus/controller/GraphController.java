package com.campus.controller;

import com.campus.dto.AlgorithmResult;
import com.campus.dto.ApiResponse;
import com.campus.dto.GraphRequest;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import com.campus.service.GraphService;
import com.campus.validation.ValidationReport;
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
     * Retrieves a single edge by edge ID.
     */
    @GetMapping("/edges/{id}")
    public ResponseEntity<?> getEdgeById(@PathVariable String id) {
        return graphService.getEdgeById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Edge not found with id: " + id)));
    }

    /**
     * Retrieves the campus graph data validation report.
     */
    @GetMapping("/validation")
    public ResponseEntity<ApiResponse<ValidationReport>> getValidationReport() {
        return ResponseEntity.ok(ApiResponse.success(
                graphService.getValidationReport(),
                "Graph validation report"));
    }

    /**
     * Runs Breadth-First Search (BFS) between startNodeId and optional endNodeId.
     */
    @GetMapping("/bfs")
    public ResponseEntity<ApiResponse<AlgorithmResult>> runBfs(
            @RequestParam String startNodeId,
            @RequestParam(required = false) String endNodeId,
            @RequestParam(defaultValue = "false") boolean accessibleOnly,
            @RequestParam(required = false) List<String> blockedEdges) {

        if (startNodeId == null || startNodeId.isBlank() || graphService.getNodeById(startNodeId).isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Start node not found with id: " + startNodeId));
        }
        if (endNodeId != null && !endNodeId.isBlank() && graphService.getNodeById(endNodeId).isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("End node not found with id: " + endNodeId));
        }

        Set<String> blocked = blockedEdges != null ? new HashSet<>(blockedEdges) : Collections.emptySet();
        AlgorithmResult result = graphService.runBfs(startNodeId, endNodeId, blocked, accessibleOnly);
        return ResponseEntity.ok(ApiResponse.success(result, "BFS executed successfully"));
    }

    /**
     * Runs Depth-First Search (DFS) for traversal, connectivity, connected components, and cycle detection.
     */
    @GetMapping("/dfs")
    public ResponseEntity<ApiResponse<AlgorithmResult>> runDfs(
            @RequestParam String startNodeId,
            @RequestParam(required = false) String endNodeId,
            @RequestParam(defaultValue = "false") boolean accessibleOnly,
            @RequestParam(defaultValue = "false") boolean iterative,
            @RequestParam(required = false) List<String> blockedEdges) {

        if (startNodeId == null || startNodeId.isBlank() || graphService.getNodeById(startNodeId).isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Start node not found with id: " + startNodeId));
        }
        if (endNodeId != null && !endNodeId.isBlank() && graphService.getNodeById(endNodeId).isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("End node not found with id: " + endNodeId));
        }

        Set<String> blocked = blockedEdges != null ? new HashSet<>(blockedEdges) : Collections.emptySet();
        AlgorithmResult result = graphService.runDfs(startNodeId, endNodeId, blocked, accessibleOnly, iterative);
        return ResponseEntity.ok(ApiResponse.success(result, "DFS executed successfully"));
    }

    /**
     * Runs Dijkstra's Shortest Path Algorithm between startNodeId and endNodeId.
     * Example: GET /api/graph/shortest-path?startNodeId=N001&endNodeId=N004&accessibleOnly=false
     */
    @GetMapping("/shortest-path")
    public ResponseEntity<ApiResponse<AlgorithmResult>> getShortestPath(
            @RequestParam String startNodeId,
            @RequestParam String endNodeId,
            @RequestParam(defaultValue = "false") boolean accessibleOnly,
            @RequestParam(required = false) List<String> blockedEdges) {

        if (startNodeId == null || startNodeId.isBlank() || graphService.getNodeById(startNodeId).isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Start node not found with id: " + startNodeId));
        }
        if (endNodeId == null || endNodeId.isBlank() || graphService.getNodeById(endNodeId).isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("End node not found with id: " + endNodeId));
        }

        Set<String> blocked = blockedEdges != null ? new HashSet<>(blockedEdges) : Collections.emptySet();
        AlgorithmResult result = graphService.runDijkstra(startNodeId, endNodeId, blocked, accessibleOnly);
        return ResponseEntity.ok(ApiResponse.success(result, "Shortest path calculated successfully"));
    }

    /**
     * Runs Prim's Minimum Spanning Tree (MST) algorithm.
     * Example: GET /api/graph/mst/prim?startNodeId=N001&accessibleOnly=false
     */
    @GetMapping("/mst/prim")
    public ResponseEntity<ApiResponse<AlgorithmResult>> getPrimsMst(
            @RequestParam(required = false) String startNodeId,
            @RequestParam(defaultValue = "false") boolean accessibleOnly,
            @RequestParam(required = false) List<String> blockedEdges) {

        if (startNodeId != null && !startNodeId.isBlank() && graphService.getNodeById(startNodeId).isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Start node not found with id: " + startNodeId));
        }

        Set<String> blocked = blockedEdges != null ? new HashSet<>(blockedEdges) : Collections.emptySet();
        AlgorithmResult result = graphService.runPrimsMst(startNodeId, blocked, accessibleOnly);
        return ResponseEntity.ok(ApiResponse.success(result, "Prim's MST calculated successfully"));
    }

    /**
     * Runs Kruskal's Minimum Spanning Tree (MST) algorithm.
     * Example: GET /api/graph/mst/kruskal?accessibleOnly=false
     */
    @GetMapping("/mst/kruskal")
    public ResponseEntity<ApiResponse<AlgorithmResult>> getKruskalsMst(
            @RequestParam(defaultValue = "false") boolean accessibleOnly,
            @RequestParam(required = false) List<String> blockedEdges) {

        Set<String> blocked = blockedEdges != null ? new HashSet<>(blockedEdges) : Collections.emptySet();
        AlgorithmResult result = graphService.runKruskalsMst(blocked, accessibleOnly);
        return ResponseEntity.ok(ApiResponse.success(result, "Kruskal's MST calculated successfully"));
    }

    /**
     * POST endpoint accepting a GraphRequest body for ANY algorithm (BFS, DFS, DIJKSTRA, PRIMS, KRUSKALS).
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<AlgorithmResult>> executeAlgorithm(@RequestBody(required = false) GraphRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Graph request body cannot be null"));
        }
        try {
            AlgorithmResult result = graphService.executeTraversalRequest(request);
            return ResponseEntity.ok(ApiResponse.success(result, result.getAlgorithm() + " executed successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }
}