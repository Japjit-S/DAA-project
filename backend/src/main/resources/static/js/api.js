/**
 * Smart Campus Navigation and Resource Optimizer
 * REST API Client & Communication Interceptor
 */
const ApiClient = (function () {
  const BASE_URL = window.location.origin;
  const listeners = [];

  function registerLogListener(callback) {
    listeners.push(callback);
  }

  function notifyListeners(logEntry) {
    listeners.forEach((cb) => {
      try {
        cb(logEntry);
      } catch (err) {
        console.error('Error in API log listener:', err);
      }
    });
  }

  async function request(endpoint, options = {}) {
    const startTime = performance.now();
    const method = options.method || 'GET';
    const url = `${BASE_URL}${endpoint}`;

    const logEntry = {
      id: 'REQ_' + Math.random().toString(36).substring(2, 9),
      timestamp: new Date().toLocaleTimeString(),
      method: method,
      endpoint: endpoint,
      status: 'PENDING',
      durationMs: 0,
      data: null,
      error: null
    };

    try {
      const response = await fetch(url, {
        headers: {
          'Content-Type': 'application/json',
          ...(options.headers || {})
        },
        ...options
      });

      const endTime = performance.now();
      logEntry.durationMs = Math.round(endTime - startTime);
      logEntry.status = response.status;

      if (!response.ok) {
        const errJson = await response.json().catch(() => ({}));
        logEntry.error = errJson.message || `HTTP ${response.status}`;
        notifyListeners(logEntry);
        throw new Error(logEntry.error);
      }

      const json = await response.json();
      logEntry.data = json;
      notifyListeners(logEntry);
      return json;
    } catch (err) {
      if (logEntry.status === 'PENDING') {
        const endTime = performance.now();
        logEntry.durationMs = Math.round(endTime - startTime);
        logEntry.status = 'FAILED';
        logEntry.error = err.message || 'Network Error';
        notifyListeners(logEntry);
      }
      throw err;
    }
  }

  return {
    onApiCall: registerLogListener,

    // Endpoints specified by requirements
    checkHealth: () => request('/api/health'),
    getBuildings: (category) => request(category ? `/api/buildings?category=${encodeURIComponent(category)}` : '/api/buildings'),
    getBuildingById: (id) => request(`/api/buildings/${encodeURIComponent(id)}`),
    getGraph: () => request('/api/graph'),
    getNodes: () => request('/api/graph/nodes'),
    getNodeById: (id) => request(`/api/graph/nodes/${encodeURIComponent(id)}`),
    getEdges: () => request('/api/graph/edges')
  };
})();
