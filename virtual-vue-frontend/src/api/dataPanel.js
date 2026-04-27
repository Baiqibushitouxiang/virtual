import request from '@/utils/request'

const API_PREFIX = '/api/data-panels'

export function getDataPanels(params = {}) {
  return request({
    url: API_PREFIX,
    method: 'get',
    params
  })
}

export function getDataPanel(id) {
  return request({
    url: `${API_PREFIX}/${id}`,
    method: 'get'
  })
}

export function createDataPanel(data) {
  return request({
    url: API_PREFIX,
    method: 'post',
    data
  })
}

export function updateDataPanel(id, data) {
  return request({
    url: `${API_PREFIX}/${id}`,
    method: 'put',
    data
  })
}

export function deleteDataPanel(id, sceneId) {
  return request({
    url: `${API_PREFIX}/${id}`,
    method: 'delete',
    params: sceneId ? { sceneId } : undefined
  })
}

export function bindDevice(panelId, deviceId, sceneId) {
  return request({
    url: `${API_PREFIX}/${panelId}/bind-device`,
    method: 'post',
    data: { deviceId, sceneId }
  })
}

export function unbindDevice(panelId, sceneId) {
  return request({
    url: `${API_PREFIX}/${panelId}/unbind-device`,
    method: 'post',
    data: { sceneId }
  })
}

export function bindModel(panelId, modelId, modelName, modelType, sceneId) {
  return request({
    url: `${API_PREFIX}/${panelId}/bind-model`,
    method: 'post',
    data: { modelId, modelName, modelType, sceneId }
  })
}

export function unbindModel(panelId, sceneId) {
  return request({
    url: `${API_PREFIX}/${panelId}/unbind-model`,
    method: 'post',
    data: { sceneId }
  })
}

export function updatePanelPosition(panelId, position, sceneId) {
  return request({
    url: `${API_PREFIX}/${panelId}/position`,
    method: 'put',
    data: { position, sceneId }
  })
}

export function updatePanelStyle(panelId, style, sceneId) {
  return request({
    url: `${API_PREFIX}/${panelId}/style`,
    method: 'put',
    data: { style, sceneId }
  })
}

export default {
  getDataPanels,
  getDataPanel,
  createDataPanel,
  updateDataPanel,
  deleteDataPanel,
  bindDevice,
  unbindDevice,
  bindModel,
  unbindModel,
  updatePanelPosition,
  updatePanelStyle
}
