package com.sustbbgz.virtualspringbootbackend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sustbbgz.virtualspringbootbackend.entity.DataPanel;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.exception.ServiceException;
import com.sustbbgz.virtualspringbootbackend.mapper.DataPanelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class DataPanelService extends ServiceImpl<DataPanelMapper, DataPanel> {

    private static final String PANEL_NOT_FOUND = "数据展板不存在";
    private static final String PANEL_SCENE_MISMATCH = "数据展板不属于当前场景";
    private static final String PANEL_SCENE_REQUIRED = "场景ID不能为空";
    private static final String PANEL_PERMISSION_DENIED = "无权操作该数据展板";

    @Autowired
    private DataPanelMapper dataPanelMapper;

    @Autowired
    private DeviceService deviceService;

    public List<DataPanel> findByUserIdAndSceneId(Long userId, Long sceneId) {
        if (sceneId == null) {
            return Collections.emptyList();
        }
        List<DataPanel> panels = userId != null
                ? dataPanelMapper.findByUserIdAndSceneId(userId, sceneId)
                : dataPanelMapper.findBySceneId(sceneId);
        enrichPanelData(panels);
        return panels;
    }

    public List<DataPanel> findByDeviceIdAndSceneId(Long deviceId, Long sceneId) {
        if (sceneId == null) {
            return Collections.emptyList();
        }
        return dataPanelMapper.findByDeviceIdAndSceneId(deviceId, sceneId);
    }

    public List<DataPanel> findByModelIdAndSceneId(String modelId, Long sceneId) {
        if (sceneId == null) {
            return Collections.emptyList();
        }
        return dataPanelMapper.findByModelIdAndSceneId(modelId, sceneId);
    }

    public DataPanel createPanel(DataPanel panel) {
        validateSceneId(panel.getSceneId());
        if (panel.getUserId() == null) {
            panel.setUserId(1L);
        }
        hydrateDeviceInfo(panel);
        panel.setCreateTime(LocalDateTime.now());
        panel.setUpdateTime(LocalDateTime.now());
        if (panel.getStatus() == null) {
            panel.setStatus(1);
        }
        save(panel);
        return panel;
    }

    public DataPanel updatePanel(DataPanel panel, Long sceneId, Long userId) {
        DataPanel existing = getScopedPanel(panel.getId(), sceneId, userId);
        existing.setName(panel.getName());
        existing.setDescription(panel.getDescription());
        existing.setStatus(panel.getStatus());
        if (panel.getPosition() != null) {
            existing.setPosition(panel.getPosition());
        }
        if (panel.getSize() != null) {
            existing.setSize(panel.getSize());
        }
        if (panel.getStyle() != null) {
            existing.setStyle(panel.getStyle());
        }
        existing.setUpdateTime(LocalDateTime.now());
        updateById(existing);
        return existing;
    }

    public void deletePanel(Long id, Long sceneId, Long userId) {
        DataPanel existing = getScopedPanel(id, sceneId, userId);
        removeById(existing.getId());
    }

    public DataPanel bindDevice(Long panelId, Long sceneId, Long deviceId, Long userId) {
        DataPanel panel = getScopedPanel(panelId, sceneId, userId);
        Device device = deviceService.getById(deviceId);
        if (device == null) {
            throw new ServiceException(404, "设备不存在");
        }
        panel.setDeviceId(deviceId);
        panel.setDeviceName(device.getName());
        panel.setUpdateTime(LocalDateTime.now());
        updateById(panel);
        return panel;
    }

    public DataPanel bindModel(Long panelId, Long sceneId, String modelId, String modelName, String modelType, Long userId) {
        DataPanel panel = getScopedPanel(panelId, sceneId, userId);
        panel.setModelId(modelId);
        panel.setModelName(modelName);
        panel.setModelType(modelType);
        panel.setUpdateTime(LocalDateTime.now());
        updateById(panel);
        return panel;
    }

    public DataPanel unbindDevice(Long panelId, Long sceneId, Long userId) {
        DataPanel panel = getScopedPanel(panelId, sceneId, userId);
        panel.setDeviceId(null);
        panel.setDeviceName(null);
        panel.setUpdateTime(LocalDateTime.now());
        updateById(panel);
        return panel;
    }

    public DataPanel unbindModel(Long panelId, Long sceneId, Long userId) {
        DataPanel panel = getScopedPanel(panelId, sceneId, userId);
        panel.setModelId(null);
        panel.setModelName(null);
        panel.setModelType(null);
        panel.setUpdateTime(LocalDateTime.now());
        updateById(panel);
        return panel;
    }

    public DataPanel updatePosition(Long panelId, Long sceneId, String position, Long userId) {
        DataPanel panel = getScopedPanel(panelId, sceneId, userId);
        panel.setPosition(position);
        panel.setUpdateTime(LocalDateTime.now());
        updateById(panel);
        return panel;
    }

    public DataPanel updateStyle(Long panelId, Long sceneId, String style, Long userId) {
        DataPanel panel = getScopedPanel(panelId, sceneId, userId);
        panel.setStyle(style);
        panel.setUpdateTime(LocalDateTime.now());
        updateById(panel);
        return panel;
    }

    public void deleteBySceneId(Long sceneId) {
        if (sceneId != null) {
            dataPanelMapper.deleteBySceneId(sceneId);
        }
    }

    private DataPanel getScopedPanel(Long panelId, Long sceneId, Long userId) {
        validateSceneId(sceneId);
        DataPanel panel = getById(panelId);
        if (panel == null) {
            throw new ServiceException(404, PANEL_NOT_FOUND);
        }
        if (!Objects.equals(panel.getSceneId(), sceneId)) {
            throw new ServiceException(400, PANEL_SCENE_MISMATCH);
        }
        if (userId != null && panel.getUserId() != null && !Objects.equals(panel.getUserId(), userId)) {
            throw new ServiceException(403, PANEL_PERMISSION_DENIED);
        }
        return panel;
    }

    private void validateSceneId(Long sceneId) {
        if (sceneId == null) {
            throw new ServiceException(400, PANEL_SCENE_REQUIRED);
        }
    }

    private void hydrateDeviceInfo(DataPanel panel) {
        if (panel.getDeviceId() == null) {
            return;
        }
        Device device = deviceService.getById(panel.getDeviceId());
        if (device == null) {
            throw new ServiceException(404, "设备不存在");
        }
        panel.setDeviceName(device.getName());
    }

    private void enrichPanelData(List<DataPanel> panels) {
        for (DataPanel panel : panels) {
            if (panel.getDeviceId() != null) {
                Device device = deviceService.getById(panel.getDeviceId());
                panel.setDevice(device);
            }
        }
    }
}
