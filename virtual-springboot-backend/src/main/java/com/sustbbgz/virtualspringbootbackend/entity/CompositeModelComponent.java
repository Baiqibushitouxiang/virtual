package com.sustbbgz.virtualspringbootbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("composite_model_components")
public class CompositeModelComponent {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("composite_model_id")
    private Long compositeModelId;
    
    @TableField("model_id")
    private String modelId;
    
    @TableField("position_x")
    private Double position_x = 0.0;
    
    @TableField("position_y")
    private Double position_y = 0.0;
    
    @TableField("position_z")
    private Double position_z = 0.0;
    
    @TableField("rotation_x")
    private Double rotation_x = 0.0;
    
    @TableField("rotation_y")
    private Double rotation_y = 0.0;
    
    @TableField("rotation_z")
    private Double rotation_z = 0.0;
    
    @TableField("scale_x")
    private Double scale_x = 1.0;
    
    @TableField("scale_y")
    private Double scale_y = 1.0;
    
    @TableField("scale_z")
    private Double scale_z = 1.0;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompositeModelId() {
        return compositeModelId;
    }

    public void setCompositeModelId(Long compositeModelId) {
        this.compositeModelId = compositeModelId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId != null ? modelId.trim() : null;
    }

    public Double getPosition_x() {
        return position_x;
    }

    public void setPosition_x(Double position_x) {
        this.position_x = position_x != null ? position_x : 0.0;
    }

    public Double getPosition_y() {
        return position_y;
    }

    public void setPosition_y(Double position_y) {
        this.position_y = position_y != null ? position_y : 0.0;
    }

    public Double getPosition_z() {
        return position_z;
    }

    public void setPosition_z(Double position_z) {
        this.position_z = position_z != null ? position_z : 0.0;
    }

    public Double getRotation_x() {
        return rotation_x;
    }

    public void setRotation_x(Double rotation_x) {
        this.rotation_x = rotation_x != null ? rotation_x : 0.0;
    }

    public Double getRotation_y() {
        return rotation_y;
    }

    public void setRotation_y(Double rotation_y) {
        this.rotation_y = rotation_y != null ? rotation_y : 0.0;
    }

    public Double getRotation_z() {
        return rotation_z;
    }

    public void setRotation_z(Double rotation_z) {
        this.rotation_z = rotation_z != null ? rotation_z : 0.0;
    }

    public Double getScale_x() {
        return scale_x;
    }

    public void setScale_x(Double scale_x) {
        this.scale_x = scale_x != null ? scale_x : 1.0;
    }

    public Double getScale_y() {
        return scale_y;
    }

    public void setScale_y(Double scale_y) {
        this.scale_y = scale_y != null ? scale_y : 1.0;
    }

    public Double getScale_z() {
        return scale_z;
    }

    public void setScale_z(Double scale_z) {
        this.scale_z = scale_z != null ? scale_z : 1.0;
    }

    @Override
    public String toString() {
        return "CompositeModelComponent{" +
                "id=" + id +
                ", compositeModelId=" + compositeModelId +
                ", modelId='" + modelId + '\'' +
                ", position_x=" + position_x +
                ", position_y=" + position_y +
                ", position_z=" + position_z +
                ", rotation_x=" + rotation_x +
                ", rotation_y=" + rotation_y +
                ", rotation_z=" + rotation_z +
                ", scale_x=" + scale_x +
                ", scale_y=" + scale_y +
                ", scale_z=" + scale_z +
                '}';
    }
}