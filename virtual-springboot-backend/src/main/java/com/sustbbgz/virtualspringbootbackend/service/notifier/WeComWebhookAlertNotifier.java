package com.sustbbgz.virtualspringbootbackend.service.notifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.config.WeComWebhookProperties;
import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "alert.notify.wecom", name = "enabled", havingValue = "true")
public class WeComWebhookAlertNotifier implements AlertNotifier {

    private static final Logger logger = LoggerFactory.getLogger(WeComWebhookAlertNotifier.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WeComWebhookProperties properties;
    private final RestOperations restOperations;
    private final ObjectMapper objectMapper;

    public WeComWebhookAlertNotifier(WeComWebhookProperties properties,
                                     RestTemplateBuilder restTemplateBuilder,
                                     ObjectMapper objectMapper) {
        this(
                properties,
                restTemplateBuilder
                        .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMillis()))
                        .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMillis()))
                        .build(),
                objectMapper
        );
    }

    WeComWebhookAlertNotifier(WeComWebhookProperties properties,
                              RestOperations restOperations,
                              ObjectMapper objectMapper) {
        this.properties = properties;
        this.restOperations = restOperations;
        this.objectMapper = objectMapper;
        validateProperties();
    }

    @Override
    public void notify(AlertRule rule, Double currentValue, String message) {
        try {
            ResponseEntity<String> response = restOperations.postForEntity(
                    properties.getWebhookUrl(),
                    buildRequestEntity(buildPayload(rule, currentValue, message)),
                    String.class
            );
            handleResponse(rule, response);
        } catch (Exception ex) {
            logger.error("Failed to send WeCom alert: ruleId={} webhook={}", rule.getId(), properties.getWebhookUrl(), ex);
        }
    }

    private HttpEntity<Map<String, Object>> buildRequestEntity(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }

    private Map<String, Object> buildPayload(AlertRule rule, Double currentValue, String message) {
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("content", buildContent(rule, currentValue, message));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "text");
        payload.put("text", text);
        return payload;
    }

    private String buildContent(AlertRule rule, Double currentValue, String message) {
        StringBuilder content = new StringBuilder();
        content.append("Device alert").append('\n')
                .append("Rule ID: ").append(defaultString(rule.getId())).append('\n')
                .append("Device Code: ").append(defaultString(rule.getDeviceCode())).append('\n')
                .append("Device Name: ").append(defaultString(rule.getDeviceName())).append('\n')
                .append("Data Type: ").append(defaultString(rule.getDataType())).append('\n')
                .append("Condition: ").append(defaultString(rule.getOperator())).append(' ')
                .append(rule.getThreshold()).append('\n')
                .append("Current Value: ").append(currentValue).append('\n')
                .append("Triggered At: ").append(formatTriggerTime(rule)).append('\n')
                .append("Message: ").append(message);
        return content.toString();
    }

    private String formatTriggerTime(AlertRule rule) {
        if (rule.getLastTriggeredAt() == null) {
            return "";
        }
        return TIME_FORMATTER.format(rule.getLastTriggeredAt());
    }

    private void handleResponse(AlertRule rule, ResponseEntity<String> response) throws Exception {
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.error("WeCom alert returned non-2xx status: ruleId={} status={}", rule.getId(), response.getStatusCodeValue());
            return;
        }
        String body = response.getBody();
        if (body == null || body.trim().isEmpty()) {
            logger.warn("WeCom alert returned empty body: ruleId={}", rule.getId());
            return;
        }
        JsonNode jsonNode = objectMapper.readTree(body);
        int errCode = jsonNode.path("errcode").asInt(-1);
        if (errCode != 0) {
            logger.error("WeCom alert failed: ruleId={} errcode={} errmsg={}",
                    rule.getId(),
                    errCode,
                    jsonNode.path("errmsg").asText());
        }
    }

    private void validateProperties() {
        if (properties.getWebhookUrl() == null || properties.getWebhookUrl().trim().isEmpty()) {
            throw new IllegalStateException("alert.notify.wecom.enabled=true requires alert.notify.wecom.webhook-url");
        }
    }

    private String defaultString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
