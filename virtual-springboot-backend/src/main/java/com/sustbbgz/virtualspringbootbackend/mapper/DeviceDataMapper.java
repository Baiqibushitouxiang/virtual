package com.sustbbgz.virtualspringbootbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sustbbgz.virtualspringbootbackend.entity.DeviceData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DeviceDataMapper extends BaseMapper<DeviceData> {

    @Select("SELECT * FROM device_data WHERE device_id = #{deviceId} ORDER BY recorded_at DESC LIMIT #{limit}")
    List<DeviceData> findLatestByDeviceId(@Param("deviceId") String deviceId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM device_data WHERE device_id = #{deviceId}")
    long countByDeviceId(@Param("deviceId") String deviceId);

    @Select("SELECT * FROM device_data WHERE device_id = #{deviceId} AND data_type = #{dataType} ORDER BY recorded_at DESC LIMIT #{limit}")
    List<DeviceData> findByDeviceIdAndType(@Param("deviceId") String deviceId, 
                                            @Param("dataType") String dataType, 
                                            @Param("limit") int limit);

    @Select("SELECT * FROM device_data WHERE device_id = #{deviceId} AND recorded_at BETWEEN #{startTime} AND #{endTime} ORDER BY recorded_at DESC")
    List<DeviceData> findByDeviceIdAndTimeRange(@Param("deviceId") String deviceId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    @Select("SELECT AVG(value) as avg_value, MIN(value) as min_value, MAX(value) as max_value FROM device_data WHERE device_id = #{deviceId} AND data_type = #{dataType} AND recorded_at BETWEEN #{startTime} AND #{endTime}")
    DeviceDataStats getStatsByDeviceIdAndTimeRange(@Param("deviceId") String deviceId,
                                                    @Param("dataType") String dataType,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

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
}
