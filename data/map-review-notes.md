# NSUT map calibration and path review

## Map registration

The local OSM Standard exports are north-up Web Mercator. `map (2).png` is the northwest reference panel; `map (3).png`, `map (4).png`, and `map (5).png` provide northeast, southwest, and southeast coverage. The four panels were aligned by SIFT feature matches with RANSAC and then anchored to the approximate northwest center supplied for the exports. Their fitted detail scale is 0.56034 projected metres per pixel. GPS points are projected to EPSG:3857 before conversion into the registered mosaic plane.

The panel overlaps agree to within one pixel at the 95th percentile: map 2–3 0.769 px, map 2–4 0.672 px, and map 2–5 0.735 px. `map.png` is the zoom-15 overview. A robust feature match to map 2 fitted scale 0.252007 with 32 inliers from 192 matches; its median residual is 0.600 px and 95th-percentile residual is 1.438 px in overview pixels. `map (6).png` is a near-duplicate of map 2 (scale 1.00005, offset approximately 0.0 by -1.5 px); it is preserved in the project root but omitted from the displayed layer stack.

Known GPS nodes in the hostel area, academic core, gate area, and sports complex were projected and visually checked against the OSM labels and footprints. The supplied panel centers are approximate and no surveyed pixel positions were provided, so these are sanity checks, not independent GPS residual measurements. The sub-pixel residuals above describe image-to-image registration only.

The app keeps the source PNGs in the project root, serves local copies from `static/assets/maps`, and displays one visible `© OpenStreetMap contributors` attribution. The overview sits below the four registered detail panels; graph paths and landmark markers use separate SVG groups.

The SVG preserves its aspect ratio; the initial and Fit campus view expands to the current viewport proportions, and the zoom controls operate on that undistorted map plane. The updated Spring Boot app was reviewed at `http://localhost:8080/`; all five local raster layers rendered and the API returned 61 nodes, 61 edges, and 35 buildings.

## Walking graph

The graph now contains 61 nodes, 61 adjacent edges, and the existing 35 building records. Twenty-one road or path junction waypoints were added. The duplicate Main Gate Junction node was removed; the Sports Complex Junction was moved off the volleyball court center. Every edge has ordered GPS geometry and a weight calculated from that geometry with Haversine segment lengths. Edge `confidence` marks building access links as low confidence where the source coordinates represent building centers rather than verified doors.

## Manual review

- The Main Gate GPS point is approximate. Verify its exact gate-to-campus connection.
- Confirm entrances and short access paths for each building; review edges marked `LOW` in `data/edges.json`.
- Check the hostel internal-road turns, north/east utility links, and sports-complex entrances on foot or against a higher-detail campus survey. Their topology follows the supplied OSM exports, but exact access points remain estimates.
- GPS visual checks do not establish survey-grade positional accuracy. Recheck the map if more precise export centers or surveyed landmark pixels become available.
