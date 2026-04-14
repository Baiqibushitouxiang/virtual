package com.sustbbgz.virtualspringbootbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("data_panel")
public class DataPanel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Long deviceId;

    private String deviceName;

    private String modelId;

    private String modelName;

    private String modelType;

    private String position;

    private String size;

    private String style;

    private Integer status;

    private Long userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Device device;

    @TableField(exist = false)
    private Object modelData;
}
