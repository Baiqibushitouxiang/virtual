package com.sustbbgz.virtualspringbootbackend.opcua.security;

import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.opcua.session.DeviceSessionManager;
import com.sustbbgz.virtualspringbootbackend.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.Optional;

@Service
public class DeviceAuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceAuthenticationService.class);

    private final DeviceCertificateValidator certificateValidator;
    private final DeviceSessionManager sessionManager;
    private final DeviceAccessControl accessControl;
    private final DeviceService deviceService;

    public DeviceAuthenticationService(
            DeviceCertificateValidator certificateValidator,
            DeviceSessionManager sessionManager,
            DeviceAccessControl accessControl,
            DeviceService deviceService) {
        this.certificateValidator = certificateValidator;
        this.sessionManager = sessionManager;
        this.accessControl = accessControl;
        this.deviceService = deviceService;
    }

    public AuthenticationResult authenticateByCertificate(X509Certificate certificate, String clientAddress) {
        DeviceCertificateValidator.ValidationResult result = certificateValidator.validate(certificate);
        
        if (!result.isValid()) {
            logger.warn("证书认证失败: {}", result.getErrorMessage());
            return AuthenticationResult.failure(result.getErrorMessage());
        }

        Device device = result.getDevice();
        DeviceSessionManager.DeviceSession session = sessionManager.createSession(device, clientAddress);

        logger.info("设备认证成功: deviceId={}, sessionId={}", device.getDeviceId(), session.getSessionId());

        return AuthenticationResult.success(device, session.getSessionId());
    }

    public AuthenticationResult authenticateByDeviceId(String deviceId, String token, String clientAddress) {
        if (!validateToken(deviceId, token)) {
            logger.warn("Token认证失败: deviceId={}", deviceId);
            return AuthenticationResult.failure("Token无效");
        }

        DeviceCertificateValidator.ValidationResult result = certificateValidator.validateByDeviceId(deviceId);
        
        if (!result.isValid()) {
            return AuthenticationResult.failure(result.getErrorMessage());
        }

        Device device = result.getDevice();
        DeviceSessionManager.DeviceSession session = sessionManager.createSession(device, clientAddress);

        return AuthenticationResult.success(device, session.getSessionId());
    }

    public AuthorizationResult authorize(String sessionId, String nodeId, 
                                         DeviceAccessControl.Permission requiredPermission) {
        Optional<DeviceSessionManager.DeviceSession> sessionOpt = sessionManager.getSession(sessionId);
        
        if (!sessionOpt.isPresent()) {
            return AuthorizationResult.failure("会话无效或已过期");
        }

        DeviceSessionManager.DeviceSession session = sessionOpt.get();
        String deviceId = session.getDeviceCode();

        if (!accessControl.hasPermission(deviceId, requiredPermission)) {
            logger.warn("权限不足: deviceId={}, requiredPermission={}", deviceId, requiredPermission);
            return AuthorizationResult.failure("权限不足");
        }

        if (nodeId != null && !accessControl.canAccessNode(deviceId, nodeId)) {
            logger.warn("节点访问被拒绝: deviceId={}, nodeId={}", deviceId, nodeId);
            return AuthorizationResult.failure("无权访问该节点");
        }

        return AuthorizationResult.success(session.getDeviceCode(), session.getDeviceName());
    }

    public void logout(String sessionId) {
        sessionManager.destroySession(sessionId);
        logger.info("设备登出: sessionId={}", sessionId);
    }

    public Optional<DeviceSessionManager.DeviceSession> validateSession(String sessionId) {
        return sessionManager.getSession(sessionId);
    }

    private boolean validateToken(String deviceId, String token) {
        Device device = deviceService.getByDeviceId(deviceId);
        if (device == null) {
            return false;
        }
        return token != null && !token.isEmpty();
    }

    public static class AuthenticationResult {
        private final boolean success;
        private final String errorMessage;
        private final Device device;
        private final String sessionId;

        private AuthenticationResult(boolean success, String errorMessage, Device device, String sessionId) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.device = device;
            this.sessionId = sessionId;
        }

        public static AuthenticationResult success(Device device, String sessionId) {
            return new AuthenticationResult(true, null, device, sessionId);
        }

        public static AuthenticationResult failure(String errorMessage) {
            return new AuthenticationResult(false, errorMessage, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Device getDevice() {
            return device;
        }

        public String getSessionId() {
            return sessionId;
        }
    }

    public static class AuthorizationResult {
        private final boolean success;
        private final String errorMessage;
        private final String deviceId;
        private final String deviceName;

        private AuthorizationResult(boolean success, String errorMessage, String deviceId, String deviceName) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.deviceId = deviceId;
            this.deviceName = deviceName;
        }

        public static AuthorizationResult success(String deviceId, String deviceName) {
            return new AuthorizationResult(true, null, deviceId, deviceName);
        }

        public static AuthorizationResult failure(String errorMessage) {
            return new AuthorizationResult(false, errorMessage, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }
    }
}
