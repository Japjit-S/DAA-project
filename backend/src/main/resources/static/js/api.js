/** Small REST client for the campus application. */
const ApiClient = (() => {
  const baseUrl = window.location.origin;

  async function request(endpoint, options = {}) {
    const response = await fetch(`${baseUrl}${endpoint}`, {
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
      ...options
    });
    const body = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(body.message || `Request failed (${response.status})`);
    return body;
  }

  return {
    checkHealth: () => request('/api/health'),
    getBuildings: category => request(category ? `/api/buildings?category=${encodeURIComponent(category)}` : '/api/buildings'),
    getBuildingById: id => request(`/api/buildings/${encodeURIComponent(id)}`),
    getGraph: () => request('/api/graph'),
    getNodes: () => request('/api/graph/nodes'),
    getNodeById: id => request(`/api/graph/nodes/${encodeURIComponent(id)}`),
    getEdges: () => request('/api/graph/edges'),
    getEdgeById: id => request(`/api/graph/edges/${encodeURIComponent(id)}`),
    getValidationReport: () => request('/api/graph/validation'),
    executeAlgorithm: payload => request('/api/graph/execute', {
      method: 'POST',
      body: JSON.stringify(payload)
    }),
    getShortestPath: (startNodeId, endNodeId, accessibleOnly = false) => request(
      `/api/graph/shortest-path?startNodeId=${encodeURIComponent(startNodeId)}&endNodeId=${encodeURIComponent(endNodeId)}&accessibleOnly=${Boolean(accessibleOnly)}`
    ),
    runBfs: (startNodeId, endNodeId, accessibleOnly = false) => request(
      `/api/graph/bfs?startNodeId=${encodeURIComponent(startNodeId)}${endNodeId ? `&endNodeId=${encodeURIComponent(endNodeId)}` : ''}&accessibleOnly=${Boolean(accessibleOnly)}`
    )
  };
})();
