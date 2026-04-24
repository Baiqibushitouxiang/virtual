package com.sustbbgz.virtualspringbootbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sustbbgz.virtualspringbootbackend.entity.ModelDataBinding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ModelDataBindingMapper extends BaseMapper<ModelDataBinding> {

    @Select("SELECT * FROM model_data_binding WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<ModelDataBinding> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM model_data_binding WHERE scene_id = #{sceneId} ORDER BY update_time DESC")
    List<ModelDataBinding> findBySceneId(@Param("sceneId") Long sceneId);

    @Select("SELECT * FROM model_data_binding WHERE scene_id = #{sceneId} AND model_id = #{modelId} LIMIT 1")
    ModelDataBinding findBySceneIdAndModelId(@Param("sceneId") Long sceneId, @Param("modelId") String modelId);
}
