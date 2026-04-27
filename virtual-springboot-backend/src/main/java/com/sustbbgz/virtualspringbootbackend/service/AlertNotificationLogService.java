package com.sustbbgz.virtualspringbootbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sustbbgz.virtualspringbootbackend.entity.AlertNotificationLog;
import com.sustbbgz.virtualspringbootbackend.mapper.AlertNotificationLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AlertNotificationLogService extends ServiceImpl<AlertNotificationLogMapper, AlertNotificationLog> {

    public AlertNotificationLog saveLog(AlertNotificationLog log) {
        LocalDateTime now = LocalDateTime.now();
        if (log.getCreateTime() == null) {
            log.setCreateTime(now);
        }
        log.setUpdateTime(now);
        save(log);
        return log;
    }

    public List<AlertNotificationLog> findLogs(Long userId,
                                               Long ruleId,
                                               String deviceCode,
                                               String channel,
                                               String sendStatus,
                                               LocalDateTime startTime,
                                               LocalDateTime endTime,
                                               Integer limit) {
        LambdaQueryWrapper<AlertNotificationLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(AlertNotificationLog::getUserId, userId);
        }
        if (ruleId != null) {
            wrapper.eq(AlertNotificationLog::getRuleId, ruleId);
        }
        if (StringUtils.hasText(deviceCode)) {
            wrapper.eq(AlertNotificationLog::getDeviceCode, deviceCode.trim());
        }
        if (StringUtils.hasText(channel)) {
            wrapper.eq(AlertNotificationLog::getChannel, channel.trim());
        }
        if (StringUtils.hasText(sendStatus)) {
            wrapper.eq(AlertNotificationLog::getSendStatus, sendStatus.trim().toUpperCase());
        }
        if (startTime != null) {
            wrapper.ge(AlertNotificationLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AlertNotificationLog::getCreateTime, endTime);
        }
        wrapper.orderByDesc(AlertNotificationLog::getCreateTime);
        wrapper.last("LIMIT " + normalizeLimit(limit));
        return list(wrapper);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 100;
        }
        return Math.min(limit, 500);
    }
}
