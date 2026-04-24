package com.sustbbgz.virtualspringbootbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;
import com.sustbbgz.virtualspringbootbackend.service.AlertRuleService;
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
class AlertRuleControllerTest {

    @Mock
    private AlertRuleService alertRuleService;

    @InjectMocks
    private AlertRuleController alertRuleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertRuleController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldListRules() throws Exception {
        when(alertRuleService.findRules(null)).thenReturn(Collections.singletonList(new AlertRule()));

        mockMvc.perform(get("/api/alert-rules"))
                .andExpect(status().isOk());

        verify(alertRuleService).findRules(null);
    }

    @Test
    void shouldCreateRule() throws Exception {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        when(alertRuleService.saveRule(any(AlertRule.class), eq(null))).thenReturn(rule);

        mockMvc.perform(post("/api/alert-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRule())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateRule() throws Exception {
        AlertRule rule = buildRule();
        rule.setId(2L);
        when(alertRuleService.saveRule(any(AlertRule.class), eq(null))).thenReturn(rule);

        mockMvc.perform(put("/api/alert-rules/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRule())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateRuleStatus() throws Exception {
        AlertRule rule = buildRule();
        rule.setId(3L);
        rule.setUserId(1L);
        when(alertRuleService.getById(3L)).thenReturn(rule);
        when(alertRuleService.saveRule(any(AlertRule.class), eq(1L))).thenReturn(rule);

        mockMvc.perform(put("/api/alert-rules/3/status").param("enabled", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    void shouldReturnErrorWhenUpdatingMissingRuleStatus() throws Exception {
        when(alertRuleService.getById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/alert-rules/99/status").param("enabled", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    void shouldDeleteRule() throws Exception {
        mockMvc.perform(delete("/api/alert-rules/4"))
                .andExpect(status().isOk());

        verify(alertRuleService).removeById(4L);
    }

    private AlertRule buildRule() {
        AlertRule rule = new AlertRule();
        rule.setDeviceId(1L);
        rule.setDataType("temperature");
        rule.setOperator(">=");
        rule.setThreshold(80.0);
        rule.setCooldownSeconds(60);
        rule.setEnabled(1);
        rule.setMessageTemplate("temp alert");
        return rule;
    }
}
