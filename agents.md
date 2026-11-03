# Smart Campus Navigation and Resource Optimizer — Project Master

> **This file is the single source of truth for all agent chats working on this project.**
> Every sub-agent chat must read this file before starting work and update the relevant sections after completing work.
> The master chat (`2868f0b7-e339-484e-9bdb-6c8c7849dd23`) is the only chat that applies changes to the codebase and pushes to Git.

---

## 🔴 GLOBAL RULES (ALL AGENTS MUST FOLLOW)

1. **NEVER push to Git unless the user explicitly says "push to git"** in the master chat.
2. All agent chats **output files as text only** — they do not write to disk. The master chat applies them.
3. All agent chats must **update the "Agent Activity Log" section** at the bottom of this file with what they did and what they output.
4. Design decisions are made **only in the master chat**. Sub-agents implement, they do not decide.
5. Do not overengineer. Simplicity and readability first.
6. The project runs entirely locally on `http://localhost:8080` via Spring Boot.
7. No React, Node.js, external databases, authentication, or unnecessary dependencies.

---

## 📋 PROJECT OVERVIEW

**Name**: Smart Campus Navigation and Resource Optimizer
**Type**: College DAA (Design and Analysis of Algorithms) coursework project
**University**: NSUT Dwarka (Netaji Subhas University of Technology), New Delhi
**GitHub**: https://github.com/Japjit-S/DAA-project.git
**Local Path**: `d:\DAA project\`

**Core Purpose**: Visualize the NSUT campus as a weighted undirected graph and demonstrate DAA algorithms (BFS, DFS, Dijkstra, Prim's, Kruskal's) on it with an interactive animated map.

---

## 🛠️ TECHNOLOGY STACK

| Component | Technology |
|---|---|
| Backend | Java 24 (JDK), Spring Boot 3.3.5 |
| Build | Apache Maven 3.9.9 (wrapper at `backend/mvnw.cmd`) |
| Frontend | Vanilla HTML5, CSS3, JavaScript (ES6+) — no frameworks |
| Map | Custom hand-crafted SVG |
| Data | JSON files in `/data/` directory |
| Communication | REST APIs (JSON) |
| Version Control | Git → GitHub |

**Maven location** (not in PATH by default): `C:\Users\JAPJIT\apache-maven-3.9.9\bin\`
**Run command**: `cd backend && .\mvnw.cmd spring-boot:run`
**App URL**: `http://localhost:8080`

---

## 📁 PROJECT STRUCTURE

```
DAA-project/
├── agents.md                           ← THIS FILE (project master)
├── README.md
├── ROADMAP.md
├── .gitignore
│
├── backend/
│   ├── pom.xml                         (Java 21 target, Spring Boot 3.3.5)
│   ├── mvnw / mvnw.cmd
│   └── src/
│       ├── main/
│       │   ├── java/com/campus/
│       │   │   ├── CampusApplication.java
│       │   │   ├── controller/
│       │   │   │   ├── HealthController.java      GET /api/health
│       │   │   │   ├── CampusController.java      GET /api/buildings, /api/buildings/{id}
│       │   │   │   └── GraphController.java       GET /api/graph, /nodes, /edges
│       │   │   ├── service/
│       │   │   │   ├── CampusService.java
│       │   │   │   └── GraphService.java
│       │   │   ├── model/
│       │   │   │   ├── Building.java
│       │   │   │   ├── Node.java
│       │   │   │   ├── Edge.java
│       │   │   │   └── Graph.java                 has getAdjacencyList(), getNeighbors()
│       │   │   ├── repository/
│       │   │   │   └── JsonDataRepository.java    reads ../data/*.json
│       │   │   └── dto/
│       │   │       ├── GraphRequest.java
│       │   │       └── ApiResponse.java
│       │   └── resources/
│       │       ├── application.properties         port=8080, data.dir=../data
│       │       └── static/                        ← served at http://localhost:8080
│       │           ├── index.html                 main dashboard
│       │           ├── css/style.css
│       │           ├── js/
│       │           │   ├── api.js                 REST client + interceptor
│       │           │   ├── app.js                 map renderer + event binder
│       │           │   └── calibration.js         GPS ↔ map transform
│       │           └── assets/
│       │               ├── campus-map.svg         legacy vector reference
│       │               └── maps/                  local OSM background panels
│       └── test/
│           └── java/com/campus/
│               └── CampusApplicationTests.java    (4 tests, all passing)
│
└── data/
    ├── buildings.json                  (35 NSUT facilities)
    ├── nodes.json                      (61 facilities and waypoints)
    ├── edges.json                      (61 geometry-backed walking edges)
    ├── calibration.json                (OSM panel registration and checks)
    ├── map-review-notes.md             (calibration and path uncertainties)
    └── graph.json                      (combined graph export)
```

---

## 🗺️ MAP STATUS

### Current State
- **Campus data**: 35 NSUT building records, 61 graph nodes, and 61 geometry-backed walking edges
- **Map background**: Local OpenStreetMap overview plus four registered detail panels; attribution is visible
- **Graph overlay**: GPS-projected landmarks and paths are rendered in separate SVG layers
- **Duplicate export**: `map (6).png` is preserved at the project root and omitted from the display because it nearly duplicates `map (2).png`

### Target State
- **Campus data**: Review approximate map centers and facility access points against campus survey data
- **Map**: Retain the registered local OSM background and refine any offsets with surveyed control points
- **Graph**: Review the marked low-confidence building access links and path turns

### SVG Map Architecture (IMPORTANT)
The inline SVG in `index.html` has two reserved groups at the bottom:
```html
<g id="graphEdgesLayer"></g>   ← JS renders edge lines here
<g id="graphNodesLayer"></g>   ← JS renders node circles here
```
The static SVG is the visual background. JS dynamically overlays the graph.
DO NOT remove these two groups when replacing the SVG.

### Node Color Scheme (in app.js)
```js
const TYPE_COLORS = {
  GATE:     '#94a3b8',
  ACADEMIC: '#3b82f6',
  LIBRARY:  '#a855f7',
  AMENITY:  '#f59e0b',
  FACILITY: '#10b981',
  HOSTEL:   '#eab308',
  JUNCTION: '#06b6d4',
  ADMIN:    '#6366f1',
  SPORTS:   '#22c55e',
  PARKING:  '#64748b'
}
```

---

## 🔌 REST API REFERENCE

| Method | Endpoint | Returns |
|---|---|---|
| GET | `/api/health` | `{"status":"UP","application":"Smart Campus Optimizer"}` |
| GET | `/api/buildings` | Array of all Building objects |
| GET | `/api/buildings?category=ACADEMIC` | Filtered buildings |
| GET | `/api/buildings/{id}` | Single Building by ID |
| GET | `/api/graph` | Full Graph object (nodes + edges) |
| GET | `/api/graph/nodes` | Array of all Node objects |
| GET | `/api/graph/nodes/{id}` | Single Node by ID |
| GET | `/api/graph/edges` | Array of all Edge objects |

### Data Model Schemas

**Node**:
```json
{ "id":"N001", "name":"Main Gate", "type":"GATE", "x":100.0, "y":450.0, "accessible":true }
```

**Edge**:
```json
{ "id":"E001", "source":"N001", "target":"N002", "weight":185.0, "bidirectional":true, "type":"WALKWAY" }
```

**Building**:
```json
{ "id":"B001", "name":"Old Academic Block", "code":"OAB", "category":"ACADEMIC", "nodeId":"N003", "description":"...", "floors":4, "facilities":["Seminar Hall","Labs"] }
```

**Graph**:
```json
{ "graphId":"NSUT_MAIN_GRAPH", "directed":false, "nodeCount":13, "edgeCount":20, "nodes":[...], "edges":[...] }
```

---

## 🧩 PENDING TASKS

| # | Task | Status | Assigned To |
|---|---|---|---|
| 1 | Collect NSUT GPS coordinates via ChatGPT computer use | ✅ Completed (2026-11-03) | User (external) |
| 2 | Process GPS coordinates → nodes.json, edges.json, buildings.json, graph.json | ✅ Completed (2026-11-03) | Sub-agent (coordinate processor) |
| 3 | Build rough working NSUT SVG map from GPS data | ✅ Completed (2026-11-03) | Sub-agent (coordinate processor) |
| 4 | Design decision: visual style for final map | 🔄 In Progress | Master chat |
| 5 | Build proper polished NSUT SVG map (geometry, footprints, roads) | ✅ Completed (2026-11-03) | Sub-agent (map designer) |
| 6 | Build animation layers & interactions for map | ✅ Completed (2026-11-03) | Sub-agent (map designer) |
| 7 | Implement BFS / DFS traversal with step animation | 🌐 External | Other contributor on GitHub |
| 8 | Implement Dijkstra shortest path with route animation | 🌐 External | Other contributor on GitHub |
| 9 | Implement Prim's / Kruskal's MST with animation | 🌐 External | Other contributor on GitHub |

---

## ✅ COMPLETED TASKS

| Task | Completed | Notes |
|---|---|---|
| Project architecture & directory structure | ✅ 2026-11-02 | Full layered Spring Boot + Vanilla JS setup |
| Backend Java models (Node, Edge, Graph, Building) | ✅ 2026-11-02 | Generic, DAA-algorithm-ready graph structure |
| REST API controllers (Health, Campus, Graph) | ✅ 2026-11-02 | All endpoints tested and working |
| JSON data repository with file-based persistence | ✅ 2026-11-02 | Auto-resolves `../data/` directory |
| Placeholder campus data (nodes.json, edges.json etc.) | ✅ 2026-11-02 | Generic, will be replaced with NSUT data |
| Frontend dashboard (index.html, style.css) | ✅ 2026-11-02 | Dark slate, responsive, building sidebar |
| REST API client + live interceptor (api.js, app.js) | ✅ 2026-11-02 | Real-time API inspector drawer |
| SVG graph renderer (nodes + edges from API) | ✅ 2026-11-02 | Dynamic rendering on top of SVG base |
| Integration test suite (4 tests) | ✅ 2026-11-02 | MockMvc, all passing |
| Maven build & package | ✅ 2026-11-02 | JAR verified working at localhost:8080 |
| Initial Git commit and push to GitHub | ✅ 2026-11-02 | Branch: main |

---

## 🎨 DESIGN DECISIONS LOG

| Decision | Status | Choice |
|---|---|---|
| Map visual style | ✅ DECIDED | OpenStreetMap Standard raster with separate SVG graph overlays |
| Animation priority | ⏳ PENDING | Algorithm step-vis vs. ambient idle animations |
| Node detail level | ✅ DECIDED | High detail — ~30-35 NSUT-specific nodes |
| Building names | ✅ DECIDED | Real NSUT names (Old Block, New Block, SAC, BH-1...) |
| Map reference source | ✅ DECIDED | User-supplied OpenStreetMap Standard exports |
| SVG construction method | ✅ DECIDED | SVG overlays over registered local raster panels |

---

## 📜 AGENT ACTIVITY LOG

> Sub-agents: append an entry here after completing work. Format:
> `[DATE] [AGENT ROLE] — What was done, what files were output`

- `[2026-11-02] Master Chat` — Initial project foundation built. All backend + frontend scaffolding created. Placeholder data files. Committed and pushed to GitHub.
- `[2026-11-02] Master Chat` — Ideation session for NSUT map. Decided on real satellite layout, hand-crafted SVG, ~30-35 nodes, real NSUT names. Design style TBD.
- `[2026-11-03] Master Chat` — Created agents.md project master. Defined coordinate-processor sub-agent prompt. No push (awaiting user confirmation).
- `[2026-11-03] Coordinate Processor Sub-Agent` — Processed NSUT Dwarka GPS coordinates into 41 nodes (N001–N041), 51 connected edges (E001–E051) with Haversine weights, 35 buildings (B001–B035), integrated graph.json, and the 1200x900 SVG campus map.
- `[2026-11-03] SVG Cartographer & Map Designer` — Engineered production-grade NSUT campus SVG map (1200x900) featuring realistic architectural footprints, dual-lane arterial boulevard, hierarchical road and pedestrian network, sports complex (400m track, cricket oval, courts), landscaping, and ambient CSS keyframe animations, maintaining strict coordinate parity with nodes N001–N041.
- `[2026-11-03] Master Chat (Phase 1)` — Normalized NSUT campus landmark data; added geographic lat/lng to Node model and nodes.json while preserving SVG x/y; applied 12 exact user GPS coordinate overrides; re-architected JsonDataRepository to compose Graph strictly from canonical nodes.json and edges.json; sanitized BOMs across all JSON files.
- `[2026-11-03] Master Chat (Phase 2)` — Calibrated NSUT campus map and established coordinate transforms: generated data/calibration.json with documented canvas dimensions (1200x900), ground metrics (828.4m x 816.8m, aspect ratio 1.014), projection formula, and 14 validated control points (residuals < 0.07px); created frontend calibration.js transform module with Haversine distance and forward/inverse projections; updated app.js and index.html to derive positions from GPS coordinates with fallback; preserved existing cartographic SVG map pending georeferenced satellite raster acquisition.
- `[2026-11-03] Master Chat (Phases 2–3)` — Registered four overlapping OSM detail panels and the overview in Web Mercator; identified `map (6).png` as a near-duplicate of `map (2).png`; added local map backgrounds, aspect-preserving fit/zoom/pan controls, geometry-backed edges, and review confidence; updated graph to 61 nodes and 61 edges while preserving the supplied GPS values. Reviewed the updated app and APIs at localhost:8080. Manual review notes: `data/map-review-notes.md`. No commit or push.
- `[2026-11-03] Master Chat (Phase 4)` — Validated and served the NSUT campus graph: implemented comprehensive startup & on-demand validation (CampusDataValidator, ValidationReport, ValidationIssue, DataValidationException) checking IDs, coordinates, geometry endpoint alignment (max deviation 0.0006m), Haversine edge weights (max deviation 0.049m), building-to-node links, single connected component (1 component, 61 nodes), and confidence tracking; synchronized graph.json export; added edge lookup by ID (`/api/graph/edges/{id}`) and validation report endpoint (`/api/graph/validation`); enforced client error handling for invalid algorithm request node IDs; expanded Spring Boot MockMvc integration test suite to 14/14 passing tests. No commit or push.
- `[2026-11-03] Master Chat (Phase 5)` — Completed interactive map and facilities directory: implemented bidirectional marker-directory selection with map camera focus, multi-field search and category filtering with clear controls, keyboard navigation (Enter/Space, focus rings) across map markers and directory cards, comprehensive 10-item legend, incident pathway connection links with metric distances, low-confidence access notices, and responsive mobile tab switcher. 14/14 tests passing. No commit or push.
- `[2026-11-03] Master Chat (Phase 6)` — Implemented campus route finding (BFS & Dijkstra): added route-finding panel with origin/destination selection, endpoint swapping, algorithm choice with comparative guidance, wheelchair toggle, and input validation; highlighted routes along ordered edge geometry with direction-aware geometry reversal; added route step-by-step turn list, metrics (distance, hops, explored nodes, time), low-confidence pathway advisories, animated dashed SVG route overlay, non-route de-emphasis, and start/end beacon highlights; fortified POST /api/graph/execute and GET endpoints to reject missing/blank required node IDs; expanded MockMvc integration tests to 17/17 passing tests. No commit or push.
- `[2026-11-04] Master Chat (Phase 7)` — Implemented BFS vs Dijkstra comparative demonstration: added side-by-side comparison action and layout with parallel execution via POST /api/graph/execute; visual dual-overlay map rendering (solid cyan Dijkstra vs dashed amber BFS with distinctive dual-tone pattern on shared segments); dynamic layer visibility filters (Both, Dijkstra Only, BFS Only); key algorithmic insight analysis computing exact metric trade-offs (distance in meters vs hops in graph edges); one-click demo pair shortcut (Main Gate to Block 5 demonstrating 225.9 m / 5 hops Dijkstra vs 226.7 m / 4 hops BFS); verified turn lists, low-confidence advisories, and expanded automated MockMvc test suite to 18/18 passing tests. No commit or push.
- `[2026-11-04] Master Chat (Phase 8)` — Completed final integration review, polish, and documentation: conducted end-to-end audit across all UI flows and REST APIs; verified map rendering over registered local OpenStreetMap panels with zero runtime tile downloads; audited graph topology discovering circuit rank M=1 explaining identical delta (0.80 m / 1 hop) across all 280 divergent pairs; rigorously verified Main Gate (N001) to Block 5 (N005) demo pair metrics against canonical edges.json (sum of edges matches route weights to 4 decimal places); clarified that cycle edges E010, E011, E012 have MEDIUM confidence while only shared access path E033 is LOW confidence; refined takeaway wording in app.js to accurately present the 0.8 m difference as theoretical graph model divergence rather than perceptible real-world saving; fully updated README.md with setup, model, algorithms, APIs, calibration, and known limits; marked all checklist items in PROJECT_PHASES.md; all 18 integration tests passing (18/18). No commit or push.
