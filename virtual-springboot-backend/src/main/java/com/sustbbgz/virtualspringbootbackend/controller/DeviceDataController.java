package com.sustbbgz.virtualspringbootbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sustbbgz.virtualspringbootbackend.common.Result;
import com.sustbbgz.virtualspringbootbackend.entity.DeviceData;
import com.sustbbgz.virtualspringbootbackend.service.DeviceDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device-data")
@CrossOrigin
public class DeviceDataController {

    private final DeviceDataService deviceDataService;

    public DeviceDataController(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @GetMapping("/latest/{deviceId}")
    public Result getLatestData(
        @PathVariable String deviceId,
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<DeviceData> data = deviceDataService.getLatestData(deviceId, limit);
        return Result.success(data);
    }

    @GetMapping("/history/{deviceId}")
    public Result getHistoryData(
        @PathVariable String deviceId,
        @RequestParam(required = false) String dataType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
        @RequestParam(defaultValue = "100") int limit
    ) {
        List<DeviceData> data;
        
        if (startTime != null && endTime != null) {
            data = deviceDataService.getDataByTimeRange(deviceId, startTime, endTime);
        } else if (dataType != null) {
            data = deviceDataService.getDataByType(deviceId, dataType, limit);
        } else {
            data = deviceDataService.getLatestData(deviceId, limit);
        }
        
        return Result.success(data);
    }

    @GetMapping("/stats/{deviceId}")
    public Result getStats(
        @PathVariable String deviceId,
        @RequestParam(defaultValue = "temperature") String dataType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusHours(24);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        
        DeviceDataService.DeviceDataStats stats = deviceDataService.getStats(deviceId, dataType, startTime, endTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("deviceId", deviceId);
        result.put("dataType", dataType);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        result.put("stats", stats);
        
        return Result.success(result);
    }

    @GetMapping("/count/{deviceId}")
    public Result countByDeviceId(@PathVariable String deviceId) {
        long count = deviceDataService.countByDeviceId(deviceId);
        return Result.success(count);
    }

    @PostMapping
    public Result saveData(@RequestBody DeviceData deviceData) {
        DeviceData saved = deviceDataService.saveDeviceData(
            deviceData.getDeviceId(),
            deviceData.getDataType(),
            deviceData.getValue(),
            deviceData.getUnit()
        );
        return Result.success(saved);
    }

    @DeleteMapping("/clean")
    public Result cleanOldData(@RequestParam(defaultValue = "30") int daysToKeep) {
        deviceDataService.cleanOldData(daysToKeep);
        return Result.success("Cleaned data older than " + daysToKeep + " days");
    }
}
