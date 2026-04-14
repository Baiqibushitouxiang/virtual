package com.sustbbgz.virtualspringbootbackend.utils;

import com.sustbbgz.virtualspringbootbackend.entity.Model;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class ModelImporter {

    // 模型文件存放的根目录（可以是你的本地路径，也可以是静态资源路径）
    private static final String MODEL_ROOT_DIR = "src/main/resources/static/models/";

    /**
     * 从文件系统读取模型文件并将其转换为 Model 实体对象
     * @return List<Model> 模型实体列表
     */
    public List<Model> importModelsFromFileSystem() {
        List<Model> models = new ArrayList<>();

        // 获取模型根目录下的所有一级分类文件夹
        File rootDir = new File(MODEL_ROOT_DIR);
        File[] categories = rootDir.listFiles(File::isDirectory);

        if (categories != null) {
            for (File category : categories) {
                // 获取分类名称
                String categoryName = category.getName();
                File[] files = category.listFiles((dir, name) -> name.endsWith(".glb") || name.endsWith(".obj")); // 根据文件后缀选择文件

                if (files != null) {
                    for (File file : files) {
                        Model model = new Model();

                        // 获取文件名并去掉扩展名（.glb 或 .obj）
                        String fileName = file.getName();
                        String modelName = fileName.substring(0, fileName.lastIndexOf('.'));  // 去掉扩展名

                        model.setName(modelName);  // 设置模型名称（去掉扩展名）
                        model.setCategory(categoryName);  // 设置分类
                        model.setFilePath("/models/" + categoryName + "/" + fileName);  // 设置文件路径

                        // 这里你可以根据需要设置描述
                        model.setDescription("这是一个自动导入的模型：" + fileName);

                        models.add(model);
                    }
                }
            }
        }

        return models;
    }
}
