package com.sustbbgz.virtualspringbootbackend.service;

import com.sustbbgz.virtualspringbootbackend.entity.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MigrationService {

    private static final Logger logger = LoggerFactory.getLogger(MigrationService.class);

    @Autowired
    private CosService cosService;

    @Autowired
    private ModelService modelService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private volatile boolean isMigrating = false;
    private final AtomicInteger migratedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private volatile String lastError = null;

    private static final String ABSOLUTE_MODELS_PATH = "D:\\CODE\\GMS_new\\数字孪生\\数字孪生_\\数字孪生\\virtual-springboot-backend\\src\\main\\resources\\static\\models";
    private static final String ABSOLUTE_SCENES_PATH = "D:\\CODE\\GMS_new\\数字孪生\\数字孪生_\\数字孪生\\virtual-springboot-backend\\data\\scenes";

    public Map<String, Object> migrateAllToCos() {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> modelsResult = migrateLocalModelsToCos();
        Map<String, Object> scenesResult = migrateScenesToCos();
        
        boolean modelsSuccess = Boolean.TRUE.equals(modelsResult.get("success"));
        boolean scenesSuccess = Boolean.TRUE.equals(scenesResult.get("success"));
        
        result.put("success", modelsSuccess && scenesSuccess);
        result.put("models", modelsResult);
        result.put("scenes", scenesResult);
        
        return result;
    }

    public Map<String, Object> migrateLocalModelsToCos() {
        Map<String, Object> result = new HashMap<>();

        if (isMigrating) {
            result.put("success", false);
            result.put("message", "迁移任务正在进行中，请稍后再试");
            return result;
        }

        isMigrating = true;
        migratedCount.set(0);
        failedCount.set(0);
        lastError = null;

        List<String> successFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        try {
            Path modelsPath = resolveModelsPath();
            if (modelsPath == null || !Files.exists(modelsPath)) {
                result.put("success", false);
                result.put("message", "模型目录不存在");
                isMigrating = false;
                return result;
            }

            logger.info("使用模型目录: {}", modelsPath.toAbsolutePath());

            List<File> modelFiles;
            try (Stream<Path> paths = Files.walk(modelsPath)) {
                modelFiles = paths
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .collect(Collectors.toList());
            }

            int totalFiles = modelFiles.size();
            logger.info("开始迁移模型，共发现 {} 个文件", totalFiles);

            for (File file : modelFiles) {
                try {
                    String relativePath = getRelativePath(modelsPath.toFile(), file);
                    String cosUrl = cosService.uploadLocalFile(file, "models/" + relativePath);
                    
                    if (cosUrl != null) {
                        successFiles.add(relativePath);
                        migratedCount.incrementAndGet();
                        
                        updateModelFilePath(relativePath, cosUrl);
                        
                        logger.info("迁移成功 ({}/{}): {}", migratedCount.get(), totalFiles, relativePath);
                    }
                } catch (Exception e) {
                    String relativePath = getRelativePath(modelsPath.toFile(), file);
                    failedFiles.add(relativePath + " - " + e.getMessage());
                    failedCount.incrementAndGet();
                    logger.error("迁移失败: {} - {}", file.getName(), e.getMessage());
                }
            }

            result.put("success", true);
            result.put("message", "模型迁移完成");
            result.put("totalFiles", totalFiles);
            result.put("migratedCount", migratedCount.get());
            result.put("failedCount", failedCount.get());
            result.put("successFiles", successFiles);
            result.put("failedFiles", failedFiles);

        } catch (Exception e) {
            lastError = e.getMessage();
            result.put("success", false);
            result.put("message", "迁移过程中发生错误: " + e.getMessage());
            logger.error("迁移过程出错", e);
        } finally {
            isMigrating = false;
        }

        return result;
    }

    public Map<String, Object> migrateScenesToCos() {
        Map<String, Object> result = new HashMap<>();

        List<String> successFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();
        int migratedSceneCount = 0;
        int failedSceneCount = 0;

        try {
            Path scenesPath = Paths.get(ABSOLUTE_SCENES_PATH);
            if (!Files.exists(scenesPath)) {
                result.put("success", true);
                result.put("message", "场景目录不存在，跳过");
                result.put("migratedCount", 0);
                return result;
            }

            logger.info("使用场景目录: {}", scenesPath.toAbsolutePath());

            List<File> sceneFiles;
            try (Stream<Path> paths = Files.walk(scenesPath)) {
                sceneFiles = paths
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .collect(Collectors.toList());
            }

            int totalFiles = sceneFiles.size();
            logger.info("开始迁移场景，共发现 {} 个文件", totalFiles);

            for (File file : sceneFiles) {
                try {
                    String relativePath = getRelativePath(scenesPath.toFile(), file);
                    String cosUrl = cosService.uploadLocalFile(file, "scenes/" + relativePath);
                    
                    if (cosUrl != null) {
                        successFiles.add(relativePath);
                        migratedSceneCount++;
                        logger.info("场景迁移成功: {}", relativePath);
                    }
                } catch (Exception e) {
                    String relativePath = getRelativePath(scenesPath.toFile(), file);
                    failedFiles.add(relativePath + " - " + e.getMessage());
                    failedSceneCount++;
                    logger.error("场景迁移失败: {} - {}", file.getName(), e.getMessage());
                }
            }

            result.put("success", true);
            result.put("message", "场景迁移完成");
            result.put("totalFiles", totalFiles);
            result.put("migratedCount", migratedSceneCount);
            result.put("failedCount", failedSceneCount);
            result.put("successFiles", successFiles);
            result.put("failedFiles", failedFiles);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "场景迁移过程中发生错误: " + e.getMessage());
            logger.error("场景迁移过程出错", e);
        }

        return result;
    }

    private Path resolveModelsPath() {
        Path absolutePath = Paths.get(ABSOLUTE_MODELS_PATH);
        if (Files.exists(absolutePath)) {
            logger.info("使用绝对路径: {}", absolutePath.toAbsolutePath());
            return absolutePath;
        }

        Path userDir = Paths.get(System.getProperty("user.dir"));
        logger.info("当前工作目录: {}", userDir.toAbsolutePath());

        String[] possiblePaths = {
            uploadDir,
            "./src/main/resources/static/models",
            "src/main/resources/static/models"
        };

        for (String path : possiblePaths) {
            Path p = Paths.get(path);
            if (Files.exists(p)) {
                return p;
            }
        }

        for (String path : possiblePaths) {
            Path p = userDir.resolve(path).normalize();
            if (Files.exists(p)) {
                return p;
            }
        }

        return null;
    }

    private String getRelativePath(File baseDir, File file) {
        String basePath = baseDir.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        return filePath.substring(basePath.length() + 1).replace("\\", "/");
    }

    private void updateModelFilePath(String relativePath, String cosUrl) {
        try {
            String fileName = relativePath;
            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }
            if (fileName.contains(".")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }

            Model model = modelService.getByName(fileName);
            if (model != null) {
                model.setFilePath(cosUrl);
                modelService.updateById(model);
                logger.info("更新模型路径: {} -> {}", fileName, cosUrl);
            }
        } catch (Exception e) {
            logger.warn("更新模型路径失败: {}", e.getMessage());
        }
    }

    public Map<String, Object> getMigrationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isMigrating", isMigrating);
        status.put("migratedCount", migratedCount.get());
        status.put("failedCount", failedCount.get());
        status.put("lastError", lastError);
        return status;
    }
}
