package com.sustbbgz.virtualspringbootbackend.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.exception.ServiceException;
import com.sustbbgz.virtualspringbootbackend.mapper.DeviceMapper;
import com.sustbbgz.virtualspringbootbackend.opcua.OpcUaServerService;
import com.sustbbgz.virtualspringbootbackend.opcua.namespace.DeviceNodeStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DeviceService extends ServiceImpl<DeviceMapper, Device> {

    @Autowired(required = false)
    @Lazy
    private OpcUaServerService opcUaServerService;

    public Device registerDevice(Device device) {
        if (device.getDeviceId() == null || device.getDeviceId().isEmpty()) {
            device.setDeviceId("DEV-" + IdUtil.simpleUUID().substring(0, 8).toUpperCase());
        }
        Device existingDevice = getByDeviceId(device.getDeviceId());
        if (existingDevice != null) {
            throw new ServiceException(500, "设备ID已存在");
        }
        if (device.getStatus() == null) {
            device.setStatus(1);
        }
        device.setLastSeenAt(LocalDateTime.now());
        save(device);
        
        createOpcUaDeviceNode(device.getDeviceId(), device.getName());
        
        return device;
    }

    public Device registerDevice(String name, String description) {
        Device device = new Device();
        device.setName(name);
        device.setDescription(description);
        device.setStatus(1);
        return registerDevice(device);
    }

    private void createOpcUaDeviceNode(String deviceId, String deviceName) {
        if (opcUaServerService != null && opcUaServerService.isRunning()) {
            try {
                DeviceNodeStore.DeviceNode existingNode = opcUaServerService
                    .getDeviceNamespace()
                    .getDeviceNodeStore()
                    .getDeviceNode(deviceId);
                
                if (existingNode == null) {
                    opcUaServerService.getDeviceNamespace()
                        .createDeviceNode(deviceId, deviceName);
                }
            } catch (Exception e) {
                System.err.println("创建OPC UA设备节点失败: " + e.getMessage());
            }
        }
    }

    public boolean bindUser(String deviceId, Long userId) {
        Device device = getByDeviceId(deviceId);
        if (device == null) {
            throw new ServiceException(500, "设备不存在");
        }
        device.setUserId(userId);
        return updateById(device);
    }

    public boolean bindUser(Long id, Long userId) {
        Device device = getById(id);
        if (device == null) {
            throw new ServiceException(500, "设备不存在");
        }
        device.setUserId(userId);
        return updateById(device);
    }

    public boolean unbindUser(String deviceId) {
        Device device = getByDeviceId(deviceId);
        if (device == null) {
            throw new ServiceException(500, "设备不存在");
        }
        device.setUserId(null);
        return updateById(device);
    }

    public boolean unbindUser(Long id) {
        Device device = getById(id);
        if (device == null) {
            throw new ServiceException(500, "设备不存在");
        }
        device.setUserId(null);
        return updateById(device);
    }

    public boolean updateStatus(String deviceId, Integer status) {
        Device device = getByDeviceId(deviceId);
        if (device == null) {
            throw new ServiceException(500, "设备不存在");
        }
        device.setStatus(status);
        return updateById(device);
    }

    public boolean updateStatus(Long id, Integer status) {
        Device device = getById(id);
        if (device == null) {
            throw new ServiceException(500, "设备不存在");
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

    public void updateLastSeenAt(String deviceId) {
        Device device = getByDeviceId(deviceId);
        if (device != null) {
            device.setLastSeenAt(LocalDateTime.now());
            updateById(device);
        }
    }

    public void updateOnlineStatus(String deviceId, Integer status) {
        Device device = getByDeviceId(deviceId);
        if (device != null) {
            device.setStatus(status);
            device.setLastSeenAt(LocalDateTime.now());
            updateById(device);
        }
    }
}
