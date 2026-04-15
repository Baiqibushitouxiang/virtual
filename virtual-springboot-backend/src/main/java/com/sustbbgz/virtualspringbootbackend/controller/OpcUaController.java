package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.common.Result;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.opcua.OpcUaServerService;
import com.sustbbgz.virtualspringbootbackend.opcua.namespace.DeviceNodeStore;
import com.sustbbgz.virtualspringbootbackend.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/opcua")
@CrossOrigin
public class OpcUaController {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaController.class);
    private static final int ONLINE_THRESHOLD_SECONDS = 30;

    private final OpcUaServerService opcUaServerService;
    private final DeviceService deviceService;

    public OpcUaController(OpcUaServerService opcUaServerService, DeviceService deviceService) {
        this.opcUaServerService = opcUaServerService;
        this.deviceService = deviceService;
    }

    @GetMapping("/status")
    public Result getServerStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put("running", opcUaServerService.isRunning());

        if (opcUaServerService.isRunning() && opcUaServerService.getDeviceNamespace() != null) {
            DeviceNodeStore nodeStore = opcUaServerService.getDeviceNamespace().getDeviceNodeStore();
            data.put("deviceCount", nodeStore.getDeviceCount());

            List<Map<String, Object>> devices = new ArrayList<>();
            for (DeviceNodeStore.DeviceNode deviceNode : nodeStore.getAllDeviceNodes()) {
                Map<String, Object> device = new HashMap<>();
                device.put("name", deviceNode.getName());
                device.put("objectId", deviceNode.getObjectId().toString());
                device.put("data", deviceNode.getData());
                device.put("status", deviceNode.getStatus());
                Device dbDevice = deviceService.getByDeviceId(deviceNode.getName());
                if (dbDevice != null) {
                    boolean enabled = dbDevice.getStatus() != null && dbDevice.getStatus() == 1;
                    boolean recentlySeen = isRecentlySeen(dbDevice.getLastSeenAt());
                    device.put("enabled", enabled);
                    device.put("lastSeenAt", dbDevice.getLastSeenAt());
                    device.put("online", enabled && recentlySeen && "Online".equals(deviceNode.getStatus()));
                } else {
                    device.put("enabled", false);
                    device.put("online", false);
                }
                devices.add(device);
            }
            data.put("devices", devices);
        }

        return Result.success(data);
    }

    private boolean isRecentlySeen(LocalDateTime lastSeenAt) {
        return lastSeenAt != null
            && ChronoUnit.SECONDS.between(lastSeenAt, LocalDateTime.now()) <= ONLINE_THRESHOLD_SECONDS;
    }

    @PostMapping("/device")
    public Result createDeviceNode(@RequestBody Map<String, String> request) {
        try {
            String deviceName = request.get("deviceName");
            String description = request.getOrDefault("description", "");

            if (deviceName == null || deviceName.isEmpty()) {
                return Result.error("Device name is required");
            }

            if (!opcUaServerService.isRunning()) {
                return Result.error("OPC UA Server is not running");
            }

            DeviceNodeStore.DeviceNode node = opcUaServerService.getDeviceNamespace().createDeviceNode(deviceName, description);

            if (node == null) {
                return Result.error("Failed to create device node");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("name", node.getName());
            data.put("objectId", node.getObjectId().toString());

            return Result.success(data);
        } catch (Exception e) {
            logger.error("Failed to create device node", e);
            return Result.error("Failed to create device node: " + e.getMessage());
        }
    }

    @PostMapping("/sync-devices")
    public Result syncDevicesToOpcUa() {
        try {
            if (!opcUaServerService.isRunning()) {
                return Result.error("OPC UA Server is not running");
            }

            List<Device> devices = deviceService.list();
            int created = 0;
            int skipped = 0;
            List<String> createdDevices = new ArrayList<>();

            for (Device device : devices) {
                DeviceNodeStore.DeviceNode existingNode = opcUaServerService
                    .getDeviceNamespace()
                    .getDeviceNodeStore()
                    .getDeviceNode(device.getDeviceId());

                if (existingNode == null) {
                    opcUaServerService.getDeviceNamespace()
                        .createDeviceNode(device.getDeviceId(), device.getName());
                    created++;
                    createdDevices.add(device.getDeviceId());
                } else {
                    skipped++;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", devices.size());
            result.put("created", created);
            result.put("skipped", skipped);
            result.put("createdDevices", createdDevices);

            return Result.success(result);
        } catch (Exception e) {
            logger.error("Failed to sync devices", e);
            return Result.error("Failed to sync devices: " + e.getMessage());
        }
    }

    @DeleteMapping("/device/{deviceName}")
    public Result deleteDeviceNode(@PathVariable String deviceName) {
        try {
            if (!opcUaServerService.isRunning()) {
                return Result.error("OPC UA Server is not running");
            }

            opcUaServerService.getDeviceNamespace().removeDeviceNode(deviceName);
            return Result.success(null);
        } catch (Exception e) {
            logger.error("Failed to delete device node", e);
            return Result.error("Failed to delete device node: " + e.getMessage());
        }
    }

    @PutMapping("/device/{deviceName}/data")
    public Result setDeviceData(
        @PathVariable String deviceName,
        @RequestBody Map<String, Object> request
    ) {
        try {
            if (!opcUaServerService.isRunning()) {
                return Result.error("OPC UA Server is not running");
            }

            String data = request.get("data") != null ? request.get("data").toString() : "{}";
            opcUaServerService.getDeviceNamespace().setDeviceData(deviceName, data);

            return Result.success(null);
        } catch (Exception e) {
            logger.error("Failed to set device data", e);
            return Result.error("Failed to set device data: " + e.getMessage());
        }
    }

    @PutMapping("/device/{deviceName}/status")
    public Result setDeviceStatus(
        @PathVariable String deviceName,
        @RequestBody Map<String, String> request
    ) {
        try {
            if (!opcUaServerService.isRunning()) {
                return Result.error("OPC UA Server is not running");
            }

            String status = request.getOrDefault("status", "Online");
            opcUaServerService.getDeviceNamespace().setDeviceStatus(deviceName, status);

            return Result.success(null);
        } catch (Exception e) {
            logger.error("Failed to set device status", e);
            return Result.error("Failed to set device status: " + e.getMessage());
        }
    }
}
