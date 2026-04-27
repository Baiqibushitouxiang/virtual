package com.sustbbgz.virtualspringbootbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.mapper.AlertRuleMapper;
import com.sustbbgz.virtualspringbootbackend.service.notifier.AlertNotifier;
import com.sustbbgz.virtualspringbootbackend.websocket.DataPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class AlertRuleService extends ServiceImpl<AlertRuleMapper, AlertRule> {

    private static final Logger logger = LoggerFactory.getLogger(AlertRuleService.class);

    @Resource
    private DeviceService deviceService;

    @Autowired(required = false)
    private List<AlertNotifier> alertNotifiers = Collections.emptyList();

    @Autowired(required = false)
    @Lazy
    private DataPushService dataPushService;

    public List<AlertRule> findRules(Long userId) {
        if (userId == null) {
            return list(new LambdaQueryWrapper<AlertRule>().orderByDesc(AlertRule::getUpdateTime));
        }
        return list(new LambdaQueryWrapper<AlertRule>()
                .eq(AlertRule::getUserId, userId)
                .orderByDesc(AlertRule::getUpdateTime));
    }

    public AlertRule saveRule(AlertRule rule, Long userId) {
        if (rule.getUserId() == null) {
            rule.setUserId(userId != null ? userId : 1L);
        }
        hydrateDevice(rule);
        if (rule.getEnabled() == null) {
            rule.setEnabled(1);
        }
        if (rule.getCooldownSeconds() == null) {
            rule.setCooldownSeconds(60);
        }
        if (rule.getCreateTime() == null) {
            rule.setCreateTime(LocalDateTime.now());
        }
        rule.setUpdateTime(LocalDateTime.now());
        saveOrUpdate(rule);
        return getById(rule.getId());
    }

    public void evaluateRules(String deviceCode, String dataType, Double value) {
        if (deviceCode == null || dataType == null || value == null) {
            return;
        }
        List<AlertRule> rules = baseMapper.findEnabledRules(deviceCode, dataType);
        for (AlertRule rule : rules) {
            if (!matches(rule.getOperator(), value, rule.getThreshold())) {
                continue;
            }
            if (isInCooldown(rule)) {
                continue;
            }

            String message = buildMessage(rule, value);
            LocalDateTime triggeredAt = LocalDateTime.now();
            rule.setLastTriggeredAt(triggeredAt);
            rule.setUpdateTime(triggeredAt);

            logger.info("Alert triggered: ruleId={} deviceCode={} dataType={} currentValue={} message={}",
                    rule.getId(),
                    rule.getDeviceCode(),
                    rule.getDataType(),
                    value,
                    message);

            pushAlertRecord(rule, message);
            notifyAlert(rule, value, message);
            updateById(rule);
        }
    }

    private void pushAlertRecord(AlertRule rule, String message) {
        if (dataPushService == null || rule.getDeviceCode() == null || rule.getDeviceCode().trim().isEmpty()) {
            return;
        }
        dataPushService.pushAlert(rule.getDeviceCode(), "warning", message);
    }

    private void notifyAlert(AlertRule rule, Double value, String message) {
        for (AlertNotifier alertNotifier : alertNotifiers) {
            try {
                alertNotifier.notify(rule, value, message);
            } catch (Exception ex) {
                logger.error("Alert notification failed: notifier={} ruleId={}",
                        alertNotifier.getClass().getSimpleName(),
                        rule.getId(),
                        ex);
            }
        }
    }

    private boolean isInCooldown(AlertRule rule) {
        if (rule.getCooldownSeconds() == null || rule.getCooldownSeconds() <= 0 || rule.getLastTriggeredAt() == null) {
            return false;
        }
        return rule.getLastTriggeredAt().plusSeconds(rule.getCooldownSeconds()).isAfter(LocalDateTime.now());
    }

    private boolean matches(String operator, Double value, Double threshold) {
        if (operator == null || threshold == null || value == null) {
            return false;
        }
        switch (operator) {
            case ">":
                return value > threshold;
            case ">=":
                return value >= threshold;
            case "<":
                return value < threshold;
            case "<=":
                return value <= threshold;
            default:
                return false;
        }
    }

    private String buildMessage(AlertRule rule, Double value) {
        String template = rule.getMessageTemplate();
        if (template == null || template.trim().isEmpty()) {
            template = "设备 ${deviceName} (${deviceCode}) 的${dataTypeLabel}当前值 ${value}，触发条件 ${operator} ${threshold}";
        }
        return template
                .replace("${deviceName}", defaultString(rule.getDeviceName()))
                .replace("${deviceCode}", defaultString(rule.getDeviceCode()))
                .replace("${dataType}", defaultString(rule.getDataType()))
                .replace("${dataTypeLabel}", formatDataTypeLabel(rule.getDataType()))
                .replace("${value}", String.valueOf(value))
                .replace("${operator}", defaultString(rule.getOperator()))
                .replace("${operatorLabel}", formatOperatorLabel(rule.getOperator()))
                .replace("${threshold}", String.valueOf(rule.getThreshold()));
    }

    private String formatDataTypeLabel(String dataType) {
        if (dataType == null) {
            return "";
        }
        switch (dataType) {
            case "temperature":
                return "温度";
            case "humidity":
                return "湿度";
            case "pressure":
                return "压力";
            case "voltage":
                return "电压";
            case "current":
                return "电流";
            case "power":
                return "功率";
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
                return "大于";
            case ">=":
                return "大于等于";
            case "<":
                return "小于";
            case "<=":
                return "小于等于";
            default:
                return operator;
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private void hydrateDevice(AlertRule rule) {
        if (rule.getDeviceId() == null) {
            return;
        }
        Device device = deviceService.getById(rule.getDeviceId());
        if (device != null) {
            rule.setDeviceCode(device.getDeviceId());
            rule.setDeviceName(device.getName());
        }
    }
}
