package com.campus.repository;

import com.campus.model.Building;
import com.campus.model.Edge;
import com.campus.model.Graph;
import com.campus.model.Node;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Storage Layer: Manages reading persistent JSON data files for buildings,
 * nodes, edges, and graphs. Exposes query methods for higher layers.
 */
@Repository
public class JsonDataRepository {

    private static final Logger logger = LoggerFactory.getLogger(JsonDataRepository.class);

    private final ObjectMapper objectMapper;

    @Value("${campus.data.dir:../data}")
    private String configuredDataDir;

    private final List<Building> buildings = new CopyOnWriteArrayList<>();
    private final List<Node> nodes = new CopyOnWriteArrayList<>();
    private final List<Edge> edges = new CopyOnWriteArrayList<>();
    private final Map<String, Building> buildingMap = new ConcurrentHashMap<>();
    private final Map<String, Node> nodeMap = new ConcurrentHashMap<>();
    private volatile Graph graph = new Graph();

    public JsonDataRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public synchronized void init() {
        loadData();
    }

    /**
     * Resolves the actual directory containing data files.
     */
    public Path resolveDataDirectory() {
        List<Path> candidates = List.of(
                Paths.get(configuredDataDir),
                Paths.get("../data"),
                Paths.get("data"),
                Paths.get("../../data"),
                Paths.get(System.getProperty("user.dir"), "data"),
                Paths.get(System.getProperty("user.dir"), "../data")
        );

        for (Path candidate : candidates) {
            if (Files.exists(candidate) && Files.isDirectory(candidate)) {
                Path sample = candidate.resolve("buildings.json");
                if (Files.exists(sample)) {
                    logger.info("Resolved data directory at: {}", candidate.toAbsolutePath().normalize());
                    return candidate.toAbsolutePath().normalize();
                }
            }
        }

        logger.warn("Could not locate data directory in candidates. Using fallback: {}", configuredDataDir);
        return Paths.get(configuredDataDir).toAbsolutePath().normalize();
    }

    /**
     * Loads JSON data into memory caches.
     */
    public synchronized void loadData() {
        Path dataDir = resolveDataDirectory();
        logger.info("Loading campus datasets from: {}", dataDir);

        try {
            // 1. Load Buildings
            File buildingsFile = dataDir.resolve("buildings.json").toFile();
            if (buildingsFile.exists()) {
                List<Building> loadedBuildings = objectMapper.readValue(
                        buildingsFile, new TypeReference<List<Building>>() {}
                );
                buildings.clear();
                buildingMap.clear();
                buildings.addAll(loadedBuildings);
                loadedBuildings.forEach(b -> buildingMap.put(b.getId(), b));
                logger.info("Loaded {} buildings", buildings.size());
            } else {
                logger.warn("File not found: {}", buildingsFile.getAbsolutePath());
            }

            // 2. Load Nodes
            File nodesFile = dataDir.resolve("nodes.json").toFile();
            if (nodesFile.exists()) {
                List<Node> loadedNodes = objectMapper.readValue(
                        nodesFile, new TypeReference<List<Node>>() {}
                );
                nodes.clear();
                nodeMap.clear();
                nodes.addAll(loadedNodes);
                loadedNodes.forEach(n -> nodeMap.put(n.getId(), n));
                logger.info("Loaded {} nodes", nodes.size());
            }

            // 3. Load Edges
            File edgesFile = dataDir.resolve("edges.json").toFile();
            if (edgesFile.exists()) {
                List<Edge> loadedEdges = objectMapper.readValue(
                        edgesFile, new TypeReference<List<Edge>>() {}
                );
                edges.clear();
                edges.addAll(loadedEdges);
                logger.info("Loaded {} edges", edges.size());
            }

            // 4. Load or synthesize Graph
            File graphFile = dataDir.resolve("graph.json").toFile();
            if (graphFile.exists()) {
                graph = objectMapper.readValue(graphFile, Graph.class);
                logger.info("Loaded graph '{}' with {} nodes and {} edges",
                        graph.getGraphId(), graph.getNodeCount(), graph.getEdgeCount());
            } else {
                graph = new Graph("CAMPUS_MAIN_GRAPH", false, new ArrayList<>(nodes), new ArrayList<>(edges));
                logger.info("Synthesized graph with {} nodes and {} edges", graph.getNodeCount(), graph.getEdgeCount());
            }

        } catch (IOException e) {
            logger.error("Error reading JSON data files: {}", e.getMessage(), e);
        }
    }

    public List<Building> findAllBuildings() {
        return Collections.unmodifiableList(buildings);
    }

    public Optional<Building> findBuildingById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(buildingMap.get(id));
    }

    public List<Node> findAllNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public Optional<Node> findNodeById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(nodeMap.get(id));
    }

    public List<Edge> findAllEdges() {
        return Collections.unmodifiableList(edges);
    }

    public Graph getGraph() {
        return graph;
    }
}
