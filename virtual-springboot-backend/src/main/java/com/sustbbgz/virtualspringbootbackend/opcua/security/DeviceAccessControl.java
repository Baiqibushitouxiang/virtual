package com.sustbbgz.virtualspringbootbackend.opcua.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceAccessControl {

    private static final Logger logger = LoggerFactory.getLogger(DeviceAccessControl.class);

    public enum Permission {
        READ(1),
        WRITE(2),
        ADMIN(4);

        private final int code;

        Permission(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Permission fromCode(int code) {
            for (Permission p : values()) {
                if (p.code == code) {
                    return p;
                }
            }
            return null;
        }
    }

    private final Map<String, Set<Permission>> devicePermissions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> nodeAccessList = new ConcurrentHashMap<>();

    public DeviceAccessControl() {
        initDefaultPermissions();
    }

    private void initDefaultPermissions() {
        grantPermission("default", Permission.READ);
    }

    public void grantPermission(String deviceId, Permission permission) {
        devicePermissions.computeIfAbsent(deviceId, k -> new HashSet<>()).add(permission);
        logger.info("授予权限: deviceId={}, permission={}", deviceId, permission);
    }

    public void revokePermission(String deviceId, Permission permission) {
        Set<Permission> permissions = devicePermissions.get(deviceId);
        if (permissions != null) {
            permissions.remove(permission);
            logger.info("撤销权限: deviceId={}, permission={}", deviceId, permission);
        }
    }

    public boolean hasPermission(String deviceId, Permission permission) {
        Set<Permission> permissions = devicePermissions.get(deviceId);
        if (permissions == null) {
            permissions = devicePermissions.get("default");
        }
        return permissions != null && permissions.contains(permission);
    }

    public void grantNodeAccess(String deviceId, String nodeId) {
        nodeAccessList.computeIfAbsent(deviceId, k -> new HashSet<>()).add(nodeId);
        logger.info("授予节点访问权限: deviceId={}, nodeId={}", deviceId, nodeId);
    }

    public void revokeNodeAccess(String deviceId, String nodeId) {
        Set<String> nodes = nodeAccessList.get(deviceId);
        if (nodes != null) {
            nodes.remove(nodeId);
        }
    }

    public boolean canAccessNode(String deviceId, String nodeId) {
        Set<String> allowedNodes = nodeAccessList.get(deviceId);
        if (allowedNodes == null) {
            return hasPermission(deviceId, Permission.READ);
        }
        return allowedNodes.contains(nodeId) || allowedNodes.contains("*");
    }

    public void setDevicePermissions(String deviceId, Set<Permission> permissions) {
        devicePermissions.put(deviceId, new HashSet<>(permissions));
    }

    public Set<Permission> getDevicePermissions(String deviceId) {
        return devicePermissions.getOrDefault(deviceId, new HashSet<>());
    }

    public void clearDevicePermissions(String deviceId) {
        devicePermissions.remove(deviceId);
        nodeAccessList.remove(deviceId);
    }

    public Map<String, Object> getAccessLog(String deviceId) {
        Map<String, Object> log = new HashMap<>();
        log.put("deviceId", deviceId);
        log.put("permissions", getDevicePermissions(deviceId));
        log.put("accessibleNodes", nodeAccessList.getOrDefault(deviceId, new HashSet<>()));
        return log;
    }
}
