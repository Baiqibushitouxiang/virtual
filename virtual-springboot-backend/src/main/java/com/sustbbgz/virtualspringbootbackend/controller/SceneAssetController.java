package com.sustbbgz.virtualspringbootbackend.controller;
import com.sustbbgz.virtualspringbootbackend.entity.SceneAsset;
import com.sustbbgz.virtualspringbootbackend.service.SceneAssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/scenes")
public class SceneAssetController {

    private final SceneAssetService service;

    public SceneAssetController(SceneAssetService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "name", required = false) String name,
                                             @RequestParam(value = "description", required = false) String description,
                                             @RequestParam(value = "id", required = false) Long id) {
        try {
            SceneAsset a = service.upload(file, name, description, id);
            return ResponseEntity.ok(a);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<SceneAsset>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SceneAsset> get(@PathVariable Long id) {
        SceneAsset a = service.get(id);
        if (a == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(a);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/{id}/textures")
    public ResponseEntity<?> updateTextureInfo(@PathVariable Long id, @RequestBody(required = false) String textureInfo) {
        try {
            service.updateTextureInfo(id, textureInfo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Update texture info failed: " + e.getMessage());
        }
    }
}
