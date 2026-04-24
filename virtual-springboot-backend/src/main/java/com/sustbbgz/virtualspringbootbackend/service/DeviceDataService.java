package com.sustbbgz.virtualspringbootbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sustbbgz.virtualspringbootbackend.entity.DeviceData;
import com.sustbbgz.virtualspringbootbackend.mapper.DeviceDataMapper;
import com.sustbbgz.virtualspringbootbackend.websocket.DataPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DeviceDataService extends ServiceImpl<DeviceDataMapper, DeviceData> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceDataService.class);

    @Autowired(required = false)
    @Lazy
    private DataPushService dataPushService;

    @Autowired(required = false)
    @Lazy
    private DeviceService deviceService;

    @Autowired(required = false)
    @Lazy
    private AlertRuleService alertRuleService;

    public static class DeviceDataStats {
        private Double avgValue;
        private Double minValue;
        private Double maxValue;
        
        public Double getAvgValue() { return avgValue; }
        public void setAvgValue(Double avgValue) { this.avgValue = avgValue; }
        public Double getMinValue() { return minValue; }
        public void setMinValue(Double minValue) { this.minValue = minValue; }
        public Double getMaxValue() { return maxValue; }
        public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }
    }

    public DeviceData saveDeviceData(String deviceId, String dataType, Double value, String unit) {
        DeviceData deviceData = new DeviceData();
        deviceData.setDeviceId(deviceId);
        deviceData.setDataType(dataType);
        deviceData.setValue(value);
        deviceData.setUnit(unit);
        deviceData.setRecordedAt(LocalDateTime.now());
        deviceData.setCreatedAt(LocalDateTime.now());
        
        save(deviceData);
        logger.debug("Saved device data: deviceId={}, type={}, value={}", deviceId, dataType, value);

        if (deviceService != null) {
            deviceService.updateOnlineStatus(deviceId, 1);
        }

        if (dataPushService != null) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("dataType", dataType);
            dataMap.put("value", value);
            dataMap.put("unit", unit);
            dataPushService.pushData(deviceId, "deviceData", dataMap);
            logger.debug("Pushed device data to WebSocket: deviceId={}, type={}, value={}", deviceId, dataType, value);
        }

        if (alertRuleService != null) {
            alertRuleService.evaluateRules(deviceId, dataType, value);
        }
        
        return deviceData;
    }

    public DeviceData saveDeviceData(String deviceId, String dataType, Double value) {
        return saveDeviceData(deviceId, dataType, value, getDefaultUnit(dataType));
    }

    private String getDefaultUnit(String dataType) {
        switch (dataType.toLowerCase()) {
            case "temperature":
                return "C";
            case "humidity":
                return "%";
            case "pressure":
                return "kPa";
            case "voltage":
                return "V";
            case "current":
                return "A";
            case "power":
                return "W";
            default:
                return "";
        }
    }

    public List<DeviceData> getLatestData(String deviceId, int limit) {
        return baseMapper.findLatestByDeviceId(deviceId, limit);
    }

    public List<DeviceData> getDataByType(String deviceId, String dataType, int limit) {
        return baseMapper.findByDeviceIdAndType(deviceId, dataType, limit);
    }

    public List<DeviceData> getDataByTimeRange(String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.findByDeviceIdAndTimeRange(deviceId, startTime, endTime);
    }

    public List<DeviceData> listByDeviceId(String deviceId) {
        LambdaQueryWrapper<DeviceData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeviceData::getDeviceId, deviceId)
              .orderByDesc(DeviceData::getRecordedAt);
        return list(wrapper);
    }

    public long countByDeviceId(String deviceId) {
        return baseMapper.countByDeviceId(deviceId);
    }

    public DeviceDataStats getStats(String deviceId, String dataType, 
                                    LocalDateTime startTime, LocalDateTime endTime) {
        DeviceDataMapper.DeviceDataStats mapperStats = baseMapper.getStatsByDeviceIdAndTimeRange(deviceId, dataType, startTime, endTime);
        if (mapperStats == null) {
            return null;
        }
        DeviceDataStats stats = new DeviceDataStats();
        stats.setAvgValue(mapperStats.getAvgValue());
        stats.setMinValue(mapperStats.getMinValue());
        stats.setMaxValue(mapperStats.getMaxValue());
        return stats;
    }

    public void cleanOldData(int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        LambdaQueryWrapper<DeviceData> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(DeviceData::getRecordedAt, cutoffTime);
        remove(wrapper);
        logger.info("Cleaned device data older than {} days", daysToKeep);
    }
}
