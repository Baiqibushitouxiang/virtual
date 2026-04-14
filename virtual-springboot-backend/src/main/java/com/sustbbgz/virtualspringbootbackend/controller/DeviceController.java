package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.common.Result;
import com.sustbbgz.virtualspringbootbackend.controller.dto.DeviceBindDTO;
import com.sustbbgz.virtualspringbootbackend.controller.dto.DeviceRegisterDTO;
import com.sustbbgz.virtualspringbootbackend.controller.dto.DeviceUpdateDTO;
import com.sustbbgz.virtualspringbootbackend.entity.Device;
import com.sustbbgz.virtualspringbootbackend.opcua.security.CertificateUtils;
import com.sustbbgz.virtualspringbootbackend.service.DeviceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*")
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    @PostMapping
    public Result registerDevice(@RequestBody DeviceRegisterDTO dto) {
        try {
            Device device = deviceService.registerDevice(dto.getName(), dto.getDescription());
            return Result.success(device);
        } catch (Exception e) {
            return Result.error("设备注册失败: " + e.getMessage());
        }
    }

    @GetMapping
    public Result getDeviceList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        try {
            List<Device> devices;
            if (userId != null) {
                devices = deviceService.getDevicesByUserId(userId);
            } else if (status != null) {
                devices = deviceService.lambdaQuery()
                    .eq(Device::getStatus, status)
                    .list();
            } else {
                devices = deviceService.list();
            }
            return Result.success(devices);
        } catch (Exception e) {
            return Result.error("获取设备列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result getDevice(@PathVariable Long id) {
        Device device = deviceService.getById(id);
        if (device == null) {
            return Result.error("设备不存在");
        }
        return Result.success(device);
    }

    @GetMapping("/deviceId/{deviceId}")
    public Result getDeviceByDeviceId(@PathVariable String deviceId) {
        Device device = deviceService.getByDeviceId(deviceId);
        if (device == null) {
            return Result.error("设备不存在");
        }
        return Result.success(device);
    }

    @PutMapping("/{id}")
    public Result updateDevice(@PathVariable Long id, @RequestBody DeviceUpdateDTO dto) {
        try {
            Device device = deviceService.getById(id);
            if (device == null) {
                return Result.error("设备不存在");
            }

            if (dto.getName() != null) {
                device.setName(dto.getName());
            }
            if (dto.getDescription() != null) {
                device.setDescription(dto.getDescription());
            }
            if (dto.getStatus() != null) {
                device.setStatus(dto.getStatus());
            }

            deviceService.updateById(device);
            return Result.success(device);
        } catch (Exception e) {
            return Result.error("更新设备失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result deleteDevice(@PathVariable Long id) {
        try {
            boolean success = deviceService.removeById(id);
            if (success) {
                return Result.success("设备删除成功");
            } else {
                return Result.error("设备删除失败");
            }
        } catch (Exception e) {
            return Result.error("设备删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/bind")
    public Result bindUser(@PathVariable Long id, @RequestBody DeviceBindDTO dto) {
        try {
            deviceService.bindUser(id, dto.getUserId());
            return Result.success("绑定成功");
        } catch (Exception e) {
            return Result.error("绑定失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/unbind")
    public Result unbindUser(@PathVariable Long id) {
        try {
            deviceService.unbindUser(id);
            return Result.success("解绑成功");
        } catch (Exception e) {
            return Result.error("解绑失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            deviceService.updateStatus(id, status);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/certificate")
    public Result generateCertificate(@PathVariable Long id) {
        try {
            Device device = deviceService.getById(id);
            if (device == null) {
                return Result.error("设备不存在");
            }

            String applicationUri = "urn:device:" + device.getDeviceId();
            KeyPair keyPair = CertificateUtils.generateKeyPair();
            X509Certificate certificate = CertificateUtils.generateSelfSignedCertificate(
                keyPair,
                "CN=" + device.getDeviceId(),
                applicationUri
            );

            String certificatePem = CertificateUtils.certificateToPem(certificate);
            String thumbprint = CertificateUtils.getCertificateThumbprint(certificate);

            device.setCertificate(certificatePem);
            device.setCertificateThumbprint(thumbprint);
            deviceService.updateById(device);

            Map<String, Object> result = new HashMap<>();
            result.put("certificate", certificatePem);
            result.put("thumbprint", thumbprint);

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("证书生成失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/certificate")
    public Result getCertificate(@PathVariable Long id) {
        Device device = deviceService.getById(id);
        if (device == null) {
            return Result.error("设备不存在");
        }

        if (device.getCertificate() == null) {
            return Result.error("设备未生成证书");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("certificate", device.getCertificate());
        result.put("thumbprint", device.getCertificateThumbprint());

        return Result.success(result);
    }

    @PostMapping("/{id}/certificate/revoke")
    public Result revokeCertificate(@PathVariable Long id) {
        try {
            Device device = deviceService.getById(id);
            if (device == null) {
                return Result.error("设备不存在");
            }

            device.setCertificate(null);
            device.setCertificateThumbprint(null);
            deviceService.updateById(device);

            return Result.success("证书已吊销");
        } catch (Exception e) {
            return Result.error("证书吊销失败: " + e.getMessage());
        }
    }
}
