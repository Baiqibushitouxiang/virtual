package com.sustbbgz.virtualspringbootbackend.opcua.namespace;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceNodeStore {

    private static final Logger logger = LoggerFactory.getLogger(DeviceNodeStore.class);

    private final Map<String, DeviceNode> deviceNodes = new ConcurrentHashMap<>();

    public DeviceNode createDeviceNode(String deviceName, NodeId objectId, NodeId dataNodeId, NodeId statusNodeId) {
        if (deviceNodes.containsKey(deviceName)) {
            logger.warn("Device node already exists: {}", deviceName);
            return deviceNodes.get(deviceName);
        }

        try {
            DeviceNode deviceNode = new DeviceNode(deviceName, objectId);
            deviceNode.setDataNode(dataNodeId);
            deviceNode.setStatusNode(statusNodeId);
            deviceNodes.put(deviceName, deviceNode);
            logger.info("Created device node in store: {}", deviceName);
            return deviceNode;
        } catch (Exception e) {
            logger.error("Failed to create device node: {}", deviceName, e);
            return null;
        }
    }

    public DeviceNode getDeviceNode(String deviceName) {
        return deviceNodes.get(deviceName);
    }

    public void removeDeviceNode(String deviceName) {
        DeviceNode removed = deviceNodes.remove(deviceName);
        if (removed != null) {
            logger.info("Removed device node: {}", deviceName);
        }
    }

    public void setDeviceData(String deviceName, String data) {
        DeviceNode node = deviceNodes.get(deviceName);
        if (node != null) {
            node.setData(data);
            logger.debug("Updated data for device: {}", deviceName);
        }
    }

    public void setDeviceStatus(String deviceName, String status) {
        DeviceNode node = deviceNodes.get(deviceName);
        if (node != null) {
            node.setStatus(status);
            logger.debug("Updated status for device: {} -> {}", deviceName, status);
        }
    }

    public Collection<DeviceNode> getAllDeviceNodes() {
        return deviceNodes.values();
    }

    public int getDeviceCount() {
        return deviceNodes.size();
    }

    public static class DeviceNode {
        private final String name;
        private final NodeId objectId;
        private NodeId dataNodeId;
        private NodeId statusNodeId;
        private NodeId temperatureNodeId;
        private volatile String data = "{}";
        private volatile String status = "Offline";

        public DeviceNode(String name, NodeId objectId) {
            this.name = name;
            this.objectId = objectId;
        }

        public String getName() {
            return name;
        }

        public NodeId getObjectId() {
            return objectId;
        }

        public NodeId getDataNodeId() {
            return dataNodeId;
        }

        public void setDataNode(NodeId dataNodeId) {
            this.dataNodeId = dataNodeId;
        }

        public NodeId getStatusNodeId() {
            return statusNodeId;
        }

        public void setStatusNode(NodeId statusNodeId) {
            this.statusNodeId = statusNodeId;
        }

        public NodeId getTemperatureNodeId() {
            return temperatureNodeId;
        }

        public void setTemperatureNode(NodeId temperatureNodeId) {
            this.temperatureNodeId = temperatureNodeId;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
