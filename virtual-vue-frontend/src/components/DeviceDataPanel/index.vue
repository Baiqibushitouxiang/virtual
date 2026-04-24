<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { API_CONFIG } from '@/config/apiConfig'
import { updatePanelPosition, unbindModel } from '@/api/dataPanel'

const props = defineProps({
  visible: { type: Boolean, default: true },
  panelConfig: { type: Object, default: null },
  boundModel: { type: Object, default: null },
  modelBinding: { type: Object, default: null },
  bindMode: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'startBind', 'cancelBind', 'updatePanel', 'deletePanel', 'unbindModel'])

const deviceData = ref(null)
const isConnected = ref(false)
const ws = ref(null)
const panelStyle = ref({ top: '80px', left: '20px' })
const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const savePositionTimeout = ref(null)

const panelName = computed(() => props.panelConfig?.name || '数据展板')
const hasModelBinding = computed(() => !!props.panelConfig?.modelId)
const activeDataBinding = computed(() => props.modelBinding || null)
const hasDataSource = computed(() => !!activeDataBinding.value?.deviceCode && !!activeDataBinding.value?.dataType)

const initPosition = () => {
  if (props.panelConfig?.position) {
    try {
      const pos = typeof props.panelConfig.position === 'string'
        ? JSON.parse(props.panelConfig.position)
        : props.panelConfig.position
      panelStyle.value = {
        top: pos.top || '80px',
        left: pos.left || '20px'
      }
    } catch (e) {
      console.error('parse panel position failed:', e)
    }
  }
}

const getWebSocketUrl = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = API_CONFIG.baseURL.replace(/^https?:\/\//, '')
  return `${protocol}${host}/ws/device-data`
}

const lineStart = computed(() => props.boundModel?.screenPosition || null)
const lineEnd = computed(() => ({
  x: parseInt(panelStyle.value.left) + 120,
  y: parseInt(panelStyle.value.top) + 40
}))

const startDrag = (e) => {
  if (props.bindMode) return
  isDragging.value = true
  dragStart.value = {
    x: e.clientX - parseInt(panelStyle.value.left),
    y: e.clientY - parseInt(panelStyle.value.top)
  }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const onDrag = (e) => {
  if (!isDragging.value) return
  panelStyle.value = {
    top: `${e.clientY - dragStart.value.y}px`,
    left: `${e.clientX - dragStart.value.x}px`
  }
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  savePositionDebounced()
}

const savePositionDebounced = async () => {
  if (!props.panelConfig?.id) return
  if (savePositionTimeout.value) {
    clearTimeout(savePositionTimeout.value)
  }
  savePositionTimeout.value = setTimeout(async () => {
    try {
      await updatePanelPosition(props.panelConfig.id, JSON.stringify(panelStyle.value))
    } catch (e) {
      console.error('save panel position failed:', e)
    }
  }, 500)
}

const formatTime = (timestamp) => {
  if (!timestamp) return '--'
  return new Date(timestamp).toLocaleTimeString('zh-CN')
}

const formatDataType = (type) => {
  const typeMap = {
    temperature: '温度',
    humidity: '湿度',
    pressure: '压力',
    voltage: '电压',
    current: '电流',
    power: '功率'
  }
  return typeMap[type] || type
}

const connect = () => {
  ws.value = new WebSocket(getWebSocketUrl())
  ws.value.onopen = () => { isConnected.value = true }
  ws.value.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      const matchesDevice = !activeDataBinding.value?.deviceCode || data.deviceId === activeDataBinding.value.deviceCode
      const dataType = data?.data?.dataType || data?.dataType
      const matchesType = !activeDataBinding.value?.dataType || dataType === activeDataBinding.value.dataType
      if (matchesDevice && matchesType) {
        deviceData.value = data
      }
    } catch (e) {
      console.error('parse websocket message failed:', e)
    }
  }
  ws.value.onclose = () => { isConnected.value = false }
  ws.value.onerror = () => { isConnected.value = false }
}

const handleStartBind = () => {
  emit('startBind', props.panelConfig)
}

const handleCancelBind = async () => {
  if (props.panelConfig?.id) {
    try {
      await unbindModel(props.panelConfig.id)
      emit('unbindModel', props.panelConfig.id)
      emit('updatePanel')
    } catch (e) {
      console.error('unbind panel model failed:', e)
    }
  }
  emit('cancelBind')
}

const handleDelete = () => {
  emit('deletePanel', props.panelConfig)
}

watch(() => props.panelConfig, () => {
  initPosition()
}, { immediate: true, deep: true })

watch(activeDataBinding, () => {
  deviceData.value = null
})

onMounted(connect)
onUnmounted(() => {
  if (ws.value) ws.value.close()
  if (savePositionTimeout.value) clearTimeout(savePositionTimeout.value)
})
</script>

<template>
  <div v-if="visible" class="data-panel" :style="panelStyle">
    <div class="panel-header" @mousedown="startDrag">
      <span class="panel-title">{{ panelName }}</span>
      <span class="status" :class="{ connected: isConnected }">{{ isConnected ? '已连接' : '未连接' }}</span>
      <div class="header-actions">
        <button class="delete-btn" @click.stop="handleDelete" title="删除展板">x</button>
        <button class="close-btn" @click.stop="$emit('close')">-</button>
      </div>
    </div>
    <div class="panel-body">
      <div class="binding-card">
        <div class="binding-row">
          <span class="label">模型</span>
          <span class="value">{{ hasModelBinding ? props.panelConfig.modelName : '未绑定' }}</span>
        </div>
        <div class="binding-row">
          <span class="label">设备</span>
          <span class="value">{{ activeDataBinding?.deviceName || '未绑定' }}</span>
        </div>
        <div class="binding-row">
          <span class="label">数据项</span>
          <span class="value">{{ formatDataType(activeDataBinding?.dataType || '-') }}</span>
        </div>
      </div>

      <div v-if="!hasModelBinding" class="empty">
        <p>请先为展板绑定模型</p>
      </div>
      <div v-else-if="!hasDataSource" class="empty">
        <p>模型还没有绑定数据源</p>
        <p class="hint">请在模型详情面板中完成设备与数据项绑定</p>
      </div>
      <div v-else-if="!deviceData" class="empty">等待数据中...</div>
      <div v-else class="data-content">
        <div class="row">
          <span class="label">设备编码:</span>
          <span class="value">{{ deviceData.deviceId }}</span>
        </div>
        <div class="row">
          <span class="label">数据项:</span>
          <span class="value">{{ formatDataType(deviceData?.data?.dataType || activeDataBinding?.dataType) }}</span>
        </div>
        <div class="row">
          <span class="label">数值:</span>
          <span class="value highlight">{{ deviceData?.data?.value }} {{ deviceData?.data?.unit || '' }}</span>
        </div>
        <div class="row">
          <span class="label">时间:</span>
          <span class="value">{{ formatTime(deviceData.timestamp) }}</span>
        </div>
      </div>

      <div class="bind-actions">
        <button v-if="!hasModelBinding" class="bind-btn" :class="{ active: bindMode }" @click="handleStartBind">
          {{ bindMode ? '点击场景中的模型...' : '绑定模型' }}
        </button>
        <button v-else class="unbind-btn" @click="handleCancelBind">解除模型</button>
      </div>
    </div>

    <svg v-if="lineStart && hasModelBinding" class="connection-line">
      <line
        :x1="lineStart.x"
        :y1="lineStart.y"
        :x2="lineEnd.x"
        :y2="lineEnd.y"
        stroke="#52c41a"
        stroke-width="2"
        stroke-dasharray="5,5"
      />
      <circle :cx="lineStart.x" :cy="lineStart.y" r="6" fill="#52c41a" />
    </svg>
  </div>
</template>

<style scoped>
.data-panel {
  position: fixed;
  width: 280px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 13px;
  z-index: 1001;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: linear-gradient(135deg, #28547a 0%, #4c7a9d 100%);
  border-radius: 8px 8px 0 0;
  cursor: move;
  color: #fff;
}

.panel-title {
  font-weight: 600;
  font-size: 14px;
}

.status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
}

.status.connected {
  background: rgba(82, 196, 26, 0.3);
  color: #b7eb8f;
}

.header-actions {
  display: flex;
  gap: 4px;
}

.close-btn,
.delete-btn,
.bind-btn,
.unbind-btn {
  border: none;
  cursor: pointer;
  border-radius: 6px;
}

.close-btn,
.delete-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 2px 8px;
}

.panel-body {
  padding: 12px;
}

.binding-card {
  background: #f6f8fb;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 12px;
}

.binding-row,
.row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.label {
  color: #666;
}

.value {
  color: #222;
  word-break: break-all;
  text-align: right;
}

.highlight {
  color: #1677ff;
  font-weight: 700;
}

.empty {
  text-align: center;
  color: #666;
  padding: 12px 0;
}

.hint {
  font-size: 12px;
  color: #999;
}

.bind-actions {
  margin-top: 12px;
}

.bind-btn,
.unbind-btn {
  width: 100%;
  padding: 8px 10px;
  color: #fff;
}

.bind-btn {
  background: #1677ff;
}

.bind-btn.active {
  background: #52c41a;
}

.unbind-btn {
  background: #f56c6c;
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
