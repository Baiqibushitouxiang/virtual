package com.sustbbgz.virtualspringbootbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.entity.ModelDataBinding;
import com.sustbbgz.virtualspringbootbackend.service.ModelDataBindingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ModelDataBindingControllerTest {

    @Mock
    private ModelDataBindingService modelDataBindingService;

    @InjectMocks
    private ModelDataBindingController modelDataBindingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(modelDataBindingController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldListBindingsBySceneId() throws Exception {
        when(modelDataBindingService.findBindings(null, 100L)).thenReturn(Collections.singletonList(new ModelDataBinding()));

        mockMvc.perform(get("/api/model-bindings").param("sceneId", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldListEmptyBindingsBySceneId() throws Exception {
        when(modelDataBindingService.findBindings(null, 101L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/model-bindings").param("sceneId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void shouldCreateBinding() throws Exception {
        ModelDataBinding binding = buildBinding();
        binding.setId(1L);
        when(modelDataBindingService.saveOrUpdateBinding(any(ModelDataBinding.class), eq(null))).thenReturn(binding);

        mockMvc.perform(post("/api/model-bindings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildBinding())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateBinding() throws Exception {
        ModelDataBinding binding = buildBinding();
        binding.setId(2L);
        when(modelDataBindingService.saveOrUpdateBinding(any(ModelDataBinding.class), eq(null))).thenReturn(binding);

        mockMvc.perform(put("/api/model-bindings/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildBinding())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteBindingById() throws Exception {
        mockMvc.perform(delete("/api/model-bindings/3"))
                .andExpect(status().isOk());

        verify(modelDataBindingService).removeById(3L);
    }

    @Test
    void shouldDeleteBindingBySceneAndModel() throws Exception {
        when(modelDataBindingService.deleteBySceneAndModel(11L, "model-1")).thenReturn(buildBinding());

        mockMvc.perform(delete("/api/model-bindings")
                        .param("sceneId", "11")
                        .param("modelId", "model-1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteMissingBindingBySceneAndModel() throws Exception {
        when(modelDataBindingService.deleteBySceneAndModel(11L, "missing-model")).thenReturn(null);

        mockMvc.perform(delete("/api/model-bindings")
                        .param("sceneId", "11")
                        .param("modelId", "missing-model"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    private ModelDataBinding buildBinding() {
        ModelDataBinding binding = new ModelDataBinding();
        binding.setSceneId(11L);
        binding.setModelId("model-1");
        binding.setModelName("水泵模型");
        binding.setDeviceId(1L);
        binding.setDeviceCode("DEV-001");
        binding.setDeviceName("设备1");
        binding.setDataType("temperature");
        binding.setRuleStatus(1);
        return binding;
    }
}
