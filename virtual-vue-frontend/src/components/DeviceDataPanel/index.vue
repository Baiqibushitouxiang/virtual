<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, Delete, Setting } from '@element-plus/icons-vue'
import { API_CONFIG } from '@/config/apiConfig'
import { getAllDevices } from '@/api/device'
import { updateDataPanel, updatePanelPosition, updatePanelStyle } from '@/api/dataPanel'
import {
  extractMessageDataType,
  extractMessageTimestamp,
  extractMessageUnit,
  extractMessageValue,
  formatDataTypeLabel,
  formatMessageContent,
  formatMessageTypeLabel,
  formatValueForDisplay
} from '@/utils/deviceMessage'

const DEFAULT_POSITION = { top: '80px', left: '20px' }
const DEFAULT_SIZE = { width: 320, height: 196 }
const DEFAULT_STYLE = { opacity: 0.92, scale: 1 }

const props = defineProps({
  visible: { type: Boolean, default: true },
  panelConfig: { type: Object, default: null },
  boundModel: { type: Object, default: null },
  modelBinding: { type: Object, default: null },
  bindMode: { type: Boolean, default: false },
  sceneId: { type: [Number, String], default: null }
})

const emit = defineEmits(['close', 'updatePanel', 'deletePanel', 'patchPanel'])

const deviceData = ref(null)
const isConnected = ref(false)
const ws = ref(null)
const devices = ref([])
const panelPosition = ref({ ...DEFAULT_POSITION })
const panelSize = ref({ ...DEFAULT_SIZE })
const panelVisualStyle = ref({ ...DEFAULT_STYLE })
const showSettings = ref(false)
const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const savePositionTimeout = ref(null)
const saveSettingsTimeout = ref(null)
const suppressSettingsSave = ref(false)

const isPersistedPanel = computed(() => typeof props.panelConfig?.id === 'number' && !!props.sceneId)
const panelName = computed(() => props.panelConfig?.name || '数据展板')
const hasModelBinding = computed(() => !!props.panelConfig?.modelId)
const modelDataBinding = computed(() => props.modelBinding || null)

const resolvedPanelDevice = computed(() => {
  const panel = props.panelConfig || {}
  if (panel.device) {
    return panel.device
  }
  if (!panel.deviceId) {
    return null
  }
  return devices.value.find(item => item.id === panel.deviceId) || null
})

const directDeviceBinding = computed(() => {
  const panel = props.panelConfig || {}
  const device = resolvedPanelDevice.value
  if (!panel.deviceId && !device) {
    return null
  }
  return {
    source: 'device',
    deviceId: panel.deviceId || device?.id || null,
    deviceName: panel.deviceName || device?.name || '--',
    deviceCode: device?.deviceId || null
  }
})

const activeDataBinding = computed(() => {
  if (directDeviceBinding.value?.deviceCode) {
    return directDeviceBinding.value
  }
  return modelDataBinding.value || null
})

const bindingModeLabel = computed(() => {
  if (directDeviceBinding.value?.deviceCode) {
    return '设备直连'
  }
  if (modelDataBinding.value?.deviceCode) {
    return '模型映射'
  }
  return '未绑定'
})

const hasDataSource = computed(() => !!activeDataBinding.value?.deviceCode)
const lineStart = computed(() => props.boundModel?.screenPosition || null)
const lineEnd = computed(() => {
  const left = parseInt(panelPosition.value.left, 10)
  const top = parseInt(panelPosition.value.top, 10)
  const width = Number(panelSize.value.width) || DEFAULT_SIZE.width
  const height = Number(panelSize.value.height) || DEFAULT_SIZE.height
  const scale = Number(panelVisualStyle.value.scale) || DEFAULT_STYLE.scale
  return {
    x: left + (width * scale) / 2,
    y: top + (height * scale) / 2
  }
})

const currentDeviceCode = computed(() => (
  deviceData.value?.deviceId
  || activeDataBinding.value?.deviceCode
  || resolvedPanelDevice.value?.deviceId
  || '--'
))
const currentDataType = computed(() => (
  extractMessageDataType(deviceData.value)
  || modelDataBinding.value?.dataType
  || ''
))
const currentUnit = computed(() => extractMessageUnit(deviceData.value))
const currentValue = computed(() => formatValueForDisplay(extractMessageValue(deviceData.value)))
const currentTime = computed(() => formatTime(extractMessageTimestamp(deviceData.value)))
const currentMessageType = computed(() => formatMessageTypeLabel(deviceData.value?.type))
const currentMessageContent = computed(() => (
  deviceData.value ? formatMessageContent(deviceData.value) : '--'
))
const isValueMessage = computed(() => ['deviceData', 'data'].includes(deviceData.value?.type))
const currentSourceDeviceName = computed(() => (
  activeDataBinding.value?.deviceName
  || props.panelConfig?.deviceName
  || '--'
))
const panelTransformStyle = computed(() => ({
  top: panelPosition.value.top,
  left: panelPosition.value.left,
  width: `${panelSize.value.width}px`,
  minHeight: `${panelSize.value.height}px`,
  opacity: panelVisualStyle.value.opacity,
  transform: `scale(${panelVisualStyle.value.scale})`,
  transformOrigin: 'top left'
}))

function parseJsonConfig(source, fallback) {
  if (!source) {
    return { ...fallback }
  }
  try {
    const parsed = typeof source === 'string' ? JSON.parse(source) : source
    return parsed && typeof parsed === 'object'
      ? { ...fallback, ...parsed }
      : { ...fallback }
  } catch (error) {
    console.error('parse panel config failed:', error)
    return { ...fallback }
  }
}

const buildPanelUpdatePayload = (overrides = {}) => ({
  name: props.panelConfig?.name || panelName.value,
  description: props.panelConfig?.description || '',
  status: props.panelConfig?.status ?? 1,
  deviceId: props.panelConfig?.deviceId ?? null,
  deviceName: props.panelConfig?.deviceName ?? null,
  modelId: props.panelConfig?.modelId ?? null,
  modelName: props.panelConfig?.modelName ?? null,
  modelType: props.panelConfig?.modelType ?? null,
  position: JSON.stringify(panelPosition.value),
  size: JSON.stringify(panelSize.value),
  sceneId: props.sceneId,
  ...overrides
})

const hydratePanelState = () => {
  suppressSettingsSave.value = true
  panelPosition.value = parseJsonConfig(props.panelConfig?.position, DEFAULT_POSITION)
  panelSize.value = parseJsonConfig(props.panelConfig?.size, DEFAULT_SIZE)
  panelVisualStyle.value = parseJsonConfig(props.panelConfig?.style, DEFAULT_STYLE)
  panelSize.value.width = Math.max(240, Number(panelSize.value.width) || DEFAULT_SIZE.width)
  panelSize.value.height = Math.max(140, Number(panelSize.value.height) || DEFAULT_SIZE.height)
  panelVisualStyle.value.opacity = Number(panelVisualStyle.value.opacity) || DEFAULT_STYLE.opacity
  panelVisualStyle.value.scale = Number(panelVisualStyle.value.scale) || DEFAULT_STYLE.scale
  requestAnimationFrame(() => {
    suppressSettingsSave.value = false
  })
}

const getWebSocketUrl = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = API_CONFIG.baseURL.replace(/^https?:\/\//, '')
  return `${protocol}${host}/ws/device-data`
}

const formatTime = (timestamp) => {
  if (!timestamp) {
    return '--'
  }
  return new Date(timestamp).toLocaleTimeString('zh-CN')
}

const loadDevices = async () => {
  try {
    const response = await getAllDevices()
    devices.value = response.data || response || []
  } catch (error) {
    console.error('load devices failed:', error)
  }
}

const startDrag = (event) => {
  if (props.bindMode) {
    return
  }
  isDragging.value = true
  dragStart.value = {
    x: event.clientX - parseInt(panelPosition.value.left, 10),
    y: event.clientY - parseInt(panelPosition.value.top, 10)
  }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const onDrag = (event) => {
  if (!isDragging.value) {
    return
  }
  panelPosition.value = {
    top: `${event.clientY - dragStart.value.y}px`,
    left: `${event.clientX - dragStart.value.x}px`
  }
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  savePositionDebounced()
}

const savePositionDebounced = async () => {
  if (!props.panelConfig?.id) {
    return
  }
  if (savePositionTimeout.value) {
    clearTimeout(savePositionTimeout.value)
  }
  savePositionTimeout.value = setTimeout(async () => {
    const position = JSON.stringify(panelPosition.value)
    try {
      if (isPersistedPanel.value) {
        await updatePanelPosition(props.panelConfig.id, position, props.sceneId)
        emit('updatePanel')
      } else {
        emit('patchPanel', {
          panelId: props.panelConfig.id,
          changes: { position }
        })
      }
    } catch (error) {
      console.error('save panel position failed:', error)
    }
  }, 400)
}

const saveSettingsDebounced = async () => {
  if (!props.panelConfig?.id || suppressSettingsSave.value) {
    return
  }
  if (saveSettingsTimeout.value) {
    clearTimeout(saveSettingsTimeout.value)
  }
  saveSettingsTimeout.value = setTimeout(async () => {
    const size = JSON.stringify(panelSize.value)
    const style = JSON.stringify(panelVisualStyle.value)
    try {
      if (isPersistedPanel.value) {
        await Promise.all([
          updateDataPanel(props.panelConfig.id, buildPanelUpdatePayload({ size })),
          updatePanelStyle(props.panelConfig.id, style, props.sceneId)
        ])
        emit('updatePanel')
      } else {
        emit('patchPanel', {
          panelId: props.panelConfig.id,
          changes: { size, style }
        })
      }
    } catch (error) {
      console.error('save panel settings failed:', error)
      ElMessage.error('保存展板设置失败')
    }
  }, 300)
}

const isRelevantMessageType = (message) => ['deviceData', 'data', 'alert', 'notification'].includes(message?.type)

const matchesBinding = (message) => {
  if (!activeDataBinding.value?.deviceCode) {
    return false
  }
  if (message.deviceId !== activeDataBinding.value.deviceCode) {
    return false
  }

  const bindingDataType = modelDataBinding.value?.dataType
  if (!bindingDataType) {
    return true
  }

  if (['alert', 'notification'].includes(message.type)) {
    return true
  }

  const incomingDataType = extractMessageDataType(message)
  return !incomingDataType || incomingDataType === bindingDataType
}

const connect = () => {
  ws.value = new WebSocket(getWebSocketUrl())
  ws.value.onopen = () => {
    isConnected.value = true
  }
  ws.value.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data)
      if (!isRelevantMessageType(message)) {
        return
      }
      if (matchesBinding(message)) {
        deviceData.value = message
      }
    } catch (error) {
      console.error('parse websocket message failed:', error)
    }
  }
  ws.value.onclose = () => {
    isConnected.value = false
  }
  ws.value.onerror = () => {
    isConnected.value = false
  }
}

const handleDelete = () => {
  emit('deletePanel', props.panelConfig)
}

watch(() => props.panelConfig, () => {
  hydratePanelState()
}, { immediate: true, deep: true })

watch(activeDataBinding, () => {
  deviceData.value = null
})

watch(panelSize, () => {
  panelSize.value.width = Math.max(240, Number(panelSize.value.width) || DEFAULT_SIZE.width)
  panelSize.value.height = Math.max(140, Number(panelSize.value.height) || DEFAULT_SIZE.height)
  saveSettingsDebounced()
}, { deep: true })

watch(panelVisualStyle, () => {
  panelVisualStyle.value.opacity = Math.min(1, Math.max(0.2, Number(panelVisualStyle.value.opacity) || DEFAULT_STYLE.opacity))
  panelVisualStyle.value.scale = Math.min(1.5, Math.max(0.75, Number(panelVisualStyle.value.scale) || DEFAULT_STYLE.scale))
  saveSettingsDebounced()
}, { deep: true })

onMounted(async () => {
  await loadDevices()
  connect()
})

onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
  }
  if (savePositionTimeout.value) {
    clearTimeout(savePositionTimeout.value)
  }
  if (saveSettingsTimeout.value) {
    clearTimeout(saveSettingsTimeout.value)
  }
})
</script>

<template>
  <div v-if="visible" class="data-panel" :style="panelTransformStyle">
    <div class="panel-header" @mousedown="startDrag">
      <div class="panel-heading">
        <span class="panel-title">{{ panelName }}</span>
        <span class="panel-source">{{ bindingModeLabel }}</span>
      </div>
      <div class="header-actions">
        <span class="status" :class="{ connected: isConnected }">{{ isConnected ? '在线' : '离线' }}</span>
        <el-button text circle class="header-btn" @click.stop="showSettings = !showSettings">
          <el-icon><Setting /></el-icon>
        </el-button>
        <el-button text circle class="header-btn" @click.stop="handleDelete">
          <el-icon><Delete /></el-icon>
        </el-button>
        <el-button text circle class="header-btn" @click.stop="$emit('close')">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>

    <div v-if="showSettings" class="settings-panel">
      <div class="setting-row">
        <span class="setting-label">透明度</span>
        <el-slider v-model="panelVisualStyle.opacity" :min="0.2" :max="1" :step="0.05" />
      </div>
      <div class="setting-grid">
        <div class="setting-item">
          <span class="setting-label">宽度</span>
          <el-input-number v-model="panelSize.width" :min="240" :max="560" :step="10" size="small" />
        </div>
        <div class="setting-item">
          <span class="setting-label">高度</span>
          <el-input-number v-model="panelSize.height" :min="140" :max="420" :step="10" size="small" />
        </div>
        <div class="setting-item setting-item--full">
          <span class="setting-label">缩放</span>
          <el-slider v-model="panelVisualStyle.scale" :min="0.75" :max="1.5" :step="0.05" />
        </div>
      </div>
    </div>

    <div class="panel-body">
      <div class="summary">
        <div class="summary-item">
          <span class="summary-label">绑定模型</span>
          <span class="summary-value">{{ hasModelBinding ? props.panelConfig.modelName : '未绑定' }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">数据来源</span>
          <span class="summary-value">{{ currentSourceDeviceName }}</span>
        </div>
      </div>

      <div v-if="!hasDataSource" class="empty-state">
        请在展板管理中绑定设备或模型
      </div>
      <div v-else-if="!deviceData" class="empty-state">
        等待实时数据...
      </div>
      <div v-else class="metric-block">
        <div class="metric-header">
          <span class="metric-device">{{ currentDeviceCode }}</span>
          <span v-if="currentDataType" class="metric-type">{{ formatDataTypeLabel(currentDataType) }}</span>
          <span v-else class="metric-type">{{ currentMessageType }}</span>
        </div>

        <div v-if="isValueMessage" class="metric-value">
          {{ currentValue }}
          <span class="metric-unit">{{ currentUnit }}</span>
        </div>
        <div v-else class="metric-message">
          {{ currentMessageContent }}
        </div>

        <div class="metric-footer">
          <span>{{ currentTime }}</span>
        </div>
      </div>
    </div>

    <svg v-if="lineStart && hasModelBinding" class="connection-line">
      <line
        :x1="lineStart.x"
        :y1="lineStart.y"
        :x2="lineEnd.x"
        :y2="lineEnd.y"
        stroke="#409eff"
        stroke-width="1.5"
        stroke-dasharray="4,4"
      />
      <circle :cx="lineStart.x" :cy="lineStart.y" r="5" fill="#409eff" />
    </svg>
  </div>
</template>

<style scoped>
.data-panel {
  position: fixed;
  z-index: 1001;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.12);
  overflow: hidden;
  color: #1f2937;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  cursor: move;
  background: rgba(248, 250, 252, 0.9);
}

.panel-heading {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.panel-source {
  font-size: 12px;
  color: #64748b;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.status {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.14);
  color: #64748b;
  font-size: 11px;
}

.status.connected {
  background: rgba(22, 119, 255, 0.12);
  color: #1677ff;
}

.header-btn {
  width: 24px;
  height: 24px;
  padding: 0;
  color: #475569;
}

.settings-panel {
  padding: 12px;
  background: #f8fafc;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.setting-row,
.setting-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.setting-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.setting-item--full {
  grid-column: 1 / -1;
}

.setting-label {
  font-size: 12px;
  color: #64748b;
}

.panel-body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.summary-item {
  min-width: 0;
}

.summary-label {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
  color: #64748b;
}

.summary-value {
  display: block;
  font-size: 13px;
  color: #0f172a;
  word-break: break-all;
}

.metric-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-header,
.metric-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}

.metric-device,
.metric-type {
  min-width: 0;
}

.metric-value {
  font-size: 30px;
  line-height: 1;
  font-weight: 600;
  color: #111827;
}

.metric-unit {
  margin-left: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
}

.metric-message {
  padding: 10px 12px;
  border-radius: 8px;
  background: rgba(241, 245, 249, 0.9);
  font-size: 13px;
  line-height: 1.5;
  color: #334155;
  word-break: break-word;
}

.empty-state {
  padding: 18px 0;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
}

.connection-line {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1000;
}
</style>
