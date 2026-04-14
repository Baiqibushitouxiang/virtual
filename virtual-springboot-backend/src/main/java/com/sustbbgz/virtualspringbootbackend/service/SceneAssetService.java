package com.sustbbgz.virtualspringbootbackend.service;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sustbbgz.virtualspringbootbackend.entity.SceneAsset;
import com.sustbbgz.virtualspringbootbackend.mapper.SceneAssetMapper;

@Service
public class SceneAssetService {

    private final SceneAssetMapper mapper;

    public SceneAssetService(SceneAssetMapper mapper) {
        this.mapper = mapper;
    }

    public SceneAsset upload(MultipartFile file, String name, String description) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("file is empty");

        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "json";
        if (!ext.matches("json|glb|gltf")) throw new IllegalArgumentException("unsupported type");

        // 外置可写目录: ./data/scenes
        Path storageDir = Paths.get(System.getProperty("user.dir"), "data", "scenes");
        Files.createDirectories(storageDir);

        // 保存文件
        String saved = System.currentTimeMillis() + "-" + UUID.randomUUID() + "." + ext;
        Path dest = storageDir.resolve(saved);
        file.transferTo(dest.toFile());

        // 记录入库（保存可直接访问的URL路径，由 StaticResourceConfig 暴露）
        SceneAsset a = new SceneAsset();
        a.setId(System.currentTimeMillis()); // 简单ID，生产建议雪花
        a.setName(name != null && !name.isEmpty() ? name : (original != null ? original.replaceFirst("\\.[^.]+$", "") : saved));
        a.setFileType(ext);
        a.setPath("/scenes/" + saved);
        a.setDescription(description);
        a.setTextureInfo(null); // 默认textureInfo为null
        mapper.insert(a);
        return a;
    }

    public List<SceneAsset> list() { return mapper.list(); }

    public SceneAsset get(Long id) { return mapper.getById(id); }

    public void delete(Long id) throws Exception {
        SceneAsset a = mapper.getById(id);
        if (a != null) {
            // a.getPath() 形如 /scenes/xxxx.ext
            String filename = a.getPath().replaceFirst("^/scenes/", "");
            Path storageDir = Paths.get(System.getProperty("user.dir"), "data", "scenes");
            Path target = storageDir.resolve(filename);
            File f = target.toFile();
            if (f.exists()) Files.delete(target);
            mapper.deleteById(id);
        }
    }
    
    public void updateTextureInfo(Long id, String textureInfo) {
        mapper.updateTextureInfo(id, textureInfo);
    }
}