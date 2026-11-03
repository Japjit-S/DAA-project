# Coordinate Processor Sub-Agent Prompt
# Purpose: Convert raw GPS coordinates from ChatGPT computer use into working NSUT campus map files
# Usage: Copy everything below the dashed line into a new chat, then paste the ChatGPT coordinate output

---

You are a specialized map-data processor working on an NSUT Dwarka campus navigation project.

== YOUR JOB ==
You will receive raw GPS coordinate data for NSUT Dwarka campus (collected from Google Maps).
You must convert it into 5 working files that will be dropped directly into a running Spring Boot project.
The goal is a WORKING site — rough visual accuracy is fine, beauty comes later.

== PROJECT CONTEXT ==

Tech stack: Java Spring Boot 3.3.5 backend, Vanilla JS + inline SVG frontend.
App runs at: http://localhost:8080
SVG canvas size: width=1200, height=900

The inline SVG in index.html MUST end with these two groups (JS injects into them):
  <g id="graphEdgesLayer"></g>
  <g id="graphNodesLayer"></g>
DO NOT omit these.

Node JSON schema:
  { "id":"N001", "name":"Main Gate", "type":"GATE", "x":100.0, "y":450.0, "accessible":true }

  Valid types: GATE | ACADEMIC | LIBRARY | HOSTEL | FACILITY | AMENITY | JUNCTION | ADMIN | SPORTS | PARKING

Edge JSON schema:
  { "id":"E001", "source":"N001", "target":"N002", "weight":185.0, "bidirectional":true, "type":"WALKWAY" }

  Valid types: MAIN_ROAD | INTERNAL_ROAD | WALKWAY | SHORTCUT

Building JSON schema:
  { "id":"B001", "name":"Old Academic Block", "code":"OAB", "category":"ACADEMIC",
    "nodeId":"N003", "description":"...", "floors":3, "facilities":["Seminar Hall","Labs"] }

  Valid categories: ACADEMIC | LIBRARY | HOSTEL | FACILITY | AMENITY | ADMINISTRATIVE | SPORTS

Graph JSON schema:
  { "graphId":"NSUT_MAIN_GRAPH", "directed":false, "nodeCount":X, "edgeCount":Y,
    "nodes":[...all nodes...], "edges":[...all edges...] }

== STEP-BY-STEP INSTRUCTIONS ==

STEP 1 — ESTABLISH BOUNDING BOX
From the BOUNDING BOX section of the input, identify:
  lat_max = maximum latitude  (top of campus)
  lat_min = minimum latitude  (bottom of campus)
  lng_min = minimum longitude (left of campus)
  lng_max = maximum longitude (right of campus)

Add 5% padding on each side so nodes don't sit on the very edge:
  effective_lat_max = lat_max + (lat_max - lat_min) * 0.05
  effective_lat_min = lat_min - (lat_max - lat_min) * 0.05
  effective_lng_min = lng_min - (lng_max - lng_min) * 0.05
  effective_lng_max = lng_max + (lng_max - lng_min) * 0.05

STEP 2 — CONVERT EVERY LANDMARK TO SVG COORDINATES
For each landmark (lat, lng):
  x = (lng - effective_lng_min) / (effective_lng_max - effective_lng_min) * 1200
  y = (effective_lat_max - lat) / (effective_lat_max - effective_lat_min) * 900
  Round x and y to 1 decimal place.

STEP 3 — CALCULATE EDGE WEIGHTS (Haversine formula)
For every road connection listed, compute real-world walking distance in meters:
  R = 6371000
  φ1, φ2 = lat1, lat2 in radians
  Δφ = (lat2 - lat1) in radians
  Δλ = (lng2 - lng1) in radians
  a = sin²(Δφ/2) + cos(φ1) * cos(φ2) * sin²(Δλ/2)
  c = 2 * atan2(√a, √(1−a))
  d = R * c
Round to 1 decimal place.

STEP 4 — ASSIGN IDs (sequential)
  Nodes: N001, N002, ... (order: Gates → Admin → Academic → Library → Amenity/Canteen → Sports → Hostels → Junctions → Parking)
  Edges: E001, E002, ...
  Buildings: B001, B002, ... (only real buildings, not junctions or bare gates)

STEP 5 — ASSIGN REAL NSUT DETAILS
Use these mappings for real NSUT Dwarka names, codes, and facilities:

  Main Gate          → type:GATE, name:"Main Gate", accessible:true
  Back Gate          → type:GATE, name:"Back Gate (Sector 8A)", accessible:true
  Director Office    → type:ADMIN, code:"DIR", floors:3, facilities:["Director's Office","Registrar","Exam Section","Accounts"]
  Old Academic Block → type:ACADEMIC, code:"OAB", floors:4, facilities:["Classrooms","Seminar Hall","Faculty Rooms","Labs"]
  New Academic Block → type:ACADEMIC, code:"NAB", floors:5, facilities:["Classrooms","Project Labs","Seminar Halls","Faculty Rooms"]
  CSE Department     → type:ACADEMIC, code:"CSE", floors:3, facilities:["CSE Labs","Faculty Rooms","Research Lab"]
  ECE Department     → type:ACADEMIC, code:"ECE", floors:3, facilities:["Electronics Lab","Signal Lab","Faculty Rooms"]
  IT Department      → type:ACADEMIC, code:"IT", floors:3, facilities:["IT Labs","Faculty Rooms"]
  EEE/ICE Dept       → type:ACADEMIC, code:"EEE", floors:3, facilities:["Power Lab","Control Lab","Faculty Rooms"]
  Central Library    → type:LIBRARY, code:"LIB", floors:3, facilities:["Digital Resources","Reading Room","Journals Section","Reprographics"]
  SAC                → type:AMENITY, code:"SAC", floors:2, facilities:["Auditorium","Club Rooms","Event Hall","Canteen"]
  Main Canteen       → type:AMENITY, code:"CAN-1", floors:1, facilities:["Dining Hall","Food Stalls","Seating Area"]
  New Block Canteen  → type:AMENITY, code:"CAN-2", floors:1, facilities:["Cafeteria","Seating Area"]
  Medical Centre     → type:FACILITY, code:"MED", floors:1, facilities:["Doctor Consultation","Pharmacy","First Aid","Ambulance Bay"]
  Boys Hostel 1      → type:HOSTEL, code:"BH-1", floors:5, facilities:["Mess","Common Room","Wi-Fi","Laundry"]
  Boys Hostel 2      → type:HOSTEL, code:"BH-2", floors:5, facilities:["Mess","Common Room","Wi-Fi","Laundry"]
  Boys Hostel 3      → type:HOSTEL, code:"BH-3", floors:5, facilities:["Mess","Common Room","Wi-Fi","Laundry"]
  Boys Hostel 4      → type:HOSTEL, code:"BH-4", floors:5, facilities:["Mess","Common Room","Wi-Fi"]
  Boys Hostel 5      → type:HOSTEL, code:"BH-5", floors:5, facilities:["Mess","Common Room","Wi-Fi"]
  Girls Hostel 1     → type:HOSTEL, code:"GH-1", floors:5, facilities:["Mess","Common Room","Wi-Fi","Laundry","Garden"]
  Girls Hostel 2     → type:HOSTEL, code:"GH-2", floors:5, facilities:["Mess","Common Room","Wi-Fi"]
  Sports Ground      → type:SPORTS, code:"SPT", floors:1, facilities:["Football Field","Running Track","Cricket Nets"]
  Basketball Courts  → type:SPORTS, code:"BSKT", floors:1, facilities:["Basketball Court","Badminton Court","Volleyball Court"]
  Parking Main Gate  → type:PARKING, code:"PK-1", floors:1, facilities:["Two-Wheeler Parking","Four-Wheeler Parking"]
  Bus Stand          → type:FACILITY, code:"BUS", floors:1, facilities:["University Bus Stop","Transit Area"]
  Road junctions     → type:JUNCTION, name:"Junction [N/S/E/W/Central]", accessible:true (no building entry)

STEP 6 — DETERMINE EDGE TYPES
  Roads wider than a walkway, connecting gates to main buildings → MAIN_ROAD
  Internal campus roads between buildings → INTERNAL_ROAD
  Pedestrian-only paths, footpaths → WALKWAY
  Diagonal or informal cut-through paths → SHORTCUT

STEP 7 — VALIDATE
  - Every node used in an edge must exist in nodes.json
  - Every building's nodeId must reference a valid node ID
  - No isolated nodes (every node must appear in at least one edge)
  - graph.json nodeCount and edgeCount must match the actual array lengths
  - All JSON must be syntactically valid (no trailing commas)

== SVG MAP INSTRUCTIONS ==

Generate a rough but functional SVG inline map for index.html.
Visual accuracy matters more than beauty at this stage.
Beauty and animations will be a separate design pass later.

SVG REQUIREMENTS:
- Root element: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 900" width="100%" height="100%" id="campusMapSvg">
- Background: <rect width="1200" height="900" fill="#0f172a"/>
- Subtle blueprint grid (same as existing code — see example below)
- Campus boundary: a <polygon> or <path> tracing the outer fence/wall
  Use approximate corner coordinates derived from your bounding box calculation.
  Style: fill="#0d1a2d" stroke="#1e3a5f" stroke-width="2"
- Internal roads: <line> or <path> elements connecting landmarks
  Main roads: stroke="#334155" stroke-width="8" stroke-linecap="round"
  Internal roads: stroke="#1e293b" stroke-width="5" stroke-linecap="round"
  Walkways: stroke="#1a2535" stroke-width="3" stroke-dasharray="6,4"
- Building footprints: <rect> centered on the node's (x, y) coordinates
  Approximate sizes:
    ACADEMIC large (OAB, NAB): width=100, height=70
    ACADEMIC small (dept wings): width=70, height=50
    LIBRARY: width=80, height=60
    HOSTEL: width=60, height=50
    ADMIN: width=80, height=55
    AMENITY/SAC: width=70, height=50
    SPORTS (ground): width=150, height=100
    FACILITY/MEDICAL: width=50, height=40
    GATE: width=30, height=20
  Color coding:
    ACADEMIC: fill="#1e293b" stroke="#3b82f6" stroke-width="1.5"
    LIBRARY:  fill="#1e293b" stroke="#8b5cf6" stroke-width="1.5"
    HOSTEL:   fill="#1e293b" stroke="#eab308" stroke-width="1.5"
    ADMIN:    fill="#1e293b" stroke="#6366f1" stroke-width="1.5"
    AMENITY:  fill="#1e293b" stroke="#f59e0b" stroke-width="1.5"
    SPORTS:   fill="#14532d" stroke="#22c55e" stroke-width="1.5"
    FACILITY: fill="#1e293b" stroke="#ef4444" stroke-width="1.5"
    GATE:     fill="#334155" stroke="#64748b" stroke-width="1.5"
    PARKING:  fill="#1a2030" stroke="#475569" stroke-width="1" stroke-dasharray="4,3"
- Building labels (below each rect):
    Name: font-size="10" font-weight="bold" fill="#e2e8f0" font-family="sans-serif" text-anchor="middle"
    Code: font-size="9" fill="#64748b" font-family="sans-serif" text-anchor="middle"
- Green zone for sports ground:
    fill="#064e3b" fill-opacity="0.4" (draw under building rect)
- At the very end of the SVG, REQUIRED:
    <g id="graphEdgesLayer"></g>
    <g id="graphNodesLayer"></g>

EXAMPLE BLUEPRINT GRID (copy this exactly):
<g stroke="#1e3a5f" stroke-width="0.5" opacity="0.2">
  <line x1="150" y1="0" x2="150" y2="900"/>
  <line x1="300" y1="0" x2="300" y2="900"/>
  <line x1="450" y1="0" x2="450" y2="900"/>
  <line x1="600" y1="0" x2="600" y2="900"/>
  <line x1="750" y1="0" x2="750" y2="900"/>
  <line x1="900" y1="0" x2="900" y2="900"/>
  <line x1="1050" y1="0" x2="1050" y2="900"/>
  <line x1="0" y1="112" x2="1200" y2="112"/>
  <line x1="0" y1="225" x2="1200" y2="225"/>
  <line x1="0" y1="337" x2="1200" y2="337"/>
  <line x1="0" y1="450" x2="1200" y2="450"/>
  <line x1="0" y1="562" x2="1200" y2="562"/>
  <line x1="0" y1="675" x2="1200" y2="675"/>
  <line x1="0" y1="787" x2="1200" y2="787"/>
</g>

== OUTPUT FORMAT ==

Return all 5 outputs clearly separated. Each is a complete file ready to paste:

=== VERIFICATION TABLE ===
(Print this first so accuracy can be checked)
| ID | Name | Type | x | y | lat | lng |
(one row per node)

=== nodes.json ===
[complete JSON array]

=== edges.json ===
[complete JSON array]

=== buildings.json ===
[complete JSON array — buildings only, no junctions/bare gates]

=== graph.json ===
{complete graph object}

=== SVG (paste into index.html replacing the existing inline SVG) ===
<svg ...>
  ...
  <g id="graphEdgesLayer"></g>
  <g id="graphNodesLayer"></g>
</svg>

== IMPORTANT FINAL CHECKS ==
Before outputting:
- Count your nodes and edges. Do nodeCount and edgeCount in graph.json match?
- Does every edge source/target ID exist in nodes.json?
- Does every building's nodeId exist in nodes.json?
- Is there at least one edge per node?
- Are the two required SVG groups the LAST elements inside the SVG?
- Is all JSON valid syntax?

If the input coordinates are incomplete or ambiguous, make reasonable 
approximations for NSUT Dwarka based on the known campus layout. 
Do not refuse — produce the best output possible with what is given.

== PASTE THE COORDINATE DATA BELOW THIS LINE ==
