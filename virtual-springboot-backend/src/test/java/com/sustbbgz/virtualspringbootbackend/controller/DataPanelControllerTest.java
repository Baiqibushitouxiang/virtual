package com.sustbbgz.virtualspringbootbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.entity.DataPanel;
import com.sustbbgz.virtualspringbootbackend.service.DataPanelService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DataPanelControllerTest {

    @Mock
    private DataPanelService dataPanelService;

    @InjectMocks
    private DataPanelController dataPanelController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dataPanelController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldListPanels() throws Exception {
        when(dataPanelService.findByUserId(null)).thenReturn(Arrays.asList(buildPanel(1L)));

        mockMvc.perform(get("/api/data-panels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void shouldGetPanelById() throws Exception {
        when(dataPanelService.getById(1L)).thenReturn(buildPanel(1L));

        mockMvc.perform(get("/api/data-panels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldCreatePanel() throws Exception {
        DataPanel panel = buildPanel(1L);
        when(dataPanelService.createPanel(any(DataPanel.class))).thenReturn(panel);

        mockMvc.perform(post("/api/data-panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(panel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.name").value("panel-1"));
    }

    @Test
    void shouldUpdatePanel() throws Exception {
        DataPanel panel = buildPanel(1L);
        panel.setName("panel-updated");
        when(dataPanelService.updatePanel(any(DataPanel.class))).thenReturn(panel);

        mockMvc.perform(put("/api/data-panels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(panel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.name").value("panel-updated"));
    }

    @Test
    void shouldDeletePanel() throws Exception {
        doNothing().when(dataPanelService).deletePanel(1L);

        mockMvc.perform(delete("/api/data-panels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    void shouldBindDevice() throws Exception {
        DataPanel panel = buildPanel(1L);
        panel.setDeviceId(8L);
        when(dataPanelService.bindDevice(1L, 8L)).thenReturn(panel);

        Map<String, Long> payload = new HashMap<>();
        payload.put("deviceId", 8L);
        mockMvc.perform(post("/api/data-panels/1/bind-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.deviceId").value(8));
    }

    @Test
    void shouldUnbindDevice() throws Exception {
        DataPanel panel = buildPanel(1L);
        panel.setDeviceId(null);
        when(dataPanelService.unbindDevice(1L)).thenReturn(panel);

        mockMvc.perform(post("/api/data-panels/1/unbind-device"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    void shouldBindModel() throws Exception {
        DataPanel panel = buildPanel(1L);
        panel.setModelId("model-1");
        when(dataPanelService.bindModel(1L, "model-1", "Pump", "scene")).thenReturn(panel);

        Map<String, Object> payload = new HashMap<>();
        payload.put("modelId", "model-1");
        payload.put("modelName", "Pump");
        payload.put("modelType", "scene");
        mockMvc.perform(post("/api/data-panels/1/bind-model")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.modelId").value("model-1"));
    }

    @Test
    void shouldUnbindModel() throws Exception {
        DataPanel panel = buildPanel(1L);
        panel.setModelId(null);
        when(dataPanelService.unbindModel(1L)).thenReturn(panel);

        mockMvc.perform(post("/api/data-panels/1/unbind-model"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    void shouldUpdatePosition() throws Exception {
        DataPanel panel = buildPanel(1L);
        panel.setPosition("{\"x\":10,\"y\":20}");
        when(dataPanelService.updatePosition(1L, "{\"x\":10,\"y\":20}")).thenReturn(panel);

        Map<String, String> payload = new HashMap<>();
        payload.put("position", "{\"x\":10,\"y\":20}");
        mockMvc.perform(put("/api/data-panels/1/position")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.position").value("{\"x\":10,\"y\":20}"));
    }

    @Test
    void shouldUpdateStyle() throws Exception {
        DataPanel panel = buildPanel(1L);
        panel.setStyle("{\"theme\":\"light\"}");
        when(dataPanelService.updateStyle(1L, "{\"theme\":\"light\"}")).thenReturn(panel);

        Map<String, String> payload = new HashMap<>();
        payload.put("style", "{\"theme\":\"light\"}");
        mockMvc.perform(put("/api/data-panels/1/style")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.style").value("{\"theme\":\"light\"}"));
    }

    private DataPanel buildPanel(Long id) {
        DataPanel panel = new DataPanel();
        panel.setId(id);
        panel.setName("panel-" + id);
        panel.setDescription("description");
        panel.setStatus(1);
        return panel;
    }
}
