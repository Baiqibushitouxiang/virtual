package com.sustbbgz.virtualspringbootbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sustbbgz.virtualspringbootbackend.entity.CompositeModelComponent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompositeModelComponentMapper extends BaseMapper<CompositeModelComponent> {
    List<CompositeModelComponent> findByCompositeModelId(@Param("compositeModelId") Long compositeModelId);
    
    int deleteByCompositeModelId(@Param("compositeModelId") Long compositeModelId);
}