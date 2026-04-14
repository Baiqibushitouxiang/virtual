#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
HTTP/WebSocket 设备客户端示例
通过 HTTP API 和 WebSocket 与数字孪生系统通信
支持设备认证和数据推送
"""

import requests
import json
import time
import random
import threading
import websocket

BASE_URL = "http://localhost:9999"
API_URL = f"{BASE_URL}/api"
WS_URL = "ws://localhost:9999/ws/device-data"


class DeviceClient:
    """设备客户端 - 支持 HTTP 和 WebSocket"""
    
    def __init__(self, device_id, device_token=None):
        self.device_id = device_id
        self.device_token = device_token
        self.ws = None
        self.ws_connected = False
        self.authenticated = False
        self.received_data = []
    
    def register(self, name, description):
        """注册设备"""
        url = f"{API_URL}/devices"
        payload = {
            "name": name,
            "description": description
        }
        
        try:
            response = requests.post(url, json=payload)
            response.raise_for_status()
            result = response.json()
            
            if result.get("code") == 200:
                data = result.get("data", {})
                self.device_id = data.get("deviceId")
                print(f"设备注册成功: {self.device_id}")
                return data
            else:
                print(f"注册失败: {result.get('msg')}")
                return None
        except Exception as e:
            print(f"注册异常: {e}")
            return None
    
    def get_certificate(self):
        """获取设备证书"""
        try:
            # 先获取设备列表找到对应的数字ID
            list_url = f"{API_URL}/devices"
            response = requests.get(list_url)
            response.raise_for_status()
            result = response.json()
            
            if result.get("code") == "200":
                devices = result.get("data", [])
                # 找到匹配的设备
                for device in devices:
                    if device.get("deviceId") == self.device_id:
                        device_num_id = device.get("id")
                        # 使用数字ID生成证书
                        cert_url = f"{API_URL}/devices/{device_num_id}/certificate"
                        cert_response = requests.post(cert_url)
                        cert_response.raise_for_status()
                        cert_result = cert_response.json()
                        if cert_result.get("code") == "200":
                            return cert_result.get("data")
                        return None
                
                # 如果没找到设备
                print(f"设备 {self.device_id} 不存在于数据库中")
                return None
            
            return None
        except Exception as e:
            print(f"获取证书失败: {e}")
            return None
    
    def send_data_http(self, data):
        """通过 HTTP 发送设备数据"""
        url = f"{API_URL}/device/send"
        payload = {
            "deviceId": self.device_id,
            "data": data,
            "timestamp": int(time.time() * 1000)
        }
        
        headers = {"Content-Type": "application/json"}
        if self.device_token:
            headers["Authorization"] = f"Bearer {self.device_token}"
        
        try:
            response = requests.post(url, json=payload, headers=headers)
            response.raise_for_status()
            return True
        except Exception as e:
            print(f"发送失败: {e}")
            return False
    
    def connect_websocket(self, on_message=None, on_error=None):
        """连接 WebSocket"""
        url = WS_URL
        if self.device_id:
            url += f"?deviceId={self.device_id}"
        if self.device_token:
            url += f"&token={self.device_token}"
        
        def on_ws_message(ws, message):
            try:
                data = json.loads(message)
                self.received_data.append(data)
                if on_message:
                    on_message(data)
            except:
                pass
        
        def on_ws_error(ws, error):
            print(f"WebSocket 错误: {error}")
            if on_error:
                on_error(error)
        
        def on_ws_open(ws):
            self.ws_connected = True
            print("WebSocket 已连接")
            
            if self.device_id and self.device_token:
                self._authenticate()
        
        def on_ws_close(ws, close_status_code, close_msg):
            self.ws_connected = False
            self.authenticated = False
            print("WebSocket 已断开")
        
        self.ws = websocket.WebSocketApp(
            url,
            on_open=on_ws_open,
            on_message=on_ws_message,
            on_error=on_ws_error,
            on_close=on_ws_close
        )
        
        ws_thread = threading.Thread(target=self.ws.run_forever)
        ws_thread.daemon = True
        ws_thread.start()
        
        time.sleep(1)
    
    def _authenticate(self):
        """发送认证消息"""
        if self.ws and self.ws_connected:
            auth_msg = {
                "type": "auth",
                "deviceId": self.device_id,
                "token": self.device_token
            }
            self.ws.send(json.dumps(auth_msg))
    
    def subscribe_device(self, target_device_id):
        """订阅设备数据"""
        if self.ws and self.ws_connected:
            sub_msg = {
                "type": "subscribe",
                "deviceId": target_device_id
            }
            self.ws.send(json.dumps(sub_msg))
            print(f"已订阅设备: {target_device_id}")
    
    def unsubscribe_device(self, target_device_id):
        """取消订阅设备"""
        if self.ws and self.ws_connected:
            unsub_msg = {
                "type": "unsubscribe",
                "deviceId": target_device_id
            }
            self.ws.send(json.dumps(unsub_msg))
    
    def send_data_ws(self, node_id, value):
        """通过 WebSocket 发送数据"""
        if self.ws and self.ws_connected:
            data_msg = {
                "type": "data",
                "nodeId": node_id,
                "value": value
            }
            self.ws.send(json.dumps(data_msg))
            return True
        return False
    
    def disconnect(self):
        """断开连接"""
        if self.ws:
            self.ws.close()


def run_http_device():
    """运行 HTTP 设备客户端示例"""
    print("=" * 60)
    print("HTTP 设备客户端示例")
    print("=" * 60)
    
    client = DeviceClient("device-001")
    
    print("\n[示例1] 发送温度数据 (HTTP)")
    print("-" * 40)
    
    try:
        count = 0
        while count < 10:
            temperature = round(random.uniform(18, 28), 2)
            data = f"温度: {temperature}℃"
            
            success = client.send_data_http(data)
            timestamp = time.strftime('%H:%M:%S')
            status = "✓" if success else "✗"
            print(f"[{timestamp}] {status} {data}")
            
            count += 1
            time.sleep(1)
            
    except KeyboardInterrupt:
        print("\n已停止")


def run_websocket_device():
    """运行 WebSocket 设备客户端示例"""
    print("\n" + "=" * 60)
    print("WebSocket 设备客户端示例")
    print("=" * 60)
    
    def on_message(data):
        msg_type = data.get("type", "")
        if msg_type == "auth":
            if data.get("success"):
                print("认证成功!")
            else:
                print(f"认证失败: {data.get('message')}")
        elif msg_type == "data":
            print(f"收到数据: {data.get('deviceId')} - {data.get('data')}")
    
    client = DeviceClient("device-001", "test-token-123")
    
    print("\n连接 WebSocket...")
    client.connect_websocket(on_message=on_message)
    
    print("\n[示例2] 发送数据 (WebSocket)")
    print("-" * 40)
    
    try:
        count = 0
        while count < 10:
            if client.ws_connected:
                temperature = round(random.uniform(18, 28), 2)
                
                success = client.send_data_ws("device.device-001.temperature", temperature)
                timestamp = time.strftime('%H:%M:%S')
                status = "✓" if success else "✗"
                print(f"[{timestamp}] {status} 温度: {temperature}℃")
                
                count += 1
            else:
                print("等待连接...")
            
            time.sleep(1)
            
    except KeyboardInterrupt:
        print("\n正在停止...")
    finally:
        client.disconnect()


def run_full_example():
    """完整示例：注册设备并通信"""
    print("\n" + "=" * 60)
    print("完整示例：设备注册 + 证书 + 通信")
    print("=" * 60)
    
    client = DeviceClient(None)
    
    print("\n步骤1: 注册设备")
    device = client.register("智能温度传感器", "高精度温度监测设备")
    if device:
        client.device_id = device.get('deviceId', client.device_id)
        print(f"  设备ID: {client.device_id}")
    else:
        print("注册失败，使用默认设备ID")
        client.device_id = "device-001"
    
    print("\n步骤2: 生成证书")
    cert = client.get_certificate()
    if cert:
        print(f"  证书指纹: {cert.get('thumbprint', '')[:20]}...")
    else:
        print("  证书生成失败，继续使用无证书模式")
    
    print("\n步骤3: 发送数据")
    for i in range(5):
        temperature = round(random.uniform(18, 28), 2)
        data = f"温度: {temperature}℃"
        client.send_data_http(data)
        print(f"  发送: {data}")
        time.sleep(0.5)
    
    print("\n完成!")


if __name__ == "__main__":
    import sys
    
    print("设备客户端示例")
    print("=" * 60)
    print("1. HTTP 设备客户端")
    print("2. WebSocket 设备客户端")
    print("3. 完整示例 (注册 + 证书 + 通信)")
    print("=" * 60)
    
    choice = input("\n请选择示例 (1/2/3): ").strip()
    
    if choice == "1":
        run_http_device()
    elif choice == "2":
        run_websocket_device()
    elif choice == "3":
        run_full_example()
    else:
        print("运行默认示例...")
        run_http_device()
