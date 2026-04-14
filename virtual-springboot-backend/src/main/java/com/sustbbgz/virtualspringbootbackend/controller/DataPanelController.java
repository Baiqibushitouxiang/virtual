package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.common.Result;
import com.sustbbgz.virtualspringbootbackend.entity.DataPanel;
import com.sustbbgz.virtualspringbootbackend.entity.User;
import com.sustbbgz.virtualspringbootbackend.service.DataPanelService;
import com.sustbbgz.virtualspringbootbackend.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data-panels")
public class DataPanelController {

    @Autowired
    private DataPanelService dataPanelService;

    @GetMapping
    public Result findAll() {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        List<DataPanel> panels = dataPanelService.findByUserId(userId);
        return Result.success(panels);
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id) {
        DataPanel panel = dataPanelService.getById(id);
        return Result.success(panel);
    }

    @PostMapping
    public Result create(@RequestBody DataPanel panel) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        panel.setUserId(userId);
        DataPanel created = dataPanelService.createPanel(panel);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody DataPanel panel) {
        panel.setId(id);
        DataPanel updated = dataPanelService.updatePanel(panel);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        dataPanelService.deletePanel(id);
        return Result.success();
    }

    @PostMapping("/{id}/bind-device")
    public Result bindDevice(@PathVariable Long id, @RequestBody Map<String, Long> params) {
        Long deviceId = params.get("deviceId");
        DataPanel panel = dataPanelService.bindDevice(id, deviceId);
        return Result.success(panel);
    }

    @PostMapping("/{id}/unbind-device")
    public Result unbindDevice(@PathVariable Long id) {
        DataPanel panel = dataPanelService.unbindDevice(id);
        return Result.success(panel);
    }

    @PostMapping("/{id}/bind-model")
    public Result bindModel(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String modelId = params.get("modelId").toString();
        String modelName = (String) params.get("modelName");
        String modelType = (String) params.get("modelType");
        DataPanel panel = dataPanelService.bindModel(id, modelId, modelName, modelType);
        return Result.success(panel);
    }

    @PostMapping("/{id}/unbind-model")
    public Result unbindModel(@PathVariable Long id) {
        DataPanel panel = dataPanelService.unbindModel(id);
        return Result.success(panel);
    }

    @PutMapping("/{id}/position")
    public Result updatePosition(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String position = params.get("position");
        DataPanel panel = dataPanelService.updatePosition(id, position);
        return Result.success(panel);
    }

    @PutMapping("/{id}/style")
    public Result updateStyle(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String style = params.get("style");
        DataPanel panel = dataPanelService.updateStyle(id, style);
        return Result.success(panel);
    }
}
