package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.common.Result;
import com.sustbbgz.virtualspringbootbackend.entity.AlertNotificationLog;
import com.sustbbgz.virtualspringbootbackend.entity.User;
import com.sustbbgz.virtualspringbootbackend.service.AlertNotificationLogService;
import com.sustbbgz.virtualspringbootbackend.utils.TokenUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/alert-notification-logs")
public class AlertNotificationLogController {

    @Resource
    private AlertNotificationLogService alertNotificationLogService;

    @GetMapping
    public Result list(@RequestParam(required = false) Long ruleId,
                       @RequestParam(required = false) String deviceCode,
                       @RequestParam(required = false) String channel,
                       @RequestParam(required = false) String sendStatus,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                       @RequestParam(defaultValue = "100") Integer limit) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        return Result.success(alertNotificationLogService.findLogs(
                userId,
                ruleId,
                deviceCode,
                channel,
                sendStatus,
                startTime,
                endTime,
                limit
        ));
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        AlertNotificationLog log = alertNotificationLogService.getById(id);
        if (log == null) {
            return Result.error("通知日志不存在");
        }
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        if (userId != null && log.getUserId() != null && !userId.equals(log.getUserId())) {
            return Result.error("无权查看该通知日志");
        }
        return Result.success(log);
    }
}
