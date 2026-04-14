package com.sustbbgz.virtualspringbootbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.controller.dto.DeviceRegisterDTO;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private DeviceController deviceController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deviceController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegisterDevice() throws Exception {
        Device device = new Device();
        device.setId(1L);
        device.setDeviceId("DEV-001");
        device.setName("测试设备");

        when(deviceService.registerDevice(anyString(), anyString())).thenReturn(device);

        DeviceRegisterDTO dto = new DeviceRegisterDTO();
        dto.setName("测试设备");
        dto.setDescription("测试描述");

        mockMvc.perform(post("/api/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDeviceList() throws Exception {
        Device device1 = new Device();
        device1.setId(1L);
        device1.setDeviceId("DEV-001");
        device1.setName("设备1");

        Device device2 = new Device();
        device2.setId(2L);
        device2.setDeviceId("DEV-002");
        device2.setName("设备2");

        List<Device> devices = Arrays.asList(device1, device2);
        when(deviceService.list()).thenReturn(devices);

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDeviceById() throws Exception {
        Device device = new Device();
        device.setId(1L);
        device.setDeviceId("DEV-001");
        device.setName("测试设备");

        when(deviceService.getById(1L)).thenReturn(device);

        mockMvc.perform(get("/api/devices/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteDevice() throws Exception {
        when(deviceService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/devices/1"))
                .andExpect(status().isOk());
    }
}
