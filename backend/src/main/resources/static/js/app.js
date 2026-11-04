/**
 * Smart Campus Navigation and Resource Optimizer
 * Frontend Application Controller — Phase 7 Algorithm Demonstration Enhanced
 */
(function () {
  'use strict';

  // Application State
  const state = {
    health: null,
    buildings: [],
    graph: { nodes: [], edges: [] },
    selectedBuildingId: null,
    selectedNodeId: null,
    searchQuery: '',
    selectedCategory: 'ALL',
    currentSidebarMode: 'directory', // 'directory' | 'route' | 'algorithms'
    currentMobileTab: 'directory',   // 'directory' | 'route' | 'algorithms' | 'map'
    algorithmDemo: { type: '', result: null },
    route: {
      startNodeId: '',
      endNodeId: '',
      algorithm: 'DIJKSTRA', // 'DIJKSTRA' | 'BFS'
      wheelchairOnly: false,
      result: null,
      compareResult: {
        active: false,
        dijkstra: null,
        bfs: null,
        activeFilter: 'both' // 'both' | 'dijkstra' | 'bfs'
      },
      loading: false,
      error: null
    },
  };
  let mapIsFit = true;

  // Node Color Scheme by Type
  const TYPE_COLORS = {
    GATE: '#94a3b8',
    ACADEMIC: '#3b82f6',
    LIBRARY: '#a855f7',
    AMENITY: '#f59e0b',
    FACILITY: '#10b981',
    HOSTEL: '#eab308',
    JUNCTION: '#06b6d4',
    ADMIN: '#6366f1',
    SPORTS: '#22c55e',
    PARKING: '#64748b'
  };

  // Category to Node Type mapping helper
  const CATEGORY_TO_TYPE = {
    ACADEMIC: 'ACADEMIC',
    ADMINISTRATIVE: 'ADMIN',
    LIBRARY: 'LIBRARY',
    HOSTEL: 'HOSTEL',
    SPORTS: 'SPORTS',
    AMENITY: 'AMENITY',
    FACILITY: 'FACILITY'
  };

  // DOM Elements
  const dom = {
    appContainer: document.getElementById('appContainer'),
    statusPill: document.getElementById('statusPill'),
    statusText: document.getElementById('statusText'),
    nodeCountBadge: document.getElementById('nodeCountBadge'),
    edgeCountBadge: document.getElementById('edgeCountBadge'),
    buildingCountBadge: document.getElementById('buildingCountBadge'),
    mobileCountBadge: document.getElementById('mobileCountBadge'),

    // Sidebar navigation tabs & containers
    modeDirectoryBtn: document.getElementById('modeDirectoryBtn'),
    modeRouteBtn: document.getElementById('modeRouteBtn'),
    modeAlgorithmBtn: document.getElementById('modeAlgorithmBtn'),
    directoryModeContainer: document.getElementById('directoryModeContainer'),
    routeModeContainer: document.getElementById('routeModeContainer'),
    algorithmModeContainer: document.getElementById('algorithmModeContainer'),

    // Directory Search & Filters
    searchInput: document.getElementById('searchInput'),
    clearSearchBtn: document.getElementById('clearSearchBtn'),
    clearFiltersBtn: document.getElementById('clearFiltersBtn'),
    facilityCountBadge: document.getElementById('facilityCountBadge'),
    categoryChips: document.querySelectorAll('.filter-chip'),
    buildingList: document.getElementById('buildingList'),
    detailPanel: document.getElementById('detailPanel'),
    detailTitle: document.getElementById('detailTitle'),
    detailContent: document.getElementById('detailContent'),

    // Route Finder Controls
    btnLoadDemoPair: document.getElementById('btnLoadDemoPair'),
    routeStartSelect: document.getElementById('routeStartSelect'),
    routeEndSelect: document.getElementById('routeEndSelect'),
    routeSwapBtn: document.getElementById('routeSwapBtn'),
    algoOptDijkstra: document.getElementById('algoOptDijkstra'),
    algoOptBfs: document.getElementById('algoOptBfs'),
    algoRadioInputs: document.querySelectorAll('input[name="routeAlgorithm"]'),
    wheelchairOnlyCheck: document.getElementById('wheelchairOnlyCheck'),
    btnFindRoute: document.getElementById('btnFindRoute'),
    btnCompareRoute: document.getElementById('btnCompareRoute'),
    dfsStartSelect: document.getElementById('dfsStartSelect'),
    btnRunDfs: document.getElementById('btnRunDfs'),
    dfsResult: document.getElementById('dfsResult'),
    mstAlgorithmSelect: document.getElementById('mstAlgorithmSelect'),
    mstStartSelect: document.getElementById('mstStartSelect'),
    btnRunMst: document.getElementById('btnRunMst'),
    mstResult: document.getElementById('mstResult'),

    // DFS Playback Controls
    dfsPlaybackControls: document.getElementById('dfsPlaybackControls'),
    dfsBtnReset: document.getElementById('dfsBtnReset'),
    dfsBtnPrev: document.getElementById('dfsBtnPrev'),
    dfsBtnPlayPause: document.getElementById('dfsBtnPlayPause'),
    dfsBtnNext: document.getElementById('dfsBtnNext'),
    dfsSpeedGroup: document.getElementById('dfsSpeedGroup'),
    dfsSlider: document.getElementById('dfsSlider'),
    dfsStepBadge: document.getElementById('dfsStepBadge'),
    dfsStatusMsg: document.getElementById('dfsStatusMsg'),

    // MST Playback Controls
    mstPlaybackControls: document.getElementById('mstPlaybackControls'),
    mstBtnReset: document.getElementById('mstBtnReset'),
    mstBtnPrev: document.getElementById('mstBtnPrev'),
    mstBtnPlayPause: document.getElementById('mstBtnPlayPause'),
    mstBtnNext: document.getElementById('mstBtnNext'),
    mstSpeedGroup: document.getElementById('mstSpeedGroup'),
    mstSlider: document.getElementById('mstSlider'),
    mstStepBadge: document.getElementById('mstStepBadge'),
    mstStatusMsg: document.getElementById('mstStatusMsg'),
    btnClearRoute: document.getElementById('btnClearRoute'),
    clearRouteMapBtn: document.getElementById('clearRouteMapBtn'),
    routeAlertBox: document.getElementById('routeAlertBox'),

    // Single Route Result Card
    routeResultCard: document.getElementById('routeResultCard'),
    routeResultTitle: document.getElementById('routeResultTitle'),
    routeAlgoBadge: document.getElementById('routeAlgoBadge'),
    metricDistance: document.getElementById('metricDistance'),
    metricHops: document.getElementById('metricHops'),
    metricVisited: document.getElementById('metricVisited'),
    metricTime: document.getElementById('metricTime'),
    routeConfidenceWarning: document.getElementById('routeConfidenceWarning'),
    routeStepsList: document.getElementById('routeStepsList'),

    // Phase 7: Comparison Result Card
    routeCompareCard: document.getElementById('routeCompareCard'),
    compareTitle: document.getElementById('compareTitle'),
    filterBothRoutes: document.getElementById('filterBothRoutes'),
    filterDijkstraOnly: document.getElementById('filterDijkstraOnly'),
    filterBfsOnly: document.getElementById('filterBfsOnly'),
    dijkstraDistVal: document.getElementById('dijkstraDistVal'),
    dijkstraHopsVal: document.getElementById('dijkstraHopsVal'),
    dijkstraExploredVal: document.getElementById('dijkstraExploredVal'),
    dijkstraTimeVal: document.getElementById('dijkstraTimeVal'),
    dijkstraHighlightBadge: document.getElementById('dijkstraHighlightBadge'),
    dijkstraAdvisory: document.getElementById('dijkstraAdvisory'),
    dijkstraStepCount: document.getElementById('dijkstraStepCount'),
    dijkstraStepsList: document.getElementById('dijkstraStepsList'),
    bfsDistVal: document.getElementById('bfsDistVal'),
    bfsHopsVal: document.getElementById('bfsHopsVal'),
    bfsExploredVal: document.getElementById('bfsExploredVal'),
    bfsTimeVal: document.getElementById('bfsTimeVal'),
    bfsHighlightBadge: document.getElementById('bfsHighlightBadge'),
    bfsAdvisory: document.getElementById('bfsAdvisory'),
    bfsStepCount: document.getElementById('bfsStepCount'),
    bfsStepsList: document.getElementById('bfsStepsList'),
    compareTakeawayBox: document.getElementById('compareTakeawayBox'),

    // Legend items in map toolbar
    legendRouteItem: document.getElementById('legendRouteItem'),
    legendCompareDijkstra: document.getElementById('legendCompareDijkstra'),
    legendCompareBfs: document.getElementById('legendCompareBfs'),
    legendCompareShared: document.getElementById('legendCompareShared'),

    // Map Canvas & SVG Layers
    mapCanvasContainer: document.querySelector('.map-canvas-container'),
    svgContainer: document.getElementById('campusMapSvg'),
    rasterLayer: document.getElementById('mapRasterLayer'),
    edgesLayer: null,
    nodesLayer: null,


    // Map Controls
    resetBtn: document.getElementById('resetHighlightsBtn'),
    reloadBtn: document.getElementById('reloadMapBtn'),
    zoomInBtn: document.getElementById('zoomInBtn'),
    zoomOutBtn: document.getElementById('zoomOutBtn'),
    fitCampusBtn: document.getElementById('fitCampusBtn'),
    mapToneBtn: document.getElementById('mapToneBtn'),

    // Mobile Switcher Tabs
    tabDirectoryBtn: document.getElementById('tabDirectoryBtn'),
    tabRouteBtn: document.getElementById('tabRouteBtn'),
    tabAlgorithmsBtn: document.getElementById('tabAlgorithmsBtn'),
    tabMapBtn: document.getElementById('tabMapBtn'),
    legendItems: document.querySelectorAll('.map-legend .legend-item')
  };

  /**
   * Initializes the application.
   */
  async function init() {
    setupMobileTabs();
    setupSidebarModeNav();
    setupRouteFinder();
    setupComparisonControls();
    setupAlgorithmDemos();
    bindEvents();

    try {
      await verifyHealth();
      await Promise.all([loadBuildings(), loadGraph()]);

      renderMapElements();
      renderBuildingList();
      populateRouteSelectors();
      populateAlgorithmSelectors();
    } catch (err) {
      console.error('Initialization error:', err);
      showOfflineStatus();
    }
  }

  /**
   * Health Check
   */
  async function verifyHealth() {
    try {
      const res = await ApiClient.checkHealth();
      state.health = res;
      if (res && res.status === 'UP') {
        dom.statusPill.classList.remove('offline');
        dom.statusText.textContent = 'System Online';
      } else {
        showOfflineStatus();
      }
    } catch (err) {
      showOfflineStatus();
    }
  }

  function showOfflineStatus() {
    dom.statusPill.classList.add('offline');
    dom.statusText.textContent = 'Backend Offline';
  }

  /**
   * Load Buildings from /api/buildings
   */
  async function loadBuildings() {
    const list = await ApiClient.getBuildings();
    state.buildings = list || [];
    if (dom.buildingCountBadge) dom.buildingCountBadge.textContent = state.buildings.length;
    if (dom.mobileCountBadge) dom.mobileCountBadge.textContent = state.buildings.length;
  }

  /**
   * Load Graph from /api/graph
   */
  async function loadGraph() {
    const graphData = await ApiClient.getGraph();
    state.graph = graphData || { nodes: [], edges: [] };
    if (dom.nodeCountBadge) dom.nodeCountBadge.textContent = state.graph.nodes ? state.graph.nodes.length : 0;
    if (dom.edgeCountBadge) dom.edgeCountBadge.textContent = state.graph.edges ? state.graph.edges.length : 0;
  }

  /**
   * Helper to project a node to SVG canvas coordinates
   */
  function getNodePoint(node) {
    if (!node) return { x: 0, y: 0 };
    if (typeof CampusCalibration !== 'undefined' && node.lat != null && node.lng != null) {
      return CampusCalibration.project(node.lat, node.lng, node.x, node.y);
    }
    return { x: node.x || 0, y: node.y || 0 };
  }

  /** Render saved campus map panels and the calibrated campus paths. */
  function renderMapElements() {
    dom.edgesLayer = document.getElementById('graphEdgesLayer');
    dom.nodesLayer = document.getElementById('graphNodesLayer');
    dom.rasterLayer = document.getElementById('mapRasterLayer');
    const roadLayer = document.getElementById('vectorRoadLayer');
    const groundLayer = document.getElementById('vectorGroundLayer');
    const footprintLayer = document.getElementById('vectorFootprintsLayer');
    const labelLayer = document.getElementById('vectorLabelsLayer');
    if (!dom.edgesLayer || !dom.nodesLayer || !dom.rasterLayer || !roadLayer || !groundLayer || !footprintLayer || !labelLayer) return;
    dom.edgesLayer.innerHTML = ''; dom.nodesLayer.innerHTML = ''; dom.rasterLayer.innerHTML = ''; roadLayer.innerHTML = ''; groundLayer.innerHTML = ''; footprintLayer.innerHTML = ''; labelLayer.innerHTML = '';
    const isOriginal = dom.mapCanvasContainer ? dom.mapCanvasContainer.classList.contains('original-map') : true;
    if (typeof CampusCalibration.imageLayout === 'function') {
      CampusCalibration.imageLayout(isOriginal ? 'original' : 'dark').forEach(panel => {
        const image = document.createElementNS('http://www.w3.org/2000/svg', 'image');
        image.setAttribute('href', panel.href);
        image.setAttribute('x', panel.x); image.setAttribute('y', panel.y);
        image.setAttribute('width', panel.width); image.setAttribute('height', panel.height);
        image.setAttribute('preserveAspectRatio', panel.preserveAspectRatio);
        image.setAttribute('data-map-panel', panel.id);
        image.setAttribute('class', 'map-panel');
        const title = document.createElementNS('http://www.w3.org/2000/svg', 'title');
        title.textContent = `NSUT campus map · ${panel.id}`;
        image.appendChild(title);
        dom.rasterLayer.appendChild(image);
      });
    }
    const nodeMap = new Map(state.graph.nodes.map(node => [node.id, node]));
    const xy = node => getNodePoint(node);
    const geometryFor = edge => {
      const coords = Array.isArray(edge.geometry) && edge.geometry.length > 1 ? edge.geometry.map(point => CampusCalibration.project(point.lat, point.lng)) : [xy(nodeMap.get(edge.source)), xy(nodeMap.get(edge.target))];
      return coords.filter(point => point && Number.isFinite(point.x) && Number.isFinite(point.y));
    };
    const pathD = points => points.map((point, i) => `${i ? 'L' : 'M'} ${point.x.toFixed(2)} ${point.y.toFixed(2)}`).join(' ');
    state.graph.edges.forEach(edge => {
      const source=nodeMap.get(edge.source), target=nodeMap.get(edge.target), points=geometryFor(edge);
      if(!source||!target||points.length<2)return;
      const d=pathD(points);
      const path=document.createElementNS('http://www.w3.org/2000/svg','path');
      path.setAttribute('d',d);path.setAttribute('class','graph-edge');path.setAttribute('data-id',edge.id);path.setAttribute('data-source',edge.source);path.setAttribute('data-target',edge.target);path.setAttribute('data-road',edge.type||'WALKWAY');path.setAttribute('data-confidence',edge.confidence||'HIGH');path.setAttribute('tabindex','0');path.setAttribute('role','button');path.setAttribute('aria-label',`Walking path between ${source.name} and ${target.name}`);
      const title=document.createElementNS('http://www.w3.org/2000/svg','title');title.textContent=`${source.name} to ${target.name} · ${Math.round(edge.weight)} m`;path.appendChild(title);
      const showEdge=()=>{displayEdgeDetails(edge,source,target);if(dom.detailPanel)dom.detailPanel.style.display='block';};path.addEventListener('click',showEdge);path.addEventListener('keydown',event=>{if(event.key==='Enter'||event.key===' '){event.preventDefault();showEdge();}});roadLayer.appendChild(path);
    });
    state.graph.nodes.forEach(node=>{
      const pt=xy(node),group=document.createElementNS('http://www.w3.org/2000/svg','g');
      group.setAttribute('class','graph-node');group.setAttribute('data-id',node.id);group.setAttribute('data-type',node.type);group.setAttribute('tabindex','0');group.setAttribute('role','button');group.setAttribute('aria-label',`${node.name} (${node.type})`);group.setAttribute('transform',`translate(${pt.x}, ${pt.y})`);
      const color=TYPE_COLORS[node.type]||'#94a3b8',junction=node.type==='JUNCTION';
      const ring=document.createElementNS('http://www.w3.org/2000/svg','circle');ring.setAttribute('r',junction?'11':'14');ring.setAttribute('class','node-selection-ring');ring.setAttribute('stroke',color);
      const dot=document.createElementNS('http://www.w3.org/2000/svg','circle');dot.setAttribute('r',junction?'4.5':'5.5');dot.setAttribute('class','node-core');dot.setAttribute('stroke',color);
      const inner=document.createElementNS('http://www.w3.org/2000/svg','circle');inner.setAttribute('r','2.4');inner.setAttribute('fill',color);
      const label=document.createElementNS('http://www.w3.org/2000/svg','text');label.setAttribute('x','12');label.setAttribute('y','-12');label.setAttribute('class','node-label');label.textContent=node.name.length>24?`${node.name.slice(0,21).trim()}…`:node.name;
      const title=document.createElementNS('http://www.w3.org/2000/svg','title');title.textContent=`${node.name} · ${node.id}`;group.append(title,ring,dot,inner,label);
      group.addEventListener('click',()=>handleNodeClick(node.id));group.addEventListener('keydown',event=>{if(event.key==='Enter'||event.key===' '){event.preventDefault();handleNodeClick(node.id);}});dom.nodesLayer.appendChild(group);
    });
    if(state.route.compareResult?.active)renderComparisonOnMap(state.route.compareResult.dijkstra,state.route.compareResult.bfs,state.route.compareResult.activeFilter);
    else if(state.route.result)renderRouteOnMap(state.route.result);
    else if(state.algorithmDemo.result)renderAlgorithmDemo();
    if (mapIsFit) fitCampus();
  }

  /**
   * Render Searchable & Filterable Building List
   */
  function updateMapRasterTheme(theme) {
    if (!dom.rasterLayer || typeof CampusCalibration.imageLayout !== 'function') return;
    const layout = CampusCalibration.imageLayout(theme);
    const layoutMap = new Map(layout.map(p => [p.id, p.href]));
    dom.rasterLayer.querySelectorAll('image[data-map-panel]').forEach(img => {
      const id = img.getAttribute('data-map-panel');
      if (layoutMap.has(id)) {
        img.setAttribute('href', layoutMap.get(id));
      }
    });
  }

  function renderBuildingList() {
    if (!dom.buildingList) return;
    dom.buildingList.innerHTML = '';

    const query = state.searchQuery.trim().toLowerCase();
    const activeCategory = state.selectedCategory.toUpperCase();

    const filtered = state.buildings.filter((b) => {
      const matchesCategory =
        activeCategory === 'ALL' ||
        b.category.toUpperCase() === activeCategory;

      if (!matchesCategory) return false;
      if (!query) return true;

      return (
        b.name.toLowerCase().includes(query) ||
        b.code.toLowerCase().includes(query) ||
        b.category.toLowerCase().includes(query) ||
        b.description.toLowerCase().includes(query) ||
        (Array.isArray(b.facilities) && b.facilities.some((f) => f.toLowerCase().includes(query)))
      );
    });

    const totalCount = state.buildings.length;
    if (dom.facilityCountBadge) {
      dom.facilityCountBadge.textContent = query || activeCategory !== 'ALL'
        ? `Showing ${filtered.length} of ${totalCount} facilities`
        : `Showing all ${totalCount} facilities`;
    }
    if (dom.mobileCountBadge) {
      dom.mobileCountBadge.textContent = filtered.length;
    }
    if (dom.clearFiltersBtn) {
      dom.clearFiltersBtn.style.display = (query || activeCategory !== 'ALL') ? 'inline-block' : 'none';
    }
    if (dom.clearSearchBtn) {
      dom.clearSearchBtn.style.display = query.length > 0 ? 'flex' : 'none';
    }

    if (filtered.length === 0) {
      dom.buildingList.innerHTML = `
        <div class="empty-search-state">
          <div class="empty-icon" aria-hidden="true">🔍</div>
          <div class="empty-title">No facilities found</div>
          <div class="empty-subtitle">No campus locations match "${escapeHtml(state.searchQuery)}". Try another name or keyword.</div>
          <button class="btn-clear-search" id="btnResetSearch">Clear search &amp; filters</button>
        </div>
      `;
      const btnReset = document.getElementById('btnResetSearch');
      if (btnReset) {
        btnReset.addEventListener('click', resetSearchAndFilters);
      }
      return;
    }

    filtered.forEach((bldg) => {
      const isSelected = state.selectedBuildingId === bldg.id;
      const card = document.createElement('div');
      card.className = `building-card ${isSelected ? 'active' : ''}`;
      card.setAttribute('data-id', bldg.id);
      card.setAttribute('tabindex', '0');
      card.setAttribute('role', 'button');
      card.setAttribute('aria-pressed', isSelected ? 'true' : 'false');
      card.setAttribute('aria-label', `${bldg.name}, ${bldg.category}, ${bldg.nodeId ? `mapped access ${bldg.nodeId}` : 'walking access not mapped'}`);

      const tagClass = (bldg.category || 'academic').toLowerCase();
      const cardAccent = ({ ACADEMIC: '#38bdf8', ADMINISTRATIVE: '#818cf8', LIBRARY: '#c084fc', HOSTEL: '#f59e0b', SPORTS: '#34d399', AMENITY: '#fb923c', FACILITY: '#6bc6a4' })[bldg.category] || '#94a3b8';
      card.style.setProperty('--card-accent', cardAccent);

      card.innerHTML = `
        <div class="bldg-card-header">
          <div class="bldg-card-title">${escapeHtml(bldg.name)}</div>
          <span class="bldg-tag ${tagClass}">${escapeHtml(bldg.code)}</span>
        </div>
      `;

      const marker = document.querySelector(`#graphNodesLayer .graph-node[data-id="${bldg.nodeId}"]`);
      const setHover = active => {
        card.classList.toggle('hovered', active);
        if (marker) marker.classList.toggle('highlighted', active);
      };
      card.addEventListener('pointerenter', () => setHover(true));
      card.addEventListener('pointerleave', () => setHover(false));
      card.addEventListener('focus', () => setHover(true));
      card.addEventListener('blur', () => setHover(false));

      card.addEventListener('click', () => {
        handleBuildingSelect(bldg.id);
      });
      card.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          handleBuildingSelect(bldg.id);
        }
      });

      dom.buildingList.appendChild(card);
    });
  }

  /**
   * Populate Route Finder Select Dropdowns
   */
  function populateRouteSelectors() {
    if (!dom.routeStartSelect || !dom.routeEndSelect) return;

    const prevStart = state.route.startNodeId || dom.routeStartSelect.value;
    const prevEnd = state.route.endNodeId || dom.routeEndSelect.value;

    const buildOptionsHtml = (selectedId) => {
      let html = '<option value="">-- Choose Location --</option>';

      // 1. Campus Facilities & Buildings (Alphabetical)
      const sortedBldgs = state.buildings.filter(b => b.nodeId && state.graph.nodes.some(n => n.id === b.nodeId))
        .sort((a, b) => a.name.localeCompare(b.name));
      if (sortedBldgs.length > 0) {
        html += '<optgroup label="Campus Facilities &amp; Buildings">';
        sortedBldgs.forEach((b) => {
          const isSel = b.nodeId === selectedId ? 'selected' : '';
          html += `<option value="${b.nodeId}" ${isSel}>${escapeHtml(b.name)} (${escapeHtml(b.code)})</option>`;
        });
        html += '</optgroup>';
      }

      // 2. Gates & Boundary Entrances
      const gates = state.graph.nodes.filter((n) => n.type === 'GATE');
      if (gates.length > 0) {
        html += '<optgroup label="Gates &amp; Campus Entrances">';
        gates.forEach((g) => {
          const isSel = g.id === selectedId ? 'selected' : '';
          html += `<option value="${g.id}" ${isSel}>${escapeHtml(g.name)} [${g.id}]</option>`;
        });
        html += '</optgroup>';
      }

      // 3. Road & Walkway Waypoints / Junctions
      const buildingAccessIds = new Set(state.buildings.map((building) => building.nodeId));
      const waypoints = state.graph.nodes.filter((n) => n.type !== 'GATE' && !buildingAccessIds.has(n.id));
      if (waypoints.length > 0) {
        html += '<optgroup label="Road &amp; Walkway Junctions">';
        waypoints.forEach((w) => {
          const isSel = w.id === selectedId ? 'selected' : '';
          html += `<option value="${w.id}" ${isSel}>${escapeHtml(w.name)} [${w.id}]</option>`;
        });
        html += '</optgroup>';
      }

      return html;
    };

    dom.routeStartSelect.innerHTML = buildOptionsHtml(prevStart);
    dom.routeEndSelect.innerHTML = buildOptionsHtml(prevEnd);

    state.route.startNodeId = prevStart;
    state.route.endNodeId = prevEnd;
  }

  function populateAlgorithmSelectors() {
    const locations = [];
    const used = new Set();
    [...state.buildings].filter(building => building.nodeId && state.graph.nodes.some(node => node.id === building.nodeId))
      .sort((a, b) => a.name.localeCompare(b.name)).forEach(building => {
      if (!used.has(building.nodeId)) {
        locations.push({ id: building.nodeId, label: `${building.name} (${building.code})` });
        used.add(building.nodeId);
      }
    });
    state.graph.nodes.filter(node => node.type === 'GATE' || node.type === 'JUNCTION')
      .sort((a, b) => a.name.localeCompare(b.name))
      .forEach(node => {
        if (!used.has(node.id)) {
          locations.push({ id: node.id, label: `${node.name} [${node.id}]` });
          used.add(node.id);
        }
      });
    const fill = (select, placeholder, selected) => {
      if (!select) return;
      select.innerHTML = `<option value="">${placeholder}</option>` + locations.map(item =>
        `<option value="${item.id}" ${item.id === selected ? 'selected' : ''}>${escapeHtml(item.label)}</option>`
      ).join('');
    };
    fill(dom.dfsStartSelect, 'Choose a starting place', dom.dfsStartSelect?.value || 'N001');
    fill(dom.mstStartSelect, 'Main Gate', dom.mstStartSelect?.value || 'N001');
  }

  function setupAlgorithmDemos() {
    dom.btnRunDfs?.addEventListener('click', runDfsDemo);
    dom.btnRunMst?.addEventListener('click', runMstDemo);

    // Setup animation controller event listeners
    setupPlaybackControls('dfs');
    setupPlaybackControls('mst');
  }

  // Animation player state
  const animState = {
    activeType: null, // 'dfs' | 'mst'
    steps: [],
    stepIndex: -1,
    isPlaying: false,
    speed: 1, // 1x = 600ms, 2x = 300ms, 5x = 120ms
    timer: null
  };

  function getIntervalMs() {
    return Math.round(600 / animState.speed);
  }

  function setupPlaybackControls(type) {
    const isDfs = type === 'dfs';
    const btnReset = isDfs ? dom.dfsBtnReset : dom.mstBtnReset;
    const btnPrev = isDfs ? dom.dfsBtnPrev : dom.mstBtnPrev;
    const btnPlayPause = isDfs ? dom.dfsBtnPlayPause : dom.mstBtnPlayPause;
    const btnNext = isDfs ? dom.dfsBtnNext : dom.mstBtnNext;
    const speedGroup = isDfs ? dom.dfsSpeedGroup : dom.mstSpeedGroup;
    const slider = isDfs ? dom.dfsSlider : dom.mstSlider;

    btnPlayPause?.addEventListener('click', () => {
      if (animState.activeType !== type) return;
      if (animState.isPlaying) {
        pauseAnimation();
      } else {
        playAnimation();
      }
    });

    btnReset?.addEventListener('click', () => {
      if (animState.activeType !== type) return;
      pauseAnimation();
      seekStep(0);
    });

    btnPrev?.addEventListener('click', () => {
      if (animState.activeType !== type) return;
      pauseAnimation();
      stepBackward();
    });

    btnNext?.addEventListener('click', () => {
      if (animState.activeType !== type) return;
      pauseAnimation();
      stepForward();
    });

    slider?.addEventListener('input', (e) => {
      if (animState.activeType !== type) return;
      pauseAnimation();
      seekStep(parseInt(e.target.value, 10));
    });

    speedGroup?.querySelectorAll('.speed-pill').forEach(btn => {
      btn.addEventListener('click', () => {
        speedGroup.querySelectorAll('.speed-pill').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        animState.speed = parseFloat(btn.dataset.speed) || 1;
        if (animState.isPlaying) {
          clearInterval(animState.timer);
          animState.timer = setInterval(stepForward, getIntervalMs());
        }
      });
    });
  }

  function playAnimation() {
    if (animState.isPlaying || animState.steps.length === 0) return;
    if (animState.stepIndex >= animState.steps.length - 1) {
      animState.stepIndex = -1;
    }
    animState.isPlaying = true;
    updatePlayPauseButton();
    animState.timer = setInterval(stepForward, getIntervalMs());
  }

  function pauseAnimation() {
    animState.isPlaying = false;
    if (animState.timer) {
      clearInterval(animState.timer);
      animState.timer = null;
    }
    updatePlayPauseButton();
  }

  function updatePlayPauseButton() {
    const isDfs = animState.activeType === 'dfs';
    const btn = isDfs ? dom.dfsBtnPlayPause : dom.mstBtnPlayPause;
    if (btn) {
      btn.textContent = animState.isPlaying ? '⏸' : '▶';
      btn.title = animState.isPlaying ? 'Pause' : 'Play';
    }
  }

  function stepForward() {
    if (animState.stepIndex < animState.steps.length - 1) {
      seekStep(animState.stepIndex + 1);
    } else {
      pauseAnimation();
    }
  }

  function stepBackward() {
    if (animState.stepIndex > 0) {
      seekStep(animState.stepIndex - 1);
    }
  }

  function seekStep(index) {
    if (index < 0 || index >= animState.steps.length) return;
    animState.stepIndex = index;
    renderCurrentAnimationStep();
  }

  function startAnimation(type, steps) {
    pauseAnimation();
    animState.activeType = type;
    animState.steps = steps;
    animState.stepIndex = 0;

    const isDfs = type === 'dfs';
    const toolbar = isDfs ? dom.dfsPlaybackControls : dom.mstPlaybackControls;
    const slider = isDfs ? dom.dfsSlider : dom.mstSlider;

    if (toolbar) toolbar.style.display = 'flex';
    if (slider) {
      slider.max = steps.length - 1;
      slider.value = 0;
    }

    renderCurrentAnimationStep();
    playAnimation();
  }

  /* ---------------------------------------------------------
     DFS STEP GENERATOR
     --------------------------------------------------------- */
  function generateDfsSteps(startNodeId) {
    const steps = [];
    const nodeMap = new Map(state.graph.nodes.map(n => [n.id, n]));
    const adj = new Map();
    state.graph.nodes.forEach(n => adj.set(n.id, []));

    state.graph.edges.forEach(e => {
      if (adj.has(e.source) && adj.has(e.target)) {
        adj.get(e.source).push({ target: e.target, edge: e });
        adj.get(e.target).push({ target: e.source, edge: e });
      }
    });

    const visitedSet = new Set();
    const traversedEdges = [];
    const callStack = [];

    function dfs(u, parent, viaEdge) {
      visitedSet.add(u);
      callStack.push(u);
      if (viaEdge) traversedEdges.push(viaEdge);

      const uNode = nodeMap.get(u);
      const uName = uNode ? uNode.name : u;

      steps.push({
        type: 'DFS',
        subType: 'VISIT',
        activeNode: u,
        visitedNodes: new Set(visitedSet),
        traversedEdges: [...traversedEdges],
        stack: [...callStack],
        msg: `<span class="status-tag visiting">Visiting</span> Exploring <strong>${escapeHtml(uName)}</strong> (${visitedSet.size} of ${state.graph.nodes.length} points reached)`
      });

      const neighbors = adj.get(u) || [];
      for (const { target: v, edge } of neighbors) {
        if (!visitedSet.has(v)) {
          dfs(v, u, edge);

          // Backtrack step
          steps.push({
            type: 'DFS',
            subType: 'BACKTRACK',
            activeNode: u,
            visitedNodes: new Set(visitedSet),
            traversedEdges: [...traversedEdges],
            stack: [...callStack],
            msg: `<span class="status-tag backtrack">Backtrack</span> Backtracking to <strong>${escapeHtml(uName)}</strong>`
          });
        }
      }

      callStack.pop();
    }

    dfs(startNodeId, null, null);

    steps.push({
      type: 'DFS',
      subType: 'COMPLETE',
      activeNode: null,
      visitedNodes: new Set(visitedSet),
      traversedEdges: [...traversedEdges],
      stack: [],
      msg: `<span class="status-tag complete">Complete</span> Depth-First Search complete! Visited ${visitedSet.size} nodes.`
    });

    return steps;
  }

  /* ---------------------------------------------------------
     KRUSKAL MST STEP GENERATOR
     --------------------------------------------------------- */
  function generateKruskalSteps() {
    const steps = [];
    const nodeMap = new Map(state.graph.nodes.map(n => [n.id, n]));
    const sortedEdges = state.graph.edges.slice().sort((a, b) => a.weight - b.weight);

    // DSU structure
    const parent = new Map();
    state.graph.nodes.forEach(n => parent.set(n.id, n.id));

    function find(i) {
      if (parent.get(i) === i) return i;
      const root = find(parent.get(i));
      parent.set(i, root);
      return root;
    }

    function union(i, j) {
      const rootI = find(i);
      const rootJ = find(j);
      if (rootI !== rootJ) {
        parent.set(rootI, rootJ);
        return true;
      }
      return false;
    }

    const acceptedEdges = [];
    const rejectedEdgeMap = new Map();
    let totalWeight = 0;

    for (const edge of sortedEdges) {
      const sName = nodeMap.get(edge.source)?.name || edge.source;
      const tName = nodeMap.get(edge.target)?.name || edge.target;
      const w = Math.round(edge.weight);

      // 1. Consideration step
      steps.push({
        type: 'MST',
        subType: 'CONSIDER',
        activeEdge: edge,
        acceptedEdges: [...acceptedEdges],
        totalWeight,
        msg: `<span class="status-tag considering">Evaluating</span> Segment <strong>${edge.id}</strong>: ${escapeHtml(sName)} ↔ ${escapeHtml(tName)} (${w} m)`
      });

      const rootS = find(edge.source);
      const rootT = find(edge.target);

      if (rootS === rootT) {
        // Cycle detected!
        rejectedEdgeMap.set(edge.id, edge);
        steps.push({
          type: 'MST',
          subType: 'REJECT',
          activeEdge: edge,
          acceptedEdges: [...acceptedEdges],
          totalWeight,
          msg: `<span class="status-tag rejected">Cycle Detected</span> Discarding <strong>${edge.id}</strong> (${w} m) — creates a redundant cycle!`
        });
      } else {
        union(edge.source, edge.target);
        acceptedEdges.push(edge);
        totalWeight += edge.weight;

        steps.push({
          type: 'MST',
          subType: 'ACCEPT',
          activeEdge: edge,
          acceptedEdges: [...acceptedEdges],
          totalWeight,
          msg: `<span class="status-tag accepted">Added to Tree</span> Accepted <strong>${edge.id}</strong> (${w} m). Spanning tree: ${acceptedEdges.length} edges, ${Math.round(totalWeight)} m`
        });
      }
    }

    steps.push({
      type: 'MST',
      subType: 'COMPLETE',
      activeEdge: null,
      acceptedEdges: [...acceptedEdges],
      totalWeight,
      msg: `<span class="status-tag complete">Complete</span> Kruskal's MST finished! ${acceptedEdges.length} edges spanning all components, total length ${Math.round(totalWeight)} m.`
    });

    return steps;
  }

  /* ---------------------------------------------------------
     PRIM MST STEP GENERATOR
     --------------------------------------------------------- */
  function generatePrimSteps(startNodeId) {
    const steps = [];
    const nodeMap = new Map(state.graph.nodes.map(n => [n.id, n]));
    const inTree = new Set([startNodeId]);
    const acceptedEdges = [];
    let totalWeight = 0;

    const startName = nodeMap.get(startNodeId)?.name || startNodeId;
    steps.push({
      type: 'MST',
      subType: 'START',
      activeEdge: null,
      inTreeNodes: new Set(inTree),
      acceptedEdges: [],
      totalWeight: 0,
      msg: `<span class="status-tag visiting">Root</span> Prim's tree seeded at <strong>${escapeHtml(startName)}</strong>`
    });

    // Run Prim until no more cut edges can be chosen
    while (true) {
      let minEdge = null;
      let minTarget = null;
      const candidateCutEdges = [];

      for (const e of state.graph.edges) {
        const uIn = inTree.has(e.source);
        const vIn = inTree.has(e.target);
        if ((uIn && !vIn) || (!uIn && vIn)) {
          candidateCutEdges.push(e);
          if (!minEdge || e.weight < minEdge.weight) {
            minEdge = e;
            minTarget = uIn ? e.target : e.source;
          }
        }
      }

      if (!minEdge) break;

      const tName = nodeMap.get(minTarget)?.name || minTarget;
      const w = Math.round(minEdge.weight);

      steps.push({
        type: 'MST',
        subType: 'CONSIDER',
        activeEdge: minEdge,
        candidateCutEdges: [...candidateCutEdges],
        inTreeNodes: new Set(inTree),
        acceptedEdges: [...acceptedEdges],
        totalWeight,
        msg: `<span class="status-tag considering">Min Cut</span> Smallest crossing edge is <strong>${minEdge.id}</strong> (${w} m) reaching ${escapeHtml(tName)}`
      });

      inTree.add(minTarget);
      acceptedEdges.push(minEdge);
      totalWeight += minEdge.weight;

      steps.push({
        type: 'MST',
        subType: 'ACCEPT',
        activeEdge: minEdge,
        inTreeNodes: new Set(inTree),
        acceptedEdges: [...acceptedEdges],
        totalWeight,
        msg: `<span class="status-tag accepted">Expanded</span> Tree grown to <strong>${escapeHtml(tName)}</strong> (${inTree.size} nodes, ${Math.round(totalWeight)} m)`
      });
    }

    steps.push({
      type: 'MST',
      subType: 'COMPLETE',
      activeEdge: null,
      inTreeNodes: new Set(inTree),
      acceptedEdges: [...acceptedEdges],
      totalWeight,
      msg: `<span class="status-tag complete">Complete</span> Prim's MST finished! ${acceptedEdges.length} edges, total length ${Math.round(totalWeight)} m.`
    });

    return steps;
  }

  /* ---------------------------------------------------------
     RENDER ANIMATION STEP ON SVG & UI
     --------------------------------------------------------- */
  function renderCurrentAnimationStep() {
    clearAlgorithmVisuals();
    if (!animState.steps || animState.stepIndex < 0 || animState.stepIndex >= animState.steps.length) return;

    const step = animState.steps[animState.stepIndex];
    const isDfs = animState.activeType === 'dfs';
    const badge = isDfs ? dom.dfsStepBadge : dom.mstStepBadge;
    const slider = isDfs ? dom.dfsSlider : dom.mstSlider;
    const statusMsg = isDfs ? dom.dfsStatusMsg : dom.mstStatusMsg;

    if (badge) badge.textContent = `Step ${animState.stepIndex + 1} / ${animState.steps.length}`;
    if (slider) slider.value = animState.stepIndex;
    if (statusMsg) statusMsg.innerHTML = step.msg || '';

    if (step.type === 'DFS') {
      // 1. Highlight visited nodes
      if (step.visitedNodes) {
        dom.nodesLayer?.querySelectorAll('.graph-node').forEach(node => {
          const id = node.getAttribute('data-id');
          node.classList.toggle('dfs-visited', step.visitedNodes.has(id));
          node.classList.toggle('dfs-active', id === step.activeNode);
        });
      }

      // 2. Render traversed edges
      (step.traversedEdges || []).forEach(edge => {
        drawSvgEdge(edge, 'algorithm-visual algorithm-edge-traversed');
      });
    } else if (step.type === 'MST') {
      // 1. Draw accepted tree edges
      (step.acceptedEdges || []).forEach(edge => {
        drawSvgEdge(edge, 'algorithm-visual mst-edge-accepted');
      });

      // 2. Draw candidate cut edges if in Prim's
      (step.candidateCutEdges || []).forEach(edge => {
        if (edge.id !== step.activeEdge?.id) {
          drawSvgEdge(edge, 'algorithm-visual mst-candidate-cut');
        }
      });

      // 3. Draw active edge (considering / rejected)
      if (step.activeEdge) {
        const cls = step.subType === 'REJECT'
          ? 'algorithm-visual mst-edge-rejected'
          : (step.subType === 'CONSIDER' ? 'algorithm-visual mst-edge-considering' : 'algorithm-visual mst-edge-accepted');
        drawSvgEdge(step.activeEdge, cls);
      }

      // 4. Highlight tree nodes
      if (step.inTreeNodes) {
        dom.nodesLayer?.querySelectorAll('.graph-node').forEach(node => {
          const id = node.getAttribute('data-id');
          node.classList.toggle('mst-active', step.inTreeNodes.has(id));
        });
      }
    }
  }

  function drawSvgEdge(edge, cssClass) {
    if (!edge || !edge.geometry || edge.geometry.length < 2) return;
    const points = edge.geometry.map(p => CampusCalibration.project(p.lat, p.lng));
    const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    path.setAttribute('d', points.map((p, i) => `${i ? 'L' : 'M'} ${p.x.toFixed(2)} ${p.y.toFixed(2)}`).join(' '));
    path.setAttribute('class', cssClass);
    dom.edgesLayer?.appendChild(path);
  }

  async function runDfsDemo() {
    const startNodeId = dom.dfsStartSelect?.value;
    if (!startNodeId) {
      showAlgorithmResult(dom.dfsResult, 'Choose a starting place to run DFS.', true);
      return;
    }
    clearAlgorithmVisuals();
    clearRoute();
    if (dom.btnRunDfs) { dom.btnRunDfs.disabled = true; dom.btnRunDfs.textContent = 'Traversing…'; }
    try {
      const response = await ApiClient.executeAlgorithm({ algorithm: 'DFS', startNodeId, wheelchairAccessibleOnly: false });
      if (!response?.success || !response.data) throw new Error(response?.message || 'DFS traversal failed.');
      const result = response.data;
      state.algorithmDemo = { type: 'DFS', result };

      const names = new Map(state.graph.nodes.map(node => [node.id, node.name]));
      const order = (result.visitedOrder || []).map(id => names.get(id) || id);

      showAlgorithmResult(dom.dfsResult,
        `<strong>Visited ${order.length} of ${state.graph.nodes.length} mapped points</strong> from ${escapeHtml(names.get(startNodeId) || startNodeId)}.<br>` +
        `${result.connectedComponentsCount ?? '1'} connected component${result.connectedComponentsCount === 1 ? '' : 's'} • ${result.cycleDetected ? 'cycle found' : 'no cycle found'} • ${result.executionTimeMicros || 0} µs` +
        `<details><summary>Traversal sequence (${order.length} points)</summary><ol>${order.map(name => `<li>${escapeHtml(name)}</li>`).join('')}</ol></details>`);

      // Start step-by-step interactive animation
      const steps = generateDfsSteps(startNodeId);
      startAnimation('dfs', steps);
    } catch (error) {
      showAlgorithmResult(dom.dfsResult, error.message || 'DFS traversal failed.', true);
    } finally {
      if (dom.btnRunDfs) { dom.btnRunDfs.disabled = false; dom.btnRunDfs.textContent = 'Run DFS traversal'; }
    }
  }

  async function runMstDemo() {
    const algorithm = dom.mstAlgorithmSelect?.value || 'PRIMS';
    const startNodeId = algorithm === 'PRIMS' ? (dom.mstStartSelect?.value || 'N001') : undefined;
    clearAlgorithmVisuals();
    clearRoute();
    if (dom.btnRunMst) { dom.btnRunMst.disabled = true; dom.btnRunMst.textContent = 'Building…'; }
    try {
      const response = await ApiClient.executeAlgorithm({ algorithm, startNodeId, wheelchairAccessibleOnly: false });
      if (!response?.success || !response.data) throw new Error(response?.message || 'Spanning tree calculation failed.');
      state.algorithmDemo = { type: 'MST', result: response.data };

      const result = response.data;
      const count = (result.pathEdges || []).length;
      const components = result.connectedComponentsCount || 1;
      const label = components > 1 ? 'spanning forest' : 'spanning tree';
      showAlgorithmResult(dom.mstResult,
        `<strong>${escapeHtml(algorithm === 'PRIMS' ? "Prim's algorithm" : "Kruskal's algorithm")}: ${count} selected segments</strong><br>` +
        `${Math.round(result.totalWeight || 0)} m total • ${components} connected component${components === 1 ? '' : 's'} • ${result.executionTimeMicros || 0} µs` +
        `<div class="algorithm-result-note">The highlighted ${label} connects the road/walkway network with minimal path length.</div>`);

      // Start step-by-step interactive animation
      let steps;
      if (algorithm === 'KRUSKALS') {
        steps = generateKruskalSteps();
      } else {
        steps = generatePrimSteps(startNodeId || 'N001');
      }
      startAnimation('mst', steps);
    } catch (error) {
      showAlgorithmResult(dom.mstResult, error.message || 'Spanning tree calculation failed.', true);
    } finally {
      if (dom.btnRunMst) { dom.btnRunMst.disabled = false; dom.btnRunMst.textContent = 'Build spanning tree'; }
    }
  }

  function showAlgorithmResult(target, html, isError = false) {
    if (!target) return;
    target.innerHTML = html;
    target.classList.toggle('error', isError);
    target.style.display = 'block';
  }

  function clearAlgorithmVisuals() {
    dom.nodesLayer?.querySelectorAll('.graph-node').forEach(node => {
      node.classList.remove('dfs-visited', 'dfs-active', 'mst-visited', 'mst-active');
    });
    dom.edgesLayer?.querySelectorAll('.algorithm-visual').forEach(path => path.remove());
  }

  function clearAlgorithmDemo() {
    pauseAnimation();
    state.algorithmDemo = { type: '', result: null };
    clearAlgorithmVisuals();
    if (dom.dfsPlaybackControls) dom.dfsPlaybackControls.style.display = 'none';
    if (dom.mstPlaybackControls) dom.mstPlaybackControls.style.display = 'none';
    if (dom.dfsResult) dom.dfsResult.style.display = 'none';
    if (dom.mstResult) dom.mstResult.style.display = 'none';
  }

  function renderAlgorithmDemo() {
    // Kept for initial API callback fallback
  }

  /**
   * Setup Route Finder Controls & Listeners
   */
  function setupRouteFinder() {
    if (dom.routeStartSelect) {
      dom.routeStartSelect.addEventListener('change', (e) => {
        state.route.startNodeId = e.target.value;
        clearRouteAlert();
      });
    }

    if (dom.routeEndSelect) {
      dom.routeEndSelect.addEventListener('change', (e) => {
        state.route.endNodeId = e.target.value;
        clearRouteAlert();
      });
    }

    if (dom.routeSwapBtn) {
      dom.routeSwapBtn.addEventListener('click', () => {
        const temp = state.route.startNodeId;
        state.route.startNodeId = state.route.endNodeId;
        state.route.endNodeId = temp;

        if (dom.routeStartSelect) dom.routeStartSelect.value = state.route.startNodeId;
        if (dom.routeEndSelect) dom.routeEndSelect.value = state.route.endNodeId;
        clearRouteAlert();
      });
    }

    // Algorithm radio selection
    dom.algoRadioInputs.forEach((radio) => {
      radio.addEventListener('change', (e) => {
        state.route.algorithm = e.target.value;
        if (dom.algoOptDijkstra) dom.algoOptDijkstra.classList.toggle('active', state.route.algorithm === 'DIJKSTRA');
        if (dom.algoOptBfs) dom.algoOptBfs.classList.toggle('active', state.route.algorithm === 'BFS');
      });
    });

    if (dom.wheelchairOnlyCheck) {
      dom.wheelchairOnlyCheck.addEventListener('change', (e) => {
        state.route.wheelchairOnly = e.target.checked;
      });
    }

    if (dom.btnFindRoute) {
      dom.btnFindRoute.addEventListener('click', findRoute);
    }

    if (dom.btnCompareRoute) {
      dom.btnCompareRoute.addEventListener('click', compareRoutes);
    }

    if (dom.btnClearRoute) {
      dom.btnClearRoute.addEventListener('click', clearRoute);
    }

    if (dom.clearRouteMapBtn) {
      dom.clearRouteMapBtn.addEventListener('click', clearRoute);
    }

    if (dom.btnLoadDemoPair) {
      dom.btnLoadDemoPair.addEventListener('click', () => {
        state.route.startNodeId = 'N001'; // Main Gate
        state.route.endNodeId = 'N005';   // Block 5
        if (dom.routeStartSelect) dom.routeStartSelect.value = 'N001';
        if (dom.routeEndSelect) dom.routeEndSelect.value = 'N005';
        compareRoutes();
      });
    }
  }

  /**
   * Setup Phase 7 Comparison Layer Filter Controls
   */
  function setupComparisonControls() {
    if (dom.filterBothRoutes) {
      dom.filterBothRoutes.addEventListener('click', () => setCompareViewFilter('both'));
    }
    if (dom.filterDijkstraOnly) {
      dom.filterDijkstraOnly.addEventListener('click', () => setCompareViewFilter('dijkstra'));
    }
    if (dom.filterBfsOnly) {
      dom.filterBfsOnly.addEventListener('click', () => setCompareViewFilter('bfs'));
    }
  }

  function setCompareViewFilter(filterName) {
    if (!state.route.compareResult.active) return;
    state.route.compareResult.activeFilter = filterName;

    if (dom.filterBothRoutes) {
      dom.filterBothRoutes.classList.toggle('active', filterName === 'both');
      dom.filterBothRoutes.setAttribute('aria-selected', filterName === 'both');
    }
    if (dom.filterDijkstraOnly) {
      dom.filterDijkstraOnly.classList.toggle('active', filterName === 'dijkstra');
      dom.filterDijkstraOnly.setAttribute('aria-selected', filterName === 'dijkstra');
    }
    if (dom.filterBfsOnly) {
      dom.filterBfsOnly.classList.toggle('active', filterName === 'bfs');
      dom.filterBfsOnly.setAttribute('aria-selected', filterName === 'bfs');
    }

    if (dom.mapCanvasContainer) {
      dom.mapCanvasContainer.classList.toggle('hide-dijkstra', filterName === 'bfs');
      dom.mapCanvasContainer.classList.toggle('hide-bfs', filterName === 'dijkstra');
    }
  }

  /**
   * Setup Sidebar Navigation Mode Tabs
   */
  function setupSidebarModeNav() {
    if (dom.modeDirectoryBtn) {
      dom.modeDirectoryBtn.addEventListener('click', () => switchSidebarMode('directory'));
    }
    if (dom.modeRouteBtn) {
      dom.modeRouteBtn.addEventListener('click', () => switchSidebarMode('route'));
    }
    if (dom.modeAlgorithmBtn) {
      dom.modeAlgorithmBtn.addEventListener('click', () => switchSidebarMode('algorithms'));
    }
  }

  function switchSidebarMode(targetMode) {
    state.currentSidebarMode = targetMode;
    if (dom.detailPanel) dom.detailPanel.style.display = 'none';

    if (dom.modeDirectoryBtn) {
      dom.modeDirectoryBtn.classList.toggle('active', targetMode === 'directory');
      dom.modeDirectoryBtn.setAttribute('aria-selected', targetMode === 'directory' ? 'true' : 'false');
    }
    if (dom.modeRouteBtn) {
      dom.modeRouteBtn.classList.toggle('active', targetMode === 'route');
      dom.modeRouteBtn.setAttribute('aria-selected', targetMode === 'route' ? 'true' : 'false');
    }
    if (dom.modeAlgorithmBtn) {
      dom.modeAlgorithmBtn.classList.toggle('active', targetMode === 'algorithms');
      dom.modeAlgorithmBtn.setAttribute('aria-selected', targetMode === 'algorithms' ? 'true' : 'false');
    }

    if (dom.directoryModeContainer) {
      dom.directoryModeContainer.style.display = targetMode === 'directory' ? 'flex' : 'none';
    }
    if (dom.routeModeContainer) {
      dom.routeModeContainer.style.display = targetMode === 'route' ? 'flex' : 'none';
    }
    if (dom.algorithmModeContainer) {
      dom.algorithmModeContainer.style.display = targetMode === 'algorithms' ? 'flex' : 'none';
    }
  }

  /**
   * Route Alert Box Controller
   */
  function showRouteAlert(message, type = 'error') {
    if (!dom.routeAlertBox) return;
    const icon = type === 'error' ? '❌' : (type === 'warning' ? '⚠️' : 'ℹ️');
    dom.routeAlertBox.className = `route-alert ${type}`;
    dom.routeAlertBox.innerHTML = `<span>${icon}</span> <div>${escapeHtml(message)}</div>`;
    dom.routeAlertBox.style.display = 'flex';
  }

  function clearRouteAlert() {
    if (!dom.routeAlertBox) return;
    dom.routeAlertBox.style.display = 'none';
    dom.routeAlertBox.innerHTML = '';
  }

  /**
   * Validation Helper for Route Endpoints
   */
  function validateEndpoints() {
    const startId = (state.route.startNodeId || '').trim();
    const endId = (state.route.endNodeId || '').trim();

    if (!startId) {
      showRouteAlert('Please select a starting campus location.', 'error');
      if (dom.routeStartSelect) dom.routeStartSelect.focus();
      return null;
    }
    if (!endId) {
      showRouteAlert('Please select a destination campus location.', 'error');
      if (dom.routeEndSelect) dom.routeEndSelect.focus();
      return null;
    }

    if (startId === endId) {
      const node = state.graph.nodes.find(n => n.id === startId);
      const name = node ? node.name : startId;
      showRouteAlert(`Start and destination are the same location (${name}). Please choose different endpoints to calculate a route.`, 'warning');
      return null;
    }

    const startNode = state.graph.nodes.find(n => n.id === startId);
    const endNode = state.graph.nodes.find(n => n.id === endId);
    if (!startNode) {
      showRouteAlert(`Start location not found with ID: ${startId}`, 'error');
      return null;
    }
    if (!endNode) {
      showRouteAlert(`Destination location not found with ID: ${endId}`, 'error');
      return null;
    }

    return { startId, endId, startNode, endNode };
  }

  /**
   * Main Route Calculation Workflow (Single Route)
   */
  async function findRoute() {
    clearAlgorithmDemo();
    clearRouteAlert();
    if (dom.routeCompareCard) dom.routeCompareCard.style.display = 'none';
    state.route.compareResult.active = false;

    const validated = validateEndpoints();
    if (!validated) return;
    const { startId, endId, startNode, endNode } = validated;

    const algo = state.route.algorithm || 'DIJKSTRA';
    const wheelchair = Boolean(state.route.wheelchairOnly);

    if (dom.btnFindRoute) {
      dom.btnFindRoute.disabled = true;
      dom.btnFindRoute.textContent = 'Finding route…';
    }

    try {
      const payload = {
        algorithm: algo,
        startNodeId: startId,
        endNodeId: endId,
        wheelchairAccessibleOnly: wheelchair
      };

      const response = await ApiClient.executeAlgorithm(payload);

      if (!response || !response.success || !response.data) {
        showRouteAlert(response && response.message ? response.message : 'Path calculation failed.', 'error');
        return;
      }

      const result = response.data;

      if (!result.reachable || !result.pathNodes || result.pathNodes.length === 0) {
        showRouteAlert(`No walking route exists between ${startNode.name} and ${endNode.name}${wheelchair ? ' with wheelchair accessibility enabled.' : '.'}`, 'warning');
        return;
      }

      state.route.result = result;
      displayRouteResult(result, startNode, endNode);
      renderRouteOnMap(result);

      if (window.innerWidth <= 900) {
        switchMobileTab('map');
      }

    } catch (err) {
      console.error('Route calculation error:', err);
      showRouteAlert(err.message || 'Network error while contacting the server.', 'error');
    } finally {
      if (dom.btnFindRoute) {
        dom.btnFindRoute.disabled = false;
        dom.btnFindRoute.textContent = 'Show route';
      }
    }
  }

  /**
   * Phase 7: Parallel BFS vs Dijkstra Algorithm Comparison
   */
  async function compareRoutes() {
    clearAlgorithmDemo();
    clearRouteAlert();
    if (dom.routeResultCard) dom.routeResultCard.style.display = 'none';
    state.route.result = null;

    const validated = validateEndpoints();
    if (!validated) return;
    const { startId, endId, startNode, endNode } = validated;

    const wheelchair = Boolean(state.route.wheelchairOnly);

    if (dom.btnCompareRoute) {
      dom.btnCompareRoute.disabled = true;
      dom.btnCompareRoute.textContent = 'Comparing…';
    }

    try {
      // Execute both algorithms in parallel against the exact same graph settings
      const dijkstraPayload = {
        algorithm: 'DIJKSTRA',
        startNodeId: startId,
        endNodeId: endId,
        wheelchairAccessibleOnly: wheelchair
      };
      const bfsPayload = {
        algorithm: 'BFS',
        startNodeId: startId,
        endNodeId: endId,
        wheelchairAccessibleOnly: wheelchair
      };

      const [dijkstraSettled, bfsSettled] = await Promise.allSettled([
        ApiClient.executeAlgorithm(dijkstraPayload),
        ApiClient.executeAlgorithm(bfsPayload)
      ]);

      const dijkstraRes = (dijkstraSettled.status === 'fulfilled' && dijkstraSettled.value.success)
        ? dijkstraSettled.value.data
        : null;
      const bfsRes = (bfsSettled.status === 'fulfilled' && bfsSettled.value.success)
        ? bfsSettled.value.data
        : null;

      if (!dijkstraRes && !bfsRes) {
        const errMsg = dijkstraSettled.status === 'rejected'
          ? (dijkstraSettled.reason ? dijkstraSettled.reason.message : 'Dijkstra failed')
          : (bfsSettled.reason ? bfsSettled.reason.message : 'BFS failed');
        showRouteAlert(errMsg || 'Both algorithms failed to execute.', 'error');
        return;
      }

      state.route.compareResult = {
        active: true,
        dijkstra: dijkstraRes,
        bfs: bfsRes,
        activeFilter: 'both'
      };

      displayComparisonResult(dijkstraRes, bfsRes, startNode, endNode);
      renderComparisonOnMap(dijkstraRes, bfsRes, 'both');

      if (window.innerWidth <= 900) {
        switchMobileTab('map');
      }

    } catch (err) {
      console.error('Comparison error:', err);
      showRouteAlert(err.message || 'Error occurred during algorithm comparison.', 'error');
    } finally {
      if (dom.btnCompareRoute) {
        dom.btnCompareRoute.disabled = false;
        dom.btnCompareRoute.textContent = 'Compare';
      }
    }
  }

  /**
   * Display Side-by-Side Comparison Metrics & Insight
   */
  function displayComparisonResult(dijkstra, bfs, startNode, endNode) {
    if (!dom.routeCompareCard) return;

    if (dom.compareTitle) {
      dom.compareTitle.textContent = `${placeNameForNode(startNode)} → ${placeNameForNode(endNode)}`;
    }

    // Reset filter chips
    setCompareViewFilter('both');

    // 1. Populate Dijkstra Column
    if (dijkstra && dijkstra.reachable) {
      if (dom.dijkstraDistVal) dom.dijkstraDistVal.textContent = `${Math.round(dijkstra.totalWeight || 0)} m`;
      if (dom.dijkstraHopsVal) dom.dijkstraHopsVal.textContent = `${dijkstra.hopCount || (dijkstra.pathEdges ? dijkstra.pathEdges.length : 0)} segments`;
      if (dom.dijkstraExploredVal) dom.dijkstraExploredVal.textContent = `${dijkstra.visitedOrder ? dijkstra.visitedOrder.length : (dijkstra.pathNodes ? dijkstra.pathNodes.length : 0)} nodes`;
      if (dom.dijkstraTimeVal) {
        const us = dijkstra.executionTimeMicros || 0;
        dom.dijkstraTimeVal.textContent = us >= 1000 ? `${(us / 1000).toFixed(2)} ms` : `${us} µs`;
      }

      const hasLowConfDijk = (Array.isArray(dijkstra.pathEdges) && dijkstra.pathEdges.some(e => e.confidence === 'LOW')) || routeHasApproxAccess(startNode, endNode);
      if (dom.dijkstraAdvisory) {
        dom.dijkstraAdvisory.textContent = routeHasApproxAccess(startNode, endNode) ? 'A destination uses the nearest mapped walkway point; the exact door is not shown.' : 'This route contains an approximate path segment.';
        dom.dijkstraAdvisory.style.display = hasLowConfDijk ? 'block' : 'none';
      }

      // Waypoints
      if (dom.dijkstraStepCount) dom.dijkstraStepCount.textContent = dijkstra.pathNodes.length;
      if (dom.dijkstraStepsList) {
        dom.dijkstraStepsList.innerHTML = dijkstra.pathNodes.map((n, i) => `
          <li class="compare-step-item" data-node-id="${n.id}">
            <span>${i + 1}. ${escapeHtml(n.name)}</span>
            <span style="color:var(--text-subtle); font-size:0.65rem;">${n.id}</span>
          </li>
        `).join('');

        dom.dijkstraStepsList.querySelectorAll('.compare-step-item').forEach(item => {
          item.addEventListener('click', () => {
            const nid = item.getAttribute('data-node-id');
            if (nid) {
              focusNodeOnMap(nid);
              highlightNode(nid);
              if (window.innerWidth <= 900) switchMobileTab('map');
            }
          });
        });
      }
    } else {
      if (dom.dijkstraDistVal) dom.dijkstraDistVal.textContent = 'Unreachable';
      if (dom.dijkstraHopsVal) dom.dijkstraHopsVal.textContent = '—';
      if (dom.dijkstraExploredVal) dom.dijkstraExploredVal.textContent = dijkstra && dijkstra.visitedOrder ? `${dijkstra.visitedOrder.length} nodes` : '—';
      if (dom.dijkstraStepsList) dom.dijkstraStepsList.innerHTML = '<li style="color:var(--accent-rose);">No route found</li>';
    }

    // 2. Populate BFS Column
    if (bfs && bfs.reachable) {
      if (dom.bfsDistVal) dom.bfsDistVal.textContent = `${Math.round(bfs.totalWeight || 0)} m`;
      if (dom.bfsHopsVal) dom.bfsHopsVal.textContent = `${bfs.hopCount || (bfs.pathEdges ? bfs.pathEdges.length : 0)} segments`;
      if (dom.bfsExploredVal) dom.bfsExploredVal.textContent = `${bfs.visitedOrder ? bfs.visitedOrder.length : (bfs.pathNodes ? bfs.pathNodes.length : 0)} nodes`;
      if (dom.bfsTimeVal) {
        const us = bfs.executionTimeMicros || 0;
        dom.bfsTimeVal.textContent = us >= 1000 ? `${(us / 1000).toFixed(2)} ms` : `${us} µs`;
      }

      const hasLowConfBfs = (Array.isArray(bfs.pathEdges) && bfs.pathEdges.some(e => e.confidence === 'LOW')) || routeHasApproxAccess(startNode, endNode);
      if (dom.bfsAdvisory) {
        dom.bfsAdvisory.textContent = routeHasApproxAccess(startNode, endNode) ? 'A destination uses the nearest mapped walkway point; the exact door is not shown.' : 'This route contains an approximate path segment.';
        dom.bfsAdvisory.style.display = hasLowConfBfs ? 'block' : 'none';
      }

      // Waypoints
      if (dom.bfsStepCount) dom.bfsStepCount.textContent = bfs.pathNodes.length;
      if (dom.bfsStepsList) {
        dom.bfsStepsList.innerHTML = bfs.pathNodes.map((n, i) => `
          <li class="compare-step-item" data-node-id="${n.id}">
            <span>${i + 1}. ${escapeHtml(n.name)}</span>
            <span style="color:var(--text-subtle); font-size:0.65rem;">${n.id}</span>
          </li>
        `).join('');

        dom.bfsStepsList.querySelectorAll('.compare-step-item').forEach(item => {
          item.addEventListener('click', () => {
            const nid = item.getAttribute('data-node-id');
            if (nid) {
              focusNodeOnMap(nid);
              highlightNode(nid);
              if (window.innerWidth <= 900) switchMobileTab('map');
            }
          });
        });
      }
    } else {
      if (dom.bfsDistVal) dom.bfsDistVal.textContent = 'Unreachable';
      if (dom.bfsHopsVal) dom.bfsHopsVal.textContent = '—';
      if (dom.bfsExploredVal) dom.bfsExploredVal.textContent = bfs && bfs.visitedOrder ? `${bfs.visitedOrder.length} nodes` : '—';
      if (dom.bfsStepsList) dom.bfsStepsList.innerHTML = '<li style="color:var(--accent-rose);">No route found</li>';
    }

    // 3. Highlight Comparison Winner Badges & Theoretical Analysis
    if (dijkstra && dijkstra.reachable && bfs && bfs.reachable) {
      const dDist = Math.round(dijkstra.totalWeight * 10) / 10;
      const bDist = Math.round(bfs.totalWeight * 10) / 10;
      const dHops = dijkstra.hopCount;
      const bHops = bfs.hopCount;

      const distDiff = Math.round((bDist - dDist) * 10) / 10;
      const hopDiff = dHops - bHops;

      let takeawayHtml = '';

      if (distDiff > 0.05 && hopDiff > 0) {
        // Classic DAA trade-off scenario
        if (dom.dijkstraHighlightBadge) {
          dom.dijkstraHighlightBadge.className = 'col-pill dijkstra winner';
          dom.dijkstraHighlightBadge.textContent = `Shortest Dist (-${distDiff} m)`;
        }
        if (dom.bfsHighlightBadge) {
          dom.bfsHighlightBadge.className = 'col-pill bfs winner';
          dom.bfsHighlightBadge.textContent = `Fewest segments (-${hopDiff})`;
        }

        takeawayHtml = `<strong>Route trade-off</strong><br>Shortest distance saves ${distDiff} m. Fewest segments takes ${hopDiff} fewer segment${hopDiff === 1 ? '' : 's'}.`;
      } else if (distDiff > 0.05) {
        if (dom.dijkstraHighlightBadge) {
          dom.dijkstraHighlightBadge.className = 'col-pill dijkstra winner';
          dom.dijkstraHighlightBadge.textContent = `Shorter (-${distDiff} m)`;
        }
        if (dom.bfsHighlightBadge) {
          dom.bfsHighlightBadge.className = 'col-pill bfs';
          dom.bfsHighlightBadge.textContent = `${bHops} Hops`;
        }

        takeawayHtml = `<strong>Route trade-off</strong><br>Shortest distance is ${distDiff} m shorter; both routes use ${dHops} segments.`;
      } else {
        // Identical or near-identical metric route
        if (dom.dijkstraHighlightBadge) {
          dom.dijkstraHighlightBadge.className = 'col-pill dijkstra';
          dom.dijkstraHighlightBadge.textContent = `${dDist} m (${dHops} hops)`;
        }
        if (dom.bfsHighlightBadge) {
          dom.bfsHighlightBadge.className = 'col-pill bfs';
          dom.bfsHighlightBadge.textContent = `${bDist} m (${bHops} hops)`;
        }

        takeawayHtml = `<strong>Same route</strong><br>Both options use ${dHops} segments across ${dDist} m.`;
      }

      if (dom.compareTakeawayBox) {
        dom.compareTakeawayBox.innerHTML = takeawayHtml;
        dom.compareTakeawayBox.style.display = 'flex';
      }
    }

    dom.routeCompareCard.style.display = 'flex';
    dom.routeCompareCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }

  /**
   * Helper to build SVG path data from ordered path nodes and edges
   */
  function buildPathGeometry(pathNodes, pathEdges) {
    const points = [];
    if (!pathNodes || !pathEdges) return points;

    for (let i = 0; i < pathEdges.length; i++) {
      const edge = pathEdges[i];
      const fromNode = pathNodes[i];
      const toNode = pathNodes[i + 1];

      let edgePoints = [];
      if (Array.isArray(edge.geometry) && edge.geometry.length >= 2) {
        const isForward = (edge.source === fromNode.id);
        const geoList = isForward ? edge.geometry : [...edge.geometry].reverse();
        edgePoints = geoList.map(pt => CampusCalibration.project(pt.lat, pt.lng));
      } else {
        const uPt = getNodePoint(fromNode);
        const vPt = getNodePoint(toNode);
        edgePoints = [uPt, vPt];
      }

      if (i === 0) {
        edgePoints.forEach(p => points.push(p));
      } else {
        edgePoints.slice(1).forEach(p => points.push(p));
      }
    }

    return points;
  }

  /**
   * Render Dual Route Overlay on Map for Comparison Mode
   */
  function renderComparisonOnMap(dijkstra, bfs, activeFilter = 'both') {
    if (!dom.edgesLayer || !dom.nodesLayer) return;

    // Clear previous route overlays
    dom.edgesLayer.querySelectorAll('.route-glow-path, .route-core-path').forEach(el => el.remove());

    const allCombinedPoints = [];

    // 1. Render Dijkstra (Solid Cyan)
    if (dijkstra && dijkstra.reachable) {
      const dijkPoints = buildPathGeometry(dijkstra.pathNodes, dijkstra.pathEdges);
      dijkPoints.forEach(p => allCombinedPoints.push(p));

      if (dijkPoints.length >= 2) {
        const dD = dijkPoints.map((pt, idx) => `${idx === 0 ? 'M' : 'L'} ${pt.x.toFixed(2)} ${pt.y.toFixed(2)}`).join(' ');

        const glowPath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        glowPath.setAttribute('d', dD);
        glowPath.setAttribute('class', 'route-glow-path dijkstra');
        dom.edgesLayer.appendChild(glowPath);

        const corePath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        corePath.setAttribute('d', dD);
        corePath.setAttribute('class', 'route-core-path dijkstra');
        dom.edgesLayer.appendChild(corePath);
      }
    }

    // 2. Render BFS (Dashed Amber)
    if (bfs && bfs.reachable) {
      const bfsPoints = buildPathGeometry(bfs.pathNodes, bfs.pathEdges);
      bfsPoints.forEach(p => allCombinedPoints.push(p));

      if (bfsPoints.length >= 2) {
        const bD = bfsPoints.map((pt, idx) => `${idx === 0 ? 'M' : 'L'} ${pt.x.toFixed(2)} ${pt.y.toFixed(2)}`).join(' ');

        const glowPath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        glowPath.setAttribute('d', bD);
        glowPath.setAttribute('class', 'route-glow-path bfs');
        dom.edgesLayer.appendChild(glowPath);

        const corePath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        corePath.setAttribute('d', bD);
        corePath.setAttribute('class', 'route-core-path bfs');
        dom.edgesLayer.appendChild(corePath);
      }
    }

    // 3. Mark active state on map container and set initial filter
    if (dom.mapCanvasContainer) {
      dom.mapCanvasContainer.classList.add('route-active');
      dom.mapCanvasContainer.classList.toggle('hide-dijkstra', activeFilter === 'bfs');
      dom.mapCanvasContainer.classList.toggle('hide-bfs', activeFilter === 'dijkstra');
    }

    // 4. Highlight Nodes and Edges
    const dijkstraNodeIds = new Set((dijkstra && dijkstra.pathNodes) ? dijkstra.pathNodes.map(n => n.id) : []);
    const bfsNodeIds = new Set((bfs && bfs.pathNodes) ? bfs.pathNodes.map(n => n.id) : []);

    const startId = state.route.startNodeId;
    const endId = state.route.endNodeId;

    dom.nodesLayer.querySelectorAll('.graph-node').forEach(nodeEl => {
      const nid = nodeEl.getAttribute('data-id');
      const inDijk = dijkstraNodeIds.has(nid);
      const inBfs = bfsNodeIds.has(nid);
      const inRoute = inDijk || inBfs;

      nodeEl.classList.toggle('in-route', inRoute);
      nodeEl.classList.toggle('in-both-routes', inDijk && inBfs);
      nodeEl.classList.toggle('in-dijkstra-route', inDijk && !inBfs);
      nodeEl.classList.toggle('in-bfs-route', inBfs && !inDijk);
      nodeEl.classList.toggle('route-start', nid === startId);
      nodeEl.classList.toggle('route-end', nid === endId);
    });

    const dijkstraEdgeIds = new Set((dijkstra && dijkstra.pathEdges) ? dijkstra.pathEdges.map(e => e.id) : []);
    const bfsEdgeIds = new Set((bfs && bfs.pathEdges) ? bfs.pathEdges.map(e => e.id) : []);

    dom.edgesLayer.querySelectorAll('.graph-edge').forEach(edgeEl => {
      const eid = edgeEl.getAttribute('data-id');
      const inDijk = dijkstraEdgeIds.has(eid);
      const inBfs = bfsEdgeIds.has(eid);
      edgeEl.classList.toggle('in-route', inDijk || inBfs);
    });

    // 5. Update Map Legend & Controls
    if (dom.legendRouteItem) dom.legendRouteItem.style.display = 'none';
    if (dom.legendCompareDijkstra) dom.legendCompareDijkstra.style.display = 'flex';
    if (dom.legendCompareBfs) dom.legendCompareBfs.style.display = 'flex';
    if (dom.legendCompareShared) dom.legendCompareShared.style.display = 'flex';
    if (dom.clearRouteMapBtn) dom.clearRouteMapBtn.style.display = 'inline-block';

    // 6. Camera auto-frame bounding box
    frameRouteOnMap(allCombinedPoints);
  }

  /**
   * Display Single Route Summary
   */
  function placeNameForNode(node) {
    return state.buildings.find(building => building.nodeId === node.id)?.name || node.name;
  }

  function routeHasApproxAccess(startNode, endNode) {
    return [startNode, endNode].some(node => state.buildings.some(building => building.nodeId === node.id && building.accessConfidence === 'LOW'));
  }

  function displayRouteResult(result, startNode, endNode) {
    if (!dom.routeResultCard) return;

    if (dom.routeResultTitle) {
      dom.routeResultTitle.textContent = `${placeNameForNode(startNode)} → ${placeNameForNode(endNode)}`;
    }
    if (dom.routeAlgoBadge) {
      dom.routeAlgoBadge.textContent = (result.algorithm || state.route.algorithm) === 'BFS' ? 'Fewest segments' : 'Shortest distance';
    }

    if (dom.metricDistance) {
      dom.metricDistance.textContent = `${Math.round(result.totalWeight || 0)} m`;
    }
    if (dom.metricHops) {
      dom.metricHops.textContent = `${result.hopCount || (result.pathEdges ? result.pathEdges.length : 0)}`;
    }
    if (dom.metricVisited) {
      dom.metricVisited.textContent = `${result.visitedOrder ? result.visitedOrder.length : (result.pathNodes ? result.pathNodes.length : 0)}`;
    }
    if (dom.metricTime) {
      const micros = result.executionTimeMicros || 0;
      dom.metricTime.textContent = micros >= 1000 ? `${(micros / 1000).toFixed(2)} ms` : `${micros} µs`;
    }

    const approximateAccess = routeHasApproxAccess(startNode, endNode);
    const hasLowConfidence = approximateAccess || (Array.isArray(result.pathEdges) && result.pathEdges.some(e => e.confidence === 'LOW'));
    if (dom.routeConfidenceWarning) {
      dom.routeConfidenceWarning.textContent = approximateAccess
        ? 'A destination uses the nearest mapped walkway point; the exact door is not shown.'
        : 'This route contains an approximate path segment.';
      dom.routeConfidenceWarning.style.display = hasLowConfidence ? 'block' : 'none';
    }

    if (dom.routeStepsList && Array.isArray(result.pathNodes)) {
      dom.routeStepsList.innerHTML = '';
      const totalSteps = result.pathNodes.length;

      result.pathNodes.forEach((node, idx) => {
        const isStart = idx === 0;
        const isEnd = idx === totalSteps - 1;
        const matchingBldg = state.buildings.find(b => b.nodeId === node.id);

        let stepBadgeClass = '';
        let stepBadgeText = `${idx + 1}`;
        if (isStart) {
          stepBadgeClass = 'start';
          stepBadgeText = 'S';
        } else if (isEnd) {
          stepBadgeClass = 'end';
          stepBadgeText = 'D';
        }

        const edge = (idx > 0 && result.pathEdges && result.pathEdges[idx - 1]) ? result.pathEdges[idx - 1] : null;

        const li = document.createElement('li');
        li.className = 'route-step-item';
        li.setAttribute('tabindex', '0');
        li.setAttribute('role', 'button');
        li.setAttribute('aria-label', `Step ${idx + 1}: ${node.name}`);

        li.innerHTML = `
          <div class="step-left">
            <span class="step-badge ${stepBadgeClass}">${stepBadgeText}</span>
            <div class="step-name" title="${escapeHtml(matchingBldg ? matchingBldg.name : node.name)}">
              ${escapeHtml(matchingBldg ? matchingBldg.name : node.name)}
              ${matchingBldg ? `<span style="font-size:0.7rem; color:var(--accent-blue)"> [${escapeHtml(matchingBldg.code)}]</span>` : ''}
            </div>
          </div>
          <div class="step-right">
            ${edge ? `<span class="step-dist">+${Math.round(edge.weight)}m</span>` : '<span class="step-dist">Start</span>'}
          </div>
        `;

        const onStepClick = () => {
          focusNodeOnMap(node.id);
          highlightNode(node.id);
          if (window.innerWidth <= 900) switchMobileTab('map');
        };

        li.addEventListener('click', onStepClick);
        li.addEventListener('keydown', (e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onStepClick();
          }
        });

        dom.routeStepsList.appendChild(li);
      });
    }

    dom.routeResultCard.style.display = 'flex';
    dom.routeResultCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }

  /**
   * Render Route Geometry and Path Highlights on SVG Map (Single Route)
   */
  function renderRouteOnMap(result) {
    if (!dom.edgesLayer || !dom.nodesLayer || !result) return;

    dom.edgesLayer.querySelectorAll('.route-glow-path, .route-core-path').forEach(el => el.remove());

    const pathNodes = result.pathNodes || [];
    const pathEdges = result.pathEdges || [];

    if (pathNodes.length === 0) return;

    const allRoutePoints = buildPathGeometry(pathNodes, pathEdges);

    if (allRoutePoints.length >= 2) {
      const pathD = allRoutePoints.map((pt, idx) =>
        `${idx === 0 ? 'M' : 'L'} ${pt.x.toFixed(2)} ${pt.y.toFixed(2)}`
      ).join(' ');

      const glowPath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
      glowPath.setAttribute('d', pathD);
      glowPath.setAttribute('class', 'route-glow-path');
      dom.edgesLayer.appendChild(glowPath);

      const corePath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
      corePath.setAttribute('d', pathD);
      corePath.setAttribute('class', 'route-core-path');
      dom.edgesLayer.appendChild(corePath);
    }

    if (dom.mapCanvasContainer) {
      dom.mapCanvasContainer.classList.add('route-active');
      dom.mapCanvasContainer.classList.remove('hide-dijkstra', 'hide-bfs');
    }

    const routeEdgeIds = new Set(pathEdges.map(e => e.id));
    dom.edgesLayer.querySelectorAll('.graph-edge').forEach(edgeEl => {
      const eid = edgeEl.getAttribute('data-id');
      edgeEl.classList.toggle('in-route', routeEdgeIds.has(eid));
    });

    const startId = pathNodes[0].id;
    const endId = pathNodes[pathNodes.length - 1].id;
    const routeNodeIds = new Set(pathNodes.map(n => n.id));

    dom.nodesLayer.querySelectorAll('.graph-node').forEach(nodeEl => {
      const nid = nodeEl.getAttribute('data-id');
      const inRoute = routeNodeIds.has(nid);
      const isStart = (nid === startId);
      const isEnd = (nid === endId);

      nodeEl.classList.toggle('in-route', inRoute);
      nodeEl.classList.toggle('route-start', isStart);
      nodeEl.classList.toggle('route-end', isEnd);
      nodeEl.classList.remove('in-both-routes', 'in-dijkstra-route', 'in-bfs-route');
    });

    if (dom.legendRouteItem) dom.legendRouteItem.style.display = 'flex';
    if (dom.legendCompareDijkstra) dom.legendCompareDijkstra.style.display = 'none';
    if (dom.legendCompareBfs) dom.legendCompareBfs.style.display = 'none';
    if (dom.legendCompareShared) dom.legendCompareShared.style.display = 'none';
    if (dom.clearRouteMapBtn) dom.clearRouteMapBtn.style.display = 'inline-block';

    frameRouteOnMap(allRoutePoints);
  }

  /**
   * Smoothly Zoom and Pan Map to Frame Computed Route(s)
   */
  function frameRouteOnMap(points) {
    if (!dom.svgContainer || points.length === 0) return;

    let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
    points.forEach(p => {
      if (p.x < minX) minX = p.x;
      if (p.x > maxX) maxX = p.x;
      if (p.y < minY) minY = p.y;
      if (p.y > maxY) maxY = p.y;
    });

    const routeWidth = maxX - minX;
    const routeHeight = maxY - minY;
    const padX = Math.max(120, routeWidth * 0.25);
    const padY = Math.max(120, routeHeight * 0.25);

    let boxWidth = routeWidth + padX * 2;
    let boxHeight = routeHeight + padY * 2;

    const view = dom.svgContainer.viewBox.baseVal;
    const aspect = (view.width > 0 && view.height > 0) ? (view.height / view.width) : (1733 / 2600);

    if (boxHeight / boxWidth < aspect) {
      boxHeight = boxWidth * aspect;
    } else {
      boxWidth = boxHeight / aspect;
    }

    mapIsFit = false;
    setMapViewBox({
      x: (minX + maxX) / 2 - boxWidth / 2,
      y: (minY + maxY) / 2 - boxHeight / 2,
      width: boxWidth,
      height: boxHeight
    });
  }

  /**
   * Reset / Clear Active Route and Comparison
   */
  function clearRoute() {
    clearAlgorithmDemo();
    state.route.result = null;
    state.route.compareResult = {
      active: false,
      dijkstra: null,
      bfs: null,
      activeFilter: 'both'
    };
    clearRouteAlert();

    if (dom.edgesLayer) {
      dom.edgesLayer.querySelectorAll('.route-glow-path, .route-core-path').forEach(el => el.remove());
      dom.edgesLayer.querySelectorAll('.graph-edge').forEach(el => el.classList.remove('in-route'));
    }

    if (dom.nodesLayer) {
      dom.nodesLayer.querySelectorAll('.graph-node').forEach(el => {
        el.classList.remove('in-route', 'route-start', 'route-end', 'in-both-routes', 'in-dijkstra-route', 'in-bfs-route');
      });
    }

    if (dom.mapCanvasContainer) {
      dom.mapCanvasContainer.classList.remove('route-active', 'hide-dijkstra', 'hide-bfs');
    }

    if (dom.routeResultCard) dom.routeResultCard.style.display = 'none';
    if (dom.routeCompareCard) dom.routeCompareCard.style.display = 'none';
    if (dom.clearRouteMapBtn) dom.clearRouteMapBtn.style.display = 'none';
    if (dom.legendRouteItem) dom.legendRouteItem.style.display = 'none';
    if (dom.legendCompareDijkstra) dom.legendCompareDijkstra.style.display = 'none';
    if (dom.legendCompareBfs) dom.legendCompareBfs.style.display = 'none';
    if (dom.legendCompareShared) dom.legendCompareShared.style.display = 'none';

    if (typeof CampusCalibration !== 'undefined') {
      fitCampus();
    }
  }

  /**
   * Focus / Center Map View on a Specific Node
   */
  function focusNodeOnMap(nodeId) {
    if (!dom.svgContainer) return;
    const node = state.graph.nodes.find((n) => n.id === nodeId);
    if (!node) return;

    const pt = getNodePoint(node);

    mapIsFit = false;
    const view = dom.svgContainer.viewBox.baseVal;
    const focusWidth = Math.min(view.width > 0 ? view.width : 2600, 1350);
    const aspect = view.width > 0 && view.height > 0 ? view.height / view.width : 1733 / 2600;
    const focusHeight = focusWidth * aspect;

    setMapViewBox({
      x: pt.x - focusWidth / 2,
      y: pt.y - focusHeight / 2,
      width: focusWidth,
      height: focusHeight
    });
  }

  /**
   * Handle Building Selection from Directory
   */
  async function handleBuildingSelect(buildingId, shouldFocusMap = true) {
    state.selectedBuildingId = buildingId;
    renderBuildingList();

    try {
      const bldg = await ApiClient.getBuildingById(buildingId);
      if (bldg) {
        state.selectedNodeId = bldg.nodeId || null;
        if (bldg.nodeId) {
          highlightNode(bldg.nodeId);
        } else {
          highlightNode(null);
        }
        if (shouldFocusMap && bldg.nodeId) {
          focusNodeOnMap(bldg.nodeId);
        }
        displayBuildingDetails(bldg);
        if (dom.detailPanel) dom.detailPanel.style.display = 'block';

        if (window.innerWidth <= 900 && state.currentMobileTab !== 'map') {
          switchMobileTab('map');
        }
      }
    } catch (err) {
      console.error('Error fetching building details:', err);
    }
  }

  /**
   * Handle Node Selection from Map Marker
   */
  async function handleNodeClick(nodeId, shouldFocusMap = true) {
    state.selectedNodeId = nodeId;
    highlightNode(nodeId);
    if (shouldFocusMap) {
      focusNodeOnMap(nodeId);
    }

    const matchingBuilding = state.buildings.find((b) => b.nodeId === nodeId);
    if (matchingBuilding) {
      state.selectedBuildingId = matchingBuilding.id;
      renderBuildingList();

      const card = dom.buildingList ? dom.buildingList.querySelector(`.building-card[data-id="${matchingBuilding.id}"]`) : null;
      if (card) {
        card.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
      displayBuildingDetails(matchingBuilding);
      if (dom.detailPanel) dom.detailPanel.style.display = 'block';
    } else {
      state.selectedBuildingId = null;
      renderBuildingList();

      try {
        const node = await ApiClient.getNodeById(nodeId);
        displayNodeDetails(node);
        if (dom.detailPanel) dom.detailPanel.style.display = 'block';
      } catch (e) {
        console.error('Error fetching node details:', e);
      }
    }
  }

  /**
   * Highlight Node and its Incident Pathways
   */
  function highlightNode(nodeId) {
    document.querySelectorAll('.graph-node').forEach((el) => {
      if (el.getAttribute('data-id') === nodeId) {
        el.classList.add('active');
        el.setAttribute('aria-selected', 'true');
      } else {
        el.classList.remove('active');
        el.removeAttribute('aria-selected');
      }
    });

    document.querySelectorAll('.graph-edge').forEach((el) => {
      const src = el.getAttribute('data-source');
      const tgt = el.getAttribute('data-target');
      if (src === nodeId || tgt === nodeId) {
        el.classList.add('active');
      } else {
        el.classList.remove('active');
      }
    });
  }

  /**
   * Reset All Selections and Visual Highlights
   */
  function resetHighlights() {
    state.selectedBuildingId = null;
    state.selectedNodeId = null;
    document.querySelectorAll('.graph-node').forEach((el) => {
      el.classList.remove('active');
      el.removeAttribute('aria-selected');
    });
    document.querySelectorAll('.graph-edge').forEach((el) => el.classList.remove('active'));
    renderBuildingList();
    renderDefaultDetails();

    if (typeof CampusCalibration !== 'undefined') {
      fitCampus();
    }
  }

  /**
   * Reset Search and Category Filters
   */
  function resetSearchAndFilters() {
    state.searchQuery = '';
    state.selectedCategory = 'ALL';
    if (dom.searchInput) dom.searchInput.value = '';
    dom.categoryChips.forEach((c) => {
      const isAll = c.getAttribute('data-category') === 'ALL';
      c.classList.toggle('active', isAll);
      c.setAttribute('aria-selected', isAll ? 'true' : 'false');
    });
    renderBuildingList();
  }

  /**
   * Get Incident Edges & Neighbors for a Node
   */
  function getConnectedPaths(nodeId) {
    if (!state.graph || !state.graph.edges) return [];
    const connected = [];

    state.graph.edges.forEach((edge) => {
      if (edge.source === nodeId || edge.target === nodeId) {
        const neighborId = edge.source === nodeId ? edge.target : edge.source;
        const neighborNode = state.graph.nodes ? state.graph.nodes.find((n) => n.id === neighborId) : null;
        connected.push({
          edgeId: edge.id,
          neighborId,
          neighborName: neighborNode ? neighborNode.name : neighborId,
          neighborType: neighborNode ? neighborNode.type : 'WAYPOINT',
          weight: edge.weight,
          type: edge.type,
          confidence: edge.confidence
        });
      }
    });

    return connected;
  }

  /**
   * Pre-fill Route Endpoints and switch to Route Finder
   */
  function setRouteEndpoint(nodeId, isDestination = false) {
    if (isDestination) {
      state.route.endNodeId = nodeId;
      if (dom.routeEndSelect) dom.routeEndSelect.value = nodeId;
    } else {
      state.route.startNodeId = nodeId;
      if (dom.routeStartSelect) dom.routeStartSelect.value = nodeId;
    }

    switchSidebarMode('route');
    clearRouteAlert();

    if (window.innerWidth <= 900) {
      switchMobileTab('route');
    }
  }

  /**
   * Display Building Details in Sidebar Panel
   */
  function displayBuildingDetails(bldg) {
    dom.detailTitle.innerHTML = `<span>${escapeHtml(bldg.name)}</span> <span style="font-size:0.75rem; color:var(--accent-blue)">[${escapeHtml(bldg.code)}]</span>`;

    const facilitiesHtml = (bldg.facilities && bldg.facilities.length > 0)
      ? bldg.facilities.map((f) => `<span class="facility-pill">${escapeHtml(f)}</span>`).join('')
      : '<span class="facility-pill">Standard campus facility</span>';

    const node = state.graph.nodes ? state.graph.nodes.find((n) => n.id === bldg.nodeId) : null;
    const connectedPaths = bldg.nodeId ? getConnectedPaths(bldg.nodeId) : [];

    const pathsHtml = connectedPaths.length > 0
      ? connectedPaths.map((cp) => `
          <button class="path-link-chip" data-node-id="${cp.neighborId}" title="Select ${escapeHtml(cp.neighborName)}">
            <span>→ ${escapeHtml(cp.neighborName)} (${cp.type.replace('_', ' ').toLowerCase()})</span>
            <span class="path-dist">${cp.weight}m</span>
          </button>
        `).join('')
      : '<span style="font-size:0.75rem; color:var(--text-subtle);">Direct walkway connections not indexed.</span>';

    const hasLowConfidence = bldg.accessConfidence === 'LOW' || bldg.accessConfidence === 'UNRESOLVED' || connectedPaths.some((p) => p.confidence === 'LOW');

    dom.detailContent.innerHTML = `
      <div class="detail-row">
        <span class="lbl">Category</span>
        <span class="val">${escapeHtml(bldg.category)}</span>
      </div>
      <div class="detail-row">
        <span class="lbl">Wheelchair Access</span>
        <span class="val">${node ? (node.accessible ? '✓ Accessible path' : '✕ Step access') : 'Not mapped'}</span>
      </div>
      <div style="margin-top: 0.5rem; font-size: 0.78rem; color: var(--text-muted); line-height:1.4;">
        ${escapeHtml(bldg.description || 'Campus landmark facility at Netaji Subhas University of Technology.')}
      </div>
      <div style="margin-top: 0.6rem;">
        <div style="font-size: 0.7rem; color: var(--text-subtle); text-transform: uppercase;">Key Facilities:</div>
        <div class="facility-pill-list">${facilitiesHtml}</div>
      </div>
      <div style="margin-top: 0.75rem;">
        <div style="font-size: 0.7rem; color: var(--text-subtle); text-transform: uppercase;">Connected Pathways:</div>
        <div class="connected-paths-list">${pathsHtml}</div>
      </div>
      ${hasLowConfidence ? `
      <div class="confidence-notice">
        ${escapeHtml(bldg.accessNote || 'The mapped endpoint is the nearest walkway access point; the exact door needs an on-site check.')}
      </div>` : ''}
      ${bldg.nodeId ? `<div class="detail-actions-row">
        <button class="btn-detail-action" id="btnFocusFacility">Focus mapped access</button>
        <button class="btn-detail-action" id="btnSetStartFacility">Set as start</button>
        <button class="btn-detail-action" id="btnSetDestFacility">Set as destination</button>
      </div>` : ''}
    `;

    const btnFocus = document.getElementById('btnFocusFacility');
    if (btnFocus) {
      btnFocus.addEventListener('click', () => {
        focusNodeOnMap(bldg.nodeId);
        if (window.innerWidth <= 900) switchMobileTab('map');
      });
    }

    const btnStart = document.getElementById('btnSetStartFacility');
    if (btnStart) {
      btnStart.addEventListener('click', () => setRouteEndpoint(bldg.nodeId, false));
    }

    const btnDest = document.getElementById('btnSetDestFacility');
    if (btnDest) {
      btnDest.addEventListener('click', () => setRouteEndpoint(bldg.nodeId, true));
    }

    dom.detailContent.querySelectorAll('.path-link-chip').forEach((chip) => {
      chip.addEventListener('click', () => {
        const targetId = chip.getAttribute('data-node-id');
        if (targetId) handleNodeClick(targetId);
      });
    });
  }

  /**
   * Display Node Details in Sidebar Panel
   */
  function displayNodeDetails(node) {
    dom.detailTitle.innerHTML = `<span>${escapeHtml(node.name)}</span> <span style="font-size:0.75rem; color:var(--accent-cyan)">[${escapeHtml(node.id)}]</span>`;

    const connectedPaths = getConnectedPaths(node.id);
    const pathsHtml = connectedPaths.length > 0
      ? connectedPaths.map((cp) => `
          <button class="path-link-chip" data-node-id="${cp.neighborId}" title="Select ${escapeHtml(cp.neighborName)}">
            <span>→ ${escapeHtml(cp.neighborName)} (${cp.type.replace('_', ' ').toLowerCase()})</span>
            <span class="path-dist">${cp.weight}m</span>
          </button>
        `).join('')
      : '<span style="font-size:0.75rem; color:var(--text-subtle);">No connected pathways recorded.</span>';

    dom.detailContent.innerHTML = `
      <div class="detail-row">
        <span class="lbl">Vertex ID</span>
        <span class="val" style="font-weight:bold;">${escapeHtml(node.id)}</span>
      </div>
      <div class="detail-row">
        <span class="lbl">Vertex Type</span>
        <span class="val" style="color:var(--accent-cyan); font-weight:600;">${escapeHtml(node.type)}</span>
      </div>
      ${node.lat != null && node.lng != null ? `
      <div class="detail-row">
        <span class="lbl">GPS (Lat, Lng)</span>
        <span class="val" style="font-family:monospace; font-size:0.75rem;">${node.lat.toFixed(6)}, ${node.lng.toFixed(6)}</span>
      </div>` : `
      <div class="detail-row">
        <span class="lbl">Map Coordinates</span>
        <span class="val">(${node.x}, ${node.y})</span>
      </div>`}
      <div class="detail-row">
        <span class="lbl">Wheelchair Accessible</span>
        <span class="val">${node.accessible ? '✓ Yes' : '✕ No'}</span>
      </div>
      <div style="margin-top: 0.6rem; font-size: 0.76rem; color: var(--text-muted);">
        ${node.type === 'JUNCTION'
          ? 'Walking pathway junction connecting campus arterial corridors.'
          : 'Campus gateway landmark and boundary entrance point.'}
      </div>
      <div style="margin-top: 0.75rem;">
        <div style="font-size: 0.7rem; color: var(--text-subtle); text-transform: uppercase;">Connected Pathways:</div>
        <div class="connected-paths-list">${pathsHtml}</div>
      </div>
      <div class="detail-actions-row">
        <button class="btn-detail-action" id="btnFocusNode">📍 Focus</button>
        <button class="btn-detail-action" id="btnSetStartNode">🚩 From Here</button>
        <button class="btn-detail-action" id="btnSetDestNode">🎯 To Here</button>
      </div>
    `;

    const btnFocus = document.getElementById('btnFocusNode');
    if (btnFocus) {
      btnFocus.addEventListener('click', () => {
        focusNodeOnMap(node.id);
        if (window.innerWidth <= 900) switchMobileTab('map');
      });
    }

    const btnStart = document.getElementById('btnSetStartNode');
    if (btnStart) {
      btnStart.addEventListener('click', () => setRouteEndpoint(node.id, false));
    }

    const btnDest = document.getElementById('btnSetDestNode');
    if (btnDest) {
      btnDest.addEventListener('click', () => setRouteEndpoint(node.id, true));
    }

    dom.detailContent.querySelectorAll('.path-link-chip').forEach((chip) => {
      chip.addEventListener('click', () => {
        const targetId = chip.getAttribute('data-node-id');
        if (targetId) handleNodeClick(targetId);
      });
    });
  }

  /**
   * Display Edge Details in Sidebar Panel
   */
  function displayEdgeDetails(edge, u, v) {
    dom.detailTitle.innerHTML = `<span>Pathway [${escapeHtml(edge.id)}]</span>`;

    dom.detailContent.innerHTML = `
      <div class="detail-row">
        <span class="lbl">Connection</span>
        <span class="val">${escapeHtml(u.name)} ↔ ${escapeHtml(v.name)}</span>
      </div>
      <div class="detail-row">
        <span class="lbl">Distance</span>
        <span class="val" style="color:var(--accent-cyan); font-weight:bold;">${edge.weight} meters</span>
      </div>
      <div class="detail-row">
        <span class="lbl">Pathway Type</span>
        <span class="val">${escapeHtml(edge.type.replace('_', ' '))}</span>
      </div>
      <div class="detail-row">
        <span class="lbl">Confidence</span>
        <span class="val">${escapeHtml(edge.confidence || 'VERIFIED')}</span>
      </div>
      <div class="detail-actions-row">
        <button class="btn-detail-action" id="btnInspectNodeU">📍 Go to ${escapeHtml(u.name)}</button>
        <button class="btn-detail-action secondary" id="btnInspectNodeV">📍 Go to ${escapeHtml(v.name)}</button>
      </div>
    `;

    const btnU = document.getElementById('btnInspectNodeU');
    if (btnU) btnU.addEventListener('click', () => handleNodeClick(edge.source));

    const btnV = document.getElementById('btnInspectNodeV');
    if (btnV) btnV.addEventListener('click', () => handleNodeClick(edge.target));
  }

  /**
   * Render Default Instructions in Detail Panel
   */
  function renderDefaultDetails() {
    dom.detailTitle.textContent = 'Location Information';
    dom.detailContent.innerHTML = `
      <p style="font-size: 0.78rem; color: var(--text-muted); line-height:1.5;">
        Select any building from the directory list or click a vertex node on the campus map to inspect its connectivity, coordinates, and facility attributes.
      </p>
    `;
  }

  /**
   * ViewBox & Zoom/Pan Navigation
   */
  function setMapViewBox(box) {
    if (!dom.svgContainer) return;
    dom.svgContainer.setAttribute('viewBox', `${box.x} ${box.y} ${box.width} ${box.height}`);
  }

  function fitCampus() {
    if (!dom.svgContainer || typeof CampusCalibration === 'undefined') return;
    setMapViewBox(CampusCalibration.fitViewBox(dom.svgContainer.clientWidth, dom.svgContainer.clientHeight));
    mapIsFit = true;
  }

  function zoomMap(factor, anchor) {
    mapIsFit = false;
    const view = dom.svgContainer.viewBox.baseVal;
    const point = anchor || { x: view.x + view.width / 2, y: view.y + view.height / 2 };
    const width = Math.min(10000, Math.max(450, view.width * factor));
    const height = (width * view.height) / view.width;
    const ratio = width / view.width;
    setMapViewBox({
      x: point.x - (point.x - view.x) * ratio,
      y: point.y - (point.y - view.y) * ratio,
      width,
      height
    });
  }

  function pointerToMapPoint(event) {
    const point = dom.svgContainer.createSVGPoint();
    point.x = event.clientX;
    point.y = event.clientY;
    return point.matrixTransform(dom.svgContainer.getScreenCTM().inverse());
  }

  function installMapNavigation() {
    if (!dom.svgContainer) return;

    if (dom.zoomInBtn) dom.zoomInBtn.addEventListener('click', () => zoomMap(0.8));
    if (dom.zoomOutBtn) dom.zoomOutBtn.addEventListener('click', () => zoomMap(1.25));
    if (dom.fitCampusBtn) dom.fitCampusBtn.addEventListener('click', fitCampus);

    fitCampus();
    window.addEventListener('resize', () => {
      if (mapIsFit) fitCampus();
    });

    dom.svgContainer.addEventListener('wheel', (event) => {
      event.preventDefault();
      const point = pointerToMapPoint(event);
      zoomMap(event.deltaY > 0 ? 1.18 : 0.85, point);
    }, { passive: false });

    let previous = null;
    dom.svgContainer.addEventListener('pointerdown', (event) => {
      if (event.button !== 0) return;
      mapIsFit = false;
      previous = { clientX: event.clientX, clientY: event.clientY };
      dom.svgContainer.setPointerCapture(event.pointerId);
      dom.svgContainer.classList.add('dragging');
    });

    dom.svgContainer.addEventListener('pointermove', (event) => {
      if (!previous) return;
      const matrix = dom.svgContainer.getScreenCTM().inverse();
      const deltaX = matrix.a * (event.clientX - previous.clientX) + matrix.c * (event.clientY - previous.clientY);
      const deltaY = matrix.b * (event.clientX - previous.clientX) + matrix.d * (event.clientY - previous.clientY);
      const view = dom.svgContainer.viewBox.baseVal;
      setMapViewBox({
        x: view.x - deltaX,
        y: view.y - deltaY,
        width: view.width,
        height: view.height
      });
      previous = { clientX: event.clientX, clientY: event.clientY };
    });

    const stopPan = () => {
      previous = null;
      dom.svgContainer.classList.remove('dragging');
    };
    dom.svgContainer.addEventListener('pointerup', stopPan);
    dom.svgContainer.addEventListener('pointercancel', stopPan);
  }

  /**
   * Mobile View Tabs Management
   */
  function setupMobileTabs() {
    if (dom.tabDirectoryBtn) {
      dom.tabDirectoryBtn.addEventListener('click', () => switchMobileTab('directory'));
    }
    if (dom.tabRouteBtn) {
      dom.tabRouteBtn.addEventListener('click', () => switchMobileTab('route'));
    }
    if (dom.tabAlgorithmsBtn) {
      dom.tabAlgorithmsBtn.addEventListener('click', () => switchMobileTab('algorithms'));
    }
    if (dom.tabMapBtn) {
      dom.tabMapBtn.addEventListener('click', () => switchMobileTab('map'));
    }
  }

  function switchMobileTab(targetTab) {
    state.currentMobileTab = targetTab;

    if (dom.tabDirectoryBtn) {
      dom.tabDirectoryBtn.classList.toggle('active', targetTab === 'directory');
      dom.tabDirectoryBtn.setAttribute('aria-selected', targetTab === 'directory' ? 'true' : 'false');
    }
    if (dom.tabRouteBtn) {
      dom.tabRouteBtn.classList.toggle('active', targetTab === 'route');
      dom.tabRouteBtn.setAttribute('aria-selected', targetTab === 'route' ? 'true' : 'false');
    }
    if (dom.tabAlgorithmsBtn) {
      dom.tabAlgorithmsBtn.classList.toggle('active', targetTab === 'algorithms');
      dom.tabAlgorithmsBtn.setAttribute('aria-selected', targetTab === 'algorithms' ? 'true' : 'false');
    }
    if (dom.tabMapBtn) {
      dom.tabMapBtn.classList.toggle('active', targetTab === 'map');
      dom.tabMapBtn.setAttribute('aria-selected', targetTab === 'map' ? 'true' : 'false');
    }

    if (dom.appContainer) {
      dom.appContainer.classList.toggle('show-directory', targetTab === 'directory');
      dom.appContainer.classList.toggle('show-route', targetTab === 'route');
      dom.appContainer.classList.toggle('show-algorithms', targetTab === 'algorithms');
      dom.appContainer.classList.toggle('show-map', targetTab === 'map');
    }

    if (targetTab === 'route') {
      switchSidebarMode('route');
    } else if (targetTab === 'directory') {
      switchSidebarMode('directory');
    } else if (targetTab === 'algorithms') {
      switchSidebarMode('algorithms');
    }

    if (targetTab === 'map' && mapIsFit) {
      setTimeout(fitCampus, 50);
    }
  }

  /**
   * Bind DOM Events
   */
  function bindEvents() {
    installMapNavigation();

    if (dom.mapToneBtn && dom.mapCanvasContainer) {
      dom.mapToneBtn.addEventListener('click', () => {
        const original = dom.mapCanvasContainer.classList.toggle('original-map');
        dom.mapToneBtn.textContent = original ? 'Darken map' : 'Original colors';
        dom.mapToneBtn.title = original ? 'Use the dark map colors' : 'Restore original map colors';
        updateMapRasterTheme(original ? 'original' : 'dark');
      });
    }

    const closeDetails = () => { if (dom.detailPanel) dom.detailPanel.style.display = 'none'; };
    document.getElementById('detailBackBtn')?.addEventListener('click', closeDetails);
    document.getElementById('closeDetailBtn')?.addEventListener('click', closeDetails);

    if (dom.searchInput) {
      dom.searchInput.addEventListener('input', (e) => {
        state.searchQuery = e.target.value;
        renderBuildingList();
      });
    }

    if (dom.clearSearchBtn) {
      dom.clearSearchBtn.addEventListener('click', () => {
        state.searchQuery = '';
        dom.searchInput.value = '';
        dom.clearSearchBtn.style.display = 'none';
        dom.searchInput.focus();
        renderBuildingList();
      });
    }

    if (dom.clearFiltersBtn) {
      dom.clearFiltersBtn.addEventListener('click', resetSearchAndFilters);
    }

    dom.categoryChips.forEach((chip) => {
      chip.addEventListener('click', () => {
        dom.categoryChips.forEach((c) => {
          c.classList.remove('active');
          c.setAttribute('aria-selected', 'false');
        });
        chip.classList.add('active');
        chip.setAttribute('aria-selected', 'true');
        state.selectedCategory = chip.getAttribute('data-category');
        renderBuildingList();
      });
    });

    if (dom.resetBtn) {
      dom.resetBtn.addEventListener('click', resetHighlights);
    }

    if (dom.reloadBtn) {
      dom.reloadBtn.addEventListener('click', async () => {
        try {
          await fetch('/api/buildings/reload', { method: 'POST' });
        } catch (e) {
          console.warn('Reload endpoint call error:', e);
        }
        await verifyHealth();
        await Promise.all([loadBuildings(), loadGraph()]);
        renderMapElements();
        renderBuildingList();
        populateRouteSelectors();
        populateAlgorithmSelectors();
      });
    }

  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // Start when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
