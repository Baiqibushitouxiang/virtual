package com.sustbbgz.virtualspringbootbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.controller.dto.DeviceRegisterDTO;
import com.sustbbgz.virtualspringbootbackend.controller.dto.DeviceUpdateDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        Device device = buildDevice(1L, "DEV-001", "test-device");
        when(deviceService.registerDevice(anyString(), anyString())).thenReturn(device);

        DeviceRegisterDTO dto = new DeviceRegisterDTO();
        dto.setName("test-device");
        dto.setDescription("test-description");

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.deviceId").value("DEV-001"));
    }

    @Test
    void testGetDeviceList() throws Exception {
        List<Device> devices = Arrays.asList(
                buildDevice(1L, "DEV-001", "device-1"),
                buildDevice(2L, "DEV-002", "device-2")
        );
        when(deviceService.list()).thenReturn(devices);

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetDeviceListByUserId() throws Exception {
        when(deviceService.getDevicesByUserId(9L)).thenReturn(Arrays.asList(buildDevice(1L, "DEV-001", "device-1")));

        mockMvc.perform(get("/api/devices").param("userId", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testGetDeviceById() throws Exception {
        when(deviceService.getById(1L)).thenReturn(buildDevice(1L, "DEV-001", "test-device"));

        mockMvc.perform(get("/api/devices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.deviceId").value("DEV-001"));
    }

    @Test
    void testGetDeviceByIdNotFound() throws Exception {
        when(deviceService.getById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/devices/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    void testGetDeviceByDeviceId() throws Exception {
        when(deviceService.getByDeviceId("DEV-001")).thenReturn(buildDevice(1L, "DEV-001", "test-device"));

        mockMvc.perform(get("/api/devices/deviceId/DEV-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetDeviceByDeviceIdNotFound() throws Exception {
        when(deviceService.getByDeviceId("UNKNOWN")).thenReturn(null);

        mockMvc.perform(get("/api/devices/deviceId/UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    void testUpdateDevice() throws Exception {
        Device device = buildDevice(1L, "DEV-001", "old-name");
        when(deviceService.getById(1L)).thenReturn(device);
        when(deviceService.updateById(any(Device.class))).thenReturn(true);

        DeviceUpdateDTO dto = new DeviceUpdateDTO();
        dto.setName("new-name");
        dto.setDescription("updated-description");
        dto.setStatus(0);

        mockMvc.perform(put("/api/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.name").value("new-name"))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    void testUpdateDeviceNotFound() throws Exception {
        when(deviceService.getById(1L)).thenReturn(null);

        mockMvc.perform(put("/api/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeviceUpdateDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    void testDeleteDevice() throws Exception {
        when(deviceService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/devices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    void testBindUser() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 7L);
        when(deviceService.bindUser(1L, 7L)).thenReturn(true);

        mockMvc.perform(post("/api/devices/1/bind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    void testUnbindUser() throws Exception {
        when(deviceService.unbindUser(1L)).thenReturn(true);

        mockMvc.perform(post("/api/devices/1/unbind"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    void testUpdateStatus() throws Exception {
        when(deviceService.updateStatus(1L, 0)).thenReturn(true);

        mockMvc.perform(put("/api/devices/1/status").param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    void testGenerateCertificate() throws Exception {
        Device device = buildDevice(1L, "DEV-001", "test-device");
        when(deviceService.getById(1L)).thenReturn(device);
        when(deviceService.updateById(any(Device.class))).thenReturn(true);

        mockMvc.perform(post("/api/devices/1/certificate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.certificate").exists())
                .andExpect(jsonPath("$.data.thumbprint").exists());
    }

    @Test
    void testGetCertificate() throws Exception {
        Device device = buildDevice(1L, "DEV-001", "test-device");
        device.setCertificate("pem-content");
        device.setCertificateThumbprint("thumbprint");
        when(deviceService.getById(1L)).thenReturn(device);

        mockMvc.perform(get("/api/devices/1/certificate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.certificate").value("pem-content"))
                .andExpect(jsonPath("$.data.thumbprint").value("thumbprint"));
    }

    @Test
    void testRevokeCertificate() throws Exception {
        Device device = buildDevice(1L, "DEV-001", "test-device");
        device.setCertificate("pem-content");
        device.setCertificateThumbprint("thumbprint");
        when(deviceService.getById(1L)).thenReturn(device);
        when(deviceService.updateById(any(Device.class))).thenReturn(true);

        mockMvc.perform(post("/api/devices/1/certificate/revoke"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    private Device buildDevice(Long id, String deviceId, String name) {
        Device device = new Device();
        device.setId(id);
        device.setDeviceId(deviceId);
        device.setName(name);
        device.setDescription("description");
        device.setStatus(1);
        return device;
    }
}
