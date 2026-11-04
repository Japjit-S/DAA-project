# NSUT Campus Walking Network

Local campus map and walking-network demonstrations for Netaji Subhas University of Technology, Dwarka. The app is served by Spring Boot at `http://localhost:8080`; the interface uses plain HTML, CSS, and JavaScript.

## Run locally

Requirements: Java 21 or newer. From `backend/` run:

```powershell
.\mvnw.cmd spring-boot:run
```

Open `http://localhost:8080`. Run the integration suite with:

```powershell
.\mvnw.cmd test
```

The current suite passes **18/18 tests**.

## Map and walking graph

The background is the five-view, locally registered OpenStreetMap set in `backend/src/main/resources/static/assets/maps/`: one overview and four detail panels. The map opens in its original colors by default, with a custom neon cartographic dark mode available from the map controls. The dark mode uses pre-rendered, high-resolution raster panels (`map-*-dark.png`) featuring obsidian surroundings (`#070b14`), vivid emerald green lawns, luminous cyan roads, glowing amber paths, and building footprints rendered in a muted copper fill with a warm glowing outline (`#8a4320` / `#ea580c`). OpenStreetMap labels and attribution remain crisp and fully legible without any muddy CSS dimming or color-inversion filters. No map tiles are fetched at runtime.

`data/nodes.json` contains 135 records: 124 routable gates, road or walkway junctions, bends, and access points, plus 11 non-routable reference landmarks. `data/edges.json` contains 134 undirected road and walkway segments. The routable graph is connected and has cycles around the academic core, the residential hostel sector, the west campus boundary, the ring road around APJ and Smart Class, and the sports complex. Every segment stores ordered GPS geometry whose first and last points match its endpoint nodes; the weight is the sum of Haversine distances along that geometry.

The campus walking network was thoroughly repaired against the supplied OpenStreetMap panels:
- **TNP Area & Sports West Entrance**: Moved TNP Road North Junction (`N144`) north to the actual road junction (`x=2420, y=1530`), placed Sports West Entrance (`N054`) on its visible road access (`x=2480, y=1470`), and connected them along the mapped road (`E016`, `E017`) while South Campus Road (`E092`) cleanly follows the pavement north of the TNP building without crossing its footprint.
- **Library Parking Road to F-Tech Front Road East**: Moved F-Tech Front Road East (`N061`) to the physical junction (`x=2345, y=930`), connected Library Parking Road (`E260`) straight south to `N181`, and routed connector `E240` from `N181` along the curved road to Sports Complex West Junction `N040` (relocated to the paved road centerline at `x=2448, y=1330`).
- **Sports Complex Geometry**: Relocated `N040` onto the road centerline and refined `E018`, `E019`, and `E101` to follow the curved paved road and court access without intersecting lawn or court boundaries.
- **Central Library Footprint Bypass (`E239`)**: Re-routed completely clear of the Central Library footprint along visible pavement from north plaza junction `N051` via `(2205.0, 1215.0)` and `(2315.0, 1165.0)` to meet Library Parking at `N181`.
- **Hostel South Road to Campus West Road Connection (`E293`)**: Added verified paved connection from Campus West Road (`N160`) past Water Softening Plant directly into Aryabhata south road (`N138`).
- **Main Gate to North Gate Internal Road**: Main Gate (`N001`) is positioned at the Guard House barrier. The gate connection runs along the parallel paved road immediately inside the campus wall (via `N152`, `N157`, `N153`, `N158`, `N159`, `N160`, `N161`, and `N043`) instead of the public highway outside.
- **Academic & Admin Core**: Building node `N006` for Block 6 is positioned inside the central courtyard and accessed via the southeast entrance approach from Central Fountain (`N039`) without cutting through walls; Block 8A (`N007`) is placed inside its footprint and connected to the north plaza; South Campus Road Junction (`N053`) is aligned with the physical T-junction and curves smoothly past Electric Substation into Faculty Housing.
- **APJ, Smart Class & Shopping Complex**: Slicing chords through APJ were replaced with a complete **Ring Road** wrapping around the complex; destination nodes were added for Smart Class (`N191`), Shopping Complex (`N192`, with direct connector to hostel road), and Guest House (`N190`).
- **F-Tech Cluster & Hostel Spine**: Preserved the rear pedestrian walkway for the F-Tech block and the complete paved road network for BH-1 through BH-5, Kaveri road, and the eastern Girls Hostel corridor.

### Building access

All 38 facilities in `data/buildings.json` are resolved to valid, routable `nodeId`s (0 unresolved). Facilities with approximate entrance positions from the map imagery are clearly flagged with `accessConfidence: "LOW"` and explanatory `accessNote`s. Routes terminate at these verified access points rather than drawing unverified lines into or across building interiors.

## Directions and algorithm demonstrations

The **Directions** tab offers:

- **Dijkstra** for the shortest modeled distance.
- **BFS** for the fewest graph segments.
- A side-by-side comparison of those two point-to-point routes with metric trade-off analysis.

The separate **Algorithms** tab features an **interactive, pedagogical animation engine**:
- **DFS Traversal**: Visualizes depth-first exploration with an active node indicator (`.dfs-active`), visited nodes (`.dfs-visited`), forward traversal paths (`.algorithm-edge-traversed`), and backtracking edges (`.algorithm-edge-backtrack`).
- **Kruskal's MST**: Demonstrates greedy minimum spanning tree construction by inspecting edges in ascending weight order. Shows the currently evaluated edge (`.mst-edge-considering`), accepted tree edges (`.mst-edge-accepted`), and highlights cycle-causing **rejected edges in crimson red** (`.mst-edge-rejected`).
- **Prim's MST**: Demonstrates cut-based tree growth from a start node, highlighting candidate boundary cut edges (`.mst-candidate-cut`) before accepting the minimum-weight connection.
- **Full Playback Controls**: Both DFS and MST visualizations include Play, Pause, Step Forward (`⏭`), Step Backward (`⏮`), Reset (`↺`), speed switching (`1x`, `2x`, `5x`), and a real-time progress scrubbing slider.

Displayed routes use the stored segment geometry. Ordinary network lines use a subdued blue-green stroke; selected Dijkstra routes use a slightly thicker dark red with high-contrast white casing, BFS routes use amber dashes, and completed DFS/MST branches use dark green with a luminous outline.

## REST API

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/health` | App status |
| `GET` | `/api/buildings` | All 38 places |
| `GET` | `/api/buildings/{id}` | One place |
| `GET` | `/api/graph` | Routable graph, 124 nodes and 134 edges |
| `GET` | `/api/graph/nodes` | Routable nodes |
| `GET` | `/api/graph/edges` | Geometry-backed segments |
| `GET` | `/api/graph/validation` | Data and graph validation report |
| `POST` | `/api/graph/execute` | Execute an algorithm |

Example request:

```json
{
  "algorithm": "DIJKSTRA",
  "startNodeId": "N001",
  "endNodeId": "N090",
  "wheelchairAccessibleOnly": false,
  "blockedEdgeIds": []
}
```

Supported algorithm values include `BFS`, `DFS`, `DIJKSTRA`, `PRIMS`, and `KRUSKALS`.

## Data review notes and limitations

See [`data/map-review-notes.md`](data/map-review-notes.md) for edge groups changed, access confidence, and map ambiguities. The supplied imagery is OpenStreetMap cartography, not aerial or satellite imagery. It is enough to trace mapped roads and footways, but it cannot prove current barriers, whether every mapped surface is pedestrian-accessible, or the exact entrance for every building. The route model deliberately leaves unsupported access links out instead of drawing across lawns, courts, or footprints.

## Project layout

```text
backend/src/main/resources/static/   HTML, CSS, JavaScript, local map panels
backend/src/main/java/com/campus/   Spring Boot APIs and algorithms
backend/src/test/                   MockMvc integration tests
data/buildings.json                 Place directory and access confidence
data/nodes.json                     Canonical route points and landmark references
data/edges.json                     Walking segments with ordered geometry
data/graph.json                     Combined graph export
data/map-review-notes.md            Registration, repair, and uncertainty notes
```
