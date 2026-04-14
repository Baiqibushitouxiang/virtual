import request from '../utils/request';

const API_PREFIX = '/api';

export const getDevices = (params) => {
  return request.get(`${API_PREFIX}/devices`, { params });
};

export const getAllDevices = () => {
  return request.get(`${API_PREFIX}/devices`);
};

export const getDevice = (id) => {
  return request.get(`${API_PREFIX}/devices/${id}`);
};

export const getDeviceByDeviceId = (deviceId) => {
  return request.get(`${API_PREFIX}/devices/deviceId/${deviceId}`);
};

export const registerDevice = (data) => {
  return request.post(`${API_PREFIX}/devices`, data);
};

export const updateDevice = (id, data) => {
  return request.put(`${API_PREFIX}/devices/${id}`, data);
};

export const deleteDevice = (id) => {
  return request.delete(`${API_PREFIX}/devices/${id}`);
};

export const bindDevice = (id, userId) => {
  return request.post(`${API_PREFIX}/devices/${id}/bind`, { userId });
};

export const unbindDevice = (id) => {
  return request.post(`${API_PREFIX}/devices/${id}/unbind`);
};

export const updateDeviceStatus = (id, status) => {
  return request.put(`${API_PREFIX}/devices/${id}/status`, null, { params: { status } });
};

export const generateDeviceCertificate = (id) => {
  return request.post(`${API_PREFIX}/devices/${id}/certificate`);
};

export const getDeviceCertificate = (id) => {
  return request.get(`${API_PREFIX}/devices/${id}/certificate`);
};

export const revokeDeviceCertificate = (id) => {
  return request.post(`${API_PREFIX}/devices/${id}/certificate/revoke`);
};

export const getDeviceData = (deviceId) => {
  return request.get(`${API_PREFIX}/device-data/latest/${deviceId}`);
};

export const getDeviceDataHistory = (deviceId, params) => {
  return request.get(`${API_PREFIX}/device-data/history/${deviceId}`, { params });
};

export const getDeviceDataStats = (deviceId, params) => {
  return request.get(`${API_PREFIX}/device-data/stats/${deviceId}`, { params });
};

export const countDeviceData = (deviceId) => {
  return request.get(`${API_PREFIX}/device-data/count/${deviceId}`);
};

export const sendDeviceData = (deviceId, dataType, value, unit) => {
  return request.post(`${API_PREFIX}/device-data`, { deviceId, dataType, value, unit });
};

export default {
  getDevices,
  getAllDevices,
  getDevice,
  getDeviceByDeviceId,
  registerDevice,
  updateDevice,
  deleteDevice,
  bindDevice,
  unbindDevice,
  updateDeviceStatus,
  generateDeviceCertificate,
  getDeviceCertificate,
  revokeDeviceCertificate,
  getDeviceData,
  getDeviceDataHistory,
  getDeviceDataStats,
  countDeviceData,
  sendDeviceData
};
