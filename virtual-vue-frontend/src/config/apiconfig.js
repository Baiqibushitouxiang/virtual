export const API_CONFIG = {
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    wsBaseURL: import.meta.env.VITE_WS_BASE_URL || '',
    timeout: 5000,
    uploadTimeout: Number(import.meta.env.VITE_API_UPLOAD_TIMEOUT || 600000)
};

export const EXTERNAL_CONFIG = {
    gateStationURL: import.meta.env.VITE_GATE_STATION_URL || ''
};

function normalizePath(base, path) {
    const cleanBase = base.replace(/\/+$/, '');
    const cleanPath = path
        .replace(/\\/g, '/')
        .replace(/\/{2,}/g, '/')
        .replace(/^\/+/, '');
    return `${cleanBase}/${cleanPath}`;
}

export function getModelUrl(path) {
    if (/^https?:\/\//i.test(path)) {
        return path;
    }
    return normalizePath(API_CONFIG.baseURL, path);
}

export function getSceneUrl(path) {
    if (/^https?:\/\//i.test(path)) {
        return path;
    }
    return normalizePath(API_CONFIG.baseURL, path);
}
