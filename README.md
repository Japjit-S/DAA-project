# Smart Campus Navigation and Resource Optimizer

[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Build-Maven-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Academic-lightgrey.svg)]()
[![Tests](https://img.shields.io/badge/Tests-18%20Passing-success.svg)]()

A comprehensive, extensible testbed for spatial pathfinding and campus resource optimization developed for college **Design and Analysis of Algorithms (DAA)** coursework at Netaji Subhas University of Technology (NSUT), Dwarka, New Delhi.

The project combines a high-performance **Java 21+ / Spring Boot 3.3.5** backend, a **canonical JSON data layer**, and an interactive, responsive **Vanilla HTML5 / CSS3 / ES6** web dashboard. The campus map features registered local **OpenStreetMap Standard** raster panels with dynamic SVG graph overlays, real-time building catalog search, shortest-path calculation (Dijkstra and BFS), and a side-by-side comparative algorithmic analysis.

---

## 1. Quickstart & Local Setup

### Prerequisites
- **Java**: JDK 17, 21, or 24 installed (`java -version`).
- **Build System**: Maven 3.9+ (or use the included Maven wrapper `mvnw` / `mvnw.cmd`).

### Option 1: Running with Maven Wrapper (Recommended)

1. Open a terminal in the `backend` directory:
   ```bash
   cd backend
   ```

2. Launch the Spring Boot application:
   - **On Windows (PowerShell / CMD)**:
     ```powershell
     .\mvnw.cmd spring-boot:run
     ```
   - **On macOS / Linux**:
     ```bash
     ./mvnw spring-boot:run
     ```

### Option 2: Running with Installed Maven
```bash
cd backend
mvn clean spring-boot:run
```

### Option 3: Building and Running Packaged JAR
```bash
cd backend
.\mvnw.cmd clean package -DskipTests
java -jar target/smart-campus-optimizer-1.0.0.jar
```

### Accessing the Dashboard
Once the console prints `Started CampusApplication in ... seconds`, navigate to:

👉 **`http://localhost:8080`**

### Running Automated Integration Tests
The project includes a 18-test MockMvc integration suite verifying all REST controllers, graph validation, and algorithmic edge cases:
```bash
cd backend
.\mvnw.cmd test
```

---

## 2. Project Architecture & Layering

```text
┌────────────────────────────────────────────────────────────────────────┐
│                   Frontend Dashboard (Vanilla HTML5 / CSS3 / ES6)       │
│   • Registered OSM Base + SVG Layers       • Searchable Facility Directory│
│   • Route Finder (BFS & Dijkstra)          • Side-by-Side Algorithm Comp  │
│   • Real-Time REST API Inspector Drawer    • Mobile Tab Switcher          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ HTTP / JSON REST APIs (Port 8080)
┌───────────────────────────────────▼────────────────────────────────────┐
│                    Spring Boot REST Controller Layer                    │
│   • HealthController (/api/health)                                     │
│   • CampusController (/api/buildings, /api/buildings/{id})             │
│   • GraphController  (/api/graph, /nodes, /edges, /execute, /validation)│
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                             Service Layer                              │
│   • CampusService  (Facilities directory, categories, search queries)  │
│   • GraphService   (Adjacency indexing, algorithm routing hooks)       │
│   • CampusDataValidator (Startup integrity & topological consistency)  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                        DAA Algorithm Engine                            │
│   • DijkstraPathfinder (Min-Heap priority queue, distance optimal)     │
│   • BfsTraversal       (FIFO ArrayDeque, hop-count optimal)            │
│   • DfsTraversal       (LIFO recursion, topological traversal)         │
│   • PrimMst            (Cut-property greedy minimum spanning tree)     │
│   • KruskalMst         (Disjoint Set Union-Find cycle prevention)      │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                        Storage & Data Model                            │
│   • Node     (id, name, type, x, y, lat, lng, accessible)              │
│   • Edge     (id, source, target, weight, geometry, confidence, type)  │
│   • Building (id, name, code, category, nodeId, floors, facilities)   │
│   • JsonDataRepository (Thread-safe JSON persistence & composition)    │
│   • data/nodes.json  • data/edges.json  • data/buildings.json          │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Campus Graph Representation & Topology

The NSUT campus is modeled as an undirected, weighted graph $G = (V, E)$:

### A. Graph Vertices (Nodes — `data/nodes.json`)
The graph contains **61 nodes**:
- **35 Campus Facilities**: Real NSUT buildings, administrative centers, student hostels, library/SAC complex, and sports amenities.
- **26 Pavement Junctions & Waypoints**: Major crossroads, arterial turns, and walkway intersections traced along physical paths.
- Each node stores both canvas coordinates (`x`, `y`) and geographic coordinates (`lat`, `lng`), along with accessibility flags.

### B. Graph Edges (Pathways — `data/edges.json`)
The graph contains **61 edges**:
- **Walkways & Arterial Roads**: Modeled directly along visible pavement from OpenStreetMap Standard exports.
- **Ordered Geographic Geometry**: Every edge contains an ordered sequence of intermediate GPS waypoints (`geometry: [{"lat": ..., "lng": ...}]`), ensuring curved paths reflect realistic walking geometry.
- **Physical Weights**: Edge weights are calculated in **meters** as the sum of Haversine arc distances across all consecutive geometry coordinates (maximum deviation between stored weight and geometry length is $< 0.05\text{ m}$).
- **Edge Confidence**:
  - `MEDIUM` (26 edges): Road and walkway segments traced along visible OpenStreetMap paths.
  - `LOW` (35 edges): Facility access links connecting building center points to adjacent walkway junctions (approximate entrance links).

### C. Single-Cycle Topology (Circuit Rank = 1)
- In graph theory, the circuit rank (number of independent cycles) for a connected graph is $M = |E| - |V| + 1$.
- Here: $M = 61 - 61 + 1 = 1$.
- The campus graph consists of a **single fundamental triangle cycle** surrounded by tree branches:
  - Cycle nodes: `N050` (Block 4 Walkway Junction), `N039` (Library Walkway Junction), `N051` (Block 5 Walkway Junction).
  - Cycle edges:
    - Direct edge `E012`: `N050` $\leftrightarrow$ `N051` ($132.5\text{ m}$, 1 hop, `MEDIUM` confidence).
    - Detour via `N039`: `E010` ($74.7\text{ m}$) + `E011` ($57.0\text{ m}$) = $131.7\text{ m}$, 2 hops (`MEDIUM` confidence).

---

## 4. Map Calibration & Background Imagery

- **Background Imagery**: Served entirely from local raster exports of OpenStreetMap Standard (`backend/src/main/resources/static/assets/maps/`):
  - `map-nw.png`, `map-ne.png`, `map-sw.png`, `map-se.png` (4 high-resolution detail panels).
  - `map-overview.png` (zoom-15 campus overview layer).
  - *No live external tile downloads or internet connections are required at runtime.*
- **Map Registration**: The detail panels are aligned using SIFT feature matching with RANSAC homography, projected in Web Mercator (EPSG:3857) at a scale of **0.56034 projected meters per pixel**.
- **Attribution**: Displayed visibly in the bottom corner with direct attribution: `© OpenStreetMap contributors` under the Open Database License (ODbL).
- **Coordinate Transformation**: Handled dynamically in `calibration.js` using forward/inverse Web Mercator projection:
  $$x = x_{\text{anchor}} + \frac{X_{\text{mercator}}(\text{lng}) - X_{\text{anchor}}}{0.56034}, \quad y = y_{\text{anchor}} - \frac{Y_{\text{mercator}}(\text{lat}) - Y_{\text{anchor}}}{0.56034}$$

---

## 5. DAA Algorithms: BFS vs. Dijkstra

| Property | Breadth-First Search (BFS) | Dijkstra's Algorithm |
|---|---|---|
| **Primary Goal** | Minimizes **topological transitions (hop count)** | Minimizes **physical walking distance (meters)** |
| **Edge Costs** | Unweighted: treats every edge as unit cost ($1$) | Weighted: accounts for exact metric road lengths ($w(e) \ge 0$) |
| **Data Structure** | FIFO Queue (`java.util.ArrayDeque`) | Min-Priority Queue (`com.campus.algorithm.datastructure.MinHeap`) |
| **Time Complexity** | $O(V + E)$ | $O((V + E) \log V)$ |
| **Space Complexity**| $O(V)$ | $O(V)$ |
| **Early Exit** | Terminates as soon as target node is dequeued | Terminates as soon as target node is settled |

### The Canonical Campus Benchmark: Main Gate (`N001`) to Block 5 (`N005`)
To demonstrate how algorithm objectives diverge on a real road network, click **"💡 Load Demo: Main Gate → Block 5"**:

```
                (Main Gate) N001 
                       │ E014 (39.0m, MAIN_ROAD)
                     N052
                       │ E013 (44.6m, MAIN_ROAD)
                     N050 (Block 4 Junction)
                    ╱    ╲
      E010 (74.7m) ╱      ╲ E012 (132.5m, 1 hop)
                  N039     │ [BFS direct branch]
      E011 (57.0m) ╲      ╱
                    ╲    ╱
                     N051 (Block 5 Junction)
                       │ E033 (10.6m, ACCESS_PATH)
                (Block 5) N005
```

- **Dijkstra Route**: `N001` $\to$ `N052` $\to$ `N050` $\to$ `N039` $\to$ `N051` $\to$ `N005`
  - Total Distance: **$225.9\text{ m}$**
  - Transitions (Hops): **5 hops**
  - Path: Takes the two shorter walkway segments `E010` + `E011` ($74.7 + 57.0 = 131.7\text{ m}$) through junction `N039`.
- **BFS Route**: `N001` $\to$ `N052` $\to$ `N050` $\to$ `N051` $\to$ `N005`
  - Total Distance: **$226.7\text{ m}$**
  - Transitions (Hops): **4 hops**
  - Path: Takes the single direct edge `E012` ($132.5\text{ m}$) because unweighted BFS treats all transitions as having cost $1$.
- **Algorithmic Insight**:
  - Dijkstra minimizes physical distance, saving $0.8\text{ m}$ of modeled path length at the cost of $1$ extra turn.
  - BFS minimizes graph transitions, saving $1$ hop at the cost of $+0.8\text{ m}$ of physical walking.
  - *Engineering Context*: The $0.8\text{ m}$ difference demonstrates theoretical objective divergence on the graph model; in practice, $0.8\text{ m}$ is within GPS and surveying precision and represents negligible pedestrian effort.
  - *Confidence Validation*: All three divergent cycle edges (`E010`, `E011`, `E012`) have `MEDIUM` confidence. The only `LOW` confidence edge is `E033` (Block 5 access path), which is shared identically by both algorithms.

---

## 6. Using the Interactive Dashboard

1. **Facilities Directory & Search**:
   - Filter by category pills: `All`, `Academic`, `Hostels`, `Library & SAC`, `Facilities`, `Amenities`, `Sports`.
   - Search in real-time across building names, codes, departments, floors, and specific facilities.
   - Click any building card to automatically center the map camera and highlight its vertex and incident pathways.
2. **Single Route Finding**:
   - Select Origin and Destination from the dropdown menus.
   - Use the **⇄ Swap** button to reverse endpoints.
   - Choose your algorithm: **Dijkstra** (recommended for shortest walking distance) or **BFS** (fewest turns/hops).
   - Toggle **Wheelchair accessible pathways only** to restrict traversal to accessible paths.
   - Click **🚀 Find Route** to render the animated route overlay, turn-by-turn waypoint list, and metric breakdown.
3. **Side-by-Side Algorithm Comparison**:
   - Click **⚖️ Compare BFS & Dijkstra** to run both algorithms concurrently.
   - Inspect the dual-card metrics: Distance, Hops, Explored Nodes, and Execution Time.
   - The map displays both routes with high-contrast styling:
     - **Solid Cyan (`#38bdf8`)**: Dijkstra's shortest-distance route.
     - **Animated Dashed Amber (`#f59e0b`)**: BFS's fewest-hop route.
     - **Dual-Tone Pattern**: Segments shared by both algorithms.
   - Toggle view filters via the filter chips: `Both Routes`, `Dijkstra Only`, `BFS Only`.
   - Click any turn waypoint in either column to focus that exact location on the map.
4. **Live REST API Inspector Drawer**:
   - Expand the bottom drawer to observe live network traffic, HTTP status codes, latency in milliseconds, and formatted JSON payloads for all API calls.

---

## 7. REST API Specification

### A. Health Check
`GET /api/health`
```json
{
  "status": "UP",
  "application": "Smart Campus Optimizer"
}
```

### B. Building Directory
- `GET /api/buildings`: Returns all 35 campus buildings.
- `GET /api/buildings?category=ACADEMIC`: Filter by category (`ACADEMIC`, `HOSTEL`, `LIBRARY`, `FACILITY`, `AMENITY`, `ADMIN`, `SPORTS`).
- `GET /api/buildings/{id}`: Returns specific building details.

### C. Graph Topology
- `GET /api/graph`: Complete campus graph object with 61 nodes and 61 edges.
- `GET /api/graph/nodes`: All 61 vertices with GPS coordinates and roles.
- `GET /api/graph/nodes/{id}`: Single node record by ID.
- `GET /api/graph/edges`: All 61 walking pathways with geometry coordinates and weights.
- `GET /api/graph/edges/{id}`: Single edge record by ID.

### D. Unified Algorithm Execution Endpoint
`POST /api/graph/execute`
- **Request Payload**:
  ```json
  {
    "algorithm": "DIJKSTRA",
    "startNodeId": "N001",
    "endNodeId": "N005",
    "wheelchairAccessibleOnly": false,
    "blockedEdgeIds": []
  }
  ```
  *(Supported `algorithm` values: `DIJKSTRA`, `BFS`, `DFS`, `PRIM`, `KRUSKAL`)*
- **Sample Success Response (200 OK)**:
  ```json
  {
    "success": true,
    "message": "Algorithm executed successfully",
    "data": {
      "algorithm": "DIJKSTRA",
      "startNodeId": "N001",
      "endNodeId": "N005",
      "reachable": true,
      "totalWeight": 225.9,
      "hopCount": 5,
      "pathNodes": [
        {"id": "N001", "name": "Main Gate", "type": "GATE", "lat": 28.60950, "lng": 77.03710},
        {"id": "N052", "name": "Boulevard South Junction", "type": "JUNCTION", "lat": 28.60985, "lng": 77.03710},
        {"id": "N050", "name": "Block 4 Walkway Junction", "type": "JUNCTION", "lat": 28.61025, "lng": 77.03710},
        {"id": "N039", "name": "Library Walkway Junction", "type": "JUNCTION", "lat": 28.61058, "lng": 77.03643},
        {"id": "N051", "name": "Block 5 Walkway Junction", "type": "JUNCTION", "lat": 28.61092, "lng": 77.03685},
        {"id": "N005", "name": "Block 5", "type": "ACADEMIC", "lat": 28.61092, "lng": 77.03695}
      ],
      "pathEdges": [
        {"id": "E014", "source": "N052", "target": "N001", "weight": 39.0, "confidence": "MEDIUM"},
        {"id": "E013", "source": "N050", "target": "N052", "weight": 44.6, "confidence": "MEDIUM"},
        {"id": "E010", "source": "N039", "target": "N050", "weight": 74.7, "confidence": "MEDIUM"},
        {"id": "E011", "source": "N039", "target": "N051", "weight": 57.0, "confidence": "MEDIUM"},
        {"id": "E033", "source": "N005", "target": "N051", "weight": 10.6, "confidence": "LOW"}
      ],
      "visitedOrder": ["N001", "N052", "N050", "N043", "N039", "N051", "N005"],
      "executionTimeMicros": 185
    },
    "timestamp": 1793732433000
  }
  ```

### E. Graph Integrity Validation
`GET /api/graph/validation`
- Evaluates complete graph topology and returns a detailed health report:
  - Validates node and edge ID uniqueness.
  - Verifies that edge geometry endpoints align with source and target node coordinates (current maximum deviation: $0.0006\text{ m}$).
  - Verifies that edge weights match Haversine arc distance of edge geometry (current maximum deviation: $0.049\text{ m}$).
  - Verifies building `nodeId` references.
  - Runs Tarjan's connected component analysis (verifies 1 single connected component across all 61 nodes).

---

## 8. Known Limitations & Remaining Review Items

To maintain scientific integrity for this academic project, the following limitations are explicitly noted:
1. **Map Imagery**: Uses local raster exports from OpenStreetMap Standard. It is **not** live satellite photography.
2. **Access Links**: The 35 building access edges (marked `LOW` confidence in `edges.json`) connect building centroid coordinates to adjacent path junctions. Physical campus doors and turnstiles were not surveyed on foot.
3. **Main Gate Placement**: The Main Gate GPS coordinate represents an approximate entrance node from campus bounding records; precise gate-to-boulevard geometry requires on-site GPS verification.
4. **Hostel Internal Turns**: Walkway paths around the northern residential sectors follow visible OpenStreetMap geometry, but unpaved shortcuts or temporary physical barriers are not modeled.
5. **Distance Granularity**: Distances are modeled mathematically via Haversine arc geometry. Modeled differences $< 1\text{ m}$ demonstrate algorithm objective differences on the graph model rather than meaningful differences in pedestrian exertion.

---

## 9. Project Directory Structure

```text
DAA-project/
├── agents.md                           # Master project log and agent guidelines
├── PROJECT_PHASES.md                   # Phase-by-phase completion roadmap
├── README.md                           # This documentation
├── .gitignore
│
├── backend/
│   ├── pom.xml                         # Maven project configuration (Java 21 target)
│   ├── mvnw / mvnw.cmd                 # Maven wrapper scripts
│   └── src/
│       ├── main/
│       │   ├── java/com/campus/
│       │   │   ├── CampusApplication.java          # Spring Boot main class
│       │   │   ├── algorithm/
│       │   │   │   ├── datastructure/MinHeap.java  # Custom min-priority queue for Dijkstra
│       │   │   │   ├── shortestpath/DijkstraPathfinder.java
│       │   │   │   ├── traversal/BfsTraversal.java
│       │   │   │   ├── traversal/DfsTraversal.java
│       │   │   │   └── mst/PrimMst.java, KruskalMst.java
│       │   │   ├── controller/
│       │   │   │   ├── HealthController.java       # GET /api/health
│       │   │   │   ├── CampusController.java       # GET /api/buildings
│       │   │   │   └── GraphController.java        # Graph and algorithm endpoints
│       │   │   ├── model/ (Node, Edge, Building, Graph, GeoPoint)
│       │   │   ├── repository/JsonDataRepository.java
│       │   │   ├── service/ (CampusService, GraphService)
│       │   │   └── validation/CampusDataValidator.java
│       │   └── resources/
│       │       ├── application.properties
│       │       └── static/
│       │           ├── index.html                  # Full dashboard UI
│       │           ├── css/style.css               # Modern slate styling & overlays
│       │           ├── js/
│       │           │   ├── api.js                  # REST API client & interceptor
│       │           │   ├── app.js                  # Map interactions & comparison engine
│       │           │   └── calibration.js          # Web Mercator projection engine
│       │           └── assets/maps/                # Registered local OSM raster panels
│       └── test/
│           └── java/com/campus/
│               └── CampusApplicationTests.java     # 18 passing integration tests
│
└── data/
    ├── buildings.json                  # 35 NSUT facilities catalog
    ├── nodes.json                      # 61 spatial nodes (canonical)
    ├── edges.json                      # 61 geometry-backed edges (canonical)
    ├── calibration.json                # Web Mercator registration parameters
    ├── map-review-notes.md             # Ground survey and alignment notes
    └── graph.json                      # Combined topological export
```
