package com.sustbbgz.virtualspringbootbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.entity.DeviceData;
import com.sustbbgz.virtualspringbootbackend.service.DeviceDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DeviceDataControllerTest {

    @Mock
    private DeviceDataService deviceDataService;

    @InjectMocks
    private DeviceDataController deviceDataController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deviceDataController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldSaveDeviceData() throws Exception {
        DeviceData saved = buildDeviceData("DEV-001", "temperature", 25.5, "C");
        when(deviceDataService.saveDeviceData("DEV-001", "temperature", 25.5, "C")).thenReturn(saved);
        Map<String, Object> payload = new HashMap<>();
        payload.put("deviceId", "DEV-001");
        payload.put("dataType", "temperature");
        payload.put("value", 25.5);
        payload.put("unit", "C");

        mockMvc.perform(post("/api/device-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.deviceId").value("DEV-001"))
                .andExpect(jsonPath("$.data.dataType").value("temperature"));
    }

    @Test
    void shouldGetLatestData() throws Exception {
        when(deviceDataService.getLatestData("DEV-001", 3))
                .thenReturn(Arrays.asList(buildDeviceData("DEV-001", "temperature", 22.0, "C")));

        mockMvc.perform(get("/api/device-data/latest/DEV-001").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void shouldGetHistoryDataByType() throws Exception {
        when(deviceDataService.getDataByType("DEV-001", "humidity", 5))
                .thenReturn(Arrays.asList(buildDeviceData("DEV-001", "humidity", 60.0, "%")));

        mockMvc.perform(get("/api/device-data/history/DEV-001")
                        .param("dataType", "humidity")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].dataType").value("humidity"));
    }

    @Test
    void shouldGetHistoryDataByTimeRange() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 4, 20, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 21, 0, 0);
        when(deviceDataService.getDataByTimeRange(eq("DEV-001"), eq(start), eq(end)))
                .thenReturn(Collections.singletonList(buildDeviceData("DEV-001", "temperature", 18.5, "C")));

        mockMvc.perform(get("/api/device-data/history/DEV-001")
                        .param("startTime", "2026-04-20T00:00:00")
                        .param("endTime", "2026-04-21T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void shouldGetStats() throws Exception {
        DeviceDataService.DeviceDataStats stats = new DeviceDataService.DeviceDataStats();
        stats.setAvgValue(20.0);
        stats.setMinValue(18.0);
        stats.setMaxValue(25.0);
        when(deviceDataService.getStats(eq("DEV-001"), eq("temperature"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(stats);

        mockMvc.perform(get("/api/device-data/stats/DEV-001").param("dataType", "temperature"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.deviceId").value("DEV-001"))
                .andExpect(jsonPath("$.data.stats.avgValue").value(20.0));
    }

    @Test
    void shouldCountByDeviceId() throws Exception {
        when(deviceDataService.countByDeviceId("DEV-001")).thenReturn(12L);

        mockMvc.perform(get("/api/device-data/count/DEV-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(12));
    }

    @Test
    void shouldCleanOldData() throws Exception {
        mockMvc.perform(delete("/api/device-data/clean").param("daysToKeep", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value("Cleaned data older than 15 days"));

        verify(deviceDataService).cleanOldData(15);
    }

    private DeviceData buildDeviceData(String deviceId, String dataType, Double value, String unit) {
        DeviceData data = new DeviceData();
        data.setId(1L);
        data.setDeviceId(deviceId);
        data.setDataType(dataType);
        data.setValue(value);
        data.setUnit(unit);
        data.setRecordedAt(LocalDateTime.of(2026, 4, 24, 10, 0));
        data.setCreatedAt(LocalDateTime.of(2026, 4, 24, 10, 0));
        return data;
    }
}
