<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { API_CONFIG } from '@/config/apiConfig';
import { updateDataPanel, bindModel, unbindModel, bindDevice, unbindDevice, updatePanelPosition } from '@/api/dataPanel';

const props = defineProps({
  visible: { type: Boolean, default: true },
  panelConfig: { type: Object, default: null },
  boundModel: { type: Object, default: null },
  bindMode: { type: Boolean, default: false }
});

const emit = defineEmits(['close', 'startBind', 'cancelBind', 'updatePanel', 'deletePanel', 'unbindModel']);

const deviceData = ref(null);
const isConnected = ref(false);
const ws = ref(null);

const panelStyle = ref({ top: '80px', left: '20px' });
const isDragging = ref(false);
const dragStart = ref({ x: 0, y: 0 });
const savePositionTimeout = ref(null);

const panelName = computed(() => {
  return props.panelConfig?.name || '设备数据展板';
});

const hasDeviceBinding = computed(() => {
  return props.panelConfig?.deviceId != null;
});

const hasModelBinding = computed(() => {
  return props.panelConfig?.modelId != null;
});

const deviceCode = computed(() => {
  return props.panelConfig?.device?.deviceId || null;
});

const initPosition = () => {
  if (props.panelConfig?.position) {
    try {
      const pos = typeof props.panelConfig.position === 'string' 
        ? JSON.parse(props.panelConfig.position) 
        : props.panelConfig.position;
      panelStyle.value = {
        top: pos.top || '80px',
        left: pos.left || '20px'
      };
    } catch (e) {
      console.error('解析位置失败:', e);
    }
  }
};

const getWebSocketUrl = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const host = API_CONFIG.baseURL.replace(/^https?:\/\//, '');
  return `${protocol}${host}/ws/device-data`;
};

const lineStart = computed(() => {
  if (!props.boundModel || !props.boundModel.screenPosition) return null;
  return props.boundModel.screenPosition;
});

const lineEnd = computed(() => {
  return {
    x: parseInt(panelStyle.value.left) + 120,
    y: parseInt(panelStyle.value.top) + 40
  };
});

const startDrag = (e) => {
  if (props.bindMode) return;
  isDragging.value = true;
  dragStart.value = {
    x: e.clientX - parseInt(panelStyle.value.left),
    y: e.clientY - parseInt(panelStyle.value.top)
  };
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('mouseup', stopDrag);
};

const onDrag = (e) => {
  if (!isDragging.value) return;
  panelStyle.value = {
    top: (e.clientY - dragStart.value.y) + 'px',
    left: (e.clientX - dragStart.value.x) + 'px'
  };
};

const stopDrag = () => {
  isDragging.value = false;
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
  
  savePositionDebounced();
};

const savePositionDebounced = async () => {
  if (!props.panelConfig?.id) return;
  
  if (savePositionTimeout.value) {
    clearTimeout(savePositionTimeout.value);
  }
  
  savePositionTimeout.value = setTimeout(async () => {
    try {
      const position = JSON.stringify(panelStyle.value);
      await updatePanelPosition(props.panelConfig.id, position);
      console.log('位置已保存');
    } catch (e) {
      console.error('保存位置失败:', e);
    }
  }, 500);
};

const connect = () => {
  ws.value = new WebSocket(getWebSocketUrl());
  ws.value.onopen = () => { isConnected.value = true; };
  ws.value.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      if (!deviceCode.value || data.deviceId === deviceCode.value) {
        deviceData.value = data;
      }
    } catch (e) {
      console.error('解析消息失败:', e);
    }
  };
  ws.value.onclose = () => { isConnected.value = false; };
  ws.value.onerror = () => { isConnected.value = false; };
};

const formatTime = (timestamp) => {
  if (!timestamp) return '--';
  return new Date(timestamp).toLocaleTimeString('zh-CN');
};

const formatDataType = (type) => {
  const typeMap = { 'temperature': '温度', 'humidity': '湿度', 'pressure': '压力', 'voltage': '电压', 'current': '电流' };
  return typeMap[type] || type;
};

const handleStartBind = () => {
  emit('startBind', props.panelConfig);
};

const handleCancelBind = async () => {
  if (props.panelConfig?.id) {
    try {
      await unbindModel(props.panelConfig.id);
      emit('unbindModel', props.panelConfig.id);
      emit('updatePanel');
    } catch (e) {
      console.error('解绑失败:', e);
    }
  }
  emit('cancelBind');
};

const handleDelete = () => {
  emit('deletePanel', props.panelConfig);
};

watch(() => props.panelConfig, () => {
  initPosition();
}, { immediate: true, deep: true });

onMounted(connect);
onUnmounted(() => { 
  if (ws.value) ws.value.close();
  if (savePositionTimeout.value) clearTimeout(savePositionTimeout.value);
});
</script>

<template>
  <div v-if="visible" class="data-panel" :style="panelStyle">
    <div class="panel-header" @mousedown="startDrag">
      <span class="panel-title">{{ panelName }}</span>
      <span class="status" :class="{ connected: isConnected }">{{ isConnected ? '已连接' : '未连接' }}</span>
      <div class="header-actions">
        <button class="delete-btn" @click.stop="handleDelete" title="删除展板">×</button>
        <button class="close-btn" @click.stop="$emit('close')">−</button>
      </div>
    </div>
    <div class="panel-body">
      <div v-if="!deviceData && !hasDeviceBinding" class="empty">
        <p>暂无数据</p>
        <p class="hint">请先绑定设备</p>
      </div>
      <div v-else-if="!deviceData" class="empty">等待数据...</div>
      <div v-else class="data-content">
        <div class="row">
          <span class="label">设备ID:</span>
          <span class="value">{{ deviceData.deviceId }}</span>
        </div>
        <div class="row" v-if="deviceData.data">
          <span class="label">数据类型:</span>
          <span class="value">{{ formatDataType(deviceData.data.dataType) }}</span>
        </div>
        <div class="row" v-if="deviceData.data">
          <span class="label">数值:</span>
          <span class="value highlight">{{ deviceData.data.value }} {{ deviceData.data.unit }}</span>
        </div>
        <div class="row">
          <span class="label">时间:</span>
          <span class="value">{{ formatTime(deviceData.timestamp) }}</span>
        </div>
      </div>
      <div class="bind-section">
        <div class="bind-info-row">
          <div class="bind-item">
            <span class="bind-label">设备:</span>
            <span :class="['bind-value', { bound: hasDeviceBinding }]">
              {{ hasDeviceBinding ? props.panelConfig.deviceName : '未绑定' }}
            </span>
          </div>
          <div class="bind-item">
            <span class="bind-label">模型:</span>
            <span :class="['bind-value', { bound: hasModelBinding }]">
              {{ hasModelBinding ? props.panelConfig.modelName : '未绑定' }}
            </span>
          </div>
        </div>
        <div class="bind-actions">
          <button 
            v-if="!hasModelBinding" 
            class="bind-btn" 
            :class="{ active: bindMode }" 
            @click="handleStartBind"
          >
            {{ bindMode ? '点击场景中的模型...' : '绑定模型' }}
          </button>
          <button v-else class="unbind-btn" @click="handleCancelBind">解绑模型</button>
        </div>
      </div>
    </div>

    <svg v-if="lineStart && hasModelBinding" class="connection-line" style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; z-index: 1000;">
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
  width: 260px;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  background: rgba(255,255,255,0.2);
}

.status.connected {
  background: rgba(82, 196, 26, 0.3);
  color: #b7eb8f;
}

.header-actions {
  display: flex;
  gap: 4px;
}

.close-btn, .delete-btn {
  background: rgba(255,255,255,0.2);
  border: none;
  font-size: 16px;
  cursor: pointer;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  line-height: 1;
}

.close-btn:hover, .delete-btn:hover {
  background: rgba(255,255,255,0.3);
}

.delete-btn {
  font-size: 14px;
}

.panel-body {
  padding: 12px;
}

.empty {
  color: #999;
  text-align: center;
  padding: 20px 0;
}

.empty .hint {
  font-size: 12px;
  color: #bbb;
  margin-top: 4px;
}

.data-content .row {
  display: flex;
  margin-bottom: 8px;
  align-items: center;
}

.data-content .label {
  width: 60px;
  color: #666;
  flex-shrink: 0;
}

.data-content .value {
  flex: 1;
  color: #333;
  word-break: break-all;
}

.data-content .value.highlight {
  color: #1890ff;
  font-weight: 600;
  font-size: 15px;
}

.bind-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.bind-info-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.bind-item {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.bind-label {
  color: #999;
  width: 40px;
}

.bind-value {
  color: #999;
}

.bind-value.bound {
  color: #52c41a;
  font-weight: 500;
}

.bind-actions {
  display: flex;
  gap: 8px;
}

.bind-btn {
  flex: 1;
  padding: 6px 12px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.bind-btn:hover {
  background: #40a9ff;
}

.bind-btn.active {
  background: #52c41a;
}

.unbind-btn {
  flex: 1;
  padding: 6px 12px;
  background: #ff4d4f;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.unbind-btn:hover {
  background: #ff7875;
}
</style>
