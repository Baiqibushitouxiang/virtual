package com.sustbbgz.virtualspringbootbackend.opcua.security;

import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeviceCertificateValidator {

    private static final Logger logger = LoggerFactory.getLogger(DeviceCertificateValidator.class);

    private final DeviceService deviceService;
    private final Map<String, Long> certificateCache = new ConcurrentHashMap<>();

    public DeviceCertificateValidator(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public ValidationResult validate(X509Certificate certificate) {
        if (certificate == null) {
            return ValidationResult.failure("证书为空");
        }

        try {
            String thumbprint = CertificateUtils.getCertificateThumbprint(certificate);
            
            Device device = deviceService.getByCertificateThumbprint(thumbprint);
            if (device == null) {
                logger.warn("未找到指纹对应的设备: {}", thumbprint);
                return ValidationResult.failure("设备未注册");
            }

            if (device.getStatus() == null || device.getStatus() != 1) {
                logger.warn("设备已禁用: {}", device.getDeviceId());
                return ValidationResult.failure("设备已禁用");
            }

            certificateCache.put(thumbprint, device.getId());
            
            return ValidationResult.success(device);

        } catch (Exception e) {
            logger.error("证书验证失败", e);
            return ValidationResult.failure("证书验证失败: " + e.getMessage());
        }
    }

    public ValidationResult validateByDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return ValidationResult.failure("设备ID为空");
        }

        Device device = deviceService.getByDeviceId(deviceId);
        if (device == null) {
            return ValidationResult.failure("设备未注册");
        }

        if (device.getStatus() == null || device.getStatus() != 1) {
            return ValidationResult.failure("设备已禁用");
        }

        return ValidationResult.success(device);
    }

    public void clearCache() {
        certificateCache.clear();
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final Device device;

        private ValidationResult(boolean valid, String errorMessage, Device device) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.device = device;
        }

        public static ValidationResult success(Device device) {
            return new ValidationResult(true, null, device);
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage, null);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Device getDevice() {
            return device;
        }
    }
}
