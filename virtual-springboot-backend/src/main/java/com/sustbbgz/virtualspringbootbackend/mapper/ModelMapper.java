package com.sustbbgz.virtualspringbootbackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sustbbgz.virtualspringbootbackend.entity.Model;


@Mapper
public interface ModelMapper extends BaseMapper<Model> {
    @Insert({
            "<script>",
            "INSERT INTO models (name, category) VALUES ",
            "<foreach collection='list' item='item' index='index' separator=','>",
            "(#{item.name}, #{item.category})",
            "</foreach>",
            "</script>"
    })
    void insertBatch(List<Model> models);
    
    Model findByName(String name);
}
