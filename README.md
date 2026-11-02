# Smart Campus Navigation and Resource Optimizer

[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Build-Maven-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Academic-lightgrey.svg)]()

A foundational, extensible testbed for spatial pathfinding and campus resource optimization developed for college **Design and Analysis of Algorithms (DAA)** coursework.

This initial foundation establishes a decoupled layered architecture, a generic graph data model, persistent JSON-based data repositories, a high-performance Java Spring Boot backend, and a responsive SVG-driven campus visualization dashboard.

---

## 1. Project Overview & Architecture

The application adopts a clean, layered architecture separating concerns across the user interface, RESTful controllers, domain services, graph data structures, and persistent file storage.

```
┌────────────────────────────────────────────────────────────────────────┐
│                   Frontend Dashboard (Vanilla HTML5 / CSS3 / ES6)       │
│   • Interactive SVG Campus Map            • Live Building Directory    │
│   • Vertex & Edge Hover/Selection         • Real-Time API Inspector    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ HTTP / REST APIs (Port 8080)
┌───────────────────────────────────▼────────────────────────────────────┐
│                    Spring Boot REST Controller Layer                    │
│   • HealthController (/api/health)                                     │
│   • CampusController (/api/buildings)                                  │
│   • GraphController  (/api/graph)                                      │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                             Service Layer                              │
│   • CampusService (Domain queries, filtering, facility metadata)       │
│   • GraphService  (Graph topology, neighbor lookup, algorithm hook)    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                       Data Layer & Graph Model                         │
│   • Node (id, name, type, x, y, accessible)                            │
│   • Edge (id, source, target, weight, bidirectional, type)             │
│   • Graph (adjacency list, vertices, weighted edges)                   │
│   • Building (code, category, floors, facilities, linked nodeId)       │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ File I/O (Jackson ObjectMapper)
┌───────────────────────────────────▼────────────────────────────────────┐
│                         Storage Layer (JSON)                           │
│   • data/buildings.json        • data/nodes.json                       │
│   • data/edges.json            • data/graph.json                       │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

| Component | Technology | Rationale |
|---|---|---|
| **Backend** | Java (JDK 21+) | Type safety, object-oriented graph representation, high throughput |
| **Backend Framework** | Spring Boot 3.3.5 | Industry-standard dependency injection, embedded web server, fast bootstrapping |
| **Build System** | Maven & Maven Wrapper (`mvnw`) | Standard dependency resolution and portable builds |
| **Frontend** | HTML5, Modern CSS3, Vanilla JavaScript (ES6+) | Zero external framework overhead (no Node.js/React complexity) |
| **Map Visualization** | Scalable Vector Graphics (SVG) | Resolution-independent, interactive DOM-bindable visual representation |
| **Data Storage** | JSON Files (`Jackson ObjectMapper`) | Human-readable, portable persistence without database setup overhead |
| **Communication** | REST APIs (`JSON`) | Universal client-server protocol |

---

## 3. Directory Structure

```text
DAA-project/
│
├── README.md                           # Comprehensive documentation & API guide
├── ROADMAP.md                          # Phase-by-phase DAA algorithm integration plan
├── .gitignore                          # Git exclusions for build targets and IDEs
│
├── backend/
│   ├── pom.xml                         # Maven project configuration
│   ├── mvnw / mvnw.cmd                 # Maven wrapper scripts (Windows & POSIX)
│   ├── .mvn/                           # Maven wrapper binaries & configurations
│   │
│   └── src/
│       ├── main/
│       │   ├── java/com/campus/
│       │   │   ├── CampusApplication.java          # Spring Boot main entrypoint
│       │   │   │
│       │   │   ├── controller/
│       │   │   │   ├── HealthController.java       # GET /api/health
│       │   │   │   ├── CampusController.java       # GET /api/buildings, /api/buildings/{id}
│       │   │   │   └── GraphController.java        # GET /api/graph, /nodes, /edges
│       │   │   │
│       │   │   ├── service/
│       │   │   │   ├── CampusService.java          # Building directory queries & category filters
│       │   │   │   └── GraphService.java           # Graph query operations & DAA algorithm hooks
│       │   │   │
│       │   │   ├── model/
│       │   │   │   ├── Building.java               # Campus building entity
│       │   │   │   ├── Node.java                   # Vertex entity (id, x, y, type, accessible)
│       │   │   │   ├── Edge.java                   # Weighted edge entity (source, target, weight)
│       │   │   │   └── Graph.java                  # Generic graph structure with adjacency lists
│       │   │   │
│       │   │   ├── repository/
│       │   │   │   └── JsonDataRepository.java     # Thread-safe JSON file reader & cache
│       │   │   │
│       │   │   └── dto/
│       │   │       ├── GraphRequest.java           # DTO contract for future pathfinding queries
│       │   │       └── ApiResponse.java            # Standard generic API envelope
│       │   │
│       │   └── resources/
│       │       ├── application.properties          # Server port, data paths, static configs
│       │       └── static/
│       │           ├── index.html                  # Responsive dashboard UI
│       │           ├── css/
│       │           │   └── style.css               # Modern slate styling & SVG animation rules
│       │           ├── js/
│       │           │   ├── api.js                  # Modular REST client & traffic interceptor
│       │           │   └── app.js                  # UI event binder & SVG graph renderer
│       │           └── assets/
│       │               └── campus-map.svg          # Base campus layout & architectural vectors
│       │
│       └── test/
│           └── java/com/campus/
│               └── CampusApplicationTests.java     # Integration tests verifying all endpoints
│
└── data/
    ├── buildings.json                  # Campus building dataset (10 structures)
    ├── nodes.json                      # Spatial waypoint vertices (13 nodes)
    ├── edges.json                      # Weighted pathways & road corridors (20 edges)
    └── graph.json                      # Combined topological graph model
```

---

## 4. REST API Specification

### A. Health Monitoring Endpoint
- **URL**: `GET /api/health`
- **Description**: Returns live server status.
- **Sample Response**:
  ```json
  {
    "status": "UP",
    "application": "Smart Campus Optimizer"
  }
  ```

### B. Building Catalog Endpoints
- **URL**: `GET /api/buildings`
- **Query Parameter (Optional)**: `?category=ACADEMIC|HOSTEL|LIBRARY|FACILITY|AMENITY`
- **Sample Response**:
  ```json
  [
    {
      "id": "B003",
      "name": "Alan Turing Computer Science Block",
      "code": "ENG-CS",
      "category": "ACADEMIC",
      "nodeId": "N004",
      "description": "Faculty of Computing, Advanced AI & Algorithms Laboratories.",
      "floors": 5,
      "facilities": ["AI/ML Computing Lab", "Algorithms Lab", "Seminar Halls"]
    }
  ]
  ```

- **URL**: `GET /api/buildings/{id}`
- **Sample Response**:
  ```json
  {
    "id": "B002",
    "name": "Central University Library",
    "code": "LIB-01",
    "category": "LIBRARY",
    "nodeId": "N003",
    "description": "Four-story repository with 100,000+ volumes, digital research lab, and 24/7 reading rooms.",
    "floors": 4,
    "facilities": ["Digital Resource Lab", "Silent Study Area", "Reprographics", "Café Kiosk"]
  }
  ```

### C. Graph Model Endpoints
- **URL**: `GET /api/graph`
- **Description**: Returns complete campus topological graph including vertices and weighted edges.
- **Sample Response**:
  ```json
  {
    "graphId": "CAMPUS_MAIN_GRAPH",
    "directed": false,
    "nodeCount": 13,
    "edgeCount": 20,
    "nodes": [
      {
        "id": "N001",
        "name": "Main Entrance Gate",
        "type": "GATE",
        "x": 100.0,
        "y": 550.0,
        "accessible": true
      }
    ],
    "edges": [
      {
        "id": "E001",
        "source": "N001",
        "target": "N002",
        "weight": 180.0,
        "bidirectional": true,
        "type": "WALKWAY"
      }
    ]
  }
  ```

- **URL**: `GET /api/graph/nodes` — Returns all campus nodes.
- **URL**: `GET /api/graph/nodes/{id}` — Returns a specific node by ID.
- **URL**: `GET /api/graph/edges` — Returns all weighted pathways.

---

## 5. Getting Started & Running Locally

### Prerequisites
- **Java**: JDK 17, 21, or 24 installed.
- **Build Tool**: Apache Maven (or use the included Maven Wrapper `./mvnw`).

### Option 1: Running with Maven Wrapper (Recommended)

1. Open a terminal in the `backend` directory:
   ```bash
   cd backend
   ```

2. Run the Spring Boot application:
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

### Option 3: Running Packaged JAR

```bash
cd backend
mvn clean package -DskipTests
java -jar target/smart-campus-optimizer-1.0.0.jar
```

### Accessing the Dashboard

Once started, open your web browser and navigate to:

👉 **`http://localhost:8080`**

---

## 6. Frontend Features & Highlights

1. **Integrated Single-Server Architecture**: The Vanilla JS client and SVG assets are served directly from Spring Boot's static resource engine (`src/main/resources/static/`).
2. **Interactive SVG Campus Map**:
   - Nodes and weighted pathways are rendered dynamically over architectural blueprints.
   - Nodes are color-coded by role (Academic, Library, Hostel, Facility, Amenity, Gate, Junction).
   - Selecting a building highlights its physical footprint and associated vertex.
   - Hovering over pathways reveals real-world distance weights in meters.
3. **Real-time Live REST API Inspector**:
   - Collapsible bottom drawer monitoring every HTTP request (`GET /api/health`, `GET /api/buildings`, `GET /api/graph`).
   - Displays real-time request status, millisecond latency, and pretty-printed JSON payloads directly in the browser.
4. **Search & Category Filters**:
   - Live search across building names, codes, departments, and facilities.
   - Quick category filtering chips.

---

## 7. Next Steps for DAA Coursework

The generic graph data structure in `com.campus.model.Graph` provides built-in adjacency list indexing (`getAdjacencyList()`) and neighbor discovery methods, purpose-built for straightforward implementation of:
- **Shortest Path Algorithms**: Dijkstra's Algorithm, Bellman-Ford, A* Search.
- **Graph Traversal**: Breadth-First Search (BFS) and Depth-First Search (DFS).
- **Minimum Spanning Tree (MST)**: Prim's and Kruskal's algorithms for campus utility and fiber-optic resource routing.

See [ROADMAP.md](ROADMAP.md) for detailed implementation milestones.
