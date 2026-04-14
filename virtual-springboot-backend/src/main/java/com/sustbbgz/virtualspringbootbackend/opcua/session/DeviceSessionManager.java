package com.sustbbgz.virtualspringbootbackend.opcua.session;

import com.sustbbgz.virtualspringbootbackend.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(DeviceSessionManager.class);

    private static final long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000;

    private final Map<String, DeviceSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> deviceToSession = new ConcurrentHashMap<>();

    public DeviceSession createSession(Device device, String clientAddress) {
        String sessionId = generateSessionId();
        
        DeviceSession session = new DeviceSession(
            sessionId,
            device.getId(),
            device.getDeviceId(),
            device.getName(),
            clientAddress,
            System.currentTimeMillis(),
            System.currentTimeMillis() + DEFAULT_SESSION_TIMEOUT
        );

        sessions.put(sessionId, session);
        deviceToSession.put(device.getDeviceId(), sessionId);

        logger.info("创建设备会话: sessionId={}, deviceId={}, address={}", 
            sessionId, device.getDeviceId(), clientAddress);

        return session;
    }

    public Optional<DeviceSession> getSession(String sessionId) {
        DeviceSession session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }

        if (session.isExpired()) {
            destroySession(sessionId);
            return Optional.empty();
        }

        session.refresh(DEFAULT_SESSION_TIMEOUT);
        return Optional.of(session);
    }

    public Optional<DeviceSession> getSessionByDeviceId(String deviceId) {
        String sessionId = deviceToSession.get(deviceId);
        if (sessionId == null) {
            return Optional.empty();
        }
        return getSession(sessionId);
    }

    public void destroySession(String sessionId) {
        DeviceSession session = sessions.remove(sessionId);
        if (session != null) {
            deviceToSession.remove(session.getDeviceId());
            logger.info("销毁设备会话: sessionId={}, deviceId={}", 
                sessionId, session.getDeviceId());
        }
    }

    public void destroySessionByDeviceId(String deviceId) {
        String sessionId = deviceToSession.get(deviceId);
        if (sessionId != null) {
            destroySession(sessionId);
        }
    }

    public Collection<DeviceSession> getAllSessions() {
        return sessions.values();
    }

    public int getSessionCount() {
        return sessions.size();
    }

    public void cleanupExpiredSessions() {
        sessions.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                deviceToSession.remove(entry.getValue().getDeviceId());
                logger.info("清理过期会话: sessionId={}", entry.getKey());
                return true;
            }
            return false;
        });
    }

    private String generateSessionId() {
        return "SESSION-" + System.currentTimeMillis() + "-" + 
            Integer.toHexString((int)(Math.random() * 0xFFFFFF));
    }

    public static class DeviceSession {
        private final String sessionId;
        private final Long deviceId;
        private final String deviceCode;
        private final String deviceName;
        private final String clientAddress;
        private final long createdAt;
        private volatile long expiresAt;
        private volatile long lastActivityAt;

        public DeviceSession(String sessionId, Long deviceId, String deviceCode, 
                           String deviceName, String clientAddress, 
                           long createdAt, long expiresAt) {
            this.sessionId = sessionId;
            this.deviceId = deviceId;
            this.deviceCode = deviceCode;
            this.deviceName = deviceName;
            this.clientAddress = clientAddress;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.lastActivityAt = createdAt;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }

        public void refresh(long timeout) {
            this.expiresAt = System.currentTimeMillis() + timeout;
            this.lastActivityAt = System.currentTimeMillis();
        }

        public String getSessionId() {
            return sessionId;
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public String getDeviceCode() {
            return deviceCode;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getClientAddress() {
            return clientAddress;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public long getExpiresAt() {
            return expiresAt;
        }

        public long getLastActivityAt() {
            return lastActivityAt;
        }
    }
}
