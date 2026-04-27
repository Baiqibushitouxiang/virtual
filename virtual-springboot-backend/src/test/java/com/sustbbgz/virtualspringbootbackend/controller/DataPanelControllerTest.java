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
import static org.mockito.ArgumentMatchers.eq;
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
    void shouldListPanelsBySceneId() throws Exception {
        when(dataPanelService.findByUserIdAndSceneId(null, 7L)).thenReturn(Arrays.asList(buildPanel(1L)));

        mockMvc.perform(get("/api/data-panels").param("sceneId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(1));
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
    void shouldUpdatePanelWithSceneId() throws Exception {
        DataPanel panel = buildPanel(1L);
        panel.setName("panel-updated");
        when(dataPanelService.updatePanel(any(DataPanel.class), eq(7L), eq(null))).thenReturn(panel);

        mockMvc.perform(put("/api/data-panels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(panel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.name").value("panel-updated"));
    }

    @Test
    void shouldDeletePanelBySceneId() throws Exception {
        doNothing().when(dataPanelService).deletePanel(1L, 7L, null);

        mockMvc.perform(delete("/api/data-panels/1").param("sceneId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    void shouldBindDeviceWithinScene() throws Exception {
        DataPanel panel = buildPanel(1L);
        panel.setDeviceId(8L);
        when(dataPanelService.bindDevice(1L, 7L, 8L, null)).thenReturn(panel);

        Map<String, Object> payload = new HashMap<>();
        payload.put("deviceId", 8L);
        payload.put("sceneId", 7L);
        mockMvc.perform(post("/api/data-panels/1/bind-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.deviceId").value(8));
    }

    @Test
    void shouldBindModelWithinScene() throws Exception {
        DataPanel panel = buildPanel(1L);
        panel.setModelId("model-1");
        when(dataPanelService.bindModel(1L, 7L, "model-1", "Pump", "scene", null)).thenReturn(panel);

        Map<String, Object> payload = new HashMap<>();
        payload.put("modelId", "model-1");
        payload.put("modelName", "Pump");
        payload.put("modelType", "scene");
        payload.put("sceneId", 7L);
        mockMvc.perform(post("/api/data-panels/1/bind-model")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.modelId").value("model-1"));
    }

    @Test
    void shouldUpdatePositionWithinScene() throws Exception {
        DataPanel panel = buildPanel(1L);
        panel.setPosition("{\"x\":10,\"y\":20}");
        when(dataPanelService.updatePosition(1L, 7L, "{\"x\":10,\"y\":20}", null)).thenReturn(panel);

        Map<String, Object> payload = new HashMap<>();
        payload.put("position", "{\"x\":10,\"y\":20}");
        payload.put("sceneId", 7L);
        mockMvc.perform(put("/api/data-panels/1/position")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.position").value("{\"x\":10,\"y\":20}"));
    }

    private DataPanel buildPanel(Long id) {
        DataPanel panel = new DataPanel();
        panel.setId(id);
        panel.setName("panel-" + id);
        panel.setDescription("description");
        panel.setSceneId(7L);
        panel.setStatus(1);
        return panel;
    }
}
