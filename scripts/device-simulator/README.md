# 设备模拟脚本

本目录包含用于测试数字孪生系统的设备模拟脚本。

## 脚本说明

| 脚本文件 | 说明 |
|---------|------|
| `device_client.py` | HTTP/WebSocket 设备客户端，支持设备注册、认证和数据发送 |
| `device_opcua_client.py` | OPC UA 设备客户端，通过 OPC UA 协议与系统通信 |
| `diagnose_startup.py` | 系统启动诊断工具，检查各服务状态 |

## 环境准备

```bash
pip install -r requirements.txt
```

## 使用方法

### 1. HTTP 设备客户端

```bash
python device_client.py
```

选择模式：
- `1` - HTTP 设备客户端
- `2` - WebSocket 设备客户端
- `3` - 完整示例（注册 + 证书 + 通信）

### 2. OPC UA 设备客户端

```bash
python device_opcua_client.py
```

确保后端 OPC UA 服务已启动（端口 4840）。

### 3. 系统诊断

```bash
python diagnose_startup.py
```

检查以下服务状态：
- 后端 HTTP 服务（端口 9999）
- OPC UA 服务（端口 4840）
- MySQL 数据库（端口 3306）
- 前端开发服务（端口 5173）

## 前置条件

1. 后端服务已启动
2. 数据库已配置
3. 如使用 OPC UA，需在 `application.yml` 中启用 `opcua.server.enabled=true`
