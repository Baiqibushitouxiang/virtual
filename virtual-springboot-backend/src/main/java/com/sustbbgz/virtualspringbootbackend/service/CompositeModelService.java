package com.sustbbgz.virtualspringbootbackend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sustbbgz.virtualspringbootbackend.entity.CompositeModel;
import com.sustbbgz.virtualspringbootbackend.entity.CompositeModelComponent;
import com.sustbbgz.virtualspringbootbackend.mapper.CompositeModelMapper;
import com.sustbbgz.virtualspringbootbackend.mapper.CompositeModelComponentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompositeModelService extends ServiceImpl<CompositeModelMapper, CompositeModel> {

    @Autowired
    private CompositeModelComponentMapper componentMapper;
    
    @Autowired
    private CompositeModelMapper compositeModelMapper;
    
    @Transactional
    public CompositeModel createCompositeModel(CompositeModel compositeModel) {
        try {
            System.out.println("开始创建组合模型服务: " + compositeModel.getName());
            System.out.println("组合模型详情: " + compositeModel);
            System.out.println("组合模型toString: " + compositeModel.toString());
            
            // 数据验证和清理
            if (compositeModel.getName() == null || compositeModel.getName().trim().isEmpty()) {
                System.out.println("组合模型名称为空或空白");
                return null;
            }
            
            compositeModel.setName(compositeModel.getName().trim());
            if (compositeModel.getDescription() != null) {
                compositeModel.setDescription(compositeModel.getDescription().trim());
            }
            
            // 设置时间戳
            LocalDateTime now = LocalDateTime.now();
            compositeModel.setCreatedAt(now);
            compositeModel.setUpdatedAt(now);
            
            System.out.println("准备保存组合模型到数据库");
            boolean saved = this.save(compositeModel);
            System.out.println("组合模型保存结果: " + saved);
            System.out.println("生成的ID: " + compositeModel.getId());
            
            if (!saved) {
                System.out.println("组合模型保存失败");
                return null;
            }
            
            // 保存组件数据
            if (compositeModel.getComponents() != null && !compositeModel.getComponents().isEmpty()) {
                System.out.println("需要保存的组件数量: " + compositeModel.getComponents().size());
                for (CompositeModelComponent component : compositeModel.getComponents()) {
                    System.out.println("处理组件: " + component);
                    component.setCompositeModelId(compositeModel.getId());
                    System.out.println("设置组件的组合模型ID: " + component.getCompositeModelId());
                    System.out.println("组件modelId: " + component.getModelId() + " 类型: " + (component.getModelId() != null ? component.getModelId().getClass() : "null"));
                    System.out.println("组件位置: " + component.getPosition_x() + ", " + component.getPosition_y() + ", " + component.getPosition_z());
                    System.out.println("组件旋转: " + component.getRotation_x() + ", " + component.getRotation_y() + ", " + component.getRotation_z());
                    System.out.println("组件缩放: " + component.getScale_x() + ", " + component.getScale_y() + ", " + component.getScale_z());
                    
                    // 确保数值字段不为null
                    if (component.getPosition_x() == null) component.setPosition_x(0.0);
                    if (component.getPosition_y() == null) component.setPosition_y(0.0);
                    if (component.getPosition_z() == null) component.setPosition_z(0.0);
                    if (component.getRotation_x() == null) component.setRotation_x(0.0);
                    if (component.getRotation_y() == null) component.setRotation_y(0.0);
                    if (component.getRotation_z() == null) component.setRotation_z(0.0);
                    if (component.getScale_x() == null) component.setScale_x(1.0);
                    if (component.getScale_y() == null) component.setScale_y(1.0);
                    if (component.getScale_z() == null) component.setScale_z(1.0);
                    
                    // 确保modelId不为空
                    if (component.getModelId() == null || component.getModelId().trim().isEmpty()) {
                        System.out.println("组件modelId为空，跳过保存");
                        continue;
                    }
                    component.setModelId(component.getModelId().trim());
                    
                    try {
                        System.out.println("准备保存组件到数据库");
                        int result = componentMapper.insert(component);
                        System.out.println("组件保存结果: " + result);
                    } catch (Exception e) {
                        System.err.println("保存组件时出错: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("保存组件失败: " + e.getMessage(), e);
                    }
                }
            } else {
                System.out.println("没有组件需要保存");
            }
            
            // 重新查询并返回完整的组合模型信息，包括组件
            System.out.println("重新查询组合模型，ID: " + compositeModel.getId());
            CompositeModel result = compositeModelMapper.selectById(compositeModel.getId());
            System.out.println("重新查询结果: " + result);
            if (result != null) {
                System.out.println("查询组件信息，组合模型ID: " + compositeModel.getId());
                result.setComponents(componentMapper.findByCompositeModelId(compositeModel.getId()));
                System.out.println("组件信息查询完成，组件数量: " + (result.getComponents() != null ? result.getComponents().size() : 0));
            }
            return result;
        } catch (Exception e) {
            System.err.println("创建组合模型时出错: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建组合模型失败: " + e.getMessage(), e);
        }
    }

    public CompositeModel updateCompositeModel(CompositeModel compositeModel) {
        System.out.println("更新组合模型: " + compositeModel.getId());
        compositeModel.setUpdatedAt(LocalDateTime.now());
        this.updateById(compositeModel);
        return compositeModel;
    }

    public List<CompositeModelComponent> getComponentsByCompositeModelId(Long compositeModelId) {
        System.out.println("获取组件信息，组合模型ID: " + compositeModelId);
        return componentMapper.findByCompositeModelId(compositeModelId);
    }

    public void saveComponents(List<CompositeModelComponent> components) {
        System.out.println("保存组件列表，组件数量: " + components.size());
        for (CompositeModelComponent component : components) {
            componentMapper.insert(component);
        }
    }

    public void deleteComponentsByCompositeModelId(Long compositeModelId) {
        System.out.println("删除组件，组合模型ID: " + compositeModelId);
        componentMapper.deleteByCompositeModelId(compositeModelId);
    }
    
    // 新增方法：获取所有组合模型及其组件
    public List<CompositeModel> getAllCompositeModelsWithComponents() {
        System.out.println("获取所有组合模型及其组件");
        List<CompositeModel> models = compositeModelMapper.selectAll();
        // 为每个模型加载组件信息
        for (CompositeModel model : models) {
            if (model.getId() != null) {
                model.setComponents(getComponentsByCompositeModelId(model.getId()));
            }
        }
        return models;
    }
    
    // 根据名称获取组合模型
    public CompositeModel getCompositeModelByName(String name) {
        System.out.println("根据名称查询组合模型: " + name);
        return compositeModelMapper.selectByName(name);
    }
}