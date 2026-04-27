package com.sustbbgz.virtualspringbootbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sustbbgz.virtualspringbootbackend.entity.DataPanel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataPanelMapper extends BaseMapper<DataPanel> {

    @Select("SELECT * FROM data_panel WHERE user_id = #{userId} AND scene_id = #{sceneId} ORDER BY create_time DESC")
    List<DataPanel> findByUserIdAndSceneId(@Param("userId") Long userId, @Param("sceneId") Long sceneId);

    @Select("SELECT * FROM data_panel WHERE scene_id = #{sceneId} ORDER BY create_time DESC")
    List<DataPanel> findBySceneId(@Param("sceneId") Long sceneId);

    @Select("SELECT * FROM data_panel WHERE device_id = #{deviceId} AND scene_id = #{sceneId}")
    List<DataPanel> findByDeviceIdAndSceneId(@Param("deviceId") Long deviceId, @Param("sceneId") Long sceneId);

    @Select("SELECT * FROM data_panel WHERE model_id = #{modelId} AND scene_id = #{sceneId}")
    List<DataPanel> findByModelIdAndSceneId(@Param("modelId") String modelId, @Param("sceneId") Long sceneId);

    @Delete("DELETE FROM data_panel WHERE scene_id = #{sceneId}")
    int deleteBySceneId(@Param("sceneId") Long sceneId);
}
