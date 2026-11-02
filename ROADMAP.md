# Project Roadmap: Smart Campus Navigation and Resource Optimizer

This roadmap outlines the planned development phases for the Design and Analysis of Algorithms (DAA) college coursework.

---

## 🎯 Phase 1: Architectural Foundation (✅ Current Milestone)
- [x] Spring Boot 3.3.x backend architecture with clean layered design (Controllers, Services, Models, Repositories).
- [x] Generic graph data model (`Node`, `Edge`, `Graph`) independent of specific campus implementations.
- [x] In-memory caching and persistent JSON repository (`buildings.json`, `nodes.json`, `edges.json`, `graph.json`).
- [x] RESTful API endpoints (`/api/health`, `/api/buildings`, `/api/graph`).
- [x] Interactive SVG campus map and responsive Vanilla JS dashboard.
- [x] Live REST API Communication Inspector for real-time verification.
- [x] Comprehensive JUnit 5 / MockMvc integration test suite.

---

## 🚀 Phase 2: Core Graph Traversals & Pathfinding (Upcoming)
### Focus: Vertex Exploration & Shortest Path Routing

1. **Graph Traversals**:
   - **Breadth-First Search (BFS)**:
     - Minimum hop routing between academic blocks.
     - Unweighted reachable zone exploration.
   - **Depth-First Search (DFS)**:
     - Cycle detection in campus pathways.
     - Full component connectivity verification.

2. **Single-Source Shortest Path (SSSP)**:
   - **Dijkstra's Algorithm**:
     - Priority Queue (Min-Heap) implementation.
     - Shortest physical walking distance routing ($O((V + E) \log V)$).
     - Step-by-step path traversal animation on the SVG map.
   - **Accessibility-Constrained Routing**:
     - Filter out non-wheelchair-accessible nodes and stairs during Dijkstra relaxation.

3. **Heuristic Search**:
   - **A* Pathfinding Algorithm**:
     - Euclidean distance heuristic based on $(x, y)$ coordinates.
     - Performance comparison: Dijkstra vs. A* node visits.

---

## ⚡ Phase 3: Minimum Spanning Tree (MST) & Resource Optimization
### Focus: Greedy Strategies & Campus Infrastructure

1. **Campus Utility & Fiber-Optic Grid Optimization**:
   - Connecting all campus buildings with minimal total cable length.
2. **Kruskal's Algorithm**:
   - Disjoint Set Union (DSU) with Union-by-Rank and Path Compression ($O(E \log E)$).
   - Visual step-by-step edge selection and cycle rejection display.
3. **Prim's Algorithm**:
   - Priority Queue-based growing tree ($O(E \log V)$).
   - Comparative runtime analysis between Prim's and Kruskal's for sparse vs. dense campus subnetworks.

---

## 🌐 Phase 4: Dynamic Obstacles & Advanced Extensions
### Focus: Real-World Campus Scenarios

1. **Dynamic Pathway Blockages**:
   - Temporarily disabling edges (e.g., construction work, event closures) and recalculating alternative routes in real time.
2. **Multi-Floor Vertical Navigation**:
   - 3D or layered graph representation connecting building stairs and elevators to campus nodes.
3. **Benchmarking & DAA Comparative Analysis Suite**:
   - Execution time, memory footprint, and vertex visit counters rendered in an interactive performance analysis tab.
