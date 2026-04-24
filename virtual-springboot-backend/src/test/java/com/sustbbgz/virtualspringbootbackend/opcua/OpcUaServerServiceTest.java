package com.sustbbgz.virtualspringbootbackend.opcua;

import com.sustbbgz.virtualspringbootbackend.config.OpcUaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpcUaServerServiceTest {

    @Mock
    private OpcUaConfig opcUaConfig;

    @Mock
    private OpcUaConfig.ServerConfig serverConfig;

    @Mock
    private OpcUaConfig.CertificateConfig certificateConfig;

    private OpcUaServerService opcUaServerService;

    @BeforeEach
    void setUp() {
        lenient().when(opcUaConfig.getServer()).thenReturn(serverConfig);
        lenient().when(serverConfig.getCertificate()).thenReturn(certificateConfig);
        lenient().when(serverConfig.isEnabled()).thenReturn(false);
        lenient().when(serverConfig.getName()).thenReturn("TestServer");
        lenient().when(serverConfig.getApplicationUri()).thenReturn("urn:test:server");
        lenient().when(serverConfig.getProductUri()).thenReturn("urn:test:product");
        lenient().when(serverConfig.getBindAddress()).thenReturn("127.0.0.1");
        lenient().when(serverConfig.getPort()).thenReturn(4840);
        lenient().when(certificateConfig.getPath()).thenReturn("test/certificates");
        lenient().when(certificateConfig.isGenerateIfMissing()).thenReturn(true);

        opcUaServerService = new OpcUaServerService(opcUaConfig);
    }

    @Test
    void testInitialization() {
        assertNotNull(opcUaServerService);
        assertFalse(opcUaServerService.isRunning());
    }

    @Test
    void testConfigProperties() {
        assertEquals("TestServer", serverConfig.getName());
        assertEquals(4840, serverConfig.getPort());
        assertFalse(serverConfig.isEnabled());
    }

    @Test
    void testNotRunningByDefault() {
        assertFalse(opcUaServerService.isRunning());
    }
}
