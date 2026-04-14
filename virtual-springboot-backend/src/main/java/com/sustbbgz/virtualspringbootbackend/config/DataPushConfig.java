package com.sustbbgz.virtualspringbootbackend.config;

import com.sustbbgz.virtualspringbootbackend.websocket.DataPushService;
import com.sustbbgz.virtualspringbootbackend.websocket.DeviceDataWebSocketHandler;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Configuration
public class DataPushConfig {

    @Resource
    private DataPushService dataPushService;

    @Resource
    private DeviceDataWebSocketHandler deviceDataWebSocketHandler;

    @PostConstruct
    public void init() {
        dataPushService.setWebSocketHandler(deviceDataWebSocketHandler);
        deviceDataWebSocketHandler.setDataPushService(dataPushService);
    }
}
