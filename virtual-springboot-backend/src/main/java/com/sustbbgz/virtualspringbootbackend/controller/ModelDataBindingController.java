package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.common.Result;
import com.sustbbgz.virtualspringbootbackend.entity.ModelDataBinding;
import com.sustbbgz.virtualspringbootbackend.entity.User;
import com.sustbbgz.virtualspringbootbackend.service.ModelDataBindingService;
import com.sustbbgz.virtualspringbootbackend.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/model-bindings")
public class ModelDataBindingController {

    @Resource
    private ModelDataBindingService modelDataBindingService;

    @GetMapping
    public Result findAll(@RequestParam(required = false) Long sceneId) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        List<ModelDataBinding> bindings = modelDataBindingService.findBindings(userId, sceneId);
        return Result.success(bindings);
    }

    @PostMapping
    public Result create(@RequestBody ModelDataBinding binding) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        return Result.success(modelDataBindingService.saveOrUpdateBinding(binding, userId));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody ModelDataBinding binding) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        binding.setId(id);
        return Result.success(modelDataBindingService.saveOrUpdateBinding(binding, userId));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        modelDataBindingService.removeById(id);
        return Result.success();
    }

    @DeleteMapping
    public Result deleteBySceneAndModel(@RequestParam Long sceneId, @RequestParam String modelId) {
        return Result.success(modelDataBindingService.deleteBySceneAndModel(sceneId, modelId));
    }
}
