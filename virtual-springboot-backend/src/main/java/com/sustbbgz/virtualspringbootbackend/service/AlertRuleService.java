package com.sustbbgz.virtualspringbootbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.mapper.AlertRuleMapper;
import com.sustbbgz.virtualspringbootbackend.service.notifier.AlertNotifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AlertRuleService extends ServiceImpl<AlertRuleMapper, AlertRule> {

    @Resource
    private DeviceService deviceService;

    @Resource
    private AlertNotifier alertNotifier;

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
            alertNotifier.notify(rule, value, message);
            rule.setLastTriggeredAt(LocalDateTime.now());
            rule.setUpdateTime(LocalDateTime.now());
            updateById(rule);
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
            template = "设备 ${deviceName} 的 ${dataType} 当前值 ${value} 触发阈值 ${operator} ${threshold}";
        }
        return template
                .replace("${deviceName}", defaultString(rule.getDeviceName()))
                .replace("${deviceCode}", defaultString(rule.getDeviceCode()))
                .replace("${dataType}", defaultString(rule.getDataType()))
                .replace("${value}", String.valueOf(value))
                .replace("${operator}", defaultString(rule.getOperator()))
                .replace("${threshold}", String.valueOf(rule.getThreshold()));
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
