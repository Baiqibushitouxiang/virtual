package com.sustbbgz.virtualspringbootbackend.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.exception.ServiceException;
import com.sustbbgz.virtualspringbootbackend.mapper.DeviceMapper;
import com.sustbbgz.virtualspringbootbackend.opcua.OpcUaServerService;
import com.sustbbgz.virtualspringbootbackend.opcua.namespace.DeviceNodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceService extends ServiceImpl<DeviceMapper, Device> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    private static final String DEVICE_NOT_FOUND = "Device not found";

    @Autowired(required = false)
    @Lazy
    private OpcUaServerService opcUaServerService;

    @Transactional
    public Device registerDevice(Device device) {
        if (device.getDeviceId() == null || device.getDeviceId().isEmpty()) {
            device.setDeviceId("DEV-" + IdUtil.simpleUUID().substring(0, 8).toUpperCase());
        }

        Device existingDevice = getByDeviceId(device.getDeviceId());
        if (existingDevice != null) {
            throw new ServiceException(500, "Device ID already exists");
        }

        if (device.getStatus() == null) {
            device.setStatus(1);
        }
        device.setLastSeenAt(null);
        save(device);

        scheduleOpcUaDeviceNodeCreation(device.getDeviceId(), device.getName());
        return device;
    }

    public Device registerDevice(String name, String description) {
        Device device = new Device();
        device.setName(name);
        device.setDescription(description);
        device.setStatus(1);
        return registerDevice(device);
    }

    private void scheduleOpcUaDeviceNodeCreation(String deviceId, String deviceName) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    createOpcUaDeviceNodeSafely(deviceId, deviceName);
                }
            });
        } else {
            createOpcUaDeviceNodeSafely(deviceId, deviceName);
        }
    }

    void createOpcUaDeviceNodeSafely(String deviceId, String deviceName) {
        if (opcUaServerService == null || !opcUaServerService.isRunning()) {
            return;
        }

        try {
            DeviceNodeStore.DeviceNode existingNode = opcUaServerService
                .getDeviceNamespace()
                .getDeviceNodeStore()
                .getDeviceNode(deviceId);

            if (existingNode == null) {
                opcUaServerService.getDeviceNamespace().createDeviceNode(deviceId, deviceName);
            }
        } catch (Exception e) {
            logger.error("Failed to create OPC UA node after device registration: deviceId={}", deviceId, e);
        }
    }

    @Transactional
    public boolean bindUser(String deviceId, Long userId) {
        Device device = getByDeviceId(deviceId);
        if (device == null) {
            throw new ServiceException(500, DEVICE_NOT_FOUND);
        }
        device.setUserId(userId);
        return updateById(device);
    }

    @Transactional
    public boolean bindUser(Long id, Long userId) {
        Device device = getById(id);
        if (device == null) {
            throw new ServiceException(500, DEVICE_NOT_FOUND);
        }
        device.setUserId(userId);
        return updateById(device);
    }

    @Transactional
    public boolean unbindUser(String deviceId) {
        Device device = getByDeviceId(deviceId);
        if (device == null) {
            throw new ServiceException(500, DEVICE_NOT_FOUND);
        }
        device.setUserId(null);
        return updateById(device);
    }

    @Transactional
    public boolean unbindUser(Long id) {
        Device device = getById(id);
        if (device == null) {
            throw new ServiceException(500, DEVICE_NOT_FOUND);
        }
        device.setUserId(null);
        return updateById(device);
    }

    @Transactional
    public boolean updateStatus(String deviceId, Integer status) {
        Device device = getByDeviceId(deviceId);
        if (device == null) {
            throw new ServiceException(500, DEVICE_NOT_FOUND);
        }
        device.setStatus(status);
        return updateById(device);
    }

    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        Device device = getById(id);
        if (device == null) {
            throw new ServiceException(500, DEVICE_NOT_FOUND);
        }
        device.setStatus(status);
        return updateById(device);
    }

    public Device getByDeviceId(String deviceId) {
        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Device::getDeviceId, deviceId);
        return getOne(queryWrapper);
    }

    public Device getByCertificateThumbprint(String thumbprint) {
        if (thumbprint == null || thumbprint.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Device::getCertificateThumbprint, thumbprint);
        return getOne(queryWrapper);
    }

    public List<Device> getDevicesByUserId(Long userId) {
        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Device::getUserId, userId);
        return list(queryWrapper);
    }

    @Transactional
    public void updateLastSeenAt(String deviceId) {
        Device device = getByDeviceId(deviceId);
        if (device != null) {
            device.setLastSeenAt(LocalDateTime.now());
            updateById(device);
        }
    }

    public void updateOnlineStatus(String deviceId, Integer status) {
        markDeviceSeen(deviceId);
    }

    @Transactional
    public boolean markDeviceSeen(String deviceId) {
        Device device = getByDeviceId(deviceId);
        if (device != null && device.getStatus() != null && device.getStatus() == 1) {
            device.setLastSeenAt(LocalDateTime.now());
            return updateById(device);
        }
        return false;
    }

    public boolean isDeviceEnabled(String deviceId) {
        Device device = getByDeviceId(deviceId);
        return device != null && device.getStatus() != null && device.getStatus() == 1;
    }
}
