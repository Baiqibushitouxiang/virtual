package com.sustbbgz.virtualspringbootbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileStorageService {

    @Value("${storage.type:local}")
    private String storageType;

    @Autowired
    private CosService cosService;

    public String uploadModel(MultipartFile file) throws IOException {
        if ("cos".equalsIgnoreCase(storageType)) {
            return cosService.uploadFile(file, "models");
        }
        throw new UnsupportedOperationException("本地存储模式暂不支持，请配置 storage.type=cos");
    }

    public String uploadModelWithRelativePath(MultipartFile file, String relativePath) throws IOException {
        if ("cos".equalsIgnoreCase(storageType)) {
            return cosService.uploadFileWithRelativePath(file, relativePath);
        }
        throw new UnsupportedOperationException("本地存储模式暂不支持，请配置 storage.type=cos");
    }

    public String uploadScene(MultipartFile file) throws IOException {
        if ("cos".equalsIgnoreCase(storageType)) {
            return cosService.uploadFile(file, "scenes");
        }
        throw new UnsupportedOperationException("本地存储模式暂不支持，请配置 storage.type=cos");
    }

    public String uploadTexture(MultipartFile file) throws IOException {
        if ("cos".equalsIgnoreCase(storageType)) {
            return cosService.uploadFile(file, "textures");
        }
        throw new UnsupportedOperationException("本地存储模式暂不支持，请配置 storage.type=cos");
    }

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if ("cos".equalsIgnoreCase(storageType)) {
            return cosService.uploadFile(file, folder);
        }
        throw new UnsupportedOperationException("本地存储模式暂不支持，请配置 storage.type=cos");
    }

    public String uploadFile(byte[] data, String fileName, String folder, String contentType) throws IOException {
        if ("cos".equalsIgnoreCase(storageType)) {
            return cosService.uploadFile(data, fileName, folder, contentType);
        }
        throw new UnsupportedOperationException("本地存储模式暂不支持，请配置 storage.type=cos");
    }

    public void deleteFile(String fileUrl) {
        if ("cos".equalsIgnoreCase(storageType)) {
            cosService.deleteFile(fileUrl);
        }
    }

    public boolean fileExists(String fileUrl) {
        if ("cos".equalsIgnoreCase(storageType)) {
            return cosService.fileExists(fileUrl);
        }
        return false;
    }

    public boolean isCosStorage() {
        return "cos".equalsIgnoreCase(storageType);
    }

    public String getFileUrl(String relativePath) {
        if ("cos".equalsIgnoreCase(storageType)) {
            return cosService.getFileUrl(relativePath);
        }
        return relativePath;
    }
}
