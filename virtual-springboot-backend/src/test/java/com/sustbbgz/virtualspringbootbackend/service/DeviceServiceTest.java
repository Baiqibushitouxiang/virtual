package com.sustbbgz.virtualspringbootbackend.service;

import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.mapper.DeviceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceService deviceService;

    private Device testDevice;

    @BeforeEach
    void setUp() {
        testDevice = new Device();
        testDevice.setId(1L);
        testDevice.setDeviceId("DEV-001");
        testDevice.setName("测试设备");
        testDevice.setDescription("测试描述");
        testDevice.setStatus(1);
    }

    @Test
    void testRegisterDevice() {
        when(deviceMapper.selectOne(any())).thenReturn(null);
        when(deviceMapper.insert(any(Device.class))).thenReturn(1);

        Device newDevice = new Device();
        newDevice.setName("新设备");
        newDevice.setDescription("新设备描述");

        Device result = deviceService.registerDevice(newDevice);

        assertNotNull(result);
        assertNotNull(result.getDeviceId());
        assertEquals(1, result.getStatus());
    }

    @Test
    void testGetByDeviceId() {
        when(deviceMapper.selectOne(any())).thenReturn(testDevice);

        Device result = deviceService.getByDeviceId("DEV-001");

        assertNotNull(result);
        assertEquals("DEV-001", result.getDeviceId());
    }

    @Test
    void testGetDevicesByUserId() {
        List<Device> devices = Arrays.asList(testDevice);
        when(deviceMapper.selectList(any())).thenReturn(devices);

        List<Device> result = deviceService.getDevicesByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
