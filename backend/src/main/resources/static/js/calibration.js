/**
 * NSUT map registration in an EPSG:3857 (Web Mercator) coordinate plane.
 * Raster panel placement comes from feature-matched overlaps; geographic scale
 * is anchored by the supplied detail-panel centers.
 */
const CampusCalibration = (() => {
  'use strict';

  const EARTH_RADIUS_M = 6378137;
  const DETAIL_M_PER_PX = 0.56034;
  const DETAIL_ANCHOR = { lat: 28.612500, lng: 77.034502, px: 1371, py: 719.5 };
  const DETAIL_PANELS = [
    { id: 'northwest', href: '/assets/maps/map-nw.png', x: 0, y: 0, width: 2742, height: 1439 },
    { id: 'northeast', href: '/assets/maps/map-ne.png', x: 1191.75, y: -0.61, width: 2742, height: 1439 },
    { id: 'southwest', href: '/assets/maps/map-sw.png', x: 0.01, y: 1131.25, width: 2742, height: 1439 },
    { id: 'southeast', href: '/assets/maps/map-se.png', x: 1191.95, y: 1131.20, width: 2742, height: 1439 }
  ];
  const OVERVIEW_TO_DETAIL = {
    scale: 1 / 0.252007,
    x: -853.4 / 0.252007,
    y: -323.64 / 0.252007,
    width: 2759 / 0.252007,
    height: 1328 / 0.252007
  };
  const INITIAL_VIEWBOX = { x: 900, y: 250, width: 2600, height: 1733 };

  /** Keep Web Mercator pixels square while fitting the campus to the viewport. */
  function fitViewBox(viewportWidth, viewportHeight) {
    const aspect = viewportWidth > 0 && viewportHeight > 0
      ? viewportWidth / viewportHeight
      : INITIAL_VIEWBOX.width / INITIAL_VIEWBOX.height;
    const width = Math.max(INITIAL_VIEWBOX.width, INITIAL_VIEWBOX.height * aspect);
    const height = Math.max(INITIAL_VIEWBOX.height, INITIAL_VIEWBOX.width / aspect);
    const centerX = INITIAL_VIEWBOX.x + INITIAL_VIEWBOX.width / 2;
    const centerY = INITIAL_VIEWBOX.y + INITIAL_VIEWBOX.height / 2;
    return { x: centerX - width / 2, y: centerY - height / 2, width, height };
  }

  function mercatorX(lng) {
    return EARTH_RADIUS_M * (lng * Math.PI / 180);
  }

  function mercatorY(lat) {
    const clamped = Math.max(-85.05112878, Math.min(85.05112878, lat));
    return EARTH_RADIUS_M * Math.log(Math.tan(Math.PI / 4 + clamped * Math.PI / 360));
  }

  const anchorX = mercatorX(DETAIL_ANCHOR.lng);
  const anchorY = mercatorY(DETAIL_ANCHOR.lat);

  /** Project GPS coordinates into the registered detail-mosaic pixel plane. */
  function project(lat, lng, fallbackX = 0, fallbackY = 0) {
    if (!Number.isFinite(Number(lat)) || !Number.isFinite(Number(lng))) {
      return { x: fallbackX, y: fallbackY };
    }
    return {
      x: DETAIL_ANCHOR.px + (mercatorX(Number(lng)) - anchorX) / DETAIL_M_PER_PX,
      y: DETAIL_ANCHOR.py - (mercatorY(Number(lat)) - anchorY) / DETAIL_M_PER_PX
    };
  }

  /** Reverse the registered mosaic transform to WGS84 coordinates. */
  function unproject(x, y) {
    const mx = anchorX + (x - DETAIL_ANCHOR.px) * DETAIL_M_PER_PX;
    const my = anchorY - (y - DETAIL_ANCHOR.py) * DETAIL_M_PER_PX;
    return {
      lat: (2 * Math.atan(Math.exp(my / EARTH_RADIUS_M)) - Math.PI / 2) * 180 / Math.PI,
      lng: mx / EARTH_RADIUS_M * 180 / Math.PI
    };
  }

  function distance(lat1, lng1, lat2, lng2) {
    const rad = (deg) => deg * Math.PI / 180;
    const dLat = rad(lat2 - lat1);
    const dLng = rad(lng2 - lng1);
    const a = Math.sin(dLat / 2) ** 2 +
      Math.cos(rad(lat1)) * Math.cos(rad(lat2)) * Math.sin(dLng / 2) ** 2;
    return 6371008.8 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  function localDistance(lat1, lng1, lat2, lng2) {
    return distance(lat1, lng1, lat2, lng2);
  }

  function imageLayout() {
    return [
      {
        id: 'overview', href: '/assets/maps/map-overview.png',
        x: OVERVIEW_TO_DETAIL.x, y: OVERVIEW_TO_DETAIL.y,
        width: OVERVIEW_TO_DETAIL.width, height: OVERVIEW_TO_DETAIL.height,
        preserveAspectRatio: 'none'
      },
      ...DETAIL_PANELS.map((panel) => ({ ...panel, preserveAspectRatio: 'none' }))
    ];
  }

  return {
    DETAIL_M_PER_PX,
    DETAIL_PANELS,
    INITIAL_VIEWBOX,
    fitViewBox,
    project,
    unproject,
    distance,
    localDistance,
    imageLayout
  };
})();

if (typeof window !== 'undefined') window.CampusCalibration = CampusCalibration;
