package com.sustbbgz.virtualspringbootbackend.entity;

import java.util.List;

public class ModelCategoryNode {
    private String id;
    private String name;
    private String type;
    private Integer modelId;
    private String category;
    private String filePath;
    private String description;
    private String url;
    private Integer modelCount;
    private List<ModelCategoryNode> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getModelCount() {
        return modelCount;
    }

    public void setModelCount(Integer modelCount) {
        this.modelCount = modelCount;
    }

    public List<ModelCategoryNode> getChildren() {
        return children;
    }

    public void setChildren(List<ModelCategoryNode> children) {
        this.children = children;
    }
}
