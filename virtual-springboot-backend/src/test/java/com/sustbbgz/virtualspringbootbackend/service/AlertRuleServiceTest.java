package com.sustbbgz.virtualspringbootbackend.service;

import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.mapper.AlertRuleMapper;
import com.sustbbgz.virtualspringbootbackend.service.notifier.AlertNotifier;
import com.sustbbgz.virtualspringbootbackend.websocket.DataPushService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertRuleServiceTest {

    @Spy
    private AlertRuleService alertRuleService;

    @Mock
    private AlertRuleMapper alertRuleMapper;

    @Mock
    private DeviceService deviceService;

    @Mock
    private DataPushService dataPushService;

    @Mock
    private AlertNotifier alertNotifier;

    @Mock
    private AlertNotifier secondaryAlertNotifier;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(alertRuleService, "baseMapper", alertRuleMapper);
        ReflectionTestUtils.setField(alertRuleService, "deviceService", deviceService);
        ReflectionTestUtils.setField(alertRuleService, "dataPushService", dataPushService);
        ReflectionTestUtils.setField(alertRuleService, "alertNotifiers", Collections.singletonList(alertNotifier));
    }

    @Test
    void shouldHydrateDeviceAndDefaultFieldsWhenSaveRule() {
        AlertRule rule = new AlertRule();
        rule.setDeviceId(1L);
        rule.setDataType("temperature");
        rule.setOperator(">=");
        rule.setThreshold(80.0);

        Device device = new Device();
        device.setId(1L);
        device.setDeviceId("DEV-001");
        device.setName("main-device");
        when(deviceService.getById(1L)).thenReturn(device);
        doReturn(true).when(alertRuleService).saveOrUpdate(any(AlertRule.class));
        doReturn(rule).when(alertRuleService).getById(any());

        AlertRule saved = alertRuleService.saveRule(rule, 9L);

        assertEquals("DEV-001", rule.getDeviceCode());
        assertEquals("main-device", rule.getDeviceName());
        assertEquals(1, rule.getEnabled());
        assertEquals(60, rule.getCooldownSeconds());
        assertEquals(9L, rule.getUserId());
        assertNotNull(rule.getCreateTime());
        assertNotNull(rule.getUpdateTime());
        assertEquals(rule, saved);
    }

    @Test
    void shouldTriggerNotificationAndPushAlertWhenRuleMatches() {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        rule.setDeviceCode("DEV-001");
        rule.setDeviceName("main-device");
        rule.setDataType("temperature");
        rule.setOperator(">=");
        rule.setThreshold(80.0);
        rule.setCooldownSeconds(30);
        when(alertRuleMapper.findEnabledRules("DEV-001", "temperature")).thenReturn(Collections.singletonList(rule));
        doReturn(true).when(alertRuleService).updateById(any(AlertRule.class));

        alertRuleService.evaluateRules("DEV-001", "temperature", 82.0);

        verify(alertNotifier).notify(eq(rule), eq(82.0), any(String.class));
        verify(dataPushService).pushAlert(eq("DEV-001"), eq("warning"), any(String.class));
        verify(alertRuleService).updateById(rule);
    }

    @Test
    void shouldNotTriggerWhenRuleDisabledByCooldown() {
        AlertRule rule = new AlertRule();
        rule.setId(2L);
        rule.setDeviceCode("DEV-001");
        rule.setDataType("temperature");
        rule.setOperator(">=");
        rule.setThreshold(80.0);
        rule.setCooldownSeconds(60);
        rule.setLastTriggeredAt(LocalDateTime.now());
        when(alertRuleMapper.findEnabledRules("DEV-001", "temperature")).thenReturn(Collections.singletonList(rule));

        alertRuleService.evaluateRules("DEV-001", "temperature", 90.0);

        verify(alertNotifier, never()).notify(any(), any(), any());
        verify(dataPushService, never()).pushAlert(any(), any(), any());
        verify(alertRuleService, never()).updateById(any(AlertRule.class));
    }

    @Test
    void shouldApplyTemplateVariables() {
        AlertRule rule = new AlertRule();
        rule.setId(3L);
        rule.setDeviceCode("DEV-001");
        rule.setDeviceName("main-device");
        rule.setDataType("temperature");
        rule.setOperator(">=");
        rule.setThreshold(80.0);
        rule.setMessageTemplate("${deviceName}-${dataTypeLabel}-${value}-${operatorLabel}-${threshold}");
        when(alertRuleMapper.findEnabledRules("DEV-001", "temperature")).thenReturn(Collections.singletonList(rule));
        doReturn(true).when(alertRuleService).updateById(any(AlertRule.class));

        alertRuleService.evaluateRules("DEV-001", "temperature", 88.0);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(alertNotifier).notify(eq(rule), eq(88.0), messageCaptor.capture());
        assertEquals("main-device-温度-88.0-大于等于-80.0", messageCaptor.getValue());
    }

    @Test
    void shouldUseChineseDefaultMessageWhenTemplateMissing() {
        AlertRule rule = new AlertRule();
        rule.setId(4L);
        rule.setDeviceCode("DEV-001");
        rule.setDeviceName("温度传感器-1号");
        rule.setDataType("temperature");
        rule.setOperator(">");
        rule.setThreshold(1.0);
        when(alertRuleMapper.findEnabledRules("DEV-001", "temperature")).thenReturn(Collections.singletonList(rule));
        doReturn(true).when(alertRuleService).updateById(any(AlertRule.class));

        alertRuleService.evaluateRules("DEV-001", "temperature", 23.75);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(alertNotifier).notify(eq(rule), eq(23.75), messageCaptor.capture());
        assertEquals("设备 温度传感器-1号 (DEV-001) 的温度当前值 23.75，触发条件 > 1.0", messageCaptor.getValue());
    }

    @Test
    void shouldSupportLessThanComparison() {
        AlertRule rule = new AlertRule();
        rule.setId(5L);
        rule.setDeviceCode("DEV-001");
        rule.setDataType("temperature");
        rule.setOperator("<");
        rule.setThreshold(10.0);
        when(alertRuleMapper.findEnabledRules("DEV-001", "temperature")).thenReturn(Collections.singletonList(rule));
        doReturn(true).when(alertRuleService).updateById(any(AlertRule.class));

        alertRuleService.evaluateRules("DEV-001", "temperature", 9.0);

        verify(alertNotifier).notify(eq(rule), eq(9.0), any(String.class));
    }

    @Test
    void shouldIgnoreNullInputDuringEvaluation() {
        alertRuleService.evaluateRules(null, "temperature", 10.0);

        verify(alertRuleMapper, never()).findEnabledRules(any(), any());
    }

    @Test
    void shouldContinueWhenOneNotifierFails() {
        AlertRule rule = new AlertRule();
        rule.setId(6L);
        rule.setDeviceCode("DEV-001");
        rule.setDataType("temperature");
        rule.setOperator(">=");
        rule.setThreshold(80.0);
        when(alertRuleMapper.findEnabledRules("DEV-001", "temperature")).thenReturn(Collections.singletonList(rule));
        ReflectionTestUtils.setField(alertRuleService, "alertNotifiers", Arrays.asList(alertNotifier, secondaryAlertNotifier));
        doThrow(new RuntimeException("boom")).when(alertNotifier).notify(eq(rule), eq(90.0), any(String.class));
        doReturn(true).when(alertRuleService).updateById(any(AlertRule.class));

        assertDoesNotThrow(() -> alertRuleService.evaluateRules("DEV-001", "temperature", 90.0));

        verify(secondaryAlertNotifier).notify(eq(rule), eq(90.0), any(String.class));
        verify(dataPushService).pushAlert(eq("DEV-001"), eq("warning"), any(String.class));
        verify(alertRuleService).updateById(rule);
    }
}
