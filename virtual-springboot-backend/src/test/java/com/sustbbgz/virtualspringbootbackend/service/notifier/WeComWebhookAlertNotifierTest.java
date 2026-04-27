package com.sustbbgz.virtualspringbootbackend.service.notifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.config.WeComWebhookProperties;
import com.sustbbgz.virtualspringbootbackend.entity.AlertNotificationLog;
import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;
import com.sustbbgz.virtualspringbootbackend.service.AlertNotificationLogService;
import com.sustbbgz.virtualspringbootbackend.websocket.DataPushService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeComWebhookAlertNotifierTest {

    @Mock
    private RestOperations restOperations;

    @Mock
    private DataPushService dataPushService;

    @Mock
    private AlertNotificationLogService alertNotificationLogService;

    @Test
    void shouldSendChineseTextPayloadToWebhookAndPersistSuccessLog() {
        WeComWebhookProperties properties = createProperties();
        WeComWebhookAlertNotifier notifier = new WeComWebhookAlertNotifier(properties, restOperations, new ObjectMapper());
        ReflectionTestUtils.setField(notifier, "dataPushService", dataPushService);
        ReflectionTestUtils.setField(notifier, "alertNotificationLogService", alertNotificationLogService);
        AlertRule rule = createRule();
        when(restOperations.postForEntity(eq(properties.getWebhookUrl()), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"errcode\":0,\"errmsg\":\"ok\"}"));

        notifier.notify(rule, 82.5, "\u8bbe\u5907 \u6e29\u5ea6\u4f20\u611f\u5668-1\u53f7 (DEV-001) \u7684\u6e29\u5ea6\u5f53\u524d\u503c 82.5\uff0c\u89e6\u53d1\u6761\u4ef6 \u5927\u4e8e 80.0");

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restOperations).postForEntity(eq(properties.getWebhookUrl()), entityCaptor.capture(), eq(String.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) entity.getBody();
        assertEquals("text", payload.get("msgtype"));
        @SuppressWarnings("unchecked")
        Map<String, Object> text = (Map<String, Object>) payload.get("text");
        String content = String.valueOf(text.get("content"));
        assertTrue(content.contains("\u8bbe\u5907\u544a\u8b66"));
        assertTrue(content.contains("\u8bbe\u5907\u540d\u79f0: \u6e29\u5ea6\u4f20\u611f\u5668-1\u53f7"));
        assertTrue(content.contains("\u6570\u636e\u9879: \u6e29\u5ea6"));
        assertTrue(content.contains("\u544a\u8b66\u5185\u5bb9: \u8bbe\u5907 \u6e29\u5ea6\u4f20\u611f\u5668-1\u53f7"));

        ArgumentCaptor<AlertNotificationLog> logCaptor = ArgumentCaptor.forClass(AlertNotificationLog.class);
        verify(alertNotificationLogService).saveLog(logCaptor.capture());
        AlertNotificationLog logRecord = logCaptor.getValue();
        assertEquals(AlertNotificationLog.CHANNEL_WECOM_WEBHOOK, logRecord.getChannel());
        assertEquals(AlertNotificationLog.STATUS_SUCCESS, logRecord.getSendStatus());
        assertEquals("DEV-001", logRecord.getDeviceCode());
        assertTrue(logRecord.getNotifyTarget().contains("key=***"));
        assertTrue(logRecord.getRequestBody().contains("\"msgtype\":\"text\""));
        assertTrue(logRecord.getResponseBody().contains("\"errcode\":0"));

        verify(dataPushService, atLeastOnce()).pushNotification(eq("DEV-001"), any(String.class), any(String.class));
    }

    @Test
    void shouldSwallowWebhookErrorsAndPersistFailedLog() {
        WeComWebhookProperties properties = createProperties();
        WeComWebhookAlertNotifier notifier = new WeComWebhookAlertNotifier(properties, restOperations, new ObjectMapper());
        ReflectionTestUtils.setField(notifier, "dataPushService", dataPushService);
        ReflectionTestUtils.setField(notifier, "alertNotificationLogService", alertNotificationLogService);
        AlertRule rule = createRule();
        when(restOperations.postForEntity(eq(properties.getWebhookUrl()), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("network down"));

        assertDoesNotThrow(() -> notifier.notify(rule, 82.5, "\u8bbe\u5907\u544a\u8b66\u6d88\u606f"));

        ArgumentCaptor<AlertNotificationLog> logCaptor = ArgumentCaptor.forClass(AlertNotificationLog.class);
        verify(alertNotificationLogService).saveLog(logCaptor.capture());
        AlertNotificationLog logRecord = logCaptor.getValue();
        assertEquals(AlertNotificationLog.STATUS_FAILED, logRecord.getSendStatus());
        assertTrue(logRecord.getErrorMessage().contains("network down"));
        verify(dataPushService, atLeastOnce()).pushNotification(eq("DEV-001"), any(String.class), any(String.class));
    }

    private WeComWebhookProperties createProperties() {
        WeComWebhookProperties properties = new WeComWebhookProperties();
        properties.setEnabled(true);
        properties.setWebhookUrl("https://example.com/wecom-webhook");
        return properties;
    }

    private AlertRule createRule() {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        rule.setDeviceId(10L);
        rule.setDeviceCode("DEV-001");
        rule.setDeviceName("\u6e29\u5ea6\u4f20\u611f\u5668-1\u53f7");
        rule.setDataType("temperature");
        rule.setOperator(">=");
        rule.setThreshold(80.0);
        rule.setUserId(7L);
        rule.setLastTriggeredAt(LocalDateTime.of(2026, 4, 26, 15, 0, 0));
        return rule;
    }
}
