package com.sustbbgz.virtualspringbootbackend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sustbbgz.virtualspringbootbackend.entity.DataPanel;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.mapper.DataPanelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DataPanelService extends ServiceImpl<DataPanelMapper, DataPanel> {

    @Autowired
    private DataPanelMapper dataPanelMapper;

    @Autowired
    private DeviceService deviceService;

    public List<DataPanel> findByUserId(Long userId) {
        List<DataPanel> panels;
        if (userId != null) {
            panels = dataPanelMapper.findByUserId(userId);
        } else {
            panels = list();
        }
        enrichPanelData(panels);
        return panels;
    }

    public List<DataPanel> findByDeviceId(Long deviceId) {
        return dataPanelMapper.findByDeviceId(deviceId);
    }

    public List<DataPanel> findByModelId(String modelId) {
        return dataPanelMapper.findByModelId(modelId);
    }

    public DataPanel createPanel(DataPanel panel) {
        if (panel.getUserId() == null) {
            panel.setUserId(1L);
        }
        panel.setCreateTime(LocalDateTime.now());
        panel.setUpdateTime(LocalDateTime.now());
        if (panel.getStatus() == null) {
            panel.setStatus(1);
        }
        save(panel);
        return panel;
    }

    public DataPanel updatePanel(DataPanel panel) {
        panel.setUpdateTime(LocalDateTime.now());
        updateById(panel);
        return panel;
    }

    public void deletePanel(Long id) {
        removeById(id);
    }

    public DataPanel bindDevice(Long panelId, Long deviceId) {
        DataPanel panel = getById(panelId);
        if (panel != null) {
            Device device = deviceService.getById(deviceId);
            if (device != null) {
                panel.setDeviceId(deviceId);
                panel.setDeviceName(device.getName());
                panel.setUpdateTime(LocalDateTime.now());
                updateById(panel);
            }
        }
        return panel;
    }

    public DataPanel bindModel(Long panelId, String modelId, String modelName, String modelType) {
        DataPanel panel = getById(panelId);
        if (panel != null) {
            panel.setModelId(modelId);
            panel.setModelName(modelName);
            panel.setModelType(modelType);
            panel.setUpdateTime(LocalDateTime.now());
            updateById(panel);
        }
        return panel;
    }

    public DataPanel unbindDevice(Long panelId) {
        DataPanel panel = getById(panelId);
        if (panel != null) {
            panel.setDeviceId(null);
            panel.setDeviceName(null);
            panel.setUpdateTime(LocalDateTime.now());
            updateById(panel);
        }
        return panel;
    }

    public DataPanel unbindModel(Long panelId) {
        DataPanel panel = getById(panelId);
        if (panel != null) {
            panel.setModelId(null);
            panel.setModelName(null);
            panel.setModelType(null);
            panel.setUpdateTime(LocalDateTime.now());
            updateById(panel);
        }
        return panel;
    }

    public DataPanel updatePosition(Long panelId, String position) {
        DataPanel panel = getById(panelId);
        if (panel != null) {
            panel.setPosition(position);
            panel.setUpdateTime(LocalDateTime.now());
            updateById(panel);
        }
        return panel;
    }

    public DataPanel updateStyle(Long panelId, String style) {
        DataPanel panel = getById(panelId);
        if (panel != null) {
            panel.setStyle(style);
            panel.setUpdateTime(LocalDateTime.now());
            updateById(panel);
        }
        return panel;
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
