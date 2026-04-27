package com.sustbbgz.virtualspringbootbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alert_notification_log")
public class AlertNotificationLog {

    public static final String CHANNEL_WECOM_WEBHOOK = "wecom-webhook";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_UNKNOWN = "UNKNOWN";

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ruleId;

    private Long deviceId;

    private String deviceCode;

    private String deviceName;

    private String dataType;

    private String channel;

    private String sendStatus;

    private String notifyTarget;

    private String messageTitle;

    private String messageContent;

    private String requestBody;

    private String responseBody;

    private String errorMessage;

    private Integer httpStatus;

    private String platformCode;

    private String platformMessage;

    private Double currentValue;

    private Double thresholdValue;

    private String operator;

    private LocalDateTime triggeredAt;

    private LocalDateTime sentAt;

    private Long userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
