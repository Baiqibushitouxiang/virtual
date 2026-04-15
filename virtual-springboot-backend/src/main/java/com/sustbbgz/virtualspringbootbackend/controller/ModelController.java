package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.entity.Model;
import com.sustbbgz.virtualspringbootbackend.entity.ModelCategoryNode;
import com.sustbbgz.virtualspringbootbackend.service.FileStorageService;
import com.sustbbgz.virtualspringbootbackend.service.ModelService;
import com.sustbbgz.virtualspringbootbackend.utils.ModelImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/models")
public class  ModelController {

    @Autowired
    private ModelService modelService;

    @Autowired
    private ModelImporter modelImportUtil;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${storage.type:local}")
    private String storageType;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam String category,
                                         @RequestParam String name,
                                         @RequestParam("modelFile") MultipartFile modelFile) {
        Map<String, Object> result = new HashMap<>();
        
        if (modelFile.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件为空，上传失败！");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        try {
            String originalFileName = modelFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            
            if (fileExtension.isEmpty()) {
                fileExtension = ".glb";
            }
            
            String fileName = StringUtils.cleanPath(name + fileExtension);
            String relativePath = normalizeRelativePath(category + "/" + fileName);
            String fileUrl;

            if ("cos".equalsIgnoreCase(storageType)) {
                fileUrl = fileStorageService.uploadModelWithRelativePath(modelFile, relativePath);
                result.put("storageType", "cos");
            } else {
                Path targetLocation = Paths.get(uploadDir, relativePath);
                File targetDir = targetLocation.getParent().toFile();
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                Files.copy(modelFile.getInputStream(), targetLocation);
                fileUrl = relativePath;
                result.put("storageType", "local");
            }

            result.put("success", true);
            result.put("message", "文件上传成功");
            result.put("url", fileUrl);
            result.put("relativePath", relativePath);
            result.put("fileName", fileName);
            return ResponseEntity.ok(result);
        } catch (IOException ex) {
            ex.printStackTrace();
            result.put("success", false);
            result.put("message", "文件上传失败，服务器错误：" + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    // 批量导入模型接口（调用工具类方法导入）
    @PostMapping("/import")
    public String importModels() {
        try {
            // 调用工具类获取模型数据
            List<Model> models = modelImportUtil.importModelsFromFileSystem();
            // 调用 ModelService 批量导入模型
            modelService.importModels(models);
            return "模型文件批量导入成功！";
        } catch (Exception e) {
            e.printStackTrace();
            return "模型文件批量导入失败！";
        }
    }

    // 获取模型菜单（树形结构）
    @GetMapping("/menu")
    public List<ModelCategoryNode> getModelMenu() {
        return modelService.getModelMenu();
    }

    // 获取所有模型列表
    @GetMapping("/")
    public List<Model> getAllModels() {
        return modelService.listWithUrls();  // 使用 MyBatis-Plus 获取所有模型
    }

    // 获取单个模型详细信息
    @GetMapping("/{id}")
    public Model getModelById(@PathVariable Long id) {
        Optional<Model> model = Optional.ofNullable(modelService.getById(id));  // 获取模型
        return model.orElseThrow(() -> new RuntimeException("模型未找到"));  // 如果模型不存在，则抛出异常
    }

    // 根据模型名称获取模型信息
    @GetMapping("/name/{name}")
    public Model getModelByName(@PathVariable String name) {
        return modelService.getByName(name);
    }

    // 获取一级目录下的模型总数
    @GetMapping("/categoryCount")
    public Map<String, Integer> getTopCategoryModelCount() {
        return modelService.getTopCategoryModelCount();
    }

    // 增加模型（Create）
    @PostMapping("/")
    public String addModel(@RequestBody Model model) {
        try {
            boolean isSaved = modelService.save(model);  // 使用 MyBatis-Plus 保存模型
            if (isSaved) {
                return "模型创建成功！";
            } else {
                return "模型创建失败！";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "模型创建失败，发生异常！";
        }
    }

    // 更新模型（Update）
    @PutMapping("/{id}")
    public boolean updateModel(@PathVariable Integer id, @RequestBody Model model) {
        try {
            model.setId(id);  // 设置传入模型的 ID，确保更新指定的模型
            boolean isUpdated = modelService.updateById(model);  // 使用 MyBatis-Plus 更新模型
            if (isUpdated) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除模型（Delete）
    @DeleteMapping("/{id}")
    public String deleteModel(@PathVariable Long id) {
        try {
            boolean isDeleted = modelService.removeById(id);  // 使用 MyBatis-Plus 删除模型
            if (isDeleted) {
                return "模型删除成功！";
            } else {
                return "模型删除失败！";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "模型删除失败，发生异常！";
        }
    }

    private String normalizeRelativePath(String path) {
        return path.replace("\\", "/")
                .replaceAll("/{2,}", "/")
                .replaceFirst("^/+", "");
    }
}
