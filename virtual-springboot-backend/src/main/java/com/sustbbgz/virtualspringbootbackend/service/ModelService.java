package com.sustbbgz.virtualspringbootbackend.service;

import java.util.ArrayList;
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
    private ModelMapper modelMapper;

    @Autowired
    private ResourceUrlService resourceUrlService;

    // 批量插入模型数据
    public void importModels(List<Model> models) {
        // 直接使用 MyBatis-Plus 的 saveBatch 方法来批量插入
        this.saveBatch(models);
    }

    // 获取模型菜单（树形结构）
    public List<ModelCategoryNode> getModelMenu() {
        // 查询所有模型
        List<Model> allModels = listWithUrls();

        // 按照分类进行分组
        Map<String, List<Model>> categoryMap = allModels.stream()
                .collect(Collectors.groupingBy(Model::getCategory));

        // 转换为树形结构
        List<ModelCategoryNode> menu = new ArrayList<>();
        
        for (Map.Entry<String, List<Model>> entry : categoryMap.entrySet()) {
            String categoryPath = entry.getKey();
            List<Model> models = entry.getValue();
            
            // 检查是否是多级分类（包含/）
            if (categoryPath.contains("/")) {
                // 多级分类，如 "水利水务/施工机械"
                String[] pathParts = categoryPath.split("/");
                String mainCategory = pathParts[0];
                String subCategory = pathParts[1];
                
                // 查找或创建主分类节点
                ModelCategoryNode mainCategoryNode = findOrCreateCategoryNode(menu, mainCategory);
                
                // 查找或创建子分类节点
                ModelCategoryNode subCategoryNode = findOrCreateCategoryNode(mainCategoryNode.getChildren(), subCategory);
                
                // 添加模型到子分类
                List<ModelCategoryNode> modelNodes = models.stream().map(model -> {
                    ModelCategoryNode modelNode = new ModelCategoryNode();
                    modelNode.setName(model.getName());
                    modelNode.setFilePath(model.getFilePath());
                    modelNode.setUrl(model.getUrl());
                    modelNode.setChildren(null);
                    return modelNode;
                }).collect(Collectors.toList());
                
                subCategoryNode.setChildren(modelNodes);
                
            } else {
                // 单级分类，如 "水坝"
            ModelCategoryNode categoryNode = new ModelCategoryNode();
                categoryNode.setName(categoryPath);

                // 创建模型子节点
                List<ModelCategoryNode> modelNodes = models.stream().map(model -> {
                ModelCategoryNode modelNode = new ModelCategoryNode();
                modelNode.setName(model.getName());
                modelNode.setFilePath(model.getFilePath());
                modelNode.setUrl(model.getUrl());
                    modelNode.setChildren(null);
                return modelNode;
            }).collect(Collectors.toList());

                categoryNode.setChildren(modelNodes);
            menu.add(categoryNode);
            }
        }
        
        return menu;
    }
    
    /**
     * 在列表中查找或创建分类节点
     */
    private ModelCategoryNode findOrCreateCategoryNode(List<ModelCategoryNode> nodes, String categoryName) {
        // 查找现有节点
        for (ModelCategoryNode node : nodes) {
            if (categoryName.equals(node.getName())) {
                return node;
            }
        }
        
        // 创建新节点
        ModelCategoryNode newNode = new ModelCategoryNode();
        newNode.setName(categoryName);
        newNode.setChildren(new ArrayList<>());
        nodes.add(newNode);
        
        return newNode;
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
            String category = model.getCategory();
            String topCategory = category.contains("/") ? category.substring(0, category.indexOf("/")) : category;
            topCategoryCount.put(topCategory, topCategoryCount.getOrDefault(topCategory, 0) + 1);
        }
        return topCategoryCount;
    }
}
