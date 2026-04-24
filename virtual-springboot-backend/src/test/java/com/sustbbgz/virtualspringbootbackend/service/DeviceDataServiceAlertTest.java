package com.sustbbgz.virtualspringbootbackend.service;

import com.sustbbgz.virtualspringbootbackend.entity.DeviceData;
import com.sustbbgz.virtualspringbootbackend.mapper.DeviceDataMapper;
import com.sustbbgz.virtualspringbootbackend.websocket.DataPushService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceDataServiceAlertTest {

    @Spy
    private DeviceDataService deviceDataService;

    @Mock
    private DeviceService deviceService;

    @Mock
    private DataPushService dataPushService;

    @Mock
    private AlertRuleService alertRuleService;

    @Mock
    private DeviceDataMapper deviceDataMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(deviceDataService, "deviceService", deviceService);
        ReflectionTestUtils.setField(deviceDataService, "dataPushService", dataPushService);
        ReflectionTestUtils.setField(deviceDataService, "alertRuleService", alertRuleService);
        ReflectionTestUtils.setField(deviceDataService, "baseMapper", deviceDataMapper);
    }

    @Test
    void shouldEvaluateAlertsAfterSavingData() {
        doReturn(true).when(deviceDataService).save(any(DeviceData.class));

        DeviceData saved = deviceDataService.saveDeviceData("DEV-001", "temperature", 81.5, "C");

        assertNotNull(saved);
        assertEquals("DEV-001", saved.getDeviceId());
        assertEquals("temperature", saved.getDataType());
        verify(deviceService).updateOnlineStatus("DEV-001", 1);
        verify(dataPushService).pushData(any(String.class), any(String.class), any());
        verify(alertRuleService).evaluateRules("DEV-001", "temperature", 81.5);
    }

    @Test
    void shouldApplyDefaultUnitWhenSavingDataWithoutUnit() {
        doReturn(true).when(deviceDataService).save(any(DeviceData.class));

        DeviceData saved = deviceDataService.saveDeviceData("DEV-001", "humidity", 66.0);

        assertEquals("%", saved.getUnit());
    }

    @Test
    void shouldMapStatsFromMapper() {
        DeviceDataMapper.DeviceDataStats mapperStats = new DeviceDataMapper.DeviceDataStats();
        mapperStats.setAvgValue(21.0);
        mapperStats.setMinValue(18.0);
        mapperStats.setMaxValue(24.5);
        when(deviceDataMapper.getStatsByDeviceIdAndTimeRange(any(), any(), any(), any())).thenReturn(mapperStats);

        DeviceDataService.DeviceDataStats stats = deviceDataService.getStats(
                "DEV-001",
                "temperature",
                LocalDateTime.of(2026, 4, 24, 0, 0),
                LocalDateTime.of(2026, 4, 24, 23, 59)
        );

        assertNotNull(stats);
        assertEquals(21.0, stats.getAvgValue());
        assertEquals(18.0, stats.getMinValue());
        assertEquals(24.5, stats.getMaxValue());
    }

    @Test
    void shouldReturnNullWhenMapperStatsMissing() {
        when(deviceDataMapper.getStatsByDeviceIdAndTimeRange(any(), any(), any(), any())).thenReturn(null);

        DeviceDataService.DeviceDataStats stats = deviceDataService.getStats(
                "DEV-001",
                "temperature",
                LocalDateTime.of(2026, 4, 24, 0, 0),
                LocalDateTime.of(2026, 4, 24, 23, 59)
        );

        assertNull(stats);
    }
}
