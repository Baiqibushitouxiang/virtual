# 数字孪生系统 OPC UA 通信框架使用说明

## 概述

本框架实现了基于 OPC UA 协议的设备通信系统，解决了原有 WebSocket 方案的问题：
- ✅ 设备身份识别与认证
- ✅ 设备持久化绑定
- ✅ 通信安全控制（证书认证、加密传输）
- ✅ 结构化数据模型

## 架构

```
┌─────────────────────────────────────────────────────────────┐
│                      数字孪生系统                             │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ 前端 Vue    │  │ 后端 API    │  │ OPC UA Server       │  │
│  │             │◄─┤             │◄─┤ (端口: 4840)        │  │
│  │ WebSocket   │  │ REST API    │  │ - 设备节点管理       │  │
│  │ 订阅/推送   │  │ (端口: 9999)│  │ - 安全策略          │  │
│  └─────────────┘  └─────────────┘  │ - 证书认证          │  │
│         ▲                          └─────────────────────┘  │
│         │                                    ▲              │
│         │ WebSocket                          │ OPC UA       │
│         │                                    │              │
└─────────┼────────────────────────────────────┼──────────────┘
          │                                    │
          │                                    │
┌─────────┴─────────┐              ┌──────────┴──────────────┐
│   浏览器客户端     │              │      设备端             │
│   (数据展示)       │              │  (传感器/PLC/网关)      │
└───────────────────┘              └─────────────────────────┘
```

## 快速开始

### 1. 数据库初始化

```sql
-- 执行 SQL 脚本创建设备表
SOURCE src/main/resources/sql/devices.sql;
```

### 2. 启动后端服务

```bash
cd virtual-springboot-backend
mvn spring-boot:run
```

服务启动后：
- REST API: http://localhost:9999
- OPC UA Server: opc.tcp://localhost:4840
- WebSocket: ws://localhost:9999/ws/device-data

### 3. 设备端使用

#### 方式一：HTTP API（简单场景）

```python
import requests
import json

# 发送设备数据
response = requests.post(
    "http://localhost:9999/api/device/send",
    json={
        "deviceId": "DEV-001",
        "data": "温度: 25.5℃",
        "timestamp": 1700000000000
    }
)
print(response.json())
```

#### 方式二：WebSocket（实时通信）

```javascript
// 前端 WebSocket 连接
const ws = new WebSocket('ws://localhost:9999/ws/device-data?deviceId=DEV-001&token=your-token');

ws.onopen = () => {
    // 发送认证消息
    ws.send(JSON.stringify({
        type: 'auth',
        deviceId: 'DEV-001',
        token: 'your-token'
    }));
};

ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log('收到数据:', data);
};

// 订阅设备
ws.send(JSON.stringify({
    type: 'subscribe',
    deviceId: 'DEV-001'
}));
```

#### 方式三：OPC UA（工业标准）

```python
from asyncua import Client

async def send_data():
    async with Client(url='opc.tcp://localhost:4840') as client:
        # 查找设备节点
        root = client.nodes.root
        objects = client.nodes.objects
        
        # 写入数据
        # ... OPC UA 节点操作
```

## 设备管理 API

### 设备注册

```http
POST /api/devices
Content-Type: application/json

{
    "name": "温度传感器",
    "description": "高精度温度监测设备"
}
```

响应：
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "id": 1,
        "deviceId": "DEV-A1B2C3D4",
        "name": "温度传感器",
        "status": 1
    }
}
```

### 获取设备列表

```http
GET /api/devices
```

### 获取设备详情

```http
GET /api/devices/{id}
GET /api/devices/deviceId/{deviceId}
```

### 绑定设备到用户

```http
POST /api/devices/{id}/bind
Content-Type: application/json

{
    "userId": 1
}
```

### 生成设备证书

```http
POST /api/devices/{id}/certificate
```

响应：
```json
{
    "code": 200,
    "data": {
        "certificate": "-----BEGIN CERTIFICATE-----\n...",
        "thumbprint": "A1B2C3D4E5F6..."
    }
}
```

### 更新设备状态

```http
PUT /api/devices/{id}/status?status=0
```

状态值：`0` = 禁用，`1` = 启用

## WebSocket 消息协议

### 消息格式

所有消息均为 JSON 格式。

### 认证消息

```json
{
    "type": "auth",
    "deviceId": "DEV-001",
    "token": "your-device-token"
}
```

响应：
```json
{
    "type": "auth",
    "success": true,
    "sessionId": "SESSION-xxx"
}
```

### 订阅消息

```json
{
    "type": "subscribe",
    "deviceId": "DEV-001"
}
```

### 数据消息

```json
{
    "type": "data",
    "nodeId": "device.DEV-001.temperature",
    "value": 25.5
}
```

### 接收数据

```json
{
    "type": "deviceData",
    "deviceId": "DEV-001",
    "data": "温度: 25.5℃",
    "timestamp": 1700000000000
}
```

## 前端集成

### 使用 Vue Hook

```javascript
import useDeviceWebSocket from '@/hooks/useDeviceWebSocket';

export default {
    setup() {
        const {
            deviceDataList,
            isConnected,
            isAuthenticated,
            connect,
            subscribe,
            sendData
        } = useDeviceWebSocket({
            deviceId: 'DEV-001',
            token: 'your-token',
            autoConnect: true
        });

        // 订阅设备
        subscribe('DEV-001');

        // 发送数据
        sendData('temperature', 25.5);

        return {
            deviceDataList,
            isConnected
        };
    }
};
```

### 设备管理页面

访问 `/backend/devices` 进行设备管理：
- 注册新设备
- 生成/下载证书
- 绑定用户
- 启用/禁用设备

## 安全配置

### OPC UA 安全策略

在 `application.yml` 中配置：

```yaml
opcua:
  server:
    security-policies:
      - None          # 无安全（开发环境）
      - Basic256Sha256  # 推荐（生产环境）
    security-modes:
      - None
      - Sign          # 签名
      - SignAndEncrypt  # 签名+加密
```

### 证书管理

1. **生成证书**
   ```http
   POST /api/devices/{id}/certificate
   ```

2. **下载证书**
   ```http
   GET /api/devices/{id}/certificate
   ```

3. **吊销证书**
   ```http
   POST /api/devices/{id}/certificate/revoke
   ```

## 设备端示例

### Python HTTP 客户端

```bash
# 安装依赖
pip install requests websocket-client

# 运行示例
python device_client.py
```

### Python OPC UA 客户端

```bash
# 安装依赖
pip install opcua-asyncio

# 运行示例
python device_opcua_client.py
```

## 常见问题

### Q: 设备连接失败？

检查：
1. 后端服务是否启动
2. 设备是否已注册
3. 设备状态是否为启用
4. 证书是否有效

### Q: WebSocket 认证失败？

确保：
1. 设备已注册
2. token 参数正确
3. 设备状态为启用

### Q: 数据没有推送？

检查：
1. 是否已订阅设备
2. WebSocket 连接是否正常
3. 设备是否在发送数据

## 文件结构

```
virtual-springboot-backend/
├── src/main/java/.../
│   ├── config/
│   │   ├── OpcUaConfig.java          # OPC UA 配置
│   │   └── WebSocketConfig.java      # WebSocket 配置
│   ├── controller/
│   │   └── DeviceController.java     # 设备管理 API
│   ├── entity/
│   │   └── Device.java               # 设备实体
│   ├── service/
│   │   └── DeviceService.java        # 设备服务
│   ├── opcua/
│   │   ├── OpcUaServerService.java   # OPC UA 服务端
│   │   ├── namespace/                # 地址空间
│   │   ├── security/                 # 安全认证
│   │   ├── session/                  # 会话管理
│   │   └── subscription/             # 订阅管理
│   └── websocket/
│       ├── DeviceDataWebSocketHandler.java
│       └── DataPushService.java
└── src/main/resources/
    ├── application.yml
    └── sql/devices.sql

virtual-vue-frontend/
├── src/
│   ├── api/device.js                 # 设备 API
│   ├── hooks/useDeviceWebSocket.js   # WebSocket Hook
│   └── views/Backend/
│       └── DeviceManagement.vue      # 设备管理页面
```

## 版本信息

- Spring Boot: 2.7.6
- Eclipse Milo (OPC UA): 0.6.8
- Vue: 3.5.x
- Element Plus: 2.9.x
