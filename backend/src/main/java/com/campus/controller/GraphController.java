package com.campus.controller;

import com.campus.dto.ApiResponse;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import com.campus.service.GraphService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing graph topology (vertices and edges)
 * and future hooks for DAA pathfinding and optimization algorithms.
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
}
