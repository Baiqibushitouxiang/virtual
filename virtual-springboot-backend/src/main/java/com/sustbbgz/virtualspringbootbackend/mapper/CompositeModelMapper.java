package com.sustbbgz.virtualspringbootbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sustbbgz.virtualspringbootbackend.entity.CompositeModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompositeModelMapper extends BaseMapper<CompositeModel> {
    CompositeModel selectById(@Param("id") Long id);
    
    CompositeModel selectByName(@Param("name") String name);
    
    List<CompositeModel> selectAll();
}