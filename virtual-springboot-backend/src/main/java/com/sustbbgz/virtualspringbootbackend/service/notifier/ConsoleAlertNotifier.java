package com.sustbbgz.virtualspringbootbackend.service.notifier;

import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ConsoleAlertNotifier implements AlertNotifier {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleAlertNotifier.class);

    @Override
    public void notify(AlertRule rule, Double currentValue, String message) {
        logger.warn(
                "ALERT ruleId={} deviceCode={} deviceName={} dataType={} operator={} threshold={} currentValue={} triggeredAt={} message={}",
                rule.getId(),
                rule.getDeviceCode(),
                rule.getDeviceName(),
                rule.getDataType(),
                rule.getOperator(),
                rule.getThreshold(),
                currentValue,
                LocalDateTime.now(),
                message
        );
    }
}
