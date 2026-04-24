import request from '@/utils/request'

const API_PREFIX = '/api/alert-rules'

export function getAlertRules() {
  return request({
    url: API_PREFIX,
    method: 'get'
  })
}

export function createAlertRule(data) {
  return request({
    url: API_PREFIX,
    method: 'post',
    data
  })
}

export function updateAlertRule(id, data) {
  return request({
    url: `${API_PREFIX}/${id}`,
    method: 'put',
    data
  })
}

export function updateAlertRuleStatus(id, enabled) {
  return request({
    url: `${API_PREFIX}/${id}/status`,
    method: 'put',
    params: { enabled }
  })
}

export function deleteAlertRule(id) {
  return request({
    url: `${API_PREFIX}/${id}`,
    method: 'delete'
  })
}
