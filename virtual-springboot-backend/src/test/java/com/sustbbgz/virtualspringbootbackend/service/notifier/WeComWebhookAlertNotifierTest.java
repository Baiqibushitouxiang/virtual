package com.sustbbgz.virtualspringbootbackend.service.notifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.config.WeComWebhookProperties;
import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeComWebhookAlertNotifierTest {

    @Mock
    private RestOperations restOperations;

    @Test
    void shouldSendTextPayloadToWebhook() {
        WeComWebhookProperties properties = createProperties();
        WeComWebhookAlertNotifier notifier = new WeComWebhookAlertNotifier(properties, restOperations, new ObjectMapper());
        AlertRule rule = createRule();
        when(restOperations.postForEntity(eq(properties.getWebhookUrl()), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"errcode\":0,\"errmsg\":\"ok\"}"));

        notifier.notify(rule, 82.5, "temperature exceeded threshold");

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restOperations).postForEntity(eq(properties.getWebhookUrl()), entityCaptor.capture(), eq(String.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) entity.getBody();
        assertEquals("text", payload.get("msgtype"));
        @SuppressWarnings("unchecked")
        Map<String, Object> text = (Map<String, Object>) payload.get("text");
        String content = String.valueOf(text.get("content"));
        assertTrue(content.contains("Device alert"));
        assertTrue(content.contains("DEV-001"));
        assertTrue(content.contains("main-device"));
        assertTrue(content.contains("temperature exceeded threshold"));
    }

    @Test
    void shouldSwallowWebhookErrors() {
        WeComWebhookProperties properties = createProperties();
        WeComWebhookAlertNotifier notifier = new WeComWebhookAlertNotifier(properties, restOperations, new ObjectMapper());
        AlertRule rule = createRule();
        when(restOperations.postForEntity(eq(properties.getWebhookUrl()), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("network down"));

        assertDoesNotThrow(() -> notifier.notify(rule, 82.5, "temperature exceeded threshold"));
    }

    private WeComWebhookProperties createProperties() {
        WeComWebhookProperties properties = new WeComWebhookProperties();
        properties.setEnabled(true);
        properties.setWebhookUrl("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=test-key");
        return properties;
    }

    private AlertRule createRule() {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        rule.setDeviceCode("DEV-001");
        rule.setDeviceName("main-device");
        rule.setDataType("temperature");
        rule.setOperator(">=");
        rule.setThreshold(80.0);
        rule.setLastTriggeredAt(LocalDateTime.of(2026, 4, 26, 15, 0, 0));
        return rule;
    }
}
