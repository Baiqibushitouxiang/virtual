const DATA_TYPE_LABELS = {
  temperature: '温度',
  humidity: '湿度',
  pressure: '压力',
  voltage: '电压',
  current: '电流',
  power: '功率'
}

const MESSAGE_TYPE_LABELS = {
  deviceData: '设备数据',
  data: '实时数据',
  alert: '告警',
  notification: '通知',
  broadcast: '广播'
}

function isPlainObject(value) {
  return Object.prototype.toString.call(value) === '[object Object]'
}

function tryParseJson(value) {
  if (typeof value !== 'string') {
    return value
  }
  const text = value.trim()
  if (!text) {
    return value
  }
  if ((text.startsWith('{') && text.endsWith('}')) || (text.startsWith('[') && text.endsWith(']'))) {
    try {
      return JSON.parse(text)
    } catch (error) {
      return value
    }
  }
  return value
}

export function formatDataTypeLabel(type) {
  if (!type) {
    return '--'
  }
  return DATA_TYPE_LABELS[type] || type
}

export function formatMessageTypeLabel(type) {
  if (!type) {
    return '未知'
  }
  return MESSAGE_TYPE_LABELS[type] || type
}

export function safeJsonStringify(value) {
  if (value == null) {
    return '--'
  }
  if (typeof value === 'string') {
    return value
  }
  try {
    return JSON.stringify(value)
  } catch (error) {
    return String(value)
  }
}

function unwrapValue(value) {
  const parsed = tryParseJson(value)
  if (parsed == null) {
    return null
  }
  if (Array.isArray(parsed)) {
    return safeJsonStringify(parsed)
  }
  if (isPlainObject(parsed)) {
    const preferredKeys = ['value', 'currentValue', 'number', 'text', 'message', 'content']
    for (const key of preferredKeys) {
      if (Object.prototype.hasOwnProperty.call(parsed, key) && typeof parsed[key] !== 'object') {
        return parsed[key]
      }
    }
    return safeJsonStringify(parsed)
  }
  return parsed
}

export function formatValueForDisplay(value) {
  const unwrapped = unwrapValue(value)
  if (unwrapped == null || unwrapped === '') {
    return '--'
  }
  if (typeof unwrapped === 'number') {
    if (Number.isInteger(unwrapped)) {
      return String(unwrapped)
    }
    return String(parseFloat(unwrapped.toFixed(4)))
  }
  return String(unwrapped)
}

export function extractMessagePayload(message) {
  if (!message) {
    return null
  }
  const payload = Object.prototype.hasOwnProperty.call(message, 'data') ? message.data : message
  return tryParseJson(payload)
}

export function extractMessageTimestamp(message) {
  const payload = extractMessagePayload(message)
  return message?.timestamp || payload?.timestamp || null
}

export function extractMessageDataType(message) {
  const payload = extractMessagePayload(message)
  if (!payload || typeof payload === 'string') {
    return null
  }
  if (payload.dataType) {
    return payload.dataType
  }
  if (typeof payload.nodeId === 'string') {
    const parts = payload.nodeId.split('.')
    return parts.length >= 3 ? parts[2] : null
  }
  return null
}

export function extractMessageUnit(message) {
  const payload = extractMessagePayload(message)
  if (!payload || typeof payload === 'string') {
    return ''
  }
  return payload.unit || ''
}

export function extractMessageValue(message) {
  const payload = extractMessagePayload(message)
  if (payload == null) {
    return null
  }
  if (typeof payload !== 'object') {
    return payload
  }
  if (Object.prototype.hasOwnProperty.call(payload, 'value')) {
    return payload.value
  }
  return payload
}

export function formatMessageContent(message) {
  const payload = extractMessagePayload(message)
  const type = message?.type

  if (type === 'alert') {
    if (payload && typeof payload === 'object') {
      const parts = [payload.level, payload.message].filter(Boolean)
      return parts.length > 0 ? parts.join(' - ') : safeJsonStringify(payload)
    }
    return formatValueForDisplay(payload)
  }

  if (type === 'notification') {
    if (payload && typeof payload === 'object') {
      const parts = [payload.title, payload.content].filter(Boolean)
      return parts.length > 0 ? parts.join(' - ') : safeJsonStringify(payload)
    }
    return formatValueForDisplay(payload)
  }

  const dataType = extractMessageDataType(message)
  const value = formatValueForDisplay(extractMessageValue(message))
  const unit = extractMessageUnit(message)
  if (dataType) {
    return `${formatDataTypeLabel(dataType)}: ${value}${unit ? ` ${unit}` : ''}`
  }

  return safeJsonStringify(payload)
}
