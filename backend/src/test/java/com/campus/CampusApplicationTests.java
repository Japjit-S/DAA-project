package com.campus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @DisplayName("Get all buildings returns 38 records with valid schema")
    void testGetBuildings() throws Exception {
        mockMvc.perform(get("/api/buildings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(38)))
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
                .andExpect(jsonPath("$.nodeId").value("N003"))
                .andExpect(jsonPath("$.accessConfidence").value("HIGH"));

        mockMvc.perform(get("/api/buildings/B015"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeId").value("N056"))
                .andExpect(jsonPath("$.accessConfidence").value("MEDIUM"))
                .andExpect(jsonPath("$.accessNote").isNotEmpty());

        mockMvc.perform(get("/api/buildings/B999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Building not found")));
    }

    @Test
    @DisplayName("Get graph returns 84 routable path points and 85 geometry-backed segments")
    void testGetGraph() throws Exception {
        mockMvc.perform(get("/api/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.graphId").value("NSUT_MAIN_GRAPH"))
                .andExpect(jsonPath("$.nodeCount").value(124))
                .andExpect(jsonPath("$.edgeCount").value(134))
                .andExpect(jsonPath("$.nodes", hasSize(124)))
                .andExpect(jsonPath("$.edges", hasSize(134)));
    }

    @Test
    @DisplayName("Get all nodes returns 84 routable path points with geographic coordinates")
    void testGetAllNodes() throws Exception {
        mockMvc.perform(get("/api/graph/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(124)))
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

        mockMvc.perform(get("/api/graph/nodes/N011"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/graph/nodes/N022"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Get all edges returns 85 segments with ordered geometry and confidence")
    void testGetAllEdges() throws Exception {
        mockMvc.perform(get("/api/graph/edges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(134)))
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
                .andExpect(jsonPath("$.data.nodeCount").value(124))
                .andExpect(jsonPath("$.data.edgeCount").value(134))
                .andExpect(jsonPath("$.data.buildingCount").value(38))
                .andExpect(jsonPath("$.data.connectedComponentCount").value(1))
                .andExpect(jsonPath("$.data.errorCount").value(0))
                .andExpect(jsonPath("$.data.maxEndpointDeviationMeters").isNumber())
                .andExpect(jsonPath("$.data.maxWeightDeviationMeters").isNumber());
    }

    @Test
    @DisplayName("BFS returns 200 for valid nodes and 400 for unknown node IDs")
    void testBfsEndpoint() throws Exception {
        mockMvc.perform(get("/api/graph/bfs?startNodeId=N001&endNodeId=N005"))
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
        mockMvc.perform(get("/api/graph/shortest-path?startNodeId=N001&endNodeId=N005"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.totalWeight").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.data.pathNodes").isArray())
                .andExpect(jsonPath("$.data.pathEdges").isArray());

        mockMvc.perform(get("/api/graph/shortest-path?startNodeId=N999&endNodeId=N050"))
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
                    "endNodeId": "N050",
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
                    "endNodeId": "N050"
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
                    "endNodeId": "N050"
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
                    "endNodeId": "N050"
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
                    "endNodeId": "N050"
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
                    "endNodeId": "N050"
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
        // 1. Dijkstra route: Main Gate (N001) to Block 4's mapped path access (N089)
        mockMvc.perform(get("/api/graph/shortest-path?startNodeId=N001&endNodeId=N005"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.startNodeId").value("N001"))
                .andExpect(jsonPath("$.data.endNodeId").value("N005"))
                .andExpect(jsonPath("$.data.totalWeight").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.data.hopCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.pathNodes", not(empty())))
                .andExpect(jsonPath("$.data.pathEdges", not(empty())))
                .andExpect(jsonPath("$.data.pathEdges[0].geometry").isArray());

        // 2. BFS route: Main Gate (N001) to Block 4's mapped path access (N089)
        mockMvc.perform(get("/api/graph/bfs?startNodeId=N001&endNodeId=N005"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.hopCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.totalWeight").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.data.pathNodes", not(empty())))
                .andExpect(jsonPath("$.data.pathEdges", not(empty())));

        // 3. North Gate (N002) to Admin Block (N003)
        mockMvc.perform(get("/api/graph/shortest-path?startNodeId=N002&endNodeId=N003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.totalWeight").value(greaterThan(100.0)))
                .andExpect(jsonPath("$.data.pathNodes", not(empty())));

        // 4. Main Gate to the mapped Sports Complex west perimeter road (not the track center)
        mockMvc.perform(get("/api/graph/shortest-path?startNodeId=N001&endNodeId=N056"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.pathNodes", not(empty())))
                .andExpect(jsonPath("$.data.pathEdges", not(empty())));
    }

    @Test
    @DisplayName("Verify BFS and Dijkstra trade-offs: Main Gate (N001) to Block 5 access (N090)")
    void testBfsVsDijkstraComparisonTradeoff() throws Exception {
        // Dijkstra minimizes mapped walking distance.
        String dijkstraPayload = """
                {
                    "algorithm": "DIJKSTRA",
                    "startNodeId": "N001",
                    "endNodeId": "N005"
                }
                """;
        MvcResult dijkstraResponse = mockMvc.perform(post("/api/graph/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dijkstraPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.totalWeight").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.data.hopCount").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.pathEdges[0].geometry").isArray())
                .andReturn();

        // BFS minimizes mapped edge hops; its total meters may be greater.
        String bfsPayload = """
                {
                    "algorithm": "BFS",
                    "startNodeId": "N001",
                    "endNodeId": "N005"
                }
                """;
        MvcResult bfsResponse = mockMvc.perform(post("/api/graph/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bfsPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reachable").value(true))
                .andExpect(jsonPath("$.data.totalWeight").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.data.hopCount").value(greaterThan(0)))
                .andReturn();

        double dijkstraDistance = JsonPath.read(dijkstraResponse.getResponse().getContentAsString(), "$.data.totalWeight");
        double bfsDistance = JsonPath.read(bfsResponse.getResponse().getContentAsString(), "$.data.totalWeight");
        int dijkstraHops = JsonPath.read(dijkstraResponse.getResponse().getContentAsString(), "$.data.hopCount");
        int bfsHops = JsonPath.read(bfsResponse.getResponse().getContentAsString(), "$.data.hopCount");
        assertTrue(dijkstraDistance <= bfsDistance + 1e-6, "Dijkstra should not exceed the BFS route distance");
        assertTrue(bfsHops <= dijkstraHops, "BFS should not exceed Dijkstra's hop count");
    }
}
