package com.sustbbgz.virtualspringbootbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("model_data_binding")
public class ModelDataBinding {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sceneId;

    private String modelId;

    private String modelName;

    private Long deviceId;

    private String deviceCode;

    private String deviceName;

    private String dataType;

    private Integer ruleStatus;

    private Long userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Device device;
}
