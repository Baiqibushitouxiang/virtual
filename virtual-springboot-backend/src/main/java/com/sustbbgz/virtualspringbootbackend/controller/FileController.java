package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.entity.Model;
import com.sustbbgz.virtualspringbootbackend.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private ModelService modelService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/{name}")
    public ResponseEntity<byte[]> downloadModelFile(@PathVariable String name) {
        try {
            Model model = modelService.getByName(name);
            if (model == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            String relativePath = model.getFilePath();
            if (relativePath == null || relativePath.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            relativePath = relativePath.replace("\\", "/");
            
            Path filePath = Paths.get(uploadDir, relativePath);

            if (!Files.exists(filePath)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            
            HttpHeaders headers = new HttpHeaders();
            String contentType = guessContentType(filePath.toString());
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + name + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String guessContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        switch (extension) {
            case ".glb":
                return "model/gltf-binary";
            case ".gltf":
                return "model/gltf+json";
            case ".obj":
                return "model/obj";
            case ".fbx":
                return "model/fbx";
            case ".stl":
                return "model/stl";
            default:
                return "application/octet-stream";
        }
    }
}
