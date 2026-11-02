/**
 * Smart Campus Navigation and Resource Optimizer
 * Frontend Application Controller
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
    apiLogs: []
  };

  // Node Color Scheme by Type
  const TYPE_COLORS = {
    GATE: '#94a3b8',
    ACADEMIC: '#3b82f6',
    LIBRARY: '#a855f7',
    AMENITY: '#f59e0b',
    FACILITY: '#10b981',
    HOSTEL: '#eab308',
    JUNCTION: '#06b6d4'
  };

  // DOM Elements
  const dom = {
    statusPill: document.getElementById('statusPill'),
    statusText: document.getElementById('statusText'),
    nodeCountBadge: document.getElementById('nodeCountBadge'),
    edgeCountBadge: document.getElementById('edgeCountBadge'),
    buildingCountBadge: document.getElementById('buildingCountBadge'),
    searchInput: document.getElementById('searchInput'),
    categoryChips: document.querySelectorAll('.filter-chip'),
    buildingList: document.getElementById('buildingList'),
    detailPanel: document.getElementById('detailPanel'),
    detailTitle: document.getElementById('detailTitle'),
    detailContent: document.getElementById('detailContent'),
    svgContainer: document.getElementById('campusMapSvg'),
    edgesLayer: null,
    nodesLayer: null,
    apiLogsList: document.getElementById('apiLogsList'),
    jsonViewer: document.getElementById('jsonViewer'),
    inspectorDrawer: document.getElementById('apiInspectorDrawer'),
    drawerToggle: document.getElementById('drawerToggle'),
    resetBtn: document.getElementById('resetHighlightsBtn'),
    reloadBtn: document.getElementById('reloadMapBtn')
  };

  /**
   * Initializes the application.
   */
  async function init() {
    setupApiInspector();
    bindEvents();

    try {
      // 1. Initial Health Check
      await verifyHealth();

      // 2. Load campus building metadata & graph topology in parallel
      await Promise.all([loadBuildings(), loadGraph()]);

      renderMapElements();
      renderBuildingList();
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
   * Load Buildings
   */
  async function loadBuildings() {
    const list = await ApiClient.getBuildings();
    state.buildings = list || [];
    dom.buildingCountBadge.textContent = state.buildings.length;
  }

  /**
   * Load Graph
   */
  async function loadGraph() {
    const graphData = await ApiClient.getGraph();
    state.graph = graphData || { nodes: [], edges: [] };
    dom.nodeCountBadge.textContent = state.graph.nodes ? state.graph.nodes.length : 0;
    dom.edgeCountBadge.textContent = state.graph.edges ? state.graph.edges.length : 0;
  }

  /**
   * Render SVG Edges and Nodes
   */
  function renderMapElements() {
    dom.edgesLayer = document.getElementById('graphEdgesLayer');
    dom.nodesLayer = document.getElementById('graphNodesLayer');

    if (!dom.edgesLayer || !dom.nodesLayer) {
      console.warn('SVG layers not ready');
      return;
    }

    dom.edgesLayer.innerHTML = '';
    dom.nodesLayer.innerHTML = '';

    const nodeCoordMap = new Map();
    state.graph.nodes.forEach((n) => {
      nodeCoordMap.set(n.id, { x: n.x, y: n.y, name: n.name, type: n.type });
    });

    // 1. Render Edges (Pathways)
    if (state.graph.edges) {
      state.graph.edges.forEach((edge) => {
        const u = nodeCoordMap.get(edge.source);
        const v = nodeCoordMap.get(edge.target);
        if (!u || !v) return;

        const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        line.setAttribute('x1', u.x);
        line.setAttribute('y1', u.y);
        line.setAttribute('x2', v.x);
        line.setAttribute('y2', v.y);
        line.setAttribute('class', 'graph-edge');
        line.setAttribute('data-id', edge.id);
        line.setAttribute('data-source', edge.source);
        line.setAttribute('data-target', edge.target);

        // Edge Weight text badge
        const midX = (u.x + v.x) / 2;
        const midY = (u.y + v.y) / 2;

        const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        text.setAttribute('x', midX);
        text.setAttribute('y', midY - 4);
        text.setAttribute('class', 'edge-weight-label');
        text.textContent = `${edge.weight}m`;

        const title = document.createElementNS('http://www.w3.org/2000/svg', 'title');
        title.textContent = `${edge.id}: ${u.name} ↔ ${v.name} (${edge.weight}m, ${edge.type})`;
        line.appendChild(title);

        dom.edgesLayer.appendChild(line);
        dom.edgesLayer.appendChild(text);
      });
    }

    // 2. Render Nodes (Vertices)
    if (state.graph.nodes) {
      state.graph.nodes.forEach((node) => {
        const group = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        group.setAttribute('class', 'graph-node');
        group.setAttribute('data-id', node.id);
        group.setAttribute('transform', `translate(${node.x}, ${node.y})`);

        const color = TYPE_COLORS[node.type] || '#3b82f6';

        // Outer glow circle
        const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        circle.setAttribute('r', '9');
        circle.setAttribute('class', 'node-core');
        circle.setAttribute('fill', '#1e293b');
        circle.setAttribute('stroke', color);

        // Node center pip
        const innerDot = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        innerDot.setAttribute('r', '4');
        innerDot.setAttribute('fill', color);

        // Label
        const label = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        label.setAttribute('y', '-14');
        label.setAttribute('class', 'node-label');
        label.textContent = node.id;

        // Hover tooltip
        const title = document.createElementNS('http://www.w3.org/2000/svg', 'title');
        title.textContent = `[${node.id}] ${node.name}\nType: ${node.type}\nAccessible: ${node.accessible ? 'Yes' : 'No'}`;
        group.appendChild(title);

        group.appendChild(circle);
        group.appendChild(innerDot);
        group.appendChild(label);

        group.addEventListener('click', () => {
          handleNodeClick(node.id);
        });

        dom.nodesLayer.appendChild(group);
      });
    }
  }

  /**
   * Render Building List
   */
  function renderBuildingList() {
    dom.buildingList.innerHTML = '';

    const filtered = state.buildings.filter((b) => {
      const matchesCategory =
        state.selectedCategory === 'ALL' ||
        b.category.toUpperCase() === state.selectedCategory.toUpperCase();

      const query = state.searchQuery.toLowerCase();
      const matchesSearch =
        !query ||
        b.name.toLowerCase().includes(query) ||
        b.code.toLowerCase().includes(query) ||
        b.description.toLowerCase().includes(query) ||
        b.facilities.some((f) => f.toLowerCase().includes(query));

      return matchesCategory && matchesSearch;
    });

    if (filtered.length === 0) {
      dom.buildingList.innerHTML = `
        <div style="padding: 1.5rem 0.5rem; text-align: center; color: var(--text-muted); font-size: 0.8rem;">
          No campus locations match your query.
        </div>
      `;
      return;
    }

    filtered.forEach((bldg) => {
      const card = document.createElement('div');
      card.className = `building-card ${state.selectedBuildingId === bldg.id ? 'active' : ''}`;
      card.setAttribute('data-id', bldg.id);

      const tagClass = (bldg.category || 'academic').toLowerCase();

      card.innerHTML = `
        <div class="bldg-card-header">
          <div class="bldg-card-title">${bldg.name}</div>
          <span class="bldg-tag ${tagClass}">${bldg.code}</span>
        </div>
        <p class="bldg-desc-snippet">${bldg.description}</p>
      `;

      card.addEventListener('click', () => {
        handleBuildingSelect(bldg.id);
      });

      dom.buildingList.appendChild(card);
    });
  }

  /**
   * Handle Building Selection
   */
  async function handleBuildingSelect(buildingId) {
    state.selectedBuildingId = buildingId;
    renderBuildingList();

    // Query backend endpoint for building details to verify /api/buildings/{id}
    try {
      const bldg = await ApiClient.getBuildingById(buildingId);
      state.selectedNodeId = bldg.nodeId;
      highlightNode(bldg.nodeId);
      displayBuildingDetails(bldg);
    } catch (err) {
      console.error('Error fetching building details:', err);
    }
  }

  /**
   * Handle Node Click on SVG
   */
  async function handleNodeClick(nodeId) {
    state.selectedNodeId = nodeId;
    highlightNode(nodeId);

    // Find if a building corresponds to this node
    const matchingBuilding = state.buildings.find((b) => b.nodeId === nodeId);
    if (matchingBuilding) {
      state.selectedBuildingId = matchingBuilding.id;
      renderBuildingList();
      displayBuildingDetails(matchingBuilding);
    } else {
      // Standalone node (like Junction or Gate)
      try {
        const node = await ApiClient.getNodeById(nodeId);
        displayNodeDetails(node);
      } catch (e) {
        console.error('Error fetching node details:', e);
      }
    }
  }

  /**
   * Visual Node and Connected Edges Highlight
   */
  function highlightNode(nodeId) {
    // 1. Highlight Node
    document.querySelectorAll('.graph-node').forEach((el) => {
      if (el.getAttribute('data-id') === nodeId) {
        el.classList.add('active');
      } else {
        el.classList.remove('active');
      }
    });

    // 2. Highlight Incident Edges
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
   * Reset Highlights
   */
  function resetHighlights() {
    state.selectedBuildingId = null;
    state.selectedNodeId = null;
    document.querySelectorAll('.graph-node').forEach((el) => el.classList.remove('active'));
    document.querySelectorAll('.graph-edge').forEach((el) => el.classList.remove('active'));
    renderBuildingList();
    renderDefaultDetails();
  }

  /**
   * Display Building Details in Sidebar Panel
   */
  function displayBuildingDetails(bldg) {
    dom.detailTitle.innerHTML = `<span>${bldg.name}</span> <span style="font-size:0.75rem; color:var(--accent-blue)">[${bldg.code}]</span>`;

    const facilitiesHtml = (bldg.facilities || [])
      .map((f) => `<span class="facility-pill">${f}</span>`)
      .join('');

    dom.detailContent.innerHTML = `
      <div class="detail-row">
        <span class="lbl">Category</span>
        <span class="val">${bldg.category}</span>
      </div>
      <div class="detail-row">
        <span class="lbl">Campus Node</span>
        <span class="val" style="color:var(--accent-cyan); font-weight:bold;">${bldg.nodeId}</span>
      </div>
      <div class="detail-row">
        <span class="lbl">Building Floors</span>
        <span class="val">${bldg.floors} Storeys</span>
      </div>
      <div style="margin-top: 0.5rem; font-size: 0.78rem; color: var(--text-muted);">
        ${bldg.description}
      </div>
      <div style="margin-top: 0.6rem;">
        <div style="font-size: 0.7rem; color: var(--text-subtle); text-transform: uppercase;">Key Facilities:</div>
        <div class="facility-pill-list">${facilitiesHtml}</div>
      </div>
    `;
  }

  /**
   * Display Node Details
   */
  function displayNodeDetails(node) {
    dom.detailTitle.innerHTML = `<span>Node: ${node.name}</span> <span style="font-size:0.75rem; color:var(--accent-cyan)">[${node.id}]</span>`;

    dom.detailContent.innerHTML = `
      <div class="detail-row">
        <span class="lbl">Vertex ID</span>
        <span class="val">${node.id}</span>
      </div>
      <div class="detail-row">
        <span class="lbl">Vertex Type</span>
        <span class="val">${node.type}</span>
      </div>
      <div class="detail-row">
        <span class="lbl">Map Coordinates</span>
        <span class="val">(${node.x}, ${node.y})</span>
      </div>
      <div class="detail-row">
        <span class="lbl">Wheelchair Accessible</span>
        <span class="val">${node.accessible ? '✓ Yes' : '✕ No'}</span>
      </div>
      <div style="margin-top: 0.75rem; font-size: 0.75rem; color: var(--text-muted);">
        This vertex serves as a campus waypoint and connects with pathways for routing algorithms.
      </div>
    `;
  }

  function renderDefaultDetails() {
    dom.detailTitle.textContent = 'Location Information';
    dom.detailContent.innerHTML = `
      <p style="font-size: 0.78rem; color: var(--text-muted);">
        Select any building from the list or click a vertex node on the campus map to inspect its connectivity, coordinates, and facility attributes.
      </p>
    `;
  }

  /**
   * Bind DOM Events
   */
  function bindEvents() {
    // Search input
    dom.searchInput.addEventListener('input', (e) => {
      state.searchQuery = e.target.value.trim();
      renderBuildingList();
    });

    // Category chips
    dom.categoryChips.forEach((chip) => {
      chip.addEventListener('click', () => {
        dom.categoryChips.forEach((c) => c.classList.remove('active'));
        chip.classList.add('active');
        state.selectedCategory = chip.getAttribute('data-category');
        renderBuildingList();
      });
    });

    // Reset button
    if (dom.resetBtn) {
      dom.resetBtn.addEventListener('click', resetHighlights);
    }

    // Reload button
    if (dom.reloadBtn) {
      dom.reloadBtn.addEventListener('click', async () => {
        await verifyHealth();
        await Promise.all([loadBuildings(), loadGraph()]);
        renderMapElements();
        renderBuildingList();
      });
    }

    // Toggle drawer
    if (dom.drawerToggle) {
      dom.drawerToggle.addEventListener('click', () => {
        dom.inspectorDrawer.classList.toggle('collapsed');
      });
    }
  }

  /**
   * Live REST API Inspector Setup
   */
  function setupApiInspector() {
    ApiClient.onApiCall((entry) => {
      state.apiLogs.unshift(entry);
      if (state.apiLogs.length > 30) state.apiLogs.pop();
      renderApiLogs();
    });
  }

  function renderApiLogs() {
    dom.apiLogsList.innerHTML = '';
    state.apiLogs.forEach((log, idx) => {
      const item = document.createElement('div');
      item.className = `log-entry ${idx === 0 ? 'active' : ''}`;
      item.innerHTML = `
        <div style="display:flex; align-items:center;">
          <span class="log-method ${log.method}">${log.method}</span>
          <span class="log-endpoint">${log.endpoint}</span>
        </div>
        <div>
          <span style="font-size:0.7rem; color:${log.status === 200 ? 'var(--accent-emerald)' : 'var(--accent-rose)'}; margin-right:6px;">
            ${log.status}
          </span>
          <span class="log-time">${log.durationMs}ms</span>
        </div>
      `;

      item.addEventListener('click', () => {
        document.querySelectorAll('.log-entry').forEach((e) => e.classList.remove('active'));
        item.classList.add('active');
        displayJsonPayload(log);
      });

      dom.apiLogsList.appendChild(item);
    });

    if (state.apiLogs.length > 0) {
      displayJsonPayload(state.apiLogs[0]);
    }
  }

  function displayJsonPayload(log) {
    if (!dom.jsonViewer) return;
    const header = `// [${log.method}] ${log.endpoint} — Status: ${log.status} (${log.durationMs}ms)\n// Timestamp: ${log.timestamp}\n\n`;
    const body = log.data ? JSON.stringify(log.data, null, 2) : (log.error ? `// ERROR: ${log.error}` : '// No payload');
    dom.jsonViewer.textContent = header + body;
  }

  // Start when DOM is loaded
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
