package com.sustbbgz.virtualspringbootbackend.service;

import com.sustbbgz.virtualspringbootbackend.entity.DataPanel;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.exception.ServiceException;
import com.sustbbgz.virtualspringbootbackend.mapper.DataPanelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataPanelServiceTest {

    @Spy
    private DataPanelService dataPanelService;

    @Mock
    private DataPanelMapper dataPanelMapper;

    @Mock
    private DeviceService deviceService;

    private DataPanel panel;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dataPanelService, "dataPanelMapper", dataPanelMapper);
        ReflectionTestUtils.setField(dataPanelService, "deviceService", deviceService);

        panel = new DataPanel();
        panel.setId(1L);
        panel.setSceneId(10L);
        panel.setUserId(5L);
        panel.setDeviceId(12L);
        panel.setDeviceName("history-device");
    }

    @Test
    void shouldReturnEmptyListWhenSceneIdMissing() {
        assertTrue(dataPanelService.findByUserIdAndSceneId(5L, null).isEmpty());
    }

    @Test
    void shouldBindModelWithoutOverwritingDeviceBinding() {
        doReturn(panel).when(dataPanelService).getById(1L);
        doReturn(true).when(dataPanelService).updateById(panel);

        DataPanel updated = dataPanelService.bindModel(1L, 10L, "model-1", "pump-model", "scene", 5L);

        assertEquals("model-1", updated.getModelId());
        assertEquals("pump-model", updated.getModelName());
        assertEquals(12L, updated.getDeviceId());
        assertEquals("history-device", updated.getDeviceName());
        verify(dataPanelService).updateById(panel);
    }

    @Test
    void shouldRejectCrossSceneMutation() {
        doReturn(panel).when(dataPanelService).getById(1L);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                dataPanelService.unbindModel(1L, 99L, 5L));

        assertEquals("数据展板不属于当前场景", exception.getMessage());
    }

    @Test
    void shouldBindDeviceAndHydrateName() {
        Device device = new Device();
        device.setId(8L);
        device.setName("boiler-device");
        doReturn(panel).when(dataPanelService).getById(1L);
        doReturn(true).when(dataPanelService).updateById(panel);
        when(deviceService.getById(8L)).thenReturn(device);

        DataPanel updated = dataPanelService.bindDevice(1L, 10L, 8L, 5L);

        assertEquals(8L, updated.getDeviceId());
        assertEquals("boiler-device", updated.getDeviceName());
    }

    @Test
    void shouldCreatePanelWithDefaults() {
        DataPanel newPanel = new DataPanel();
        newPanel.setName("new-panel");
        newPanel.setSceneId(10L);
        doReturn(true).when(dataPanelService).save(any(DataPanel.class));

        DataPanel created = dataPanelService.createPanel(newPanel);

        assertNotNull(created.getCreateTime());
        assertNotNull(created.getUpdateTime());
        assertEquals(1, created.getStatus());
        assertEquals(1L, created.getUserId());
        assertEquals(10L, created.getSceneId());
    }

    @Test
    void shouldDeleteBySceneId() {
        dataPanelService.deleteBySceneId(10L);

        verify(dataPanelMapper).deleteBySceneId(10L);
    }
}
