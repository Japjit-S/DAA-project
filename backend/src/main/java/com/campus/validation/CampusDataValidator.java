package com.campus.validation;

import com.campus.model.Building;
import com.campus.model.Edge;
import com.campus.model.GeoPoint;
import com.campus.model.Node;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Service providing strict data validation for campus nodes, edges, buildings,
 * geometry alignments, and graph connectivity.
 */
@Component
public class CampusDataValidator {

    public static final double ENDPOINT_TOLERANCE_METERS = 2.0;
    public static final double WEIGHT_TOLERANCE_METERS = 0.5;

    public static final double MIN_LAT = 28.50;
    public static final double MAX_LAT = 28.70;
    public static final double MIN_LNG = 77.00;
    public static final double MAX_LNG = 77.10;

    private static final Set<String> ALLOWED_NODE_TYPES = Set.of(
            "GATE", "ACADEMIC", "LIBRARY", "AMENITY", "FACILITY",
            "HOSTEL", "JUNCTION", "ADMIN", "SPORTS", "PARKING"
    );

    private static final Set<String> ALLOWED_EDGE_TYPES = Set.of(
            "MAIN_ROAD", "INTERNAL_ROAD", "WALKWAY", "ACCESS_PATH"
    );

    private static final Set<String> ALLOWED_BUILDING_CATEGORIES = Set.of(
            "ACADEMIC", "ADMINISTRATIVE", "LIBRARY", "HOSTEL", "SPORTS", "AMENITY", "FACILITY"
    );

    /**
     * Executes comprehensive validation on the supplied dataset.
     */
    public ValidationReport validate(List<Node> nodes, List<Edge> edges, List<Building> buildings) {
        ValidationReport report = new ValidationReport();

        List<Node> safeNodes = nodes != null ? nodes : Collections.emptyList();
        List<Edge> safeEdges = edges != null ? edges : Collections.emptyList();
        List<Building> safeBuildings = buildings != null ? buildings : Collections.emptyList();

        report.setNodeCount((int) safeNodes.stream().filter(Objects::nonNull).filter(Node::isRoutable).count());
        report.setEdgeCount(safeEdges.size());
        report.setBuildingCount(safeBuildings.size());

        // 1. Validate Nodes
        Map<String, Node> nodeMap = new HashMap<>();
        Set<String> nodeIds = new HashSet<>();

        for (Node node : safeNodes) {
            validateNode(node, nodeIds, nodeMap, report);
        }

        // 2. Validate Edges
        Set<String> edgeIds = new HashSet<>();
        Set<String> undirectedPairs = new HashSet<>();
        double maxEndpointDeviation = 0.0;
        double maxWeightDeviation = 0.0;
        int lowConfidenceCount = 0;

        for (Edge edge : safeEdges) {
            double[] deviations = validateEdge(edge, edgeIds, undirectedPairs, nodeMap, report);
            maxEndpointDeviation = Math.max(maxEndpointDeviation, deviations[0]);
            maxWeightDeviation = Math.max(maxWeightDeviation, deviations[1]);
            if (deviations[2] > 0.0) {
                lowConfidenceCount++;
            }
        }

        report.setMaxEndpointDeviationMeters(Math.round(maxEndpointDeviation * 10000.0) / 10000.0);
        report.setMaxWeightDeviationMeters(Math.round(maxWeightDeviation * 10000.0) / 10000.0);
        report.setLowConfidenceEdgeCount(lowConfidenceCount);

        // 3. Validate Buildings
        Set<String> buildingIds = new HashSet<>();
        for (Building building : safeBuildings) {
            validateBuilding(building, buildingIds, nodeMap, report);
        }

        // 4. Validate Graph Connectivity and Connected Components
        validateConnectivity(nodeMap, safeEdges, report);

        return report;
    }

    private void validateNode(
            Node node,
            Set<String> nodeIds,
            Map<String, Node> nodeMap,
            ValidationReport report) {

        if (node == null) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.NODE, "UNKNOWN", "node", "Null node record in dataset", null));
            return;
        }

        String id = node.getId();
        if (id == null || id.isBlank()) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.NODE, "BLANK", "id", "Node ID cannot be null or blank", id));
            return;
        }

        if (nodeIds.contains(id)) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.NODE, id, "id", "Duplicate node ID found", id));
        } else {
            nodeIds.add(id);
            nodeMap.put(id, node);
        }

        if (node.getName() == null || node.getName().isBlank()) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.NODE, id, "name", "Node name cannot be null or blank", node.getName()));
        }

        if (node.getType() == null || !ALLOWED_NODE_TYPES.contains(node.getType().trim().toUpperCase())) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.NODE, id, "type",
                    "Invalid node type. Allowed: " + ALLOWED_NODE_TYPES, node.getType()));
        }

        if (!Double.isFinite(node.getX()) || !Double.isFinite(node.getY())) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.NODE, id, "coordinates",
                    "Node canvas coordinates (x, y) must be finite", "(" + node.getX() + ", " + node.getY() + ")"));
        }

        Double lat = node.getLat();
        Double lng = node.getLng();
        if (lat == null || lng == null) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.NODE, id, "geographicCoordinates",
                    "Geographic latitude and longitude are required", null));
        } else if (!Double.isFinite(lat) || !Double.isFinite(lng)) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.NODE, id, "geographicCoordinates",
                    "Geographic coordinates must be finite numbers", "(" + lat + ", " + lng + ")"));
        } else if (lat < MIN_LAT || lat > MAX_LAT || lng < MIN_LNG || lng > MAX_LNG) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.NODE, id, "geographicCoordinates",
                    String.format("Coordinates outside valid campus range [%.2f..%.2f, %.2f..%.2f]",
                            MIN_LAT, MAX_LAT, MIN_LNG, MAX_LNG),
                    "(" + lat + ", " + lng + ")"));
        }
    }

    /**
     * Validates an edge and returns [endpointDeviation, weightDeviation, isLowConfidence (1.0 or 0.0)].
     */
    private double[] validateEdge(
            Edge edge,
            Set<String> edgeIds,
            Set<String> undirectedPairs,
            Map<String, Node> nodeMap,
            ValidationReport report) {

        double endpointDev = 0.0;
        double weightDev = 0.0;
        double isLowConfidence = 0.0;

        if (edge == null) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, "UNKNOWN", "edge", "Null edge record in dataset", null));
            return new double[]{0.0, 0.0, 0.0};
        }

        String id = edge.getId();
        if (id == null || id.isBlank()) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, "BLANK", "id", "Edge ID cannot be null or blank", id));
            return new double[]{0.0, 0.0, 0.0};
        }

        if (edgeIds.contains(id)) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, id, "id", "Duplicate edge ID found", id));
        } else {
            edgeIds.add(id);
        }

        String source = edge.getSource();
        String target = edge.getTarget();

        if (source == null || source.isBlank()) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, id, "source", "Edge source cannot be null or blank", source));
        }
        if (target == null || target.isBlank()) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, id, "target", "Edge target cannot be null or blank", target));
        }

        boolean sourceExists = source != null && nodeMap.containsKey(source);
        boolean targetExists = target != null && nodeMap.containsKey(target);

        if (source != null && !sourceExists) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, id, "source",
                    "Edge source node does not exist in nodes dataset", source));
        }
        if (target != null && !targetExists) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, id, "target",
                    "Edge target node does not exist in nodes dataset", target));
        }
        if ((sourceExists && !nodeMap.get(source).isRoutable()) || (targetExists && !nodeMap.get(target).isRoutable())) {
            report.addIssue(ValidationIssue.error(ValidationIssue.EntityType.EDGE, id, "routableEndpoints",
                    "Edges must connect routable path nodes", source + " -> " + target));
        }

        if (source != null && source.equals(target)) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, id, "selfLoop",
                    "Self-loop edges are not permitted", source + " -> " + target));
        }

        if (sourceExists && targetExists && !source.equals(target)) {
            String pairKey = source.compareTo(target) < 0 ? source + "--" + target : target + "--" + source;
            if (undirectedPairs.contains(pairKey)) {
                report.addIssue(ValidationIssue.error(
                        ValidationIssue.EntityType.EDGE, id, "duplicateEdge",
                        "Duplicate undirected edge between nodes " + source + " and " + target, pairKey));
            } else {
                undirectedPairs.add(pairKey);
            }
        }

        double weight = edge.getWeight();
        if (!Double.isFinite(weight) || weight <= 0.0) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, id, "weight",
                    "Edge weight must be a positive finite number", weight));
        }

        if (edge.getType() == null || !ALLOWED_EDGE_TYPES.contains(edge.getType().trim().toUpperCase())) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, id, "type",
                    "Invalid edge type. Allowed: " + ALLOWED_EDGE_TYPES, edge.getType()));
        }

        // Geometry validation
        List<GeoPoint> geometry = edge.getGeometry();
        if (geometry == null || geometry.isEmpty()) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, id, "geometry",
                    "Edge must contain non-empty ordered geometry points", null));
        } else if (geometry.size() < 2) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.EDGE, id, "geometry",
                    "Edge geometry must have at least 2 points (endpoints)", geometry.size()));
        } else {
            // Check finite geometry coordinates
            for (int i = 0; i < geometry.size(); i++) {
                GeoPoint pt = geometry.get(i);
                if (!Double.isFinite(pt.getLat()) || !Double.isFinite(pt.getLng())
                        || pt.getLat() < MIN_LAT || pt.getLat() > MAX_LAT
                        || pt.getLng() < MIN_LNG || pt.getLng() > MAX_LNG) {
                    report.addIssue(ValidationIssue.error(
                            ValidationIssue.EntityType.EDGE, id, "geometry[" + i + "]",
                            "Geometry point outside valid campus bounds", "(" + pt.getLat() + ", " + pt.getLng() + ")"));
                }
            }

            // Check endpoint alignment with Node lat/lng
            if (sourceExists && targetExists) {
                Node sNode = nodeMap.get(source);
                Node tNode = nodeMap.get(target);
                GeoPoint firstPt = geometry.get(0);
                GeoPoint lastPt = geometry.get(geometry.size() - 1);

                double dForwardS = haversineMeters(sNode.getLat(), sNode.getLng(), firstPt.getLat(), firstPt.getLng());
                double dForwardT = haversineMeters(tNode.getLat(), tNode.getLng(), lastPt.getLat(), lastPt.getLng());
                double dReverseS = haversineMeters(sNode.getLat(), sNode.getLng(), lastPt.getLat(), lastPt.getLng());
                double dReverseT = haversineMeters(tNode.getLat(), tNode.getLng(), firstPt.getLat(), firstPt.getLng());

                double forwardDev = Math.max(dForwardS, dForwardT);
                double reverseDev = Math.max(dReverseS, dReverseT);
                endpointDev = Math.min(forwardDev, reverseDev);

                if (endpointDev > ENDPOINT_TOLERANCE_METERS) {
                    report.addIssue(ValidationIssue.error(
                            ValidationIssue.EntityType.EDGE, id, "geometryEndpoints",
                            String.format("Geometry endpoints deviate from nodes (%s, %s) by %.3fm (tolerance %.1fm)",
                                    source, target, endpointDev, ENDPOINT_TOLERANCE_METERS),
                            endpointDev));
                }
            }

            // Check geometry length vs stored weight
            double computedLength = 0.0;
            for (int i = 0; i < geometry.size() - 1; i++) {
                GeoPoint p1 = geometry.get(i);
                GeoPoint p2 = geometry.get(i + 1);
                computedLength += haversineMeters(p1.getLat(), p1.getLng(), p2.getLat(), p2.getLng());
            }

            weightDev = Math.abs(computedLength - weight);
            if (weightDev > WEIGHT_TOLERANCE_METERS) {
                report.addIssue(ValidationIssue.error(
                        ValidationIssue.EntityType.EDGE, id, "weightAgreement",
                        String.format("Stored weight (%.2fm) differs from geometry path length (%.2fm) by %.3fm (tolerance %.1fm)",
                                weight, computedLength, weightDev, WEIGHT_TOLERANCE_METERS),
                        weightDev));
            }
        }

        // Confidence tracking (Uncertainty documentation, not error)
        String confidence = edge.getConfidence();
        if (confidence != null && !confidence.isBlank()) {
            String confUpper = confidence.trim().toUpperCase();
            if ("LOW".equals(confUpper) || "MEDIUM".equals(confUpper)) {
                isLowConfidence = 1.0;
                report.addIssue(ValidationIssue.info(
                        ValidationIssue.EntityType.EDGE, id, "confidence",
                        String.format("Edge is flagged with %s confidence (%s); subject to ground review",
                                confUpper, edge.getType()),
                        confUpper));
            }
        }

        return new double[]{endpointDev, weightDev, isLowConfidence};
    }

    private void validateBuilding(
            Building building,
            Set<String> buildingIds,
            Map<String, Node> nodeMap,
            ValidationReport report) {

        if (building == null) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.BUILDING, "UNKNOWN", "building", "Null building record in dataset", null));
            return;
        }

        String id = building.getId();
        if (id == null || id.isBlank()) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.BUILDING, "BLANK", "id", "Building ID cannot be null or blank", id));
            return;
        }

        if (buildingIds.contains(id)) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.BUILDING, id, "id", "Duplicate building ID found", id));
        } else {
            buildingIds.add(id);
        }

        if (building.getName() == null || building.getName().isBlank()) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.BUILDING, id, "name", "Building name cannot be null or blank", building.getName()));
        }

        if (building.getCode() == null || building.getCode().isBlank()) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.BUILDING, id, "code", "Building code cannot be null or blank", building.getCode()));
        }

        if (building.getCategory() == null || !ALLOWED_BUILDING_CATEGORIES.contains(building.getCategory().trim().toUpperCase())) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.BUILDING, id, "category",
                    "Invalid building category. Allowed: " + ALLOWED_BUILDING_CATEGORIES, building.getCategory()));
        }

        String nodeId = building.getNodeId();
        if (nodeId == null || nodeId.isBlank()) {
            boolean accessUnresolved = "UNRESOLVED".equalsIgnoreCase(building.getAccessConfidence());
            boolean hasAccessNote = building.getAccessNote() != null && !building.getAccessNote().isBlank();
            if (!accessUnresolved || !hasAccessNote) {
                report.addIssue(ValidationIssue.error(
                        ValidationIssue.EntityType.BUILDING, id, "nodeId",
                        "A missing route node must be explicitly marked UNRESOLVED with an access note", nodeId));
            } else {
                report.addIssue(ValidationIssue.info(
                        ValidationIssue.EntityType.BUILDING, id, "access",
                        "No routable building access is claimed because the supplied map does not support one",
                        building.getAccessConfidence()));
            }
        } else if (!nodeMap.containsKey(nodeId) || !nodeMap.get(nodeId).isRoutable()) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.BUILDING, id, "nodeId",
                    "Building nodeId must refer to a routable access node: " + nodeId, nodeId));
        } else if (building.getAccessNote() == null || building.getAccessNote().isBlank()) {
            report.addIssue(ValidationIssue.error(
                    ValidationIssue.EntityType.BUILDING, id, "accessNote",
                    "Mapped building access must explain its confidence and any entrance uncertainty", building.getAccessNote()));
        }

        String referenceNodeId = building.getReferenceNodeId();
        if (referenceNodeId != null && !referenceNodeId.isBlank()) {
            Node reference = nodeMap.get(referenceNodeId);
            if (reference == null || reference.isRoutable()) {
                report.addIssue(ValidationIssue.error(ValidationIssue.EntityType.BUILDING, id, "referenceNodeId",
                        "referenceNodeId must refer to a preserved non-routable landmark: " + referenceNodeId, referenceNodeId));
            }
        }
    }

    private void validateConnectivity(
            Map<String, Node> nodeMap,
            List<Edge> edges,
            ValidationReport report) {

        Map<String, Node> routableNodes = new HashMap<>();
        nodeMap.forEach((id, node) -> { if (node.isRoutable()) routableNodes.put(id, node); });
        if (routableNodes.isEmpty()) {
            report.setConnectedComponentCount(0);
            return;
        }

        Map<String, List<String>> adj = new HashMap<>();
        for (String nid : routableNodes.keySet()) {
            adj.put(nid, new ArrayList<>());
        }

        for (Edge e : edges) {
            String u = e.getSource();
            String v = e.getTarget();
            if (adj.containsKey(u) && adj.containsKey(v)
                    && routableNodes.containsKey(u) && routableNodes.containsKey(v)) {
                adj.get(u).add(v);
                adj.get(v).add(u);
            }
        }

        Set<String> visited = new HashSet<>();
        List<List<String>> components = new ArrayList<>();

        for (String nid : routableNodes.keySet()) {
            if (!visited.contains(nid)) {
                List<String> component = new ArrayList<>();
                Queue<String> queue = new ArrayDeque<>();
                visited.add(nid);
                queue.add(nid);

                while (!queue.isEmpty()) {
                    String curr = queue.poll();
                    component.add(curr);
                    for (String neighbor : adj.get(curr)) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
                components.add(component);
            }
        }

        report.setConnectedComponentCount(components.size());
        report.setConnectedComponents(components);

        if (components.size() > 1) {
            report.addIssue(ValidationIssue.warning(
                    ValidationIssue.EntityType.GRAPH, "TOPOLOGY", "connectedComponents",
                    String.format("Graph has %d disconnected components", components.size()),
                    components.size()));
        }
    }

    /**
     * Standard WGS84 Haversine distance in meters.
     */
    public static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return R * c;
    }
}
