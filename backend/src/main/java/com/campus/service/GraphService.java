package com.campus.service;

import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import com.campus.repository.JsonDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service Layer: Provides graph queries and operations.
 * Designed to cleanly host DAA graph algorithms (BFS, DFS, Dijkstra, Prim's, Kruskal's) in subsequent phases.
 */
@Service
public class GraphService {

    private final JsonDataRepository repository;

    public GraphService(JsonDataRepository repository) {
        this.repository = repository;
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
}
