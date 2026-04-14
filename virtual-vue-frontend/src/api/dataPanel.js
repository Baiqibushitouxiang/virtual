import request from '@/utils/request'

const API_PREFIX = '/api/data-panels'

export function getDataPanels() {
  return request({
    url: API_PREFIX,
    method: 'get'
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

export function deleteDataPanel(id) {
  return request({
    url: `${API_PREFIX}/${id}`,
    method: 'delete'
  })
}

export function bindDevice(panelId, deviceId) {
  return request({
    url: `${API_PREFIX}/${panelId}/bind-device`,
    method: 'post',
    data: { deviceId }
  })
}

export function unbindDevice(panelId) {
  return request({
    url: `${API_PREFIX}/${panelId}/unbind-device`,
    method: 'post'
  })
}

export function bindModel(panelId, modelId, modelName, modelType) {
  return request({
    url: `${API_PREFIX}/${panelId}/bind-model`,
    method: 'post',
    data: { modelId, modelName, modelType }
  })
}

export function unbindModel(panelId) {
  return request({
    url: `${API_PREFIX}/${panelId}/unbind-model`,
    method: 'post'
  })
}

export function updatePanelPosition(panelId, position) {
  return request({
    url: `${API_PREFIX}/${panelId}/position`,
    method: 'put',
    data: { position }
  })
}

export function updatePanelStyle(panelId, style) {
  return request({
    url: `${API_PREFIX}/${panelId}/style`,
    method: 'put',
    data: { style }
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
