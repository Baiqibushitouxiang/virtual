import { ref, onMounted, onUnmounted, computed } from 'vue';
import { API_CONFIG } from '@/config/apiConfig';

const getDefaultWsUrl = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const host = API_CONFIG.baseURL.replace(/^https?:\/\//, '');
  return `${protocol}${host}/ws/device-data`;
};

export default function useDeviceWebSocket(options = {}) {
  const {
    wsUrl = getDefaultWsUrl(),
    deviceId = null,
    token = null,
    autoConnect = true,
    reconnectInterval = 5000,
    maxReconnectAttempts = 5
  } = options;

  const deviceDataList = ref([]);
  const isConnected = ref(false);
  const isAuthenticated = ref(false);
  const ws = ref(null);
  const reconnectAttempts = ref(0);
  const connectionError = ref(null);

  const connectionUrl = computed(() => {
    let url = wsUrl;
    const params = [];
    if (deviceId) params.push(`deviceId=${deviceId}`);
    if (token) params.push(`token=${token}`);
    if (params.length > 0) {
      url += (url.includes('?') ? '&' : '?') + params.join('&');
    }
    return url;
  });

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
    if (ws.value?.readyState === WebSocket.OPEN) return;

    try {
      ws.value = new WebSocket(connectionUrl.value);
      connectionError.value = null;

      ws.value.onopen = () => {
        console.log('WebSocket连接成功');
        isConnected.value = true;
        reconnectAttempts.value = 0;

        if (deviceId && token) {
          authenticate();
        }
      };

      ws.value.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          handleMessage(data);
        } catch (e) {
          console.error('解析消息失败:', e);
        }
      };

      ws.value.onclose = (event) => {
        console.log('WebSocket连接关闭', event.code, event.reason);
        isConnected.value = false;
        isAuthenticated.value = false;

        if (reconnectAttempts.value < maxReconnectAttempts && autoConnect) {
          setTimeout(() => {
            reconnectAttempts.value++;
            console.log(`尝试重连 (${reconnectAttempts.value}/${maxReconnectAttempts})`);
            connect();
          }, reconnectInterval);
        }
      };

      ws.value.onerror = (error) => {
        console.error('WebSocket连接错误:', error);
        connectionError.value = '连接错误';
        isConnected.value = false;
      };
    } catch (e) {
      console.error('创建WebSocket连接失败:', e);
      connectionError.value = e.message;
    }
  };

  const disconnect = () => {
    if (ws.value) {
      ws.value.close();
      ws.value = null;
      isConnected.value = false;
      isAuthenticated.value = false;
    }
  };

  const authenticate = () => {
    if (!deviceId || !token) return;
    
    sendMessage({
      type: 'auth',
      deviceId,
      token
    });
  };

  const subscribe = (targetDeviceId) => {
    sendMessage({
      type: 'subscribe',
      deviceId: targetDeviceId || deviceId
    });
  };

  const unsubscribe = (targetDeviceId) => {
    sendMessage({
      type: 'unsubscribe',
      deviceId: targetDeviceId || deviceId
    });
  };

  const sendData = (nodeId, value) => {
    sendMessage({
      type: 'data',
      nodeId,
      value
    });
  };

  const sendMessage = (data) => {
    if (ws.value?.readyState === WebSocket.OPEN) {
      ws.value.send(JSON.stringify(data));
    } else {
      console.warn('WebSocket未连接，无法发送消息');
    }
  };

  const handleMessage = (data) => {
    switch (data.type) {
      case 'auth':
        if (data.success) {
          isAuthenticated.value = true;
          console.log('认证成功:', data.sessionId);
        } else {
          isAuthenticated.value = false;
          connectionError.value = data.message || '认证失败';
          console.error('认证失败:', data.message);
        }
        break;

      case 'subscribe':
        console.log('订阅结果:', data.success ? '成功' : '失败', data.deviceId);
        break;

      case 'unsubscribe':
        console.log('取消订阅:', data.deviceId);
        break;

      case 'data':
      case 'deviceData':
        handleDataMessage(data);
        break;

      case 'notification':
        console.log('通知:', data.data);
        break;

      case 'alert':
        console.log('告警:', data.data);
        break;

      case 'error':
        console.error('服务器错误:', data.message);
        break;

      default:
        console.log('未知消息类型:', data.type, data);
    }
  };

  const handleDataMessage = (data) => {
    deviceDataList.value.unshift({
      deviceId: data.deviceId,
      data: data.data,
      timestamp: data.timestamp,
      formattedTime: formatTime(data.timestamp)
    });

    if (deviceDataList.value.length > 50) {
      deviceDataList.value.pop();
    }
  };

  const clearData = () => {
    deviceDataList.value = [];
  };

  const reconnect = () => {
    disconnect();
    reconnectAttempts.value = 0;
    connect();
  };

  onMounted(() => {
    if (autoConnect) {
      connect();
    }
  });

  onUnmounted(() => {
    disconnect();
  });

  return {
    deviceDataList,
    isConnected,
    isAuthenticated,
    connectionError,
    connect,
    disconnect,
    reconnect,
    authenticate,
    subscribe,
    unsubscribe,
    sendData,
    clearData
  };
}
