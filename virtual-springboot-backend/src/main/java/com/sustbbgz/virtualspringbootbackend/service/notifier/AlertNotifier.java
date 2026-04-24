package com.sustbbgz.virtualspringbootbackend.service.notifier;

import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;

public interface AlertNotifier {

    void notify(AlertRule rule, Double currentValue, String message);
}
