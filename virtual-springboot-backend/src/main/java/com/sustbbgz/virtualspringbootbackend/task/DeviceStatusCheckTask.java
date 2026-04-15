package com.sustbbgz.virtualspringbootbackend.task;

import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.opcua.OpcUaServerService;
import com.sustbbgz.virtualspringbootbackend.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DeviceStatusCheckTask {

    private static final Logger logger = LoggerFactory.getLogger(DeviceStatusCheckTask.class);
    
    private static final int OFFLINE_THRESHOLD_SECONDS = 30;

    @Autowired
    private DeviceService deviceService;

    @Autowired(required = false)
    @Lazy
    private OpcUaServerService opcUaServerService;

    @Scheduled(fixedRate = 10000)
    public void checkDeviceStatus() {
        List<Device> devices = deviceService.list();
        LocalDateTime now = LocalDateTime.now();
        
        for (Device device : devices) {
            if (device.getStatus() == null || device.getStatus() == 0) {
                continue;
            }
            
            LocalDateTime lastSeen = device.getLastSeenAt();
            if (lastSeen == null) {
                deviceService.updateStatus(device.getId(), 0);
                updateOpcUaStatus(device.getDeviceId(), "Offline");
                logger.info("设备 {} 从未上线，标记为离线", device.getDeviceId());
                continue;
            }
            
            long seconds = ChronoUnit.SECONDS.between(lastSeen, now);
            if (seconds > OFFLINE_THRESHOLD_SECONDS) {
                deviceService.updateStatus(device.getId(), 0);
                updateOpcUaStatus(device.getDeviceId(), "Offline");
                logger.info("设备 {} 超时 {} 秒未活动，标记为离线", device.getDeviceId(), seconds);
            }
        }
    }

    private void updateOpcUaStatus(String deviceId, String status) {
        if (opcUaServerService != null && opcUaServerService.isRunning() && opcUaServerService.getDeviceNamespace() != null) {
            opcUaServerService.getDeviceNamespace().setDeviceStatus(deviceId, status);
        }
    }
}
