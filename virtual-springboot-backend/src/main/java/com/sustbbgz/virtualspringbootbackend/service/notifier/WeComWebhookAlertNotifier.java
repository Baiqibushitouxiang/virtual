package com.sustbbgz.virtualspringbootbackend.service.notifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sustbbgz.virtualspringbootbackend.config.WeComWebhookProperties;
import com.sustbbgz.virtualspringbootbackend.entity.AlertNotificationLog;
import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;
import com.sustbbgz.virtualspringbootbackend.service.AlertNotificationLogService;
import com.sustbbgz.virtualspringbootbackend.websocket.DataPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "alert.notify.wecom", name = "enabled", havingValue = "true")
public class WeComWebhookAlertNotifier implements AlertNotifier {

    private static final Logger logger = LoggerFactory.getLogger(WeComWebhookAlertNotifier.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CHANNEL_NAME = AlertNotificationLog.CHANNEL_WECOM_WEBHOOK;
    private static final String ALERT_TITLE = "\u8bbe\u5907\u544a\u8b66";
    private static final String SEND_TITLE = "\u4f01\u4e1a\u5fae\u4fe1\u53d1\u9001\u4e2d";
    private static final String SEND_SUCCESS_TITLE = "\u4f01\u4e1a\u5fae\u4fe1\u53d1\u9001\u6210\u529f";
    private static final String SEND_FAILED_TITLE = "\u4f01\u4e1a\u5fae\u4fe1\u53d1\u9001\u5931\u8d25";
    private static final String SEND_UNKNOWN_TITLE = "\u4f01\u4e1a\u5fae\u4fe1\u53d1\u9001\u7ed3\u679c\u672a\u77e5";

    private final WeComWebhookProperties properties;
    private final RestOperations restOperations;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    @Lazy
    private DataPushService dataPushService;

    @Autowired(required = false)
    @Lazy
    private AlertNotificationLogService alertNotificationLogService;

    @Autowired
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
        String content = buildContent(rule, currentValue, message);
        Map<String, Object> payload = buildPayload(content);
        AlertNotificationLog logRecord = buildBaseLog(rule, currentValue, content, toJson(payload));

        try {
            logger.info("WeCom alert sending: ruleId={} deviceCode={} webhook={} content={}",
                    rule.getId(),
                    rule.getDeviceCode(),
                    maskWebhookUrl(properties.getWebhookUrl()),
                    abbreviate(content, 160));
            pushSendNotification(rule, SEND_TITLE,
                    "\u89c4\u5219 " + defaultString(rule.getId()) + " \u6b63\u5728\u63a8\u9001\u5230\u4f01\u4e1a\u5fae\u4fe1");

            ResponseEntity<String> response = restOperations.postForEntity(
                    properties.getWebhookUrl(),
                    buildRequestEntity(payload),
                    String.class
            );
            handleResponse(rule, content, response, logRecord);
        } catch (Exception ex) {
            logger.error("WeCom alert send failed: ruleId={} deviceCode={} webhook={}",
                    rule.getId(),
                    rule.getDeviceCode(),
                    maskWebhookUrl(properties.getWebhookUrl()),
                    ex);

            logRecord.setSendStatus(AlertNotificationLog.STATUS_FAILED);
            logRecord.setErrorMessage(abbreviate(defaultString(ex.getMessage()), 500));
            logRecord.setSentAt(LocalDateTime.now());
            persistLog(logRecord);

            pushSendNotification(rule, SEND_FAILED_TITLE, buildFailureNotificationContent(rule, ex.getMessage()));
        }
    }

    private HttpEntity<Map<String, Object>> buildRequestEntity(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }

    private Map<String, Object> buildPayload(String content) {
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("content", content);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "text");
        payload.put("text", text);
        return payload;
    }

    private AlertNotificationLog buildBaseLog(AlertRule rule,
                                              Double currentValue,
                                              String content,
                                              String requestBody) {
        AlertNotificationLog logRecord = new AlertNotificationLog();
        logRecord.setRuleId(rule.getId());
        logRecord.setDeviceId(rule.getDeviceId());
        logRecord.setDeviceCode(rule.getDeviceCode());
        logRecord.setDeviceName(rule.getDeviceName());
        logRecord.setDataType(rule.getDataType());
        logRecord.setChannel(CHANNEL_NAME);
        logRecord.setNotifyTarget(maskWebhookUrl(properties.getWebhookUrl()));
        logRecord.setMessageTitle(ALERT_TITLE);
        logRecord.setMessageContent(content);
        logRecord.setRequestBody(requestBody);
        logRecord.setCurrentValue(currentValue);
        logRecord.setThresholdValue(rule.getThreshold());
        logRecord.setOperator(rule.getOperator());
        logRecord.setTriggeredAt(rule.getLastTriggeredAt());
        logRecord.setUserId(rule.getUserId());
        return logRecord;
    }

    private String buildContent(AlertRule rule, Double currentValue, String message) {
        StringBuilder content = new StringBuilder();
        content.append(ALERT_TITLE).append('\n')
                .append("\u89c4\u5219ID: ").append(defaultString(rule.getId())).append('\n')
                .append("\u8bbe\u5907\u7f16\u7801: ").append(defaultString(rule.getDeviceCode())).append('\n')
                .append("\u8bbe\u5907\u540d\u79f0: ").append(defaultString(rule.getDeviceName())).append('\n')
                .append("\u6570\u636e\u9879: ").append(formatDataTypeLabel(rule.getDataType())).append('\n')
                .append("\u89e6\u53d1\u6761\u4ef6: ").append(formatOperatorLabel(rule.getOperator())).append(' ')
                .append(rule.getThreshold()).append('\n')
                .append("\u5f53\u524d\u503c: ").append(currentValue).append('\n')
                .append("\u89e6\u53d1\u65f6\u95f4: ").append(formatTriggerTime(rule)).append('\n')
                .append("\u544a\u8b66\u5185\u5bb9: ").append(message);
        return content.toString();
    }

    private String formatTriggerTime(AlertRule rule) {
        if (rule.getLastTriggeredAt() == null) {
            return "";
        }
        return TIME_FORMATTER.format(rule.getLastTriggeredAt());
    }

    private void handleResponse(AlertRule rule,
                                String content,
                                ResponseEntity<String> response,
                                AlertNotificationLog logRecord) throws Exception {
        logRecord.setSentAt(LocalDateTime.now());
        logRecord.setHttpStatus(response.getStatusCodeValue());

        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.error("WeCom alert returned non-2xx status: ruleId={} status={} webhook={}",
                    rule.getId(),
                    response.getStatusCodeValue(),
                    maskWebhookUrl(properties.getWebhookUrl()));

            logRecord.setSendStatus(AlertNotificationLog.STATUS_FAILED);
            logRecord.setErrorMessage(abbreviate(
                    "HTTP status " + response.getStatusCodeValue() + " returned by WeCom webhook",
                    500
            ));
            logRecord.setResponseBody(abbreviate(response.getBody(), 4000));
            persistLog(logRecord);

            pushSendNotification(rule, SEND_FAILED_TITLE,
                    "HTTP \u72b6\u6001\u7801 " + response.getStatusCodeValue() +
                            "\uff0c\u89c4\u5219 " + defaultString(rule.getId()) + " \u672a\u53d1\u9001\u6210\u529f");
            return;
        }

        String body = response.getBody();
        logRecord.setResponseBody(abbreviate(body, 4000));
        if (body == null || body.trim().isEmpty()) {
            logger.warn("WeCom alert returned empty body: ruleId={} webhook={}",
                    rule.getId(),
                    maskWebhookUrl(properties.getWebhookUrl()));

            logRecord.setSendStatus(AlertNotificationLog.STATUS_UNKNOWN);
            logRecord.setPlatformMessage(abbreviate("empty response body", 255));
            persistLog(logRecord);

            pushSendNotification(rule, SEND_UNKNOWN_TITLE,
                    "\u4f01\u4e1a\u5fae\u4fe1\u8fd4\u56de\u7a7a\u54cd\u5e94\uff0c\u89c4\u5219 " +
                            defaultString(rule.getId()) + " \u9700\u8981\u4eba\u5de5\u786e\u8ba4");
            return;
        }

        JsonNode jsonNode = objectMapper.readTree(body);
        int errCode = jsonNode.path("errcode").asInt(-1);
        String errMsg = jsonNode.path("errmsg").asText();
        logRecord.setPlatformCode(String.valueOf(errCode));
        logRecord.setPlatformMessage(abbreviate(errMsg, 255));

        if (errCode == 0) {
            logger.info("WeCom alert sent successfully: ruleId={} deviceCode={} webhook={} errmsg={} content={}",
                    rule.getId(),
                    rule.getDeviceCode(),
                    maskWebhookUrl(properties.getWebhookUrl()),
                    errMsg,
                    abbreviate(content, 160));

            logRecord.setSendStatus(AlertNotificationLog.STATUS_SUCCESS);
            persistLog(logRecord);

            pushSendNotification(rule, SEND_SUCCESS_TITLE,
                    "\u89c4\u5219 " + defaultString(rule.getId()) +
                            " \u5df2\u53d1\u9001\uff0c\u4f01\u4e1a\u5fae\u4fe1\u8fd4\u56de: " + defaultString(errMsg));
            return;
        }

        logger.error("WeCom alert failed: ruleId={} deviceCode={} webhook={} errcode={} errmsg={}",
                rule.getId(),
                rule.getDeviceCode(),
                maskWebhookUrl(properties.getWebhookUrl()),
                errCode,
                errMsg);

        logRecord.setSendStatus(AlertNotificationLog.STATUS_FAILED);
        logRecord.setErrorMessage(abbreviate("errcode=" + errCode + ", errmsg=" + defaultString(errMsg), 500));
        persistLog(logRecord);

        pushSendNotification(rule, SEND_FAILED_TITLE,
                "\u89c4\u5219 " + defaultString(rule.getId()) +
                        " \u53d1\u9001\u5931\u8d25\uff0cerrcode=" + errCode +
                        "\uff0cerrmsg=" + defaultString(errMsg));
    }

    private void persistLog(AlertNotificationLog logRecord) {
        if (alertNotificationLogService == null) {
            return;
        }
        try {
            alertNotificationLogService.saveLog(logRecord);
        } catch (Exception ex) {
            logger.error("Failed to persist alert notification log: ruleId={} channel={} deviceCode={}",
                    logRecord.getRuleId(),
                    logRecord.getChannel(),
                    logRecord.getDeviceCode(),
                    ex);
        }
    }

    private void pushSendNotification(AlertRule rule, String title, String content) {
        if (dataPushService == null) {
            return;
        }
        String deviceCode = rule.getDeviceCode();
        if (deviceCode == null || deviceCode.trim().isEmpty()) {
            deviceCode = "system";
        }
        dataPushService.pushNotification(deviceCode, title, content);
    }

    private String buildFailureNotificationContent(AlertRule rule, String reason) {
        return "\u89c4\u5219 " + defaultString(rule.getId()) +
                " \u53d1\u9001\u5f02\u5e38: " + defaultString(reason);
    }

    private void validateProperties() {
        if (properties.getWebhookUrl() == null || properties.getWebhookUrl().trim().isEmpty()) {
            throw new IllegalStateException("alert.notify.wecom.enabled=true requires alert.notify.wecom.webhook-url");
        }
    }

    private String formatDataTypeLabel(String dataType) {
        if (dataType == null) {
            return "";
        }
        switch (dataType) {
            case "temperature":
                return "\u6e29\u5ea6";
            case "humidity":
                return "\u6e7f\u5ea6";
            case "pressure":
                return "\u538b\u529b";
            case "voltage":
                return "\u7535\u538b";
            case "current":
                return "\u7535\u6d41";
            case "power":
                return "\u529f\u7387";
            default:
                return dataType;
        }
    }

    private String formatOperatorLabel(String operator) {
        if (operator == null) {
            return "";
        }
        switch (operator) {
            case ">":
                return "\u5927\u4e8e";
            case ">=":
                return "\u5927\u4e8e\u7b49\u4e8e";
            case "<":
                return "\u5c0f\u4e8e";
            case "<=":
                return "\u5c0f\u4e8e\u7b49\u4e8e";
            default:
                return operator;
        }
    }

    private String maskWebhookUrl(String webhookUrl) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            return "";
        }
        return webhookUrl.replaceAll("key=[^&]+", "key=***");
    }

    private String toJson(Object value) {
        if (value == null) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }

    private String abbreviate(String content, int maxLength) {
        if (content == null) {
            return null;
        }
        if (maxLength <= 0 || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    private String defaultString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
