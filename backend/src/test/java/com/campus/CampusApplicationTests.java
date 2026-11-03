package com.campus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CampusApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("Health endpoint returns UP")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("Smart Campus Optimizer"));
    }

    @Test
    @DisplayName("Get all buildings returns 35 records with valid schema")
    void testGetBuildings() throws Exception {
        mockMvc.perform(get("/api/buildings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(35)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].nodeId").exists())
                .andExpect(jsonPath("$[0].category").exists());
    }

    @Test
    @DisplayName("Get building by ID returns building when found and 404 when not found")
    void testGetBuildingById() throws Exception {
        mockMvc.perform(get("/api/buildings/B001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("B001"))
                .andExpect(jsonPath("$.nodeId").value("N003"));

        mockMvc.perform(get("/api/buildings/B999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Building not found")));
    }

    @Test
    @DisplayName("Get graph returns canonical 61 nodes and 61 edges with dynamic counts")
    void testGetGraph() throws Exception {
        mockMvc.perform(get("/api/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.graphId").value("NSUT_MAIN_GRAPH"))
                .andExpect(jsonPath("$.nodeCount").value(61))
                .andExpect(jsonPath("$.edgeCount").value(61))
                .andExpect(jsonPath("$.nodes", hasSize(61)))
                .andExpect(jsonPath("$.edges", hasSize(61)));
    }

    @Test
    @DisplayName("Get all nodes returns 61 nodes with geographic coordinates")
    void testGetAllNodes() throws Exception {
        mockMvc.perform(get("/api/graph/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(61)))
                .andExpect(jsonPath("$[0].lat").isNumber())
                .andExpect(jsonPath("$[0].lng").isNumber());
    }

    @Test
    @DisplayName("Get node by ID returns node when found and 404 when not found")
    void testGetNodeById() throws Exception {
        mockMvc.perform(get("/api/graph/nodes/N001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("N001"))
                .andExpect(jsonPath("$.name").value("Main Gate"))
                .andExpect(jsonPath("$.lat").isNumber())
                .andExpect(jsonPath("$.lng").isNumber());

        mockMvc.perform(get("/api/graph/nodes/N999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Node not found")));
    }

    @Test
    @DisplayName("Get all edges returns 61 edges with ordered geometry and confidence")
    void testGetAllEdges() throws Exception {
        mockMvc.perform(get("/api/graph/edges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(61)))
                .andExpect(jsonPath("$[0].geometry").isArray())
                .andExpect(jsonPath("$[0].geometry", not(empty())))
                .andExpect(jsonPath("$[0].weight").isNumber());
    }

    @Test
    @DisplayName("Get edge by ID returns edge when found and 404 when not found")
    void testGetEdgeById() throws Exception {
        mockMvc.perform(get("/api/graph/edges/E001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("E001"))
                .andExpect(jsonPath("$.source").value("N002"))
                .andExpect(jsonPath("$.target").value("N043"))
                .andExpect(jsonPath("$.geometry").isArray());

        mockMvc.perform(get("/api/graph/edges/E999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Edge not found")));
    }

    @Test
    @DisplayName("Graph validation endpoint reports VALID with 1 connected component")
    void testValidationEndpoint() throws Exception {
        mockMvc.perform(get("/api/graph/validation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.nodeCount").value(61))
                .andExpect(jsonPath("$.data.edgeCount").value(61))
                .andExpect(jsonPath("$.data.buildingCount").value(35))
                .andExpect(jsonPath("$.data.connectedComponentCount").value(1))
                .andExpect(jsonPath("$.data.errorCount").value(0))
                .andExpect(jsonPath("$.data.maxEndpointDeviationMeters").isNumber())
                .andExpect(jsonPath("$.data.maxWeightDeviationMeters").isNumber());
    }

    @Test
    @DisplayName("BFS returns 200 for valid nodes and 400 for unknown node IDs")
    void testBfsEndpoint() throws Exception {
        mockMvc.perform(get("/api/graph/bfs?startNodeId=N001&endNodeId=N004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.pathNodes").isArray());

        mockMvc.perform(get("/api/graph/bfs?startNodeId=N999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Start node not found")));

        mockMvc.perform(get("/api/graph/bfs?startNodeId=N001&endNodeId=N999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("End node not found")));
    }

    @Test
    @DisplayName("Dijkstra returns shortest path for valid nodes and 400 for unknown node IDs")
    void testDijkstraEndpoint() throws Exception {
        mockMvc.perform(get("/api/graph/shortest-path?startNodeId=N001&endNodeId=N004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.totalWeight").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.data.pathNodes").isArray())
                .andExpect(jsonPath("$.data.pathEdges").isArray());

        mockMvc.perform(get("/api/graph/shortest-path?startNodeId=N999&endNodeId=N004"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Start node not found")));
    }

    @Test
    @DisplayName("Prim and Kruskal MST endpoints return spanning tree and handle invalid inputs")
    void testMstEndpoints() throws Exception {
        mockMvc.perform(get("/api/graph/mst/kruskal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pathEdges").isArray());

        mockMvc.perform(get("/api/graph/mst/prim?startNodeId=N001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pathEdges").isArray());

        mockMvc.perform(get("/api/graph/mst/prim?startNodeId=N999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Start node not found")));
    }

    @Test
    @DisplayName("Execute endpoint handles POST traversal requests and invalid inputs")
    void testExecuteEndpoint() throws Exception {
        String validPayload = """
                {
                    "algorithm": "DIJKSTRA",
                    "startNodeId": "N001",
                    "endNodeId": "N003",
                    "wheelchairAccessibleOnly": false
                }
                """;

        mockMvc.perform(post("/api/graph/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true));

        String invalidPayload = """
                {
                    "algorithm": "DIJKSTRA",
                    "startNodeId": "INVALID_NODE",
                    "endNodeId": "N003"
                }
                """;

        mockMvc.perform(post("/api/graph/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Start node not found")));
    }

    @Test
    @DisplayName("Execute endpoint rejects missing or blank required node IDs and unsupported algorithms")
    void testExecuteEndpointRejectsMissingOrBlankRequiredIds() throws Exception {
        // 1. Missing startNodeId for DIJKSTRA
        String missingStartPayload = """
                {
                    "algorithm": "DIJKSTRA",
                    "endNodeId": "N003"
                }
                """;
        mockMvc.perform(post("/api/graph/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingStartPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Start node ID is required")));

        // 2. Blank startNodeId for DIJKSTRA
        String blankStartPayload = """
                {
                    "algorithm": "DIJKSTRA",
                    "startNodeId": "   ",
                    "endNodeId": "N003"
                }
                """;
        mockMvc.perform(post("/api/graph/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blankStartPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Start node ID is required")));

        // 3. Missing endNodeId for DIJKSTRA
        String missingEndPayload = """
                {
                    "algorithm": "DIJKSTRA",
                    "startNodeId": "N001"
                }
                """;
        mockMvc.perform(post("/api/graph/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingEndPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("End node ID is required")));

        // 4. Missing startNodeId for BFS
        String missingBfsStartPayload = """
                {
                    "algorithm": "BFS",
                    "endNodeId": "N003"
                }
                """;
        mockMvc.perform(post("/api/graph/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingBfsStartPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Start node ID is required")));

        // 5. Unsupported algorithm
        String unsupportedAlgoPayload = """
                {
                    "algorithm": "NON_EXISTENT_ALGO",
                    "startNodeId": "N001",
                    "endNodeId": "N003"
                }
                """;
        mockMvc.perform(post("/api/graph/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unsupportedAlgoPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Unsupported algorithm")));
    }

    @Test
    @DisplayName("Route finding correctly handles same start and destination endpoints")
    void testRouteFindingSameStartAndEnd() throws Exception {
        mockMvc.perform(get("/api/graph/shortest-path?startNodeId=N001&endNodeId=N001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.hopCount").value(0))
                .andExpect(jsonPath("$.data.totalWeight").value(0.0))
                .andExpect(jsonPath("$.data.pathNodes", hasSize(1)))
                .andExpect(jsonPath("$.data.pathNodes[0].id").value("N001"));

        mockMvc.perform(get("/api/graph/bfs?startNodeId=N001&endNodeId=N001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.hopCount").value(0))
                .andExpect(jsonPath("$.data.totalWeight").value(0.0))
                .andExpect(jsonPath("$.data.pathNodes", hasSize(1)));
    }

    @Test
    @DisplayName("Verify concrete campus route examples with geometry and metric distance")
    void testConcreteCampusRoutes() throws Exception {
        // 1. Dijkstra Route: Main Gate (N001) to Block 4 (N004)
        mockMvc.perform(get("/api/graph/shortest-path?startNodeId=N001&endNodeId=N004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.startNodeId").value("N001"))
                .andExpect(jsonPath("$.data.endNodeId").value("N004"))
                .andExpect(jsonPath("$.data.totalWeight").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.data.hopCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.pathNodes", not(empty())))
                .andExpect(jsonPath("$.data.pathEdges", not(empty())))
                .andExpect(jsonPath("$.data.pathEdges[0].geometry").isArray());

        // 2. BFS Route: Main Gate (N001) to Block 4 (N004)
        mockMvc.perform(get("/api/graph/bfs?startNodeId=N001&endNodeId=N004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.hopCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.totalWeight").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.data.pathNodes", not(empty())))
                .andExpect(jsonPath("$.data.pathEdges", not(empty())));

        // 3. North Gate (N002) to Director's Admin Block (N003)
        mockMvc.perform(get("/api/graph/shortest-path?startNodeId=N002&endNodeId=N003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.totalWeight").value(greaterThan(100.0)))
                .andExpect(jsonPath("$.data.pathNodes", not(empty())));
    }

    @Test
    @DisplayName("Verify BFS vs Dijkstra benchmark trade-off: Main Gate (N001) to Block 5 (N005)")
    void testBfsVsDijkstraComparisonTradeoff() throws Exception {
        // Dijkstra execution: optimizes walking distance in meters (225.9 m, 5 hops)
        String dijkstraPayload = """
                {
                    "algorithm": "DIJKSTRA",
                    "startNodeId": "N001",
                    "endNodeId": "N005"
                }
                """;
        mockMvc.perform(post("/api/graph/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dijkstraPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.totalWeight").value(closeTo(225.9, 0.2)))
                .andExpect(jsonPath("$.data.hopCount").value(5))
                .andExpect(jsonPath("$.data.pathNodes", hasSize(6)))
                .andExpect(jsonPath("$.data.pathNodes[3].id").value("N039")); // Takes N039 shortcut

        // BFS execution: optimizes graph edge hops (226.7 m, 4 hops)
        String bfsPayload = """
                {
                    "algorithm": "BFS",
                    "startNodeId": "N001",
                    "endNodeId": "N005"
                }
                """;
        mockMvc.perform(post("/api/graph/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bfsPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.totalWeight").value(closeTo(226.7, 0.2)))
                .andExpect(jsonPath("$.data.hopCount").value(4)) // 1 fewer hop
                .andExpect(jsonPath("$.data.pathNodes", hasSize(5))); // 5 nodes instead of 6
    }
}
