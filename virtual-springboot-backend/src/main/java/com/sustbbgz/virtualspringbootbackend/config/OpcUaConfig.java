package com.sustbbgz.virtualspringbootbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "opcua")
public class OpcUaConfig {

    private ServerConfig server = new ServerConfig();
    private ClientConfig client = new ClientConfig();

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public ClientConfig getClient() {
        return client;
    }

    public void setClient(ClientConfig client) {
        this.client = client;
    }

    public static class ServerConfig {
        private boolean enabled = true;
        private String name = "DigitalTwinOPCUAServer";
        private String applicationUri = "urn:digital-twin:opcua:server";
        private String productUri = "urn:digital-twin:opcua:product";
        private String bindAddress = "0.0.0.0";
        private int port = 4840;
        private List<String> securityPolicies = Arrays.asList("None", "Basic256Sha256");
        private List<String> securityModes = Arrays.asList("None", "SignAndEncrypt");
        private CertificateConfig certificate = new CertificateConfig();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getApplicationUri() {
            return applicationUri;
        }

        public void setApplicationUri(String applicationUri) {
            this.applicationUri = applicationUri;
        }

        public String getProductUri() {
            return productUri;
        }

        public void setProductUri(String productUri) {
            this.productUri = productUri;
        }

        public String getBindAddress() {
            return bindAddress;
        }

        public void setBindAddress(String bindAddress) {
            this.bindAddress = bindAddress;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public List<String> getSecurityPolicies() {
            return securityPolicies;
        }

        public void setSecurityPolicies(List<String> securityPolicies) {
            this.securityPolicies = securityPolicies;
        }

        public List<String> getSecurityModes() {
            return securityModes;
        }

        public void setSecurityModes(List<String> securityModes) {
            this.securityModes = securityModes;
        }

        public CertificateConfig getCertificate() {
            return certificate;
        }

        public void setCertificate(CertificateConfig certificate) {
            this.certificate = certificate;
        }
    }

    public static class ClientConfig {
        private boolean enabled = true;
        private int connectionTimeout = 5000;
        private long sessionTimeout = 60000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public long getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(long sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }
    }

    public static class CertificateConfig {
        private String path = "opcua/certificates";
        private boolean generateIfMissing = true;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isGenerateIfMissing() {
            return generateIfMissing;
        }

        public void setGenerateIfMissing(boolean generateIfMissing) {
            this.generateIfMissing = generateIfMissing;
        }
    }
}
