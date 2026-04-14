package com.sustbbgz.virtualspringbootbackend.config;

import com.sustbbgz.virtualspringbootbackend.websocket.DeviceDataWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private DeviceDataWebSocketHandler deviceDataWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(deviceDataWebSocketHandler, "/ws/device-data")
                .setAllowedOriginPatterns("*");
    }
}
