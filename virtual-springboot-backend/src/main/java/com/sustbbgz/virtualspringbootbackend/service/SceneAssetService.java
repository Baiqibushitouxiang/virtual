package com.sustbbgz.virtualspringbootbackend.service;

import com.sustbbgz.virtualspringbootbackend.entity.SceneAsset;
import com.sustbbgz.virtualspringbootbackend.mapper.SceneAssetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class SceneAssetService {

    private final SceneAssetMapper mapper;
    private final CosService cosService;
    private final ResourceUrlService resourceUrlService;
    private final DataPanelService dataPanelService;

    @Value("${storage.type:local}")
    private String storageType;

    public SceneAssetService(SceneAssetMapper mapper, CosService cosService, ResourceUrlService resourceUrlService, DataPanelService dataPanelService) {
        this.mapper = mapper;
        this.cosService = cosService;
        this.resourceUrlService = resourceUrlService;
        this.dataPanelService = dataPanelService;
    }

    public SceneAsset upload(MultipartFile file, String name, String description) throws Exception {
        return upload(file, name, description, null);
    }

    public SceneAsset upload(MultipartFile file, String name, String description, Long id) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }

        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
                : "json";
        if (!ext.matches("json|glb|gltf")) {
            throw new IllegalArgumentException("unsupported type");
        }

        SceneAsset existing = id != null ? mapper.getById(id) : null;
        Long assetId = existing != null ? existing.getId() : (id != null ? id : System.currentTimeMillis());
        String saved = resolveSavedFilename(existing, assetId, ext);
        saved = cleanSceneFilename(saved);
        String path = "scenes/" + saved;

        if ("cos".equalsIgnoreCase(storageType)) {
            cosService.uploadFileToKey(file, "scenes/" + saved);
        } else {
            Path storageDir = Paths.get(System.getProperty("user.dir"), "data", "scenes");
            Files.createDirectories(storageDir);
            Path dest = storageDir.resolve(saved);
            file.transferTo(dest.toFile());
        }

        SceneAsset asset = new SceneAsset();
        asset.setId(assetId);
        asset.setName(name != null && !name.isEmpty()
                ? name
                : (original != null ? original.replaceFirst("\\.[^.]+$", "") : saved));
        asset.setFileType(ext);
        asset.setPath(path);
        asset.setDescription(description);
        asset.setTextureInfo(existing != null ? existing.getTextureInfo() : null);

        if (existing != null) {
            mapper.update(asset);
        } else {
            mapper.insert(asset);
        }

        return enrich(asset);
    }

    private String resolveSavedFilename(SceneAsset existing, Long assetId, String ext) {
        if (existing != null && existing.getPath() != null) {
            String saved = cleanSceneFilename(extractSceneFilename(existing.getPath()));
            if (saved.toLowerCase().endsWith("." + ext)) {
                return saved;
            }
        }
        return assetId + "-" + UUID.randomUUID() + "." + ext;
    }

    public List<SceneAsset> list() {
        List<SceneAsset> assets = mapper.list();
        assets.forEach(this::enrich);
        return assets;
    }

    public SceneAsset get(Long id) {
        return enrich(mapper.getById(id));
    }

    public void delete(Long id) throws Exception {
        SceneAsset asset = mapper.getById(id);
        if (asset != null) {
            dataPanelService.deleteBySceneId(id);
            String filename = extractSceneFilename(asset.getPath());
            if ("cos".equalsIgnoreCase(storageType)) {
                cosService.deleteObjectByKey("scenes/" + filename);
            } else {
                Path storageDir = Paths.get(System.getProperty("user.dir"), "data", "scenes");
                Path target = storageDir.resolve(filename);
                File file = target.toFile();
                if (file.exists()) {
                    Files.delete(target);
                }
            }
            mapper.deleteById(id);
        }
    }

    private String extractSceneFilename(String path) {
        int scenesIndex = path.lastIndexOf("/scenes/");
        if (scenesIndex >= 0) {
            return cleanSceneFilename(path.substring(scenesIndex + "/scenes/".length()));
        }
        return cleanSceneFilename(path.replaceFirst("^/scenes/", ""));
    }

    private String cleanSceneFilename(String filename) {
        return filename.replace("\\", "/")
                .replaceAll("/{2,}", "/")
                .replaceFirst("^/+", "");
    }

    public void updateTextureInfo(Long id, String textureInfo) {
        mapper.updateTextureInfo(id, textureInfo);
    }

    private SceneAsset enrich(SceneAsset asset) {
        if (asset != null) {
            asset.setUrl(resourceUrlService.buildSceneUrl(asset.getPath()));
        }
        return asset;
    }
}
