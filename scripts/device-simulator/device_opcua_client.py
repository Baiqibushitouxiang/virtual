#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import asyncio
import random
import sys
import io
from datetime import datetime

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', line_buffering=True)
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', line_buffering=True)

try:
    from asyncua import Client, ua
    OPCUA_AVAILABLE = True
except ImportError:
    OPCUA_AVAILABLE = False
    print("警告: 未安装 opcua-asyncio 库")
    print("请运行: pip install opcua-asyncio")


class OPCUADeviceClient:
    """OPC UA 设备客户端"""
    
    def __init__(self, server_url, device_id, device_name):
        if not OPCUA_AVAILABLE:
            raise RuntimeError("OPC UA 库未安装")
        
        self.server_url = server_url
        self.device_id = device_id
        self.device_name = device_name
        self.client = None
        self.connected = False
        self.idx = None
        self.device_node = None
    
    async def connect(self):
        """连接到 OPC UA 服务器"""
        print(f"正在连接 OPC UA 服务器: {self.server_url}")
        
        self.client = Client(url=self.server_url)
        await self.client.connect()
        
        self.connected = True
        print(f"设备 {self.device_id} 已连接到 OPC UA 服务器")
        
        ns_array = await self.client.get_namespace_array()
        print(f"命名空间数组: {ns_array}")
        
        for i, ns in enumerate(ns_array):
            if "devices" in ns.lower():
                self.idx = i
                print(f"找到设备命名空间索引: {i} -> {ns}")
                break
        
        if self.idx is None:
            for i, ns in enumerate(ns_array):
                if "digital-twin" in ns.lower():
                    self.idx = i
                    print(f"使用digital-twin命名空间索引: {i} -> {ns}")
                    break
        
        if self.idx is None:
            self.idx = 2
            print(f"未找到匹配命名空间，使用默认索引: {self.idx}")
        
        print(f"最终使用命名空间索引: {self.idx}")
    
    async def disconnect(self):
        """断开连接"""
        if self.client and self.connected:
            await self.client.disconnect()
            self.connected = False
            print(f"设备 {self.device_id} 已断开")
    
    async def find_device_node(self):
        """查找设备节点 - 使用多种策略"""
        if not self.connected:
            print("未连接到服务器")
            return None
        
        if self.device_node is not None:
            return self.device_node
        
        try:
            node = await self._find_by_direct_nodeid()
            if node is not None:
                self.device_node = node
                return node
            
            node = await self._find_by_browsing()
            if node is not None:
                self.device_node = node
                return node
            
            print(f"未找到设备节点: {self.device_id}")
            return None
            
        except Exception as e:
            print(f"查找设备节点失败: {e}")
            return None
    
    async def _find_by_direct_nodeid(self):
        """通过直接构造NodeId查找设备节点"""
        for ns_idx in [self.idx, 2, 1, 3]:
            try:
                node_id = ua.NodeId(
                    Identifier=f"Device_{self.device_id}",
                    NamespaceIndex=ns_idx,
                    NodeIdType=ua.NodeIdType.String
                )
                node = self.client.get_node(node_id)
                browse_name = await node.read_browse_name()
                print(f"通过NodeId直接找到设备节点: {browse_name.Name} (ns={ns_idx})")
                self.idx = ns_idx
                return node
            except Exception:
                continue
        return None
    
    async def _find_by_browsing(self):
        """通过浏览地址空间查找设备节点"""
        try:
            objects = self.client.nodes.objects
            
            for node in await objects.get_children():
                browse_name = await node.read_browse_name()
                if browse_name.Name == "Devices":
                    for device_node in await node.get_children():
                        name = await device_node.read_browse_name()
                        if self.device_id in name.Name:
                            print(f"通过浏览找到设备节点: {name.Name}")
                            return device_node
            
            for node in await objects.get_children():
                browse_name = await node.read_browse_name()
                if self.device_id in browse_name.Name:
                    print(f"在Objects下直接找到设备节点: {browse_name.Name}")
                    return node
            
            return None
        except Exception:
            return None
    
    async def write_data(self, data_name, value):
        """写入数据到服务器"""
        if not self.connected:
            print("未连接到服务器")
            return False
        
        try:
            device_node = await self.find_device_node()
            if device_node is None:
                return False
            
            for child in await device_node.get_children():
                browse_name = await child.read_browse_name()
                if data_name.lower() in browse_name.Name.lower():
                    data_value = ua.DataValue(ua.Variant(value, ua.VariantType.Double))
                    await child.write_value(data_value)
                    return True
            
            try:
                ns = self.idx
                var_node_id = ua.NodeId(
                    Identifier=f"Device_{self.device_id}_{data_name}",
                    NamespaceIndex=ns,
                    NodeIdType=ua.NodeIdType.String
                )
                var_node = self.client.get_node(var_node_id)
                data_value = ua.DataValue(ua.Variant(value, ua.VariantType.Double))
                await var_node.write_value(data_value)
                return True
            except Exception:
                pass
            
            print(f"未找到数据节点: {data_name}")
            return False
            
        except Exception as e:
            print(f"写入数据失败: {e}")
            return False
    
    async def read_data(self, data_name):
        """读取数据"""
        if not self.connected:
            print("未连接到服务器")
            return None
        
        try:
            device_node = await self.find_device_node()
            if device_node is None:
                return None
            
            for child in await device_node.get_children():
                browse_name = await child.read_browse_name()
                if data_name.lower() in browse_name.Name.lower():
                    value = await child.read_value()
                    return value.Value.Value
            
            return None
            
        except Exception as e:
            print(f"读取数据失败: {e}")
            return None
    
    async def update_status(self, status):
        """更新设备状态"""
        if not self.connected:
            return False
        
        try:
            device_node = await self.find_device_node()
            if device_node is None:
                return False
            
            for child in await device_node.get_children():
                browse_name = await child.read_browse_name()
                if "status" in browse_name.Name.lower():
                    data_value = ua.DataValue(ua.Variant(status, ua.VariantType.String))
                    await child.write_value(data_value)
                    return True
            
            return False
        except Exception as e:
            print(f"更新状态失败: {e}")
            return False
    
    async def subscribe_to_data_changes(self, callback):
        """订阅数据变化"""
        if not self.connected:
            print("未连接到服务器")
            return None
        
        try:
            subscription = await self.client.create_subscription(500, callback)
            
            device_node = await self.find_device_node()
            if device_node:
                for child in await device_node.get_children():
                    await subscription.subscribe_data_change(child)
                
                print(f"已订阅设备 {self.device_id} 的数据变化")
                return subscription
            
            return None
            
        except Exception as e:
            print(f"订阅失败: {e}")
            return None


async def run_opcua_device():
    """运行 OPC UA 设备客户端示例"""
    
    SERVER_URL = "opc.tcp://localhost:4840"
    DEVICE_ID = "DEV-001"
    DEVICE_NAME = "温度传感器"
    
    client = OPCUADeviceClient(SERVER_URL, DEVICE_ID, DEVICE_NAME)
    
    try:
        await client.connect()
        
        await client.update_status("在线")
        
        print("\n开始发送数据 (按 Ctrl+C 停止)...")
        print("=" * 50)
        
        count = 0
        while True:
            temperature = round(random.uniform(18, 28), 2)
            
            success = await client.write_data("Temperature", temperature)
            
            timestamp = datetime.now().strftime('%H:%M:%S')
            status = "✓" if success else "✗"
            print(f"[{timestamp}] {status} 温度: {temperature}℃")
            
            count += 1
            if count % 10 == 0:
                await client.update_status(f"在线 - 已发送 {count} 条数据")
            
            await asyncio.sleep(1)
            
    except KeyboardInterrupt:
        print("\n正在停止...")
    except Exception as e:
        print(f"\n连接失败: {e}")
        print("\n提示: 请确保后端 OPC UA 服务已启动")
        print("  1. 检查 application.yml 中 opcua.server.enabled=true")
        print("  2. 重启后端服务")
        print("  3. 查看后端日志确认 OPC UA 服务启动状态")
    finally:
        await client.update_status("离线")
        await client.disconnect()


if __name__ == "__main__":
    if not OPCUA_AVAILABLE:
        print("\n请先安装依赖:")
        print("  pip install opcua-asyncio")
        exit(1)
    
    print("=" * 60)
    print("OPC UA 设备客户端示例")
    print("=" * 60)
    print("\n注意: 此示例需要后端 OPC UA 服务已启动")
    print("  - 检查 application.yml 中 opcua.server.enabled=true")
    print("  - 后端启动时会自动启动 OPC UA 服务 (端口 4840)")
    print("=" * 60)
    
    asyncio.run(run_opcua_device())
