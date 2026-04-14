package com.sustbbgz.virtualspringbootbackend.opcua;

import com.sustbbgz.virtualspringbootbackend.config.OpcUaConfig;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.opcua.namespace.DeviceNamespace;
import com.sustbbgz.virtualspringbootbackend.opcua.namespace.DeviceNodeStore;
import com.sustbbgz.virtualspringbootbackend.service.DeviceDataService;
import com.sustbbgz.virtualspringbootbackend.service.DeviceService;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OpcUaServerService {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaServerService.class);

    private final OpcUaConfig config;
    private OpcUaServer server;
    private DeviceNamespace deviceNamespace;
    private boolean running = false;

    @Autowired(required = false)
    @Lazy
    private DeviceService deviceService;

    @Autowired(required = false)
    @Lazy
    private DeviceDataService deviceDataService;

    public OpcUaServerService(OpcUaConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        if (config.getServer().isEnabled()) {
            try {
                start();
                syncDevicesFromDatabase();
            } catch (Exception e) {
                logger.error("Failed to start OPC UA server", e);
            }
        }
    }

    private void syncDevicesFromDatabase() {
        if (deviceService == null || deviceNamespace == null) {
            return;
        }
        try {
            List<Device> devices = deviceService.list();
            int created = 0;
            for (Device device : devices) {
                DeviceNodeStore.DeviceNode existingNode = deviceNamespace
                    .getDeviceNodeStore()
                    .getDeviceNode(device.getDeviceId());
                if (existingNode == null) {
                    deviceNamespace.createDeviceNode(device.getDeviceId(), device.getName());
                    created++;
                }
            }
            logger.info("OPC UA device sync complete: {} devices created from database (total: {})", created, devices.size());
        } catch (Exception e) {
            logger.error("Failed to sync devices from database", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (running) {
            stop();
        }
    }

    public void start() throws Exception {
        if (running) {
            logger.warn("OPC UA Server is already running");
            return;
        }

        logger.info("Starting OPC UA Server...");

        OpcUaConfig.ServerConfig serverConfig = config.getServer();

        List<SecurityPolicy> securityPolicies = parseSecurityPolicies(serverConfig.getSecurityPolicies());
        Set<EndpointConfiguration> endpointConfigurations = createEndpointConfigurations(
            serverConfig, securityPolicies
        );

        OpcUaServerConfig opcUaServerConfig = OpcUaServerConfig.builder()
            .setApplicationUri(serverConfig.getApplicationUri())
            .setProductUri(serverConfig.getProductUri())
            .setApplicationName(LocalizedText.english(serverConfig.getName()))
            .setBuildInfo(
                BuildInfo.builder()
                    .productUri(serverConfig.getProductUri())
                    .manufacturerName("SUST")
                    .productName(serverConfig.getName())
                    .softwareVersion("1.0.0")
                    .build()
            )
            .setEndpoints(endpointConfigurations)
            .build();

        server = new OpcUaServer(opcUaServerConfig);

        deviceNamespace = new DeviceNamespace(server);
        
        if (deviceDataService != null) {
            deviceNamespace.setTemperatureDataCallback((deviceId, temperature) -> {
                try {
                    deviceDataService.saveDeviceData(deviceId, "temperature", temperature, "°C");
                } catch (Exception e) {
                    logger.warn("Failed to persist temperature data for device {}: {}", deviceId, e.getMessage());
                }
            });
        }
        
        deviceNamespace.startup();

        server.startup().get();

        running = true;
        logger.info("OPC UA Server started on port {}", serverConfig.getPort());
        logger.info("OPC UA Endpoint: opc.tcp://localhost:{}", serverConfig.getPort());
    }

    private Set<EndpointConfiguration> createEndpointConfigurations(
        OpcUaConfig.ServerConfig serverConfig,
        List<SecurityPolicy> securityPolicies
    ) {
        Set<EndpointConfiguration> configurations = new HashSet<>();

        for (SecurityPolicy securityPolicy : securityPolicies) {
            EndpointConfiguration.Builder builder = EndpointConfiguration.newBuilder()
                .setBindAddress(serverConfig.getBindAddress())
                .setBindPort(serverConfig.getPort())
                .setTransportProfile(TransportProfile.TCP_UASC_UABINARY)
                .setSecurityPolicy(securityPolicy);

            if (securityPolicy == SecurityPolicy.None) {
                builder.setSecurityMode(MessageSecurityMode.None);
            } else {
                builder.setSecurityMode(MessageSecurityMode.SignAndEncrypt);
            }

            builder.addTokenPolicy(new UserTokenPolicy(
                "anonymous",
                org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType.Anonymous,
                null, null, null
            ));
            builder.addTokenPolicy(new UserTokenPolicy(
                "username",
                org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType.UserName,
                null, null, null
            ));

            configurations.add(builder.build());
        }

        return configurations;
    }

    public void stop() {
        if (!running || server == null) {
            return;
        }

        try {
            if (deviceNamespace != null) {
                deviceNamespace.shutdown();
            }
            server.shutdown().get();
            running = false;
            logger.info("OPC UA Server stopped");
        } catch (Exception e) {
            logger.error("Error stopping OPC UA Server", e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public OpcUaServer getServer() {
        return server;
    }

    public DeviceNamespace getDeviceNamespace() {
        return deviceNamespace;
    }

    private List<SecurityPolicy> parseSecurityPolicies(List<String> policyNames) {
        List<SecurityPolicy> policies = new ArrayList<>();
        
        policies.add(SecurityPolicy.None);

        return policies;
    }
}
