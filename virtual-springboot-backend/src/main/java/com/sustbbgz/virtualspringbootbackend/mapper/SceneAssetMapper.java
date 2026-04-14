package com.sustbbgz.virtualspringbootbackend.mapper;
import com.sustbbgz.virtualspringbootbackend.entity.SceneAsset;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SceneAssetMapper {

    @Insert("INSERT INTO scene_assets (id, name, file_type, path, description, texture_info) VALUES (#{id}, #{name}, #{fileType}, #{path}, #{description}, #{textureInfo})")
    int insert(SceneAsset a);

    @Select("SELECT * FROM scene_assets ORDER BY created_at DESC")
    List<SceneAsset> list();

    @Select("SELECT * FROM scene_assets WHERE id = #{id}")
    SceneAsset getById(@Param("id") Long id);

    @Delete("DELETE FROM scene_assets WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
    
    @Update("UPDATE scene_assets SET texture_info = #{textureInfo} WHERE id = #{id}")
    int updateTextureInfo(@Param("id") Long id, @Param("textureInfo") String textureInfo);
}