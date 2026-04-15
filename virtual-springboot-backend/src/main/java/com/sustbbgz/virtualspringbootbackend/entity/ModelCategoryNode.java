package com.sustbbgz.virtualspringbootbackend.entity;

import java.util.List;

public class ModelCategoryNode {
    private String name;  // 分类或模型名称
    private String filePath;  // 模型文件路径
    private String url;  // 后端生成的模型访问地址
    private List<ModelCategoryNode> children;  // 子节点（可以是模型或子分类）

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<ModelCategoryNode> getChildren() {
        return children;
    }

    public void setChildren(List<ModelCategoryNode> children) {
        this.children = children;
    }
}
