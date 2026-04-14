package com.sustbbgz.virtualspringbootbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${storage.type:local}")
    private String storageType;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("=== Static Resource Configuration ===");
        System.out.println("Storage Type: " + storageType);

        if ("cos".equalsIgnoreCase(storageType)) {
            System.out.println("Using COS storage - skipping local directory configuration");
            System.out.println("Models will be served from: " + getCosBaseUrl() + "/models/");
            System.out.println("Scenes will be served from: " + getCosBaseUrl() + "/scenes/");
            System.out.println("====================================");
            return;
        }

        File scenesDir = findScenesDirectory();
        File modelsDir = findModelsDirectory();
        
        System.out.println("Scenes Directory: " + (scenesDir != null ? scenesDir.getAbsolutePath() : "NOT FOUND"));
        System.out.println("Models Directory: " + (modelsDir != null ? modelsDir.getAbsolutePath() : "NOT FOUND"));
        
        if (scenesDir != null && scenesDir.exists() && scenesDir.isDirectory()) {
            String resourceLocation = "file:" + scenesDir.getAbsolutePath().replace("\\", "/") + "/";
            System.out.println("Scenes Resource Location: " + resourceLocation);
            
            registry.addResourceHandler("/scenes/**")
                    .addResourceLocations(resourceLocation)
                    .setCachePeriod(0);
        } else {
            System.err.println("WARNING: Scenes directory not found!");
        }
        
        if (modelsDir != null && modelsDir.exists() && modelsDir.isDirectory()) {
            String resourceLocation = "file:" + modelsDir.getAbsolutePath().replace("\\", "/") + "/";
            System.out.println("Models Resource Location: " + resourceLocation);
            
            registry.addResourceHandler("/models/**")
                    .addResourceLocations(resourceLocation)
                    .setCachePeriod(0);
        } else {
            System.err.println("WARNING: Models directory not found!");
        }
        System.out.println("====================================");
    }

    @Value("${tencent.cos.base-url:}")
    private String cosBaseUrl;

    private String getCosBaseUrl() {
        return cosBaseUrl;
    }
    
    private File findModelsDirectory() {
        try {
            URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
            File codeSourceFile = new File(location.toURI());
            
            File projectDir = codeSourceFile;
            while (projectDir != null && !new File(projectDir, "pom.xml").exists()) {
                projectDir = projectDir.getParentFile();
            }
            
            if (projectDir != null) {
                File modelsDir = new File(projectDir, "src/main/resources/static/models");
                if (modelsDir.exists() && modelsDir.isDirectory()) {
                    return modelsDir;
                }
            }
            
            File fallbackDir = new File("src/main/resources/static/models");
            if (fallbackDir.exists() && fallbackDir.isDirectory()) {
                return fallbackDir.getAbsoluteFile();
            }
            
            return null;
        } catch (URISyntaxException e) {
            System.err.println("Error finding models directory: " + e.getMessage());
            return null;
        }
    }
    
    private File findScenesDirectory() {
        try {
            URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
            File codeSourceFile = new File(location.toURI());
            
            File projectDir = codeSourceFile;
            while (projectDir != null && !new File(projectDir, "pom.xml").exists()) {
                projectDir = projectDir.getParentFile();
            }
            
            if (projectDir != null) {
                File scenesDir = new File(projectDir, "data/scenes");
                if (scenesDir.exists() && scenesDir.isDirectory()) {
                    return scenesDir;
                }
            }
            
            File fallbackDir = new File("data/scenes");
            if (fallbackDir.exists() && fallbackDir.isDirectory()) {
                return fallbackDir.getAbsoluteFile();
            }
            
            return null;
        } catch (URISyntaxException e) {
            System.err.println("Error finding scenes directory: " + e.getMessage());
            return null;
        }
    }
}
