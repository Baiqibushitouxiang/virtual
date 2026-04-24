import request from '@/utils/request'

const API_PREFIX = '/api/model-bindings'

export function getModelBindings(params) {
  return request({
    url: API_PREFIX,
    method: 'get',
    params
  })
}

export function createModelBinding(data) {
  return request({
    url: API_PREFIX,
    method: 'post',
    data
  })
}

export function updateModelBinding(id, data) {
  return request({
    url: `${API_PREFIX}/${id}`,
    method: 'put',
    data
  })
}

export function deleteModelBinding(id) {
  return request({
    url: `${API_PREFIX}/${id}`,
    method: 'delete'
  })
}

export function deleteModelBindingBySceneAndModel(sceneId, modelId) {
  return request({
    url: API_PREFIX,
    method: 'delete',
    params: { sceneId, modelId }
  })
}
