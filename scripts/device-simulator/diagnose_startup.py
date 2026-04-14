#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
数字孪生系统启动诊断工具
诊断后端服务、前端服务、数据库连接、OPC UA 服务状态
"""

import sys
import io
import socket
import subprocess
import time
from datetime import datetime

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

COLORS = {
    'GREEN': '\033[92m',
    'RED': '\033[91m',
    'YELLOW': '\033[93m',
    'BLUE': '\033[94m',
    'RESET': '\033[0m'
}

def print_header(title):
    print(f"\n{'='*60}")
    print(f" {title}")
    print(f"{'='*60}")

def print_status(name, status, detail=""):
    color = COLORS['GREEN'] if status else COLORS['RED']
    symbol = "[OK]" if status else "[FAIL]"
    print(f"{color}{symbol}{COLORS['RESET']} {name}")
    if detail:
        print(f"    -> {detail}")

def check_port(host, port, timeout=3):
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(timeout)
        result = sock.connect_ex((host, port))
        sock.close()
        return result == 0
    except Exception:
        return False

def check_http_endpoint(url, timeout=5):
    try:
        import urllib.request
        urllib.request.urlopen(url, timeout=timeout)
        return True
    except Exception:
        return False

def diagnose_backend():
    print_header("后端服务诊断")
    
    port_9999 = check_port("127.0.0.1", 9999)
    print_status("后端 HTTP 服务 (端口 9999)", port_9999, 
                 f"http://127.0.0.1:9999" if port_9999 else "服务未启动")
    
    if port_9999:
        api_health = check_http_endpoint("http://127.0.0.1:9999/api/users")
        print_status("后端 API 响应", api_health,
                     "API 正常响应" if api_health else "API 无响应")
    
    return port_9999

def diagnose_opcua():
    print_header("OPC UA 服务诊断")
    
    port_4840 = check_port("127.0.0.1", 4840)
    print_status("OPC UA 服务 (端口 4840)", port_4840,
                 "opc.tcp://localhost:4840" if port_4840 else "服务未启动")
    
    if port_4840:
        try:
            import asyncio
            from asyncua import Client
            
            async def test_connect():
                try:
                    client = Client(url="opc.tcp://localhost:4840")
                    await client.connect()
                    ns = await client.get_namespace_array()
                    await client.disconnect()
                    return True, f"命名空间数量: {len(ns)}"
                except Exception as e:
                    return False, str(e)
            
            result, detail = asyncio.run(test_connect())
            print_status("OPC UA 连接测试", result, detail)
        except ImportError:
            print_status("OPC UA 连接测试", False, "未安装 asyncua 库")
    
    return port_4840

def diagnose_database():
    print_header("数据库连接诊断")
    
    try:
        import pymysql
        
        conn = pymysql.connect(
            host="127.0.0.1",
            port=3306,
            user="root",
            password="123456",
            database="dam",
            connect_timeout=5
        )
        
        cursor = conn.cursor()
        cursor.execute("SELECT COUNT(*) FROM devices")
        count = cursor.fetchone()[0]
        cursor.close()
        conn.close()
        
        print_status("MySQL 数据库连接", True, f"设备表记录数: {count}")
        return True
    except ImportError:
        print_status("MySQL 数据库连接", False, "未安装 pymysql 库")
        return False
    except Exception as e:
        print_status("MySQL 数据库连接", False, str(e))
        return False

def diagnose_frontend():
    print_header("前端服务诊断")
    
    port_5173 = check_port("127.0.0.1", 5173)
    print_status("前端开发服务 (端口 5173)", port_5173,
                 "http://localhost:5173" if port_5173 else "服务未启动")
    
    port_3000 = check_port("127.0.0.1", 3000)
    print_status("前端备用端口 (端口 3000)", port_3000,
                 "http://localhost:3000" if port_3000 else "未使用")
    
    return port_5173 or port_3000

def print_summary(results):
    print_header("诊断摘要")
    
    all_passed = all(results.values())
    
    for name, status in results.items():
        print_status(name, status)
    
    print(f"\n{'='*60}")
    if all_passed:
        print(f"{COLORS['GREEN']}所有服务运行正常!{COLORS['RESET']}")
    else:
        print(f"{COLORS['RED']}部分服务异常，请检查上述诊断结果{COLORS['RESET']}")
        print("\n启动建议:")
        if not results.get("后端服务", True):
            print("  1. 启动后端: cd virtual-springboot-backend && mvn spring-boot:run")
        if not results.get("前端服务", True):
            print("  2. 启动前端: cd virtual-vue-frontend && npm run dev")
        if not results.get("数据库", True):
            print("  3. 检查 MySQL 服务是否启动，确认用户名密码正确")
        if not results.get("OPC UA", True):
            print("  4. 检查 application.yml 中 opcua.server.enabled=true")
    print(f"{'='*60}")

def main():
    print(f"\n{COLORS['BLUE']}数字孪生系统启动诊断工具{COLORS['RESET']}")
    print(f"诊断时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    results = {}
    
    results["后端服务"] = diagnose_backend()
    results["OPC UA"] = diagnose_opcua()
    results["数据库"] = diagnose_database()
    results["前端服务"] = diagnose_frontend()
    
    print_summary(results)

if __name__ == "__main__":
    main()
