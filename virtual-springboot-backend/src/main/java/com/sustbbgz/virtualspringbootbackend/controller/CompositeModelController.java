package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.entity.CompositeModel;
import com.sustbbgz.virtualspringbootbackend.entity.CompositeModelComponent;
import com.sustbbgz.virtualspringbootbackend.service.CompositeModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/composite-models")
@CrossOrigin(origins = "*") // 添加跨域支持
public class CompositeModelController {

    @Autowired
    private CompositeModelService compositeModelService;

    // 测试接口
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("测试接口被调用");
        return ResponseEntity.ok("测试成功");
    }

    // 创建组合模型
    @PostMapping
    public ResponseEntity<CompositeModel> createCompositeModel(@RequestBody CompositeModel compositeModel) {
        try {
            System.out.println("接收到组合模型创建请求: " + compositeModel);
            System.out.println("接收到的完整数据: " + compositeModel.toString());
            
            // 数据验证
            if (compositeModel.getName() == null || compositeModel.getName().trim().isEmpty()) {
                System.out.println("组合模型名称为空");
                return ResponseEntity.badRequest().build();
            }
            
            // 清理和验证数据
            compositeModel.setName(compositeModel.getName().trim());
            if (compositeModel.getDescription() != null) {
                compositeModel.setDescription(compositeModel.getDescription().trim());
            }
            
            // 检查是否已存在同名模型
            CompositeModel existingModel = compositeModelService.getCompositeModelByName(compositeModel.getName());
            if (existingModel != null) {
                System.out.println("已存在同名组合模型: " + compositeModel.getName());
                return ResponseEntity.status(409).build(); // 409 Conflict
            }
            
            // 验证组件数据
            if (compositeModel.getComponents() != null) {
                System.out.println("组件数量: " + compositeModel.getComponents().size());
                for (CompositeModelComponent component : compositeModel.getComponents()) {
                    System.out.println("组件信息: " + component);
                    System.out.println("组件modelId类型: " + (component.getModelId() != null ? component.getModelId().getClass() : "null"));
                    // 确保所有必需字段都有默认值
                    if (component.getPosition_x() == null) component.setPosition_x(0.0);
                    if (component.getPosition_y() == null) component.setPosition_y(0.0);
                    if (component.getPosition_z() == null) component.setPosition_z(0.0);
                    if (component.getRotation_x() == null) component.setRotation_x(0.0);
                    if (component.getRotation_y() == null) component.setRotation_y(0.0);
                    if (component.getRotation_z() == null) component.setRotation_z(0.0);
                    if (component.getScale_x() == null) component.setScale_x(1.0);
                    if (component.getScale_y() == null) component.setScale_y(1.0);
                    if (component.getScale_z() == null) component.setScale_z(1.0);
                    
                    // 验证modelId
                    if (component.getModelId() == null || component.getModelId().trim().isEmpty()) {
                        System.out.println("组件modelId为空");
                        return ResponseEntity.badRequest().build();
                    }
                    component.setModelId(component.getModelId().trim());
                }
            } else {
                System.out.println("组件数据为null");
            }
            
            System.out.println("开始创建组合模型: " + compositeModel.getName());
            CompositeModel created = compositeModelService.createCompositeModel(compositeModel);
            System.out.println("组合模型创建完成: " + created);
            if (created != null) {
                return ResponseEntity.ok(created);
            } else {
                System.out.println("组合模型创建返回null");
                return ResponseEntity.status(500).build(); // 500 Internal Server Error
            }
        } catch (Exception e) {
            System.err.println("创建组合模型时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // 获取所有组合模型
    @GetMapping
    public ResponseEntity<List<CompositeModel>> getAllCompositeModels() {
        try {
            System.out.println("获取所有组合模型");
            List<CompositeModel> models = compositeModelService.getAllCompositeModelsWithComponents();
            System.out.println("返回组合模型数量: " + models.size());
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            System.err.println("获取组合模型列表时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // 获取单个组合模型
    @GetMapping("/{id}")
    public ResponseEntity<CompositeModel> getCompositeModel(@PathVariable Long id) {
        try {
            System.out.println("获取组合模型，ID: " + id);
            CompositeModel model = compositeModelService.getById(id);
            if (model == null) {
                System.out.println("未找到ID为 " + id + " 的组合模型");
                return ResponseEntity.notFound().build();
            }
            // 加载组件信息
            model.setComponents(compositeModelService.getComponentsByCompositeModelId(id));
            return ResponseEntity.ok(model);
        } catch (Exception e) {
            System.err.println("获取组合模型时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // 更新组合模型
    @PutMapping("/{id}")
    public ResponseEntity<CompositeModel> updateCompositeModel(@PathVariable Long id, @RequestBody CompositeModel compositeModel) {
        try {
            System.out.println("更新组合模型，ID: " + id);
            compositeModel.setId(id);
            CompositeModel updated = compositeModelService.updateCompositeModel(compositeModel);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("更新组合模型时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // 删除组合模型
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompositeModel(@PathVariable Long id) {
        try {
            System.out.println("删除组合模型，ID: " + id);
            compositeModelService.removeById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("删除组合模型时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // 获取组合模型的组件
    @GetMapping("/{id}/components")
    public ResponseEntity<List<CompositeModelComponent>> getComponents(@PathVariable Long id) {
        try {
            System.out.println("获取组合模型组件，ID: " + id);
            List<CompositeModelComponent> components = compositeModelService.getComponentsByCompositeModelId(id);
            return ResponseEntity.ok(components);
        } catch (Exception e) {
            System.err.println("获取组合模型组件时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // 保存组合模型的组件（替换所有现有组件）
    @PostMapping("/{id}/components")
    public ResponseEntity<Void> saveComponents(@PathVariable Long id, @RequestBody List<CompositeModelComponent> components) {
        try {
            System.out.println("保存组合模型组件，ID: " + id + ", 组件数量: " + components.size());
            // 先删除现有的所有组件
            compositeModelService.deleteComponentsByCompositeModelId(id);
            
            // 设置组合模型ID并保存新组件
            for (CompositeModelComponent component : components) {
                component.setCompositeModelId(id);
            }
            compositeModelService.saveComponents(components);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("保存组合模型组件时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}