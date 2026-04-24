package com.sustbbgz.virtualspringbootbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.entity.ModelDataBinding;
import com.sustbbgz.virtualspringbootbackend.mapper.ModelDataBindingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ModelDataBindingService extends ServiceImpl<ModelDataBindingMapper, ModelDataBinding> {

    @Resource
    private DeviceService deviceService;

    public List<ModelDataBinding> findBindings(Long userId, Long sceneId) {
        if (sceneId != null) {
            return baseMapper.findBySceneId(sceneId);
        }
        if (userId != null) {
            return baseMapper.findByUserId(userId);
        }
        return list();
    }

    public ModelDataBinding saveOrUpdateBinding(ModelDataBinding binding, Long userId) {
        if (binding.getUserId() == null) {
            binding.setUserId(userId != null ? userId : 1L);
        }
        hydrateDevice(binding);
        binding.setRuleStatus(binding.getRuleStatus() == null ? 1 : binding.getRuleStatus());
        if (binding.getId() == null) {
            ModelDataBinding existing = baseMapper.findBySceneIdAndModelId(binding.getSceneId(), binding.getModelId());
            if (existing != null) {
                binding.setId(existing.getId());
                binding.setCreateTime(existing.getCreateTime());
            }
        }
        if (binding.getCreateTime() == null) {
            binding.setCreateTime(LocalDateTime.now());
        }
        binding.setUpdateTime(LocalDateTime.now());
        saveOrUpdate(binding);
        return getById(binding.getId());
    }

    public ModelDataBinding deleteBySceneAndModel(Long sceneId, String modelId) {
        LambdaQueryWrapper<ModelDataBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelDataBinding::getSceneId, sceneId)
                .eq(ModelDataBinding::getModelId, modelId);
        ModelDataBinding existing = getOne(wrapper, false);
        if (existing != null) {
            removeById(existing.getId());
        }
        return existing;
    }

    private void hydrateDevice(ModelDataBinding binding) {
        if (binding.getDeviceId() == null) {
            return;
        }
        Device device = deviceService.getById(binding.getDeviceId());
        if (device != null) {
            binding.setDeviceCode(device.getDeviceId());
            binding.setDeviceName(device.getName());
        }
    }
}
