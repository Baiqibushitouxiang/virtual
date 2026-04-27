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
    public Result findAll(@RequestParam(required = false) Long sceneId) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        List<DataPanel> panels = dataPanelService.findByUserIdAndSceneId(userId, sceneId);
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
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        panel.setId(id);
        DataPanel updated = dataPanelService.updatePanel(panel, panel.getSceneId(), userId);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id, @RequestParam(required = false) Long sceneId) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        dataPanelService.deletePanel(id, sceneId, userId);
        return Result.success();
    }

    @PostMapping("/{id}/bind-device")
    public Result bindDevice(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        Long deviceId = asLong(params.get("deviceId"));
        Long sceneId = asLong(params.get("sceneId"));
        DataPanel panel = dataPanelService.bindDevice(id, sceneId, deviceId, userId);
        return Result.success(panel);
    }

    @PostMapping("/{id}/unbind-device")
    public Result unbindDevice(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        Long sceneId = params == null ? null : asLong(params.get("sceneId"));
        DataPanel panel = dataPanelService.unbindDevice(id, sceneId, userId);
        return Result.success(panel);
    }

    @PostMapping("/{id}/bind-model")
    public Result bindModel(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        String modelId = params.get("modelId").toString();
        String modelName = (String) params.get("modelName");
        String modelType = (String) params.get("modelType");
        Long sceneId = asLong(params.get("sceneId"));
        DataPanel panel = dataPanelService.bindModel(id, sceneId, modelId, modelName, modelType, userId);
        return Result.success(panel);
    }

    @PostMapping("/{id}/unbind-model")
    public Result unbindModel(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        Long sceneId = params == null ? null : asLong(params.get("sceneId"));
        DataPanel panel = dataPanelService.unbindModel(id, sceneId, userId);
        return Result.success(panel);
    }

    @PutMapping("/{id}/position")
    public Result updatePosition(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        String position = (String) params.get("position");
        Long sceneId = asLong(params.get("sceneId"));
        DataPanel panel = dataPanelService.updatePosition(id, sceneId, position, userId);
        return Result.success(panel);
    }

    @PutMapping("/{id}/style")
    public Result updateStyle(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        String style = (String) params.get("style");
        Long sceneId = asLong(params.get("sceneId"));
        DataPanel panel = dataPanelService.updateStyle(id, sceneId, style, userId);
        return Result.success(panel);
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }
}
