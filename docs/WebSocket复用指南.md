# WebSocket 设备数据实时同步功能复用指南

## 功能概述

本功能实现了设备数据的实时同步，支持：
- 后端 WebSocket 服务推送
- 前端实时接收并展示
- Python 脚本模拟设备发送数据

---

## 一、后端 Spring Boot 项目配置

### 1.1 添加 Maven 依赖

在 `pom.xml` 中添加 WebSocket 依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 1.2 创建 WebSocket 配置类

创建 `src/main/java/.../config/WebSocketConfig.java`：

```java
package com.yourpackage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private DeviceDataWebSocketHandler deviceDataWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(deviceDataWebSocketHandler, "/ws/device-data")
                .setAllowedOriginPatterns("*");
    }
}
```

**注意**：`/ws/device-data` 是 WebSocket 端点路径，可根据项目需求修改。

### 1.3 创建 WebSocket 消息处理器

创建 `src/main/java/.../config/DeviceDataWebSocketHandler.java`：

```java
package com.yourpackage.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourpackage.dto.DeviceDataDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DeviceDataWebSocketHandler extends TextWebSocketHandler {

    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket连接建立: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket连接关闭: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("收到消息: " + message.getPayload());
    }

    public void broadcastDeviceData(DeviceDataDTO data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            TextMessage textMessage = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        } catch (IOException e) {
            System.err.println("广播消息失败: " + e.getMessage());
        }
    }
}
```

### 1.4 创建数据对象 DTO

创建 `src/main/java/.../dto/DeviceDataDTO.java`：

```java
package com.yourpackage.dto;

import lombok.Data;

@Data
public class DeviceDataDTO {
    private String deviceId;   // 设备ID
    private String data;      // 数据内容
    private Long timestamp;    // 时间戳
}
```

### 1.5 创建 REST 控制器

创建 `src/main/java/.../controller/DeviceDataController.java`：

```java
package com.yourpackage.controller;

import com.yourpackage.common.Result;
import com.yourpackage.config.DeviceDataWebSocketHandler;
import com.yourpackage.dto.DeviceDataDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/device")
@CrossOrigin(origins = "*")
public class DeviceDataController {

    @Resource
    private DeviceDataWebSocketHandler webSocketHandler;

    @PostMapping("/send")
    public Result sendDeviceData(@RequestBody DeviceDataDTO deviceDataDTO) {
        if (deviceDataDTO.getTimestamp() == null) {
            deviceDataDTO.setTimestamp(System.currentTimeMillis());
        }
        webSocketHandler.broadcastDeviceData(deviceDataDTO);
        return Result.success("数据发送成功");
    }
}
```

### 1.6 统一响应结果类（可选）

如果项目没有统一的 `Result` 类，创建 `src/main/java/.../common/Result.java`：

```java
package com.yourpackage.common;

public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(String msg) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    // getters and setters
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
```

---

## 二、前端 Vue 3 项目配置

### 2.1 安装依赖

```bash
npm install sockjs-client@1.6.1 stompjs@2.3.3
```

### 2.2 创建设备数据监控页面

创建 `src/views/Backend/DeviceDataMonitor.vue`：

```vue
<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const isConnected = ref(false);
const deviceDataList = ref([]);
const ws = ref(null);

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
  // 修改为你的后端地址
  const wsUrl = 'ws://localhost:9999/ws/device-data';
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
        <li>WebSocket连接地址: <code>ws://localhost:9999/ws/device-data</code></li>
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

.status-text {
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

.info-section li {
  margin-bottom: 8px;
  color: #666;
  line-height: 1.6;
}

.info-section code {
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
  color: #d4380d;
}
</style>
```

### 2.3 配置路由

在 `src/router/index.js` 中添加路由：

```javascript
{
  path: '/backend/device-data',
  name: 'DeviceDataMonitor',
  component: () => import('@/views/Backend/DeviceDataMonitor.vue'),
  meta: { title: '设备数据监控' }
}
```

### 2.4 添加菜单项（如果使用 Element Plus）

在后台管理菜单中添加"设备数据监控"菜单项。

---

## 三、Python 数据发送脚本

### 3.1 安装依赖

```bash
pip install requests
```

或创建 `requirements.txt`：

```
requests>=2.25.0
```

### 3.2 创建发送脚本

创建 `device_data_sender.py`：

```python
#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import json
import time

# 修改为你的后端地址
BASE_URL = "http://localhost:9999"
SEND_URL = f"{BASE_URL}/api/device/send"

def send_device_data(device_id, data):
    payload = {
        "deviceId": device_id,
        "data": data,
        "timestamp": int(time.time() * 1000)
    }
    
    headers = {
        "Content-Type": "application/json"
    }
    
    try:
        response = requests.post(SEND_URL, data=json.dumps(payload), headers=headers)
        response.raise_for_status()
        result = response.json()
        print(f"✓ 发送成功: {result.get('msg', '操作成功')}")
    except requests.exceptions.RequestException as e:
        print(f"✗ 发送失败: {e}")

def main():
    print("=" * 50)
    print("设备数据发送工具")
    print("=" * 50)
    print(f"后端地址: {BASE_URL}")
    print("输入 'quit' 或 'exit' 退出程序")
    print("=" * 50)
    
    while True:
        try:
            device_id = input("\n请输入设备ID (默认: device-001): ").strip()
            if not device_id:
                device_id = "device-001"
            
            if device_id.lower() in ['quit', 'exit']:
                print("再见!")
                break
            
            data = input("请输入数据内容: ").strip()
            if not data:
                print("数据内容不能为空，请重试")
                continue
            
            if data.lower() in ['quit', 'exit']:
                print("再见!")
                break
            
            send_device_data(device_id, data)
            
        except KeyboardInterrupt:
            print("\n\n程序已终止")
            break
        except Exception as e:
            print(f"发生错误: {e}")

if __name__ == "__main__":
    main()
```

### 3.3 运行脚本

```bash
python device_data_sender.py
```

---

## 四、完整使用流程

### 4.1 启动后端服务

1. 确保 MySQL 数据库运行
2. 启动 Spring Boot 应用（端口：`9999`）

### 4.2 启动前端服务

```bash
cd virtual-vue-frontend
npm run dev
```

### 4.3 访问前端页面

1. 打开浏览器访问前端地址
2. 登录系统
3. 进入后台管理 → 设备数据监控

### 4.4 运行 Python 脚本发送数据

```bash
python device_data_sender.py
```

### 4.5 测试验证

1. 在 Python 脚本中输入设备ID（如 `device-001`）
2. 输入数据内容（如 `温度：25.5℃`）
3. 按 Enter 发送
4. 前端页面应实时显示接收到的数据

---

## 五、配置参数修改清单

复用时需要修改的配置项：

### 后端

| 配置项 | 默认值 | 修改位置 |
|--------|--------|----------|
| WebSocket 端点 | `/ws/device-data` | WebSocketConfig.java |
| REST 接口路径 | `/api/device/send` | DeviceDataController.java |
| 服务端口 | `9999` | application.yml |

### 前端

| 配置项 | 默认值 | 修改位置 |
|--------|--------|----------|
| WebSocket 连接地址 | `ws://localhost:9999/ws/device-data` | DeviceDataMonitor.vue |
| 后端 API 地址 | `http://localhost:9999` | device_data_sender.py |

### Python 脚本

| 配置项 | 默认值 | 修改位置 |
|--------|--------|----------|
| 后端地址 | `http://localhost:9999` | device_data_sender.py |

---

## 六、技术栈版本

| 组件 | 推荐版本 |
|------|----------|
| Spring Boot | 2.7.x |
| Java | 8+ |
| Vue | 3.x |
| sockjs-client | 1.6.1 |
| stompjs | 2.3.3 |
| Python | 3.6+ |
| requests | 2.25.0+ |

---

## 七、注意事项

1. **跨域问题**：后端已配置 `setAllowedOriginPatterns("*")`，如需限制请修改为具体域名
2. **数据保留**：前端默认最多保留 50 条数据，超出后自动删除最早的记录
3. **连接管理**：页面销毁时自动断开 WebSocket 连接，避免内存泄漏
4. **安全建议**：生产环境请添加认证机制，限制 WebSocket 连接权限
5. **网络问题**：确保前端能够访问后端 WebSocket 端口，注意防火墙设置

---

## 八、故障排查

### 8.1 WebSocket 连接失败

1. 检查后端服务是否启动
2. 检查端口是否正确（默认 9999）
3. 检查 WebSocket 端点路径是否匹配
4. 检查浏览器控制台错误信息

### 8.2 数据发送失败

1. 检查 REST 接口是否正常（可用 Postman 测试）
2. 检查 JSON 数据格式是否正确
3. 检查后端日志输出

### 8.3 前端页面空白

1. 检查路由配置是否正确
2. 检查组件引入路径
3. 检查浏览器控制台错误信息
