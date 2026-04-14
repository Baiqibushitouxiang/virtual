package com.sustbbgz.virtualspringbootbackend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.dto.DeviceDataDTO;
import com.sustbbgz.virtualspringbootbackend.opcua.subscription.DataChangeMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataPushService implements DataChangeMonitor.DataChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(DataPushService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private DeviceDataWebSocketHandler webSocketHandler;
    private DataChangeMonitor dataChangeMonitor;

    public void setWebSocketHandler(DeviceDataWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    public void setDataChangeMonitor(DataChangeMonitor dataChangeMonitor) {
        this.dataChangeMonitor = dataChangeMonitor;
        this.dataChangeMonitor.addListener("*", this);
    }

    public void pushData(String deviceId, Object data) {
        pushData(deviceId, "data", data);
    }

    public void pushData(String deviceId, String type, Object data) {
        if (webSocketHandler == null) {
            logger.warn("WebSocketHandler 未设置，无法推送数据");
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("deviceId", deviceId);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());

        webSocketHandler.broadcastMessage(message);
        logger.debug("推送数据: deviceId={}, type={}", deviceId, type);
    }

    public void pushDeviceData(DeviceDataDTO deviceData) {
        if (webSocketHandler == null) {
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("type", "deviceData");
        message.put("deviceId", deviceData.getDeviceId());
        message.put("data", deviceData.getData());
        message.put("timestamp", deviceData.getTimestamp());

        webSocketHandler.broadcastDeviceData(deviceData);
    }

    public void pushNotification(String deviceId, String title, String content) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("content", content);
        notification.put("timestamp", System.currentTimeMillis());

        pushData(deviceId, "notification", notification);
    }

    public void pushAlert(String deviceId, String level, String message) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("level", level);
        alert.put("message", message);
        alert.put("timestamp", System.currentTimeMillis());

        pushData(deviceId, "alert", alert);
    }

    public void pushToDeviceList(List<String> deviceIds, Object data) {
        for (String deviceId : deviceIds) {
            pushData(deviceId, data);
        }
    }

    public void broadcast(Object data) {
        if (webSocketHandler == null) {
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("type", "broadcast");
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());

        webSocketHandler.broadcastDeviceData(
            new DeviceDataDTO() {{
                setDeviceId("broadcast");
                setData(objectMapper.valueToTree(data).toString());
                setTimestamp(System.currentTimeMillis());
            }}
        );
    }

    @Override
    public void onDataChange(DataChangeMonitor.DataChangeEvent event) {
        String nodeId = event.getNodeId();
        
        if (nodeId != null && nodeId.startsWith("device.")) {
            String[] parts = nodeId.split("\\.");
            if (parts.length >= 2) {
                String deviceId = parts[1];
                pushData(deviceId, Map.of(
                    "nodeId", nodeId,
                    "value", event.getNewValue().getValue().getValue(),
                    "timestamp", event.getTimestamp()
                ));
            }
        }
    }
}
