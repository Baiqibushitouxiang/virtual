package com.sustbbgz.virtualspringbootbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResourceUrlService {

    @Value("${storage.type:local}")
    private String storageType;

    @Value("${tencent.cos.base-url:}")
    private String cosBaseUrl;

    @Value("${app.public-base-url:http://127.0.0.1:9999}")
    private String publicBaseUrl;

    public String buildModelUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return filePath;
        }
        if (isAbsoluteUrl(filePath)) {
            return filePath;
        }

        String normalized = normalize(filePath);
        normalized = stripBefore(normalized, "static/models/");
        normalized = stripPrefix(normalized, "models/");
        return join(getResourceBaseUrl(), "models/" + normalized);
    }

    public String buildSceneUrl(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        if (isAbsoluteUrl(path)) {
            return path;
        }

        String normalized = stripPrefix(normalize(path), "scenes/");
        return join(getResourceBaseUrl(), "scenes/" + normalized);
    }

    private String getResourceBaseUrl() {
        if ("cos".equalsIgnoreCase(storageType) && cosBaseUrl != null && !cosBaseUrl.isEmpty()) {
            return cosBaseUrl;
        }
        return publicBaseUrl;
    }

    private boolean isAbsoluteUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private String normalize(String value) {
        return value.replace("\\", "/")
                .replaceAll("/{2,}", "/")
                .replaceFirst("^/+", "");
    }

    private String stripPrefix(String value, String prefix) {
        String normalized = normalize(value);
        return normalized.startsWith(prefix) ? normalized.substring(prefix.length()) : normalized;
    }

    private String stripBefore(String value, String marker) {
        int index = value.indexOf(marker);
        return index >= 0 ? value.substring(index + marker.length()) : value;
    }

    private String join(String base, String path) {
        String cleanBase = base.replaceAll("/+$", "");
        String cleanPath = normalize(path);
        return cleanBase + "/" + cleanPath;
    }
}
