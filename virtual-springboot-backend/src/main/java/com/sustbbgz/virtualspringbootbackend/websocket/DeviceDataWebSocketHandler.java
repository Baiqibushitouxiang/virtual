package com.sustbbgz.virtualspringbootbackend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.dto.DeviceDataDTO;
import com.sustbbgz.virtualspringbootbackend.opcua.security.DeviceAuthenticationService;
import com.sustbbgz.virtualspringbootbackend.opcua.session.DeviceSessionManager;
import com.sustbbgz.virtualspringbootbackend.opcua.subscription.DataChangeMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DeviceDataWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeviceDataWebSocketHandler.class);

    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, WebSocketSessionInfo> sessionInfoMap = new ConcurrentHashMap<>();
    private final Map<String, List<WebSocketSession>> deviceSubscriptions = new ConcurrentHashMap<>();

    private DeviceAuthenticationService authenticationService;
    private DataChangeMonitor dataChangeMonitor;
    private DataPushService dataPushService;

    public void setAuthenticationService(DeviceAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setDataChangeMonitor(DataChangeMonitor dataChangeMonitor) {
        this.dataChangeMonitor = dataChangeMonitor;
    }

    public void setDataPushService(DataPushService dataPushService) {
        this.dataPushService = dataPushService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        
        String deviceId = extractDeviceId(session);
        String token = extractToken(session);
        
        WebSocketSessionInfo sessionInfo = new WebSocketSessionInfo(session.getId(), deviceId, token);
        sessionInfoMap.put(session.getId(), sessionInfo);

        if (deviceId != null && authenticationService != null) {
            DeviceAuthenticationService.AuthenticationResult result = 
                authenticationService.authenticateByDeviceId(deviceId, token, session.getRemoteAddress().toString());
            
            if (result.isSuccess()) {
                sessionInfo.setAuthenticated(true);
                sessionInfo.setDeviceName(result.getDevice().getName());
                deviceSubscriptions.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>()).add(session);
                logger.info("WebSocket认证成功: sessionId={}, deviceId={}", session.getId(), deviceId);
            } else {
                sessionInfo.setAuthenticated(false);
                logger.warn("WebSocket认证失败: sessionId={}, error={}", session.getId(), result.getErrorMessage());
            }
        }

        logger.info("WebSocket连接建立: sessionId={}, deviceId={}, authenticated={}", 
            session.getId(), deviceId, sessionInfo.isAuthenticated());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        WebSocketSessionInfo sessionInfo = sessionInfoMap.remove(session.getId());
        
        if (sessionInfo != null && sessionInfo.getDeviceId() != null) {
            List<WebSocketSession> subs = deviceSubscriptions.get(sessionInfo.getDeviceId());
            if (subs != null) {
                subs.remove(session);
            }
        }

        logger.info("WebSocket连接关闭: sessionId={}, status={}", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.debug("收到WebSocket消息: sessionId={}, payload={}", session.getId(), payload);

        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String type = (String) data.get("type");

            switch (type != null ? type : "") {
                case "auth":
                    handleAuthMessage(session, data);
                    break;
                case "subscribe":
                    handleSubscribeMessage(session, data);
                    break;
                case "unsubscribe":
                    handleUnsubscribeMessage(session, data);
                    break;
                case "data":
                    handleDataMessage(session, data);
                    break;
                default:
                    sendMessage(session, Map.of("type", "error", "message", "未知的消息类型"));
            }
        } catch (Exception e) {
            logger.error("处理WebSocket消息失败: sessionId={}", session.getId(), e);
            sendMessage(session, Map.of("type", "error", "message", e.getMessage()));
        }
    }

    private void handleAuthMessage(WebSocketSession session, Map<String, Object> data) {
        String deviceId = (String) data.get("deviceId");
        String token = (String) data.get("token");

        WebSocketSessionInfo sessionInfo = sessionInfoMap.get(session.getId());
        if (sessionInfo == null) {
            return;
        }

        if (authenticationService != null && deviceId != null) {
            DeviceAuthenticationService.AuthenticationResult result = 
                authenticationService.authenticateByDeviceId(deviceId, token, session.getRemoteAddress().toString());
            
            if (result.isSuccess()) {
                sessionInfo.setDeviceId(deviceId);
                sessionInfo.setAuthenticated(true);
                sessionInfo.setDeviceName(result.getDevice().getName());
                deviceSubscriptions.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>()).add(session);
                
                sendMessage(session, Map.of("type", "auth", "success", true, "sessionId", result.getSessionId()));
            } else {
                sendMessage(session, Map.of("type", "auth", "success", false, "message", result.getErrorMessage()));
            }
        }
    }

    private void handleSubscribeMessage(WebSocketSession session, Map<String, Object> data) {
        String deviceId = (String) data.get("deviceId");
        
        WebSocketSessionInfo sessionInfo = sessionInfoMap.get(session.getId());
        if (sessionInfo == null || !sessionInfo.isAuthenticated()) {
            sendMessage(session, Map.of("type", "error", "message", "未认证"));
            return;
        }

        if (deviceId != null) {
            deviceSubscriptions.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>()).add(session);
            sendMessage(session, Map.of("type", "subscribe", "success", true, "deviceId", deviceId));
            logger.info("订阅设备: sessionId={}, deviceId={}", session.getId(), deviceId);
        }
    }

    private void handleUnsubscribeMessage(WebSocketSession session, Map<String, Object> data) {
        String deviceId = (String) data.get("deviceId");

        if (deviceId != null) {
            List<WebSocketSession> subs = deviceSubscriptions.get(deviceId);
            if (subs != null) {
                subs.remove(session);
            }
            sendMessage(session, Map.of("type", "unsubscribe", "success", true, "deviceId", deviceId));
        }
    }

    private void handleDataMessage(WebSocketSession session, Map<String, Object> data) {
        WebSocketSessionInfo sessionInfo = sessionInfoMap.get(session.getId());
        if (sessionInfo == null || !sessionInfo.isAuthenticated()) {
            sendMessage(session, Map.of("type", "error", "message", "未认证"));
            return;
        }

        if (dataChangeMonitor != null) {
            String nodeId = (String) data.get("nodeId");
            Object value = data.get("value");
            dataChangeMonitor.notifyDataChange(nodeId, value);
        }
    }

    public void broadcastDeviceData(DeviceDataDTO data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            TextMessage textMessage = new TextMessage(json);
            
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        } catch (IOException e) {
            logger.error("广播消息失败", e);
        }
    }

    public void broadcastMessage(Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            TextMessage textMessage = new TextMessage(json);
            
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
            logger.debug("广播消息给 {} 个客户端: {}", sessions.size(), data.get("type"));
        } catch (IOException e) {
            logger.error("广播消息失败", e);
        }
    }

    public void sendToDeviceSubscribers(String deviceId, Object data) {
        List<WebSocketSession> subs = deviceSubscriptions.get(deviceId);
        if (subs == null || subs.isEmpty()) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(data);
            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession session : subs) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        } catch (IOException e) {
            logger.error("发送设备消息失败: deviceId={}", deviceId, e);
        }
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            logger.error("发送消息失败: sessionId={}", session.getId(), e);
        }
    }

    private String extractDeviceId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            String query = uri.getQuery();
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "deviceId".equals(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    private String extractToken(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            String query = uri.getQuery();
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "token".equals(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    public int getSessionCount() {
        return sessions.size();
    }

    public static class WebSocketSessionInfo {
        private final String sessionId;
        private String deviceId;
        private String token;
        private boolean authenticated;
        private String deviceName;

        public WebSocketSessionInfo(String sessionId, String deviceId, String token) {
            this.sessionId = sessionId;
            this.deviceId = deviceId;
            this.token = token;
            this.authenticated = false;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getToken() {
            return token;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public void setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }
    }
}
