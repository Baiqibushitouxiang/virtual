package com.sustbbgz.virtualspringbootbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alert_rule")
public class AlertRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private String deviceCode;

    private String deviceName;

    private String dataType;

    private String operator;

    private Double threshold;

    private Integer enabled;

    private String messageTemplate;

    private Integer cooldownSeconds;

    private LocalDateTime lastTriggeredAt;

    private Long userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
