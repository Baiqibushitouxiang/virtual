package com.sustbbgz.virtualspringbootbackend.service;

import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.mapper.DeviceMapper;
import com.sustbbgz.virtualspringbootbackend.opcua.OpcUaServerService;
import com.sustbbgz.virtualspringbootbackend.opcua.namespace.DeviceNamespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceMapper deviceMapper;

    @Mock
    private OpcUaServerService opcUaServerService;

    @Mock
    private DeviceNamespace deviceNamespace;

    @InjectMocks
    private DeviceService deviceService;

    private Device testDevice;

    @BeforeEach
    void setUp() {
        testDevice = new Device();
        testDevice.setId(1L);
        testDevice.setDeviceId("DEV-001");
        testDevice.setName("test-device");
        testDevice.setDescription("test-description");
        testDevice.setStatus(1);
    }

    @Test
    void testRegisterDevice() {
        when(deviceMapper.selectOne(any())).thenReturn(null);
        when(deviceMapper.insert(any(Device.class))).thenReturn(1);
        when(opcUaServerService.isRunning()).thenReturn(false);

        Device newDevice = new Device();
        newDevice.setName("new-device");
        newDevice.setDescription("new-description");

        Device result = deviceService.registerDevice(newDevice);

        assertNotNull(result);
        assertNotNull(result.getDeviceId());
        assertEquals(1, result.getStatus());
    }

    @Test
    void testRegisterDeviceSucceedsWhenOpcUaNodeCreationFails() {
        when(deviceMapper.selectOne(any())).thenReturn(null);
        when(deviceMapper.insert(any(Device.class))).thenReturn(1);
        when(opcUaServerService.isRunning()).thenReturn(true);
        when(opcUaServerService.getDeviceNamespace()).thenReturn(deviceNamespace);
        when(deviceNamespace.getDeviceNodeStore()).thenThrow(new RuntimeException("opc ua unavailable"));

        Device newDevice = new Device();
        newDevice.setName("new-device");
        newDevice.setDescription("new-description");

        Device result = deviceService.registerDevice(newDevice);

        assertNotNull(result);
        assertNotNull(result.getDeviceId());
        assertEquals(1, result.getStatus());
        verify(opcUaServerService).getDeviceNamespace();
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

    @Test
    void testRegisterDeviceRejectsDuplicateDeviceId() {
        when(deviceMapper.selectOne(any())).thenReturn(testDevice);

        Device duplicate = new Device();
        duplicate.setDeviceId("DEV-001");
        duplicate.setName("duplicate-device");

        Exception exception = assertThrows(Exception.class, () -> deviceService.registerDevice(duplicate));
        assertEquals("Device ID already exists", exception.getMessage());
    }

    @Test
    void testDisabledDeviceShouldNotBeMarkedOnline() {
        testDevice.setStatus(0);
        DeviceService spyService = org.mockito.Mockito.spy(deviceService);
        doReturn(testDevice).when(spyService).getByDeviceId("DEV-001");

        boolean updated = spyService.markDeviceSeen("DEV-001");

        assertFalse(updated);
    }
}
