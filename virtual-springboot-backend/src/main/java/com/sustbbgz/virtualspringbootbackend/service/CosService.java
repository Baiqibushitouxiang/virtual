package com.sustbbgz.virtualspringbootbackend.service;

import cn.hutool.core.util.IdUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.sustbbgz.virtualspringbootbackend.config.CosConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class CosService {

    private static final Logger logger = LoggerFactory.getLogger(CosService.class);

    @Autowired
    private COSClient cosClient;

    @Autowired
    private CosConfig cosConfig;

    private static final String[] ALLOWED_EXTENSIONS = {
            ".glb", ".gltf", ".fbx", ".obj", ".png", ".jpg", ".jpeg", ".json", ".mtl"
    };

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String newFileName = IdUtil.fastSimpleUUID() + extension;
        String key = cleanKey(folder + "/" + datePath + "/" + newFileName);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucketName(),
                    key,
                    inputStream,
                    metadata
            );
            PutObjectResult result = cosClient.putObject(putRequest);
            logger.info("文件上传成功, ETag: {}", result.getETag());
        } catch (CosClientException e) {
            logger.error("上传文件到COS失败: {}", e.getMessage());
            throw new IOException("上传文件失败: " + e.getMessage());
        }

        return joinUrl(cosConfig.getBaseUrl(), key);
    }

    public String uploadFileWithRelativePath(MultipartFile file, String relativePath) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        String key = cleanKey("models/" + relativePath);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(getContentType(extension));

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucketName(),
                    key,
                    inputStream,
                    metadata
            );
            PutObjectResult result = cosClient.putObject(putRequest);
            logger.info("文件上传成功(保留目录结构), Key: {}, ETag: {}", key, result.getETag());
        } catch (CosClientException e) {
            logger.error("上传文件到COS失败: {}", e.getMessage());
            throw new IOException("上传文件失败: " + e.getMessage());
        }

        return joinUrl(cosConfig.getBaseUrl(), key);
    }

    public String uploadFileToKey(MultipartFile file, String key) throws IOException {
        key = cleanKey(key);
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(getContentType(extension));

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucketName(),
                    key,
                    inputStream,
                    metadata
            );
            PutObjectResult result = cosClient.putObject(putRequest);
            logger.info("文件上传成功, Key: {}, ETag: {}", key, result.getETag());
        } catch (CosClientException e) {
            logger.error("上传文件到COS失败: {}", e.getMessage());
            throw new IOException("上传文件失败: " + e.getMessage());
        }

        return joinUrl(cosConfig.getBaseUrl(), key);
    }

    public String uploadLocalFile(File localFile, String relativePath) throws IOException {
        if (!localFile.exists()) {
            throw new IOException("文件不存在: " + localFile.getAbsolutePath());
        }

        String extension = getFileExtension(localFile.getName());
        if (!isAllowedExtension(extension)) {
            logger.warn("跳过不支持的文件类型: {}", localFile.getName());
            return null;
        }

        String key = cleanKey(relativePath);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(localFile.length());
        metadata.setContentType(getContentType(extension));

        try (FileInputStream inputStream = new FileInputStream(localFile)) {
            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucketName(),
                    key,
                    inputStream,
                    metadata
            );
            PutObjectResult result = cosClient.putObject(putRequest);
            logger.info("本地文件上传成功, Key: {}, ETag: {}", key, result.getETag());
        } catch (CosClientException e) {
            logger.error("上传本地文件到COS失败: {}", e.getMessage());
            throw new IOException("上传文件失败: " + e.getMessage());
        }

        return joinUrl(cosConfig.getBaseUrl(), key);
    }

    public String uploadFile(byte[] data, String fileName, String folder, String contentType) throws IOException {
        String extension = getFileExtension(fileName);
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String newFileName = IdUtil.fastSimpleUUID() + extension;
        String key = cleanKey(folder + "/" + datePath + "/" + newFileName);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(data.length);
        metadata.setContentType(contentType);

        try {
            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucketName(),
                    key,
                    new java.io.ByteArrayInputStream(data),
                    metadata
            );
            PutObjectResult result = cosClient.putObject(putRequest);
            logger.info("文件上传成功, ETag: {}", result.getETag());
        } catch (CosClientException e) {
            logger.error("上传文件到COS失败: {}", e.getMessage());
            throw new IOException("上传文件失败: " + e.getMessage());
        }

        return joinUrl(cosConfig.getBaseUrl(), key);
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        String key = extractKeyFromUrl(fileUrl);
        if (key == null) {
            return;
        }

        try {
            cosClient.deleteObject(cosConfig.getBucketName(), key);
            logger.info("文件删除成功: {}", key);
        } catch (CosClientException e) {
            logger.error("删除COS文件失败: {}", e.getMessage());
        }
    }

    public void deleteObjectByKey(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        key = cleanKey(key);

        try {
            cosClient.deleteObject(cosConfig.getBucketName(), key);
            logger.info("文件删除成功: {}", key);
        } catch (CosClientException e) {
            logger.error("删除COS文件失败: {}", e.getMessage());
        }
    }

    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        String key = extractKeyFromUrl(fileUrl);
        if (key == null) {
            return false;
        }

        try {
            return cosClient.doesObjectExist(cosConfig.getBucketName(), key);
        } catch (CosClientException e) {
            logger.error("检查文件是否存在失败: {}", e.getMessage());
            return false;
        }
    }

    public String getFileUrl(String relativePath) {
        return joinUrl(cosConfig.getBaseUrl(), cleanKey("models/" + relativePath));
    }

    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl.startsWith(cosConfig.getBaseUrl())) {
            return cleanKey(fileUrl.substring(cosConfig.getBaseUrl().length() + 1));
        }
        return null;
    }

    private String cleanKey(String key) {
        return key.replace("\\", "/")
                .replaceAll("/{2,}", "/")
                .replaceFirst("^/+", "");
    }

    private String joinUrl(String base, String key) {
        return base.replaceAll("/+$", "") + "/" + cleanKey(key);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex).toLowerCase();
        }
        return "";
    }

    private boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    private String getContentType(String extension) {
        switch (extension.toLowerCase()) {
            case ".glb":
                return "model/gltf-binary";
            case ".gltf":
                return "model/gltf+json";
            case ".obj":
                return "text/plain";
            case ".fbx":
                return "application/octet-stream";
            case ".png":
                return "image/png";
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".json":
                return "application/json";
            default:
                return "application/octet-stream";
        }
    }
}
