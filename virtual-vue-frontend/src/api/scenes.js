import request from '@/utils/request'

export function listScenes() {
  return request({
    url: '/api/scenes',
    method: 'get'
  })
}

export function getScene(id) {
  return request({
    url: `/api/scenes/${id}`,
    method: 'get'
  })
}

export function uploadScene(file, name, description, id) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('name', name)
  formData.append('description', description || '')
  if (id) {
    formData.append('id', id)
  }

  return request({
    url: '/api/scenes/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function deleteScene(id) {
  return request({
    url: `/api/scenes/${id}`,
    method: 'delete'
  })
}

export function saveSceneTextures(sceneId, textureInfo) {
  return request({
    url: `/api/scenes/${sceneId}/textures`,
    method: 'post',
    data: textureInfo
  })
}

export default {
  listScenes,
  getScene,
  uploadScene,
  deleteScene,
  saveSceneTextures
}
