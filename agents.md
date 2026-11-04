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
| Map | Five registered local OpenStreetMap panels with SVG graph overlays |
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
│       │           │   ├── api.js                 Campus REST client
│       │           │   ├── app.js                 map renderer + event binder
│       │           │   └── calibration.js         GPS ↔ map transform
│       │           └── assets/
│       │               ├── campus-map.svg         legacy vector reference
│       │               └── maps/                  registered local campus map panels
│       └── test/
│           └── java/com/campus/
│               └── CampusApplicationTests.java    (4 tests, all passing)
│
└── data/
    ├── buildings.json                  (35 NSUT facilities)
    ├── nodes.json                      (119 records: 84 routable, 35 references)
    ├── edges.json                      (85 geometry-backed walking edges)
    ├── calibration.json                (map calibration and checks)
    ├── map-review-notes.md             (calibration and path uncertainties)
    └── graph.json                      (combined graph export)
```

---

## 🗺️ MAP STATUS

### Current State
- **Campus data**: 35 places, 84 routable nodes, 35 non-routable landmark references, and 85 geometry-backed segments
- **Map background**: Registered local OSM overview and four detail panels; original colors are the default and an optional dark treatment is available
- **Graph overlay**: GPS-projected walking geometry follows mapped roads and walkways; selected routes use distinct cyan or amber strokes
- **Topology**: One connected component, with the academic-core loop and a mapped sports-perimeter loop
- **Uncertain access**: 23 approximate building access nodes are marked `LOW`; 12 facilities remain searchable but unroutable until their access is supported
- **Duplicate export**: `map (6).png` is preserved at the project root and omitted from the display because it nearly duplicates `map (2).png`

### Target State
- **Campus data**: Replace approximate entrances with surveyed access points when a campus survey is available
- **Graph**: Resolve the 12 destinations with no supported path entrance and verify map-derived low-confidence approaches on foot

### Map Architecture (IMPORTANT)
The inline SVG in `index.html` has reserved map-image and runtime groups at the bottom:
```html
<g id="mapRasterLayer"></g>      ← JS loads registered local map panels
<g id="vectorRoadLayer"></g>     ← JS draws geometry-backed graph paths
<g id="graphEdgesLayer"></g>   ← JS renders edge lines here
<g id="graphNodesLayer"></g>   ← JS renders node circles here
```
JS positions the locally saved map panels and graph paths against the calibrated GPS coordinates. Keep the runtime graph groups.

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
{ "id":"N001", "name":"Main Gate", "type":"GATE", "x":1763.0, "y":1390.0, "lat":28.6095, "lng":77.0365, "accessible":true }
{ "id":"N004", "name":"Block 4 landmark reference", "type":"ACADEMIC", "routable":false, "lat":28.61, "lng":77.037 }
```

**Edge**:
```json
{ "id":"E062", "source":"N002", "target":"N063", "weight":21.53, "bidirectional":true, "type":"MAIN_ROAD", "geometry":[...], "confidence":"MEDIUM" }
```

**Building**:
```json
{ "id":"B002", "name":"Block 4", "code":"BLK-4", "category":"ACADEMIC", "nodeId":"N089", "referenceNodeId":"N004", "accessConfidence":"LOW", "accessNote":"..." }
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
| 4 | Final map presentation: registered OSM imagery, original colors by default | ✅ Completed (2026-11-04) | Master chat |
| 5 | Repair the road and walkway graph against supplied map panels | ✅ Completed (2026-11-04) | Master chat |
| 6 | Review route geometry, access confidence, map fit, and detailed zoom | ✅ Completed (2026-11-04) | Master chat |
| 7 | Keep BFS routes and expose separate DFS traversal | ✅ Completed (2026-11-04) | Master chat |
| 8 | Keep Dijkstra route finding and route comparison | ✅ Completed (2026-11-04) | Master chat |
| 9 | Expose Prim and Kruskal minimum-spanning-tree demos separately | ✅ Completed (2026-11-04) | Master chat |

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
| Map visual style | ✅ DECIDED | Registered campus map panels in original colors by default, with optional dark treatment |
| Animation priority | ⏳ PENDING | Algorithm step-vis vs. ambient idle animations |
| Node detail level | ✅ DECIDED | Road and walkway network with 84 routable points plus 35 retained facility references |
| Building names | ✅ DECIDED | Real NSUT names (Old Block, New Block, SAC, BH-1...) |
| Map reference source | ✅ DECIDED | Five registered local OpenStreetMap panels and canonical GPS coordinates |
| Map construction method | ✅ DECIDED | Local OSM imagery with geometry-backed SVG road, walkway, and node overlays |

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

- `[2026-11-04] Master Chat (UI/UX Overhaul)` — Replaced the raster-backed dashboard with a restrained dark interface, pure SVG campus scene, GPS-registered building footprints, and geometry-backed walking paths; simplified Places and Directions controls; removed visible API diagnostics and DAA readiness copy; connected place cards and map footprints with hover selection; updated README and map status. Maven tests: 18/18 passing. No commit or push.
- `[2026-11-04] Master Chat (Map Restoration)` — Restored the five registered local campus map panels with dark/original color switching; placed all 61 canonical graph paths and 61 nodes over the map; corrected map and README descriptions to match the panel-based map. Browser review confirmed the full mapped campus road network is visible in the background; Maven tests: 18/18 passing. No commit or push.
- `[2026-11-04] Master Chat (Walking Network Repair)` — Rebuilt the campus network against all five registered OSM panels: removed lawn/nursery shortcut chords, repositioned the diagonal south-road junctions, added the mapped faculty and library-to-SAC approaches, and traced the sports perimeter outside fields, courts, and the electrical substation. The canonical graph now has 84 routable nodes and 85 geometry-backed edges (one component, cycle rank 2); 23 facilities use explicit low-confidence access points and 12 remain unresolved. Softened ordinary road lines and route labels, kept original map colors by default, refreshed README and map-review notes, and reviewed campus-fit plus detailed gate-to-academic, hostel, and sports-perimeter routes in Chrome. DFS visited all 84 points; Prim and Kruskal each returned 83 tree segments; Maven tests passed 18/18. No commit or push.
- `[2026-11-04] Master Chat (Hostel Spine Repair & Algorithm Animation Engine)` — Completed thorough repair of hostel area paths and algorithm animations: eliminated all straight chords cutting through hostel building footprints (BH-1, BH-2, BH-3, BH-4, BH-5) by routing along North Road, Central Walkway Spine, and South Road with short entrance spurs; mapped the paved north road directly to Kaveri Hostel entrance (N143); established the east road and cul-de-sac spurs serving TNP Cell (B009/N145), Alaknanda (B026/N100), and Saraswati (B025/N118); resolved all 35 campus facilities to routable nodes (0 unresolved). Built interactive step-by-step DAA algorithm animation engine for DFS traversal (active node, traversal stack, backtrack edges), Kruskal's MST (edge consideration, candidate cuts, cycle-creating rejection in crimson red), and Prim's MST (cut boundary exploration and tree expansion) with play/pause, step forward/backward, slider scrubbing, and speed toggles (1x, 2x, 5x). Graph now has 82 routable nodes, 86 geometry-backed edges, 0 validation errors. Maven tests: 18/18 passing. No commit or push.
- `[2026-11-04] Master Chat (Main Gate, F-Tech & Academic Core Repair)` — Relocated Main Gate (N001) to the true Guard House location on Azad Hind Fauj Marg (x=1505, y=1666, lat=28.6083174, lng=77.0351765); mapped the road-following corridor along Azad Hind Fauj Marg between Main Gate and North Gate (N002) with 5 intermediate bends (570.85m, 6 hops); routed entrance boulevard through Guard House security past Bose Statue around Nescii Lawn to the Auditorium roundabout; rebuilt the F-Tech cluster to approach exclusively from the visible rear/west pedestrian walkway with short entrance spurs to Fashion Technology Block (N008), Medical Centre (N030), and Central Workshop while preserving the front road for vehicles; audited and repaired the central academic core, replacing diagonal lawn chords (Moksha Ground E008, Library lawn E024, Connecting Block crossing E012) with authentic walkways radiating from Central Fountain (N039) to Admin (N003), Block 6 (N006), Block 8A (N007), Block 4 (N004), Block 5 (N005), and Central Library (N012). Graph now has 118 total nodes (107 routable), 116 geometry-backed edges, 0 validation errors/warnings, and 18/18 MockMvc tests passing. No commit or push.
- `[2026-11-04] Master Chat (Final Walking Network Corrections)` — Completed final NSUT walking network corrections: moved Main Gate to North Gate route to the parallel internal campus perimeter road inside the boundary wall (N001 -> N152 -> N157 -> N153 -> N158 -> N159 -> N160 -> N161 -> N043 -> N002, 576.62m, 9 hops) eliminating the outside public highway route and lawn chords E219/E220; repositioned Block 6 node N006 into the central/northeast courtyard (x=2045, y=1180) approached via southeast entrance walkway N193 from Central Fountain N039 without piercing outer walls, and mapped northern walkway N194 -> N204 -> N205 replacing diagonal lawn chord E230; placed Block 8A node N007 inside its footprint (x=2035, y=1360) connected via north plaza spur N180; shifted South Campus Road Junction N053 southwest to the physical T-junction (x=2160, y=1610) and mapped curved geometry past Electric Substation into Faculty Housing replacing straight chord E262; added Guest House node N190 (B036); established continuous Ring Road around APJ Kalam Complex and Smart Class, eliminating chords cutting through APJ; added Smart Class destination node N191 (B037) and Shopping Complex destination node N192 (B038) with northern connector road to hostel road N140. Graph now has 136 total nodes (125 routable), 134 geometry-backed edges, 38 buildings, single connected component, 0 validation errors/warnings, and 18/18 MockMvc tests passing. No commit or push.
- [2026-11-04] Master Chat (Campus Network Follow-up) — Removed the unsupported dead-end Hostel South Road west-turn node N067 and dangling edge E082; removed the 485.59 m Water Softening Plant–Sewage Treatment Plant chord E069; added upper north-road connection E292 between N132 and N133 with ordered geometry (135.39 m); refreshed node labels with node IDs on hover and adjusted the optional dark map to a restrained neon-green lawn / charcoal base. Documented that the Central Library perimeter route and a mapped south-road-to-gate link still need imagery-backed path tracing. No commit or push.
- `[2026-11-04] Master Chat (Neon Dark Map Polish & Walking Network Refinement)` — Replaced CSS dimming/grayscale filter with deliberate neon cartographic raster panels (`map-*-dark.png`) featuring near-black surroundings (`#070b14`), vivid neon emerald lawns (`#059669` / `#10b981`), luminous cyan roads, glowing amber paths, and dark slate buildings (`#1e293b`) while preserving crisp OSM labels and attribution; re-routed `E239` around Central Library footprint along visible pavement; confirmed and added unbroken Hostel South Road connection `E293` between `N160` and `N138`; adjusted `N133` and curved road geometry of `E292`/`E091` above Raman to Substation 2; verified `N067`/`E082` and chord `E069` remain removed. Graph has 135 nodes (124 routable), 134 geometry-backed edges, 38 buildings, single connected component, 0 validation errors/warnings, and 18/18 MockMvc tests passing. No commit or push.
- `[2026-11-04] Master Chat (Final NSUT Map and Route Polish)` — Completed TNP area repair by moving TNP Road North Junction N144 to physical junction (x=2420, y=1530) and routing South Road E092 north of TNP building without crossing it; placed Sports West Entrance N054 on visible road access (x=2480, y=1470) and connected N144-N054 (E016) and N054-N056 (E017); moved F-Tech Front Road East N061 to physical junction (x=2345, y=930) and connected Library Parking road E260 and N040 connector E240 without chords; adjusted sports perimeter curves E018 and E019; changed selected shortest route (Dijkstra) to thicker dark red (#991b1b) with high-contrast white casing and directional animation; applied dark green (#065f46) with luminous outline to completed traversal branches (DFS/MST); refined dark map raster panels with muted copper/terracotta fills (#8a4320) and warm outlines (#ea580c) for buildings against emerald lawns. All 18 MockMvc integration tests passing (18/18). Committed and pushed to Git per explicit user authorization.
