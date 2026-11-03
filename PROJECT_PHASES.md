# NSUT Campus Navigation Project Roadmap

## Project goal

Build a polished, manageable Design and Analysis of Algorithms class project: an interactive map of the NSUT Dwarka campus, a realistic road and walkway graph, a searchable facilities directory, and working graph-algorithm demonstrations.

The project should be complete and presentable without growing into a production GIS or campus operations system.

## Current project baseline

- Backend: Java and Spring Boot, with controllers, services, graph models, and JSON-backed data.
- Frontend: HTML, CSS, and vanilla JavaScript served by Spring Boot.
- Map: an SVG campus map and JSON data files currently exist.
- Data files: `data/nodes.json`, `data/edges.json`, `data/buildings.json`, and `data/graph.json`.
- Existing working changes include a 41-node/51-edge draft and map/design prompts. Inspect and preserve these changes; do not reset, replace, or silently discard them.
- Make `nodes.json`, `edges.json`, and `buildings.json` the editable source of truth. The current `JsonDataRepository` separately loads all three and then loads `graph.json` if present, which can silently make the API serve a different graph from the node/edge files. Change this in Phase 1: compose the in-memory `Graph` from nodes and edges. Keep `graph.json` only as a generated export if something needs it, or deprecate it after checking all consumers. Never maintain it manually as a second source of truth.

## Data decisions already established

- The Central Library, SAC, and Gym are one building/node.
- The four core academic blocks are Blocks 4, 5, 6, and 8A (Connecting). F-Tech is a separate building, not the second Department of Design building.
- There is one Department of Design building.
- Boys hostels include Aryabhata, Ramanujan, J. C. Bose, Raman, and Bhaskara.
- Girls hostels include Saraswati, Alaknanda, and Kaveri.
- Sports landmarks include the running track, cricket ground, basketball court, volleyball court, and lawn tennis courts.
- Coordinates supplied by the user should be used as the source values for the facilities they identify. Preserve their decimal precision in the data.
- The North Gate has a Google Maps place pin. The Main Gate and the campus bounding box still have approximate coordinates; keep that uncertainty explicit until verified.
- Do not invent names, access details, road geometry, or coordinates. Mark uncertain facts for review.

## Working rules for Antigravity

Use one phase prompt at a time. Before editing, inspect the current files and summarize the relevant implementation. Make the smallest coherent change for the phase, preserve unrelated work, and report files changed, decisions made, assumptions, and remaining uncertainties.

Keep the current technology stack unless a concrete problem requires a change. Avoid adding a database, frontend framework, authentication, live traffic, multi-floor routing, or other production features that do not serve the class project.

Do not draw route edges directly between building centers. Road and walkway edges must follow the campus paths through ordered waypoints. A building landmark should connect to the graph at its entrance or nearest path node.

## Phases

### Phase 0 — Audit the current implementation

**Goal:** Establish what already works and align the plan with the actual codebase.

**Tasks:**
- Inspect the existing frontend, backend endpoints, data models, SVG, JSON files, and current uncommitted changes.
- Identify duplicate or stale graph representations and document which files are authoritative.
- Run the app using its existing instructions and check the current map and API behavior.
- Report inconsistencies without rewriting unrelated files.

**Done when:** There is a short verified inventory of the current app, known issues, data contracts, and safe next edits.

**Give Antigravity:** This roadmap and the repository. Ask it to audit first and not modify files in this phase unless a small blocker prevents running the app.

### Phase 1 — Normalize campus data and graph source of truth

**Goal:** Make the landmark catalog and graph data internally consistent before tracing paths.

**Tasks:**
- Update landmarks with the user-supplied coordinates and corrected names.
- Represent the Library/SAC/Gym as one landmark, keep F-Tech separate from Design, and use the five boys' and three girls' hostel names above.
- Check unique IDs, valid types, coordinate ranges, duplicate locations, and that every building `nodeId` exists.
- Make `nodes.json` and `edges.json` the graph source of truth, with `buildings.json` as the landmark catalog. Update `JsonDataRepository` to compose the in-memory graph from the node and edge caches instead of preferring a possibly stale `graph.json`.
- Check all code, scripts, and frontend requests for `graph.json` consumers. If needed, generate `graph.json` from the canonical files; do not hand-edit both copies.
- Add provenance/confidence fields if they fit the current models cleanly; otherwise keep an accompanying data notes file rather than expanding the schema unnecessarily.

**Done when:** All JSON parses; names and coordinates agree across files; uncertain points are visibly marked; there are no dangling references.

**Give Antigravity:** The corrected landmark list and coordinates from the user, current JSON files, and an instruction to preserve the current ID scheme where possible.

### Phase 2 — Calibrate the map background and coordinate transforms

**Goal:** Put the campus map and geographic coordinates into the same coordinate system.

**Tasks:**
- Choose a clear, top-down campus base image that can be included and attributed appropriately.
- Measure the actual image bounds (`lat_min`, `lat_max`, `lng_min`, `lng_max`) from reliable map control points.
- Identify the projection of the chosen image and use the matching transform to place GPS points on it. For a Web Mercator map image, transform coordinates to Web Mercator before converting to pixels; do not apply a longitude cosine correction on top of Web Mercator.
- Separately calculate local ground distances with a suitable method: for this campus-sized area, use a local tangent/equirectangular approximation (`x ∝ Δlongitude × cos(latitude₀)`, `y ∝ Δlatitude`) or a geodesic distance calculation. Do not use raw degree differences as meter weights.
- Check the transform against several well-separated points, especially the gates, academic core, hostels, and sports complex.
- Keep the map image as a background layer; draw routes and markers in separate SVG/DOM layers.

**Done when:** Known points line up across the map without visible systematic drift, and the bounds, projection, source, and attribution are documented. Do not use placeholder bounds as measured facts. Validate the transform against at least four well-spaced control points and check the campus aspect ratio against the source image.

**Give Antigravity:** The selected image asset or its source, measured bounds, attribution requirements, and the verified GPS control points. If these are not ready, ask it to build the calibration mechanism with clearly labeled temporary values, not to guess final bounds.

### Phase 3 — Trace the road and walkway topology

**Goal:** Replace straight-line building-to-building routes with a graph that follows visible pavement.

**Tasks:**
- Trace a simple primary network first: Main Gate, academic core, North Gate/hostel spine, and sports complex.
- Add waypoint nodes at meaningful bends, corners, crossings, and T-junctions. Use human-readable names in the UI and stable IDs in data. Based on the current 41-node draft, expect roughly 15–20 additional road/path waypoints, then adjust to the actual visible geometry rather than targeting a fixed count.
- Connect only adjacent waypoints along a road or walkway. Add landmark-to-path links at entrances or closest access points.
- Add secondary paths to the hostels, canteens, residences, medical centre, and utility landmarks where visible.
- Record an edge type only when useful (for example, main road, internal road, or walkway); avoid excessive categories.

**Done when:** Routes follow mapped paths, no edge visibly cuts through a building or field, and the intended destinations are connected. Ambiguous path segments are flagged rather than fabricated.

**Give Antigravity:** Calibrated map, current landmark data, and a waypoint/edge draft or annotated path tracing. Ask it to preserve path geometry as coordinates, not infer paths from landmark centers.

### Phase 4 — Validate and serve the graph

**Goal:** Ensure backend and data agree and expose predictable graph data to the frontend.

**Tasks:**
- Align Java models and API responses with the normalized node, edge, and building schema.
- Validate node IDs, edge endpoints, weights, bidirectionality, coordinate bounds, duplicate edges, and connectivity.
- Calculate walking-edge weights from path length (or document manually estimated lengths); never use an endpoint-to-endpoint straight-line weight when the path curves.
- Return clear API errors for unknown nodes or invalid route requests.
- Keep JSON persistence and the existing REST structure unless Phase 0 shows a specific issue.

**Done when:** The app serves consistent data and invalid graph records are caught with understandable errors.

**Give Antigravity:** The finalized schema and graph JSON, plus the current controllers, services, models, and repository code.

### Phase 5 — Finish the interactive map and directory

**Goal:** Make the campus information easy to explore before adding algorithm controls.

**Tasks:**
- Render the calibrated base map, road graph, waypoints, and landmark markers in separate visual layers.
- Add a readable legend, map zoom/pan if needed, and distinct but restrained styling for roads, walkways, landmarks, and selected routes.
- Make markers selectable and show clean building names and concise details.
- Add directory search and simple category filters. Selecting a result should focus the matching marker.
- Ensure keyboard access, useful focus states, and a layout that works on laptop and narrow screens.

**Done when:** Users can identify facilities, search/select one, and understand the map without needing developer tools.

**Give Antigravity:** The accepted data names/categories, map design direction, and specific screenshots or visual references if available.

### Phase 6 — Implement route finding

**Goal:** Let a user choose origin and destination and see a valid route on the graph.

**Tasks:**
- Implement BFS for fewest-edge paths and Dijkstra for shortest weighted walking distance.
- Use the same graph and route response format for both algorithms.
- Show the selected route on the map, its total distance, and a concise ordered list of waypoints/landmarks.
- Handle same-origin/destination, unknown IDs, disconnected areas, and invalid input.
- Keep algorithm code understandable for a DAA presentation; avoid premature optimization.
- Define the route API contract before implementation or integration with any outside algorithm contribution. A request should include `startNodeId`, `endNodeId`, and `algorithm`; a response should include ordered node IDs, total distance, hop count, and visited-node count. Add per-step animation data only if the existing UI needs it.
- If the GitHub collaborator's algorithm implementation is still part of the plan, compare its request/response contract to this one before integrating; avoid parallel incompatible APIs.

**Done when:** Both algorithms return correct paths on small hand-checked graphs and valid campus routes, and the UI clearly displays results and failure states. Add small focused unit tests for algorithm correctness and API validation as part of this phase.

**Give Antigravity:** Route API requirements, algorithm selection, and a few expected origin/destination examples with known correct paths.

### Phase 7 — Add a focused algorithm demonstration

**Goal:** Make the DAA learning objective visible without turning the app into a large analytics platform.

**Tasks:**
- Let users compare BFS and Dijkstra on the same origin/destination pair.
- Show route, distance/hops, visited-node count, and optionally elapsed time as an illustrative metric.
- If time permits, add A* as an optional comparison only after the map projection and heuristic are validated.
- Keep MST (Prim/Kruskal), dynamic closures, and advanced accessibility routing as stretch goals, not completion blockers.

**Done when:** The demo explains what each algorithm optimizes and presents a fair, understandable comparison.

**Give Antigravity:** The required comparison fields and the exact algorithms in scope. Ask it not to claim meaningful speed differences from tiny timing samples.

### Phase 8 — Integration review, polish, and hand-in

**Goal:** Deliver a reliable, documented class project and a short demonstration flow.

**Tasks:**
- Review the complete app from a clean start: load map, search landmark, choose route, compare algorithms, clear route.
- Check responsive layout, missing-data behavior, console/API errors, route visibility, and JSON validity.
- Update README setup steps, data sources/attribution, coordinate uncertainty, graph model, and algorithms actually implemented.
- Prepare a short demo route and a concise explanation of graph representation and algorithm complexity.
- Remove stale claims and placeholder descriptions; leave stretch ideas clearly labeled as future work.

**Done when:** A classmate can run the app from the README and understand the map, route graph, and DAA demonstration without setup help.

**Give Antigravity:** The final bug list, project screenshots if desired, README requirements, and the chosen demo scenario.

## Overall completion checklist

- [x] Correct, human-readable campus landmark catalog.
- [x] Calibrated map background with source and bounds recorded.
- [x] Realistic road/walkway graph with meaningful waypoint names.
- [x] Consistent JSON, backend models, and REST responses.
- [x] Searchable map and facilities directory.
- [x] BFS and Dijkstra route finding with clear route display.
- [x] Focused algorithm comparison suitable for a DAA demonstration.
- [x] Responsive, polished interface and understandable error states.
- [x] README supports a clean run and explains data limitations.
- [x] Final demo flow rehearsed.

## How to proceed

Work through one phase at a time. Before each phase, prepare a focused Antigravity prompt that names the relevant files, inputs, constraints, deliverables, and acceptance checks. Review Antigravity's summary and the visible app/data before moving to the next phase.

## Deferred cleanup from Phase 1

- `data/graph.json` is no longer used as the backend graph source, but it is still a checked-in copy without an automatic generator. Later, either generate it from canonical files or remove/deprecate it after confirming there are no consumers.
- Some `buildings.json` descriptions, facility lists, and floor counts may be unverified. Later, replace unsupported claims with verified information or neutral/empty descriptions.
