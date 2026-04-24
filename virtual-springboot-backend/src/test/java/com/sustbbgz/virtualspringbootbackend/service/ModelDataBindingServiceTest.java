package com.sustbbgz.virtualspringbootbackend.service;

import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.entity.ModelDataBinding;
import com.sustbbgz.virtualspringbootbackend.mapper.ModelDataBindingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelDataBindingServiceTest {

    @Spy
    private ModelDataBindingService modelDataBindingService;

    @Mock
    private ModelDataBindingMapper modelDataBindingMapper;

    @Mock
    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(modelDataBindingService, "baseMapper", modelDataBindingMapper);
        ReflectionTestUtils.setField(modelDataBindingService, "deviceService", deviceService);
    }

    @Test
    void shouldCreateBindingWithDefaults() {
        ModelDataBinding binding = buildBinding();
        Device device = new Device();
        device.setId(1L);
        device.setDeviceId("DEV-001");
        device.setName("main-device");
        when(deviceService.getById(1L)).thenReturn(device);
        when(modelDataBindingMapper.findBySceneIdAndModelId(11L, "model-1")).thenReturn(null);
        doReturn(true).when(modelDataBindingService).saveOrUpdate(any(ModelDataBinding.class));
        doReturn(binding).when(modelDataBindingService).getById(any());

        ModelDataBinding saved = modelDataBindingService.saveOrUpdateBinding(binding, 8L);

        assertEquals("DEV-001", binding.getDeviceCode());
        assertEquals("main-device", binding.getDeviceName());
        assertEquals(1, binding.getRuleStatus());
        assertEquals(8L, binding.getUserId());
        assertNotNull(binding.getCreateTime());
        assertNotNull(binding.getUpdateTime());
        assertEquals(saved, binding);
    }

    @Test
    void shouldUpdateExistingBindingBySceneAndModel() {
        ModelDataBinding existing = buildBinding();
        existing.setId(99L);
        existing.setCreateTime(LocalDateTime.now().minusDays(1));
        ModelDataBinding binding = buildBinding();
        when(modelDataBindingMapper.findBySceneIdAndModelId(11L, "model-1")).thenReturn(existing);
        doReturn(true).when(modelDataBindingService).saveOrUpdate(any(ModelDataBinding.class));
        doReturn(binding).when(modelDataBindingService).getById(99L);

        modelDataBindingService.saveOrUpdateBinding(binding, 1L);

        assertEquals(99L, binding.getId());
        assertEquals(existing.getCreateTime(), binding.getCreateTime());
    }

    @Test
    void shouldLeaveDeviceInfoEmptyWhenDeviceMissing() {
        ModelDataBinding binding = buildBinding();
        when(deviceService.getById(1L)).thenReturn(null);
        when(modelDataBindingMapper.findBySceneIdAndModelId(11L, "model-1")).thenReturn(null);
        doReturn(true).when(modelDataBindingService).saveOrUpdate(any(ModelDataBinding.class));
        doReturn(binding).when(modelDataBindingService).getById(any());

        modelDataBindingService.saveOrUpdateBinding(binding, 8L);

        assertNull(binding.getDeviceCode());
        assertNull(binding.getDeviceName());
    }

    @Test
    void shouldDeleteBindingBySceneAndModel() {
        ModelDataBinding existing = buildBinding();
        existing.setId(77L);
        doReturn(existing).when(modelDataBindingService).getOne(any(), eq(false));
        doReturn(true).when(modelDataBindingService).removeById(77L);

        ModelDataBinding deleted = modelDataBindingService.deleteBySceneAndModel(11L, "model-1");

        assertEquals(existing, deleted);
        verify(modelDataBindingService).removeById(77L);
    }

    @Test
    void shouldFindBindingsBySceneFirst() {
        when(modelDataBindingMapper.findBySceneId(11L)).thenReturn(Collections.singletonList(buildBinding()));
        assertEquals(1, modelDataBindingService.findBindings(5L, 11L).size());
        verify(modelDataBindingMapper).findBySceneId(11L);
    }

    private ModelDataBinding buildBinding() {
        ModelDataBinding binding = new ModelDataBinding();
        binding.setSceneId(11L);
        binding.setModelId("model-1");
        binding.setModelName("pump-model");
        binding.setDeviceId(1L);
        binding.setDataType("temperature");
        return binding;
    }
}
