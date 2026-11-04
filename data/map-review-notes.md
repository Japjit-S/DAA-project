# NSUT map registration and walking-network review

## Map imagery and calibration

The five displayed views are local OpenStreetMap Standard exports: the zoom-15 overview plus four registered detail panels. They are cartographic map imagery, not satellite photographs. `map (6).png` is a near-duplicate of `map (2).png` and remains preserved at the project root, outside the displayed panel stack. The UI provides two distinct visual presentations:
1. **Original Colors (Default)**: Authentic OpenStreetMap cartographic presentation with light terrain, clear building outlines, and subdued slate graph overlays.
2. **Neon Cartographic Dark Mode (Optional)**: A purpose-designed high-contrast night palette using pre-rendered raster panels (`map-*-dark.png`). Surroundings are deep obsidian (`#070b14`), campus lawns are vivid neon emerald (`#059669` / `#10b981`), roads and walkways are luminous cyan and warm amber, and major facility footprints are rendered in a **muted copper / terracotta fill with a warm glowing outline** (`#8a4320` / `#ea580c`). OpenStreetMap labels and the attribution `© OpenStreetMap contributors` remain crisp and fully legible without any muddy CSS dimming or color-inversion filters.

The panels are north-up Web Mercator and are projected through `backend/src/main/resources/static/js/calibration.js`. The four detail panels were aligned by image matching; their overlap agreement is below one pixel at the 95th percentile. The overview-to-detail fit has a 1.438 px 95th-percentile residual in overview pixels. These are image-registration measurements, not survey-grade GPS accuracy.

## Repaired graph

The canonical dataset contains 135 node records (124 routable path and access points, plus 11 non-routable reference landmarks) and 134 undirected segments. The routable graph is one connected component. It includes road/path loops around the academic core, the residential hostel corridors, the west campus boundary, the ring road around APJ and Smart Class, and the sports complex. Route geometry comes from ordered geographic points; segment weights are calculated as the cumulative Haversine distance along that geometry. Endpoint alignment, weight agreement, duplicate-edge, zero-length, and connectivity checks are validated at app startup.

### Main Gate to North Gate internal campus road

- **Main Gate (`N001`)**: Preserved at the true **Guard House / barrier entrance** on Azad Hind Fauj Marg (`x=1505.0, y=1666.0`, `lat=28.6083174, lng=77.0351765`).
- **Internal Campus Perimeter Road**: The gate connection follows the parallel paved road immediately inside the campus boundary wall:
  - `N001` (Main Gate) → `N152` (Security inside) → `N157` (west of Bose statue) → `N153` (Nescii Lawn Boulevard junction) → `N158` (mid Nescii Lawn west) → `N159` (west of Moksha Ground) → `N160` (opposite Water Softening Plant) → `N161` (north perimeter approach) → `N043` (North Gate South Approach / PWD) → `N002` (North Gate). Total distance: 576.62 m across 9 hops.
- **Elimination of lawn chords**: Moksha Ground and Nescii Lawn chords remain eliminated. The perimeter road cleanly follows the paved corridor inside the boundary wall, with branches to the boulevard (`N154` → `N155`), Water Softening Plant (`N035`), and the Hostel South Road link (`E293`).

### Academic and administrative core corrections

- **Central Library Footprint Bypass (`E239`)**:
  - Re-routed `E239` completely clear of the Central Library (`B010` / `N012`) building footprint.
  - Follows visible pavement from the library north plaza junction `N051` (`2170.0, 1255.0`) north-east around the building perimeter via `(2205.0, 1215.0)` and `(2315.0, 1165.0)` to meet Library Parking at `N181` (`2340.0, 1130.0`).
- **Block 6 (Old Academic Block)**:
  - Relocated building node `N006` from the outer south wall to the central/northeast position inside the hexagonal courtyard (`x=2045.0, y=1180.0`).
  - Approach from Central Fountain (`N039`) connects via southeast plaza approach `N193` directly into the open courtyard entrance without cutting through outer building walls.
  - Northern walkway extends from `N194` north through `N204` and `N205` to meet the ring road at `N198`.
- **Block 8A (Connecting Block)**:
  - Relocated building node `N007` inside the Block 8A footprint at `x=2035.0, y=1360.0`.
  - Connected to Central Fountain plaza via north entrance spur `N180`. Walkway continues south past Block 4 north entrance (`N168` → `N004`) to Mini Auditorium and South Campus Road.
- **South Campus Road Junction (`N053`) & Faculty Housing**:
  - Shifted `N053` slightly southwest onto the physical T-junction (`x=2160.0, y=1610.0`) where the Mini Auditorium approach meets the south road.
  - Curved road sequence follows mapped geometry past Electric Substation into Faculty Housing (`N053` → `N185` → `N186` → `N187` → `N188` → `N189` → `N057`).
- **Guest House (`B036` / `N190`)**:
  - Located at `x=2122.0, y=1685.0`, type `FACILITY`. Connected to the south road via the mapped walkway spur branching from `N185` (`accessConfidence: "HIGH"`).
- **APJ Lecture Theatre Complex & Smart Class (Ring Road)**:
  - Continuous **Ring Road** wraps around APJ Lecture Theatre Complex and Smart Class without cutting through either footprint:
    - West side: `N046` → `N196` → `N197`.
    - South side: `N197` → `N167` (Students Centre & APJ south entrance) → `N198` → `N173`.
    - East side: `N173` → `N200` (Smart Class parking plaza) → `N201`.
    - North side: `N201` → `N202` → `N203` → `N139` → `N046`.
- **Smart Class (`B037` / `N191`)**: Connected via east ring road parking plaza at `N200` (`accessConfidence: "HIGH"`).
- **Shopping Complex (`B038` / `N192`)**: Connected to northern ring loop at `N203` with direct connector running north-northwest to hostel road at `N140`.

### Sports complex, TNP, and Library Parking road corrections

- **TNP Road North Junction (`N144`)**:
  - Moved slightly north to the actual physical road junction at `x=2420.0, y=1530.0` (`lat=28.6089847, lng=77.0397823`).
  - South Campus Road (`E092`, `N053 -- N144`) follows the paved road through `(2250, 1580)`, `(2310, 1550)`, and `(2360, 1535)` (154.26 m), running cleanly north of the TNP building footprint without cutting across it.
  - Paved access to Training & Placement entrance (`E093`, `N144 -- N145`) heads south along the east road and into the entrance foyer at `(2390, 1625)` (68.42 m).
  - Paved road to Alaknanda (`E094`, `N144 -- N146`) runs south along the road without piercing the building (109.93 m).
- **Sports West Entrance (`N054`)**:
  - Placed on the visible road access at `x=2480.0, y=1470.0` (`lat=28.6092834, lng=77.0400843`).
  - Connected to TNP Road North Junction along the mapped road (`E016`, `N144 -- N054`) via `(2445, 1515)` and `(2475, 1490)` (68.51 m).
  - Connected along the sports road to Sports South Access Junction (`E017`, `N054 -- N056`) via `(2485, 1455)` (28.46 m).
- **Sports Complex West Junction (`N040`) to Library Parking Road (`N181`) and F-Tech Front Road East (`N061`)**:
  - Moved `N061` ("F-Tech Front Road East") to the physical road junction with Library Parking Road at `x=2345.0, y=930.0` (`lat=28.6115698, lng=77.0394047`).
  - F-Tech Front Road (`E259`, `N179 -- N061`) follows the front road through `(2300, 930)` to meet `N061` at the junction (61.12 m).
  - Library Parking Road (`E260`, `N061 -- N181`) follows the road straight south through `(2340, 1050)` to `N181` (112.59 m), eliminating diagonal lawn chords.
  - Connector road (`E240`, `N181 -- N040`) follows the curved pavement through `(2345, 1200)`, `(2355, 1260)`, and `(2368, 1305)` to meet `N040` (114.77 m).
- **Sports Perimeter Geometry (`E018`, `E019`)**:
  - `E018` (`N040 -- N055`) curves along the paved northern path through `(2390, 1260)`, `(2420, 1235)`, and `(2450, 1215)` (98.66 m).
  - `E019` (`N040 -- N056`) follows the road through `(2420, 1335)`, `(2450, 1345)`, `(2470, 1395)`, and `(2485, 1435)` (92.51 m).

### Preserved hostel spine network

All improved hostel routes remain intact:
- Paved North Road, Central Walkway Spine, and South Road connecting BH-1 through BH-5 with short entrance spurs.
- Paved avenue from Kaveri Junction (`N062`) to Kaveri Hostel entrance (`N143`).
- Paved eastern road and cul-de-sacs serving Training & Placement (`N145`), Alaknanda (`N100`), and Saraswati (`N118`).
- Hostel South Road link (`E293`, 128.27 m) connecting Campus West Road (`N160`) to Aryabhata (`N138`).

### Enforced Removals
- Isolated spur `N067` and dangling edge `E082` remain completely removed.
- Unsupported cross-campus chord `E069` (Water Softening Plant to STP) remains completely removed; STP is accessed solely via service road `E256`.

## Route and Algorithm Visual Styling

1. **Selected Shortest Route (Dijkstra)**: Rendered in a **slightly thicker dark red** (`#991b1b`, stroke-width: 4.8) paired with a high-contrast white casing glow (`#ffffff`, stroke-width: 11, opacity: 0.85) and animated directional dashes. It maintains crisp visibility across both original (light OSM) and dark map modes.
2. **Algorithm Traversal & Minimum Spanning Trees**: DFS completed traversal branches (`.algorithm-edge-traversed`) and accepted MST edges (`.mst-edge-accepted`, `.algorithm-mst-edge`) use **dark green** (`#065f46`, stroke-width: 5) with a luminous emerald outline and white highlight (`filter: drop-shadow(0 0 4px rgba(52, 211, 153, 0.85)) drop-shadow(0 0 1px #ffffff)`), clearly differentiating completed branches from ordinary roads and unvisited edges.
3. **Dark Map Styling**: Re-rendered all 5 dark panels with **muted copper / terracotta fills (`#8a4320`) and warm glowing copper outlines (`#ea580c` / `#f59e0b`)** for building footprints against vivid emerald lawns (`#059669`) and deep obsidian surroundings (`#070b14`). Labels and OSM attribution remain sharp and crisp.

## App review and validation

- **Spring Boot MockMvc Test Suite**: 18/18 tests passing.
- **Startup Data Validation**: `status: "VALID"`, 0 errors, 0 warnings.
- **Sample Verified Routes**:
  - Main Gate → North Gate: 576.62 m, 9 hops (Internal campus road).
  - Main Gate → Central Library: 523.16 m, 13 hops (Clean bypass around north wall).
  - Main Gate → Training and Placement: 673.88 m, 14 hops (Boulevard, South Road, TNP road east approach).
  - Main Gate → Sports West Entrance: 673.97 m, 13 hops (Boulevard, South Road, Sports access road).
  - Campus West Road (`N160`) → Aryabhata (`N138`): 128.27 m, 1 hop via `E293`.

## Remaining limits

OpenStreetMap panels show the mapped roads, buildings, footways, and grounds at export time. They do not prove present-day interior doors, turnstiles, temporary security barriers, or construction status. Entrances marked `LOW` confidence reflect visual alignment to visible roads and paths on the OSM panels, not survey-grade on-foot door verification.
