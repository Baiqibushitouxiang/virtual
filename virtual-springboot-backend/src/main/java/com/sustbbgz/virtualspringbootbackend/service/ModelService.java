package com.sustbbgz.virtualspringbootbackend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sustbbgz.virtualspringbootbackend.entity.Model;
import com.sustbbgz.virtualspringbootbackend.entity.ModelCategoryNode;
import com.sustbbgz.virtualspringbootbackend.mapper.ModelMapper;

@Service
public class ModelService extends ServiceImpl<ModelMapper, Model> {

    @Autowired
    private ResourceUrlService resourceUrlService;

    public void importModels(List<Model> models) {
        this.saveBatch(models);
    }

    public List<ModelCategoryNode> getModelMenu() {
        List<Model> allModels = listWithUrls();
        List<ModelCategoryNode> roots = new ArrayList<>();
        Map<String, ModelCategoryNode> categoryNodeMap = new HashMap<>();

        for (Model model : allModels) {
            String category = normalizeCategory(model.getCategory());
            String[] categoryParts = category.split("/");
            List<ModelCategoryNode> currentLevel = roots;
            StringBuilder pathBuilder = new StringBuilder();
            ModelCategoryNode parentCategory = null;

            for (String rawPart : categoryParts) {
                String part = rawPart.trim();
                if (part.isEmpty()) {
                    continue;
                }

                if (pathBuilder.length() > 0) {
                    pathBuilder.append("/");
                }
                pathBuilder.append(part);

                String categoryPath = pathBuilder.toString();
                ModelCategoryNode categoryNode = categoryNodeMap.get(categoryPath);
                if (categoryNode == null) {
                    categoryNode = new ModelCategoryNode();
                    categoryNode.setId("category:" + categoryPath);
                    categoryNode.setName(part);
                    categoryNode.setType("category");
                    categoryNode.setCategory(categoryPath);
                    categoryNode.setModelCount(0);
                    categoryNode.setChildren(new ArrayList<>());
                    categoryNodeMap.put(categoryPath, categoryNode);
                    currentLevel.add(categoryNode);
                }

                categoryNode.setModelCount(categoryNode.getModelCount() + 1);
                parentCategory = categoryNode;
                currentLevel = categoryNode.getChildren();
            }

            if (parentCategory == null) {
                parentCategory = findOrCreateUncategorizedNode(roots, categoryNodeMap);
            }

            parentCategory.getChildren().add(createModelNode(model));
        }

        sortNodes(roots);
        return roots;
    }

    private ModelCategoryNode createModelNode(Model model) {
        ModelCategoryNode modelNode = new ModelCategoryNode();
        modelNode.setId("model:" + model.getId());
        modelNode.setName(model.getName());
        modelNode.setType("model");
        modelNode.setModelId(model.getId());
        modelNode.setCategory(model.getCategory());
        modelNode.setFilePath(model.getFilePath());
        modelNode.setDescription(model.getDescription());
        modelNode.setUrl(model.getUrl());
        modelNode.setModelCount(1);
        modelNode.setChildren(null);
        return modelNode;
    }

    private ModelCategoryNode findOrCreateUncategorizedNode(List<ModelCategoryNode> roots, Map<String, ModelCategoryNode> categoryNodeMap) {
        String categoryPath = "未分类";
        ModelCategoryNode categoryNode = categoryNodeMap.get(categoryPath);
        if (categoryNode == null) {
            categoryNode = new ModelCategoryNode();
            categoryNode.setId("category:" + categoryPath);
            categoryNode.setName(categoryPath);
            categoryNode.setType("category");
            categoryNode.setCategory(categoryPath);
            categoryNode.setModelCount(0);
            categoryNode.setChildren(new ArrayList<>());
            categoryNodeMap.put(categoryPath, categoryNode);
            roots.add(categoryNode);
        }
        categoryNode.setModelCount(categoryNode.getModelCount() + 1);
        return categoryNode;
    }

    private String normalizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "";
        }
        return category.replace("\\", "/").replaceAll("/{2,}", "/").replaceAll("^/|/$", "");
    }

    private void sortNodes(List<ModelCategoryNode> nodes) {
        nodes.sort(Comparator
                .comparing((ModelCategoryNode node) -> "model".equals(node.getType()) ? 1 : 0)
                .thenComparing(ModelCategoryNode::getName, String.CASE_INSENSITIVE_ORDER));

        for (ModelCategoryNode node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortNodes(node.getChildren());
            }
        }
    }

    public Model getByName(String name) {
        return enrich(this.lambdaQuery().eq(Model::getName, name).one());
    }

    public Model getById(Long id) {
        return enrich(this.lambdaQuery().eq(Model::getId, id).one());
    }

    public List<Model> listWithUrls() {
        return this.list().stream().map(this::enrich).collect(Collectors.toList());
    }

    public Model enrich(Model model) {
        if (model != null) {
            model.setUrl(resourceUrlService.buildModelUrl(model.getFilePath()));
        }
        return model;
    }

    public Map<String, Integer> getTopCategoryModelCount() {
        List<Model> allModels = this.list();
        Map<String, Integer> topCategoryCount = new HashMap<>();
        for (Model model : allModels) {
            String category = normalizeCategory(model.getCategory());
            String topCategory = category.contains("/") ? category.substring(0, category.indexOf("/")) : category;
            if (topCategory.isEmpty()) {
                topCategory = "未分类";
            }
            topCategoryCount.put(topCategory, topCategoryCount.getOrDefault(topCategory, 0) + 1);
        }
        return topCategoryCount;
    }
}
