package com.sustbbgz.virtualspringbootbackend.entity;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

public class SceneAsset {
    private Long id;
    private String name;
    private String fileType;
    private String path;
    private String description;
    private LocalDateTime createdAt;
    private String textureInfo; // 存储贴图信息(JSON格式)

    // 默认构造函数
    public SceneAsset() {
    }

    // 带参数的构造函数
    public SceneAsset(Long id, String name, String fileType, String path, String description, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.fileType = fileType;
        this.path = path;
        this.description = description;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getTextureInfo() {
        return textureInfo;
    }
    
    public void setTextureInfo(String textureInfo) {
        this.textureInfo = textureInfo;
    }

    // toString 方法，便于调试和日志输出
    @Override
    public String toString() {
        return "SceneAsset{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fileType='" + fileType + '\'' +
                ", path='" + path + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", textureInfo='" + textureInfo + '\'' +
                '}';
    }
}