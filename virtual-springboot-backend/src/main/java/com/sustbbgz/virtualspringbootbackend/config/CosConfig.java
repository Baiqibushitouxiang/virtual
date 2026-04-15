package com.sustbbgz.virtualspringbootbackend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosConfig {

    @Value("${tencent.cos.secret-id}")
    private String secretId;

    @Value("${tencent.cos.secret-key}")
    private String secretKey;

    @Value("${tencent.cos.region}")
    private String region;

    @Value("${tencent.cos.bucket-name}")
    private String bucketName;

    @Value("${tencent.cos.base-url}")
    private String baseUrl;

    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "cos")
    public COSClient cosClient() {
        validateCosConfig();
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        Region regionObj = new Region(region);
        ClientConfig clientConfig = new ClientConfig(regionObj);
        return new COSClient(cred, clientConfig);
    }

    private void validateCosConfig() {
        if (isBlank(secretId) || isBlank(secretKey) || isBlank(region) || isBlank(bucketName) || isBlank(baseUrl)) {
            throw new IllegalStateException("storage.type=cos 时必须配置 tencent.cos.secret-id、secret-key、region、bucket-name、base-url");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
