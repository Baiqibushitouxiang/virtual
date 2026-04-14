package com.sustbbgz.virtualspringbootbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("device_data")
public class DeviceData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String deviceId;
    
    private String dataType;
    
    private Double value;
    
    private String unit;
    
    private String metadata;
    
    private LocalDateTime recordedAt;
    
    private LocalDateTime createdAt;
}
