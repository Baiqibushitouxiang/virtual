<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { API_CONFIG } from '@/config/apiConfig';

const isConnected = ref(false);
const deviceDataList = ref([]);
const ws = ref(null);

const getWebSocketUrl = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const host = API_CONFIG.baseURL.replace(/^https?:\/\//, '');
  return `${protocol}${host}/ws/device-data`;
};

const formatTime = (timestamp) => {
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

const connect = () => {
  const wsUrl = getWebSocketUrl();
  ws.value = new WebSocket(wsUrl);
  
  ws.value.onopen = () => {
    console.log('WebSocket连接成功');
    isConnected.value = true;
  };
  
  ws.value.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      deviceDataList.value.unshift({
        deviceId: data.deviceId,
        data: data.data,
        timestamp: data.timestamp,
        formattedTime: formatTime(data.timestamp)
      });
      
      if (deviceDataList.value.length > 50) {
        deviceDataList.value.pop();
      }
    } catch (e) {
      console.error('解析消息失败:', e);
    }
  };
  
  ws.value.onclose = () => {
    console.log('WebSocket连接关闭');
    isConnected.value = false;
  };
  
  ws.value.onerror = (error) => {
    console.error('WebSocket连接错误:', error);
    isConnected.value = false;
  };
};

const disconnect = () => {
  if (ws.value) {
    ws.value.close();
    ws.value = null;
    isConnected.value = false;
  }
};

const clearData = () => {
  deviceDataList.value = [];
};

onMounted(() => {
  connect();
});

onUnmounted(() => {
  disconnect();
});
</script>

<template>
  <div class="websocket-container">
    <div class="header">
      <h1>设备数据实时监控</h1>
      <div class="connection-status">
        <span class="status-label">连接状态:</span>
        <span class="status-dot" :class="{ connected: isConnected, disconnected: !isConnected }"></span>
        <span class="status-text">{{ isConnected ? '已连接' : '未连接' }}</span>
      </div>
    </div>

    <div class="controls">
      <button @click="connect" :disabled="isConnected" class="btn btn-connect">连接</button>
      <button @click="disconnect" :disabled="!isConnected" class="btn btn-disconnect">断开</button>
      <button @click="clearData" class="btn btn-clear">清空数据</button>
    </div>

    <div class="data-section">
      <div class="section-header">
        <h2>实时数据</h2>
        <span class="data-count">共 {{ deviceDataList.length }} 条</span>
      </div>
      
      <div class="data-list">
        <div v-if="deviceDataList.length === 0" class="empty-state">
          <p>暂无数据，等待数据推送...</p>
        </div>
        <div v-else class="data-item" v-for="(item, index) in deviceDataList" :key="index">
          <div class="item-header">
            <span class="device-id">{{ item.deviceId }}</span>
            <span class="time">{{ item.formattedTime }}</span>
          </div>
          <div class="item-content">{{ item.data }}</div>
        </div>
      </div>
    </div>

    <div class="info-section">
      <h3>使用说明</h3>
      <ul>
        <li>WebSocket连接地址: <code>{{ getWebSocketUrl() }}</code></li>
        <li>数据推送接口: <code>POST /api/device/send</code></li>
        <li>使用Python脚本输入数据后按Enter键发送</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.websocket-container {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 180px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

h1 {
  font-size: 32px;
  color: #333;
  font-weight: 700;
  margin: 0;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-label {
  font-size: 14px;
  color: #666;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.connected {
  background-color: #52c41a;
  box-shadow: 0 0 8px rgba(82, 196, 26, 0.5);
}

.status-dot.disconnected {
  background-color: #ff4d4f;
  box-shadow: 0 0 8px rgba(255, 77, 79, 0.5);
}

.status-text{
  font-size: 14px;
  font-weight: 500;
}

.controls {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-connect {
  background-color: #1890ff;
  color: white;
}

.btn-connect:hover:not(:disabled) {
  background-color: #40a9ff;
}

.btn-disconnect {
  background-color: #ff4d4f;
  color: white;
}

.btn-disconnect:hover:not(:disabled) {
  background-color: #ff7875;
}

.btn-clear {
  background-color: #f0f0f0;
  color: #333;
}

.btn-clear:hover {
  background-color: #d9d9d9;
}

.data-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.data-count {
  font-size: 14px;
  color: #666;
}

.data-list {
  max-height: 500px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
  color: #999;
}

.data-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.data-item:last-child {
  border-bottom: none;
}

.data-item:hover {
  background-color: #fafafa;
}

.item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.device-id {
  font-weight: 600;
  color: #1890ff;
}

.time {
  font-size: 12px;
  color: #999;
}

.item-content {
  color: #333;
  word-break: break-all;
}

.info-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.info-section h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
}

.info-section ul {
  margin: 0;
  padding-left: 20px;
}

.info-section li{
  margin-bottom: 8px;
  color: #666;
  line-height: 1.6;
}

.info-section code{
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
  color: #d4380d;
}
</style>
