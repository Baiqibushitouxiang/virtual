export const API_CONFIG = {
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:9999',
    wsBaseURL: import.meta.env.VITE_WS_BASE_URL || 'ws://127.0.0.1:9999',
    timeout: 5000
};

export const STATIC_CONFIG = {
    modelBaseURL: import.meta.env.VITE_MODEL_BASE_URL || 'http://127.0.0.1:9999',
    sceneBaseURL: import.meta.env.VITE_SCENE_BASE_URL || 'http://127.0.0.1:9999'
};

export const EXTERNAL_CONFIG = {
    gateStationURL: import.meta.env.VITE_GATE_STATION_URL || 'http://127.0.0.1:8888'
};

export const COS_CONFIG = {
    enabled: import.meta.env.VITE_COS_ENABLED === 'true',
    baseUrl: import.meta.env.VITE_COS_BASE_URL || ''
};

function normalizePath(base, path) {
    const cleanBase = base.endsWith('/') ? base.slice(0, -1) : base;
    const cleanPath = path.startsWith('/') ? path.slice(1) : path;
    return `${cleanBase}/${cleanPath}`;
}

export function getModelUrl(path) {
    if (COS_CONFIG.enabled && COS_CONFIG.baseUrl) {
        return normalizePath(COS_CONFIG.baseUrl, path);
    }
    return normalizePath(STATIC_CONFIG.modelBaseURL, path);
}

export function getSceneUrl(path) {
    if (/^https?:\/\//i.test(path)) {
        return path;
    }
    if (COS_CONFIG.enabled && COS_CONFIG.baseUrl) {
        return normalizePath(COS_CONFIG.baseUrl, path);
    }
    return normalizePath(STATIC_CONFIG.sceneBaseURL, path);
}
