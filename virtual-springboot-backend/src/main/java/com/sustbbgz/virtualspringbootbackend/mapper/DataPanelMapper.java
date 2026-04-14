package com.sustbbgz.virtualspringbootbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sustbbgz.virtualspringbootbackend.entity.DataPanel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataPanelMapper extends BaseMapper<DataPanel> {

    @Select("SELECT * FROM data_panel WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<DataPanel> findByUserId(Long userId);

    @Select("SELECT * FROM data_panel WHERE device_id = #{deviceId}")
    List<DataPanel> findByDeviceId(Long deviceId);

    @Select("SELECT * FROM data_panel WHERE model_id = #{modelId}")
    List<DataPanel> findByModelId(String modelId);
}
