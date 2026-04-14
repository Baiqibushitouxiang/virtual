package com.sustbbgz.virtualspringbootbackend.opcua.namespace;

import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiConsumer;

public class DeviceNamespace extends ManagedNamespaceWithLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(DeviceNamespace.class);

    private final DeviceNodeStore deviceNodeStore;
    private UaFolderNode devicesFolder;
    private BiConsumer<String, Double> temperatureDataCallback;
    private BiConsumer<String, Object> deviceDataCallback;

    public DeviceNamespace(OpcUaServer server) {
        super(server, "urn:digital-twin:devices");
        this.deviceNodeStore = new DeviceNodeStore();

        getLifecycleManager().addStartupTask(this::createAddressSpace);
        getLifecycleManager().addShutdownTask(this::cleanup);
    }

    public void setTemperatureDataCallback(BiConsumer<String, Double> callback) {
        this.temperatureDataCallback = callback;
    }

    public void setDeviceDataCallback(BiConsumer<String, Object> callback) {
        this.deviceDataCallback = callback;
    }

    private void createAddressSpace() {
        try {
            UShort namespaceIndex = getNamespaceIndex();

            NodeId folderNodeId = new NodeId(namespaceIndex, "Devices");
            devicesFolder = new UaFolderNode(
                getNodeContext(),
                folderNodeId,
                new QualifiedName(namespaceIndex, "Devices"),
                new LocalizedText("Devices")
            );

            getNodeManager().addNode(devicesFolder);

            devicesFolder.addReference(new Reference(
                devicesFolder.getNodeId(),
                Identifiers.Organizes,
                Identifiers.ObjectsFolder.expanded(),
                Reference.Direction.INVERSE
            ));

            logger.info("Created Devices folder in address space with namespace index: {}", namespaceIndex);
        } catch (Exception e) {
            logger.error("Failed to create address space", e);
        }
    }

    private void cleanup() {
        logger.info("Device namespace shutting down");
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        for (MonitoredItem item : monitoredItems) {
            logger.debug("Monitoring mode changed for item: {}", item.getId());
        }
    }

    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        for (DataItem item : dataItems) {
            logger.debug("Data item created: {}", item.getReadValueId());
        }
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        for (DataItem item : dataItems) {
            logger.debug("Data item deleted: {}", item.getReadValueId());
        }
    }

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        for (DataItem item : dataItems) {
            logger.debug("Data item modified: {}", item.getReadValueId());
        }
    }

    private UaVariableNode createWritableVariable(
        NodeId nodeId, QualifiedName browseName, LocalizedText displayName
    ) {
        UaVariableNode node = new UaVariableNode(
            getNodeContext(),
            nodeId,
            browseName,
            displayName
        );
        UByte accessLevel = UByte.valueOf(3);
        node.setAccessLevel(accessLevel);
        node.setUserAccessLevel(accessLevel);
        return node;
    }

    public DeviceNodeStore.DeviceNode createDeviceNode(String deviceName, String description) {
        try {
            if (deviceNodeStore.getDeviceNode(deviceName) != null) {
                logger.warn("Device node already exists: {}", deviceName);
                return deviceNodeStore.getDeviceNode(deviceName);
            }

            UShort namespaceIndex = getNamespaceIndex();

            UaFolderNode deviceFolder = new UaFolderNode(
                getNodeContext(),
                new NodeId(namespaceIndex, "Device_" + deviceName),
                new QualifiedName(namespaceIndex, deviceName),
                new LocalizedText(deviceName)
            );
            getNodeManager().addNode(deviceFolder);

            if (devicesFolder != null) {
                devicesFolder.addOrganizes(deviceFolder);
            }

            NodeId dataNodeId = new NodeId(namespaceIndex, "Device_" + deviceName + "_Data");
            DeviceDataVariableNode dataNode = new DeviceDataVariableNode(
                getNodeContext(),
                dataNodeId,
                new QualifiedName(namespaceIndex, "Data"),
                new LocalizedText("Data"),
                deviceName,
                deviceDataCallback
            );
            dataNode.setDataType(Identifiers.String);
            dataNode.setValue(new DataValue(new Variant("{}")));
            getNodeManager().addNode(dataNode);
            deviceFolder.addOrganizes(dataNode);

            NodeId statusNodeId = new NodeId(namespaceIndex, "Device_" + deviceName + "_Status");
            DeviceDataVariableNode statusNode = new DeviceDataVariableNode(
                getNodeContext(),
                statusNodeId,
                new QualifiedName(namespaceIndex, "Status"),
                new LocalizedText("Status"),
                deviceName,
                deviceDataCallback
            );
            statusNode.setDataType(Identifiers.String);
            statusNode.setValue(new DataValue(new Variant("Offline")));
            getNodeManager().addNode(statusNode);
            deviceFolder.addOrganizes(statusNode);

            NodeId temperatureNodeId = new NodeId(namespaceIndex, "Device_" + deviceName + "_Temperature");
            TemperatureVariableNode temperatureNode = new TemperatureVariableNode(
                getNodeContext(),
                temperatureNodeId,
                new QualifiedName(namespaceIndex, "Temperature"),
                new LocalizedText("Temperature"),
                deviceName,
                temperatureDataCallback
            );
            temperatureNode.setDataType(Identifiers.Double);
            temperatureNode.setValue(new DataValue(new Variant(0.0)));
            getNodeManager().addNode(temperatureNode);
            deviceFolder.addOrganizes(temperatureNode);

            DeviceNodeStore.DeviceNode deviceNode = deviceNodeStore.createDeviceNode(
                deviceName, deviceFolder.getNodeId(), dataNodeId, statusNodeId
            );
            deviceNode.setTemperatureNode(temperatureNodeId);

            logger.info("Device node created: {} with folder ID: {} (includes Temperature with persistence)", deviceName, deviceFolder.getNodeId());
            return deviceNode;
        } catch (Exception e) {
            logger.error("Failed to create device node: {}", deviceName, e);
            return null;
        }
    }

    public void removeDeviceNode(String deviceName) {
        deviceNodeStore.removeDeviceNode(deviceName);
        logger.info("Device node removed: {}", deviceName);
    }

    public void setDeviceData(String deviceName, String data) {
        DeviceNodeStore.DeviceNode node = deviceNodeStore.getDeviceNode(deviceName);
        if (node != null) {
            node.setData(data);
            UaVariableNode dataVarNode = (UaVariableNode) getNodeManager().getNode(node.getDataNodeId()).orElse(null);
            if (dataVarNode != null) {
                dataVarNode.setValue(new DataValue(new Variant(data)));
            }
        }
    }

    public void setDeviceStatus(String deviceName, String status) {
        DeviceNodeStore.DeviceNode node = deviceNodeStore.getDeviceNode(deviceName);
        if (node != null) {
            node.setStatus(status);
            UaVariableNode statusVarNode = (UaVariableNode) getNodeManager().getNode(node.getStatusNodeId()).orElse(null);
            if (statusVarNode != null) {
                statusVarNode.setValue(new DataValue(new Variant(status)));
            }
        }
    }

    public void setDeviceTemperature(String deviceName, double temperature) {
        DeviceNodeStore.DeviceNode node = deviceNodeStore.getDeviceNode(deviceName);
        if (node != null && node.getTemperatureNodeId() != null) {
            UaVariableNode tempVarNode = (UaVariableNode) getNodeManager().getNode(node.getTemperatureNodeId()).orElse(null);
            if (tempVarNode != null) {
                tempVarNode.setValue(new DataValue(new Variant(temperature)));
            }
        }
    }

    public DeviceNodeStore getDeviceNodeStore() {
        return deviceNodeStore;
    }
}
