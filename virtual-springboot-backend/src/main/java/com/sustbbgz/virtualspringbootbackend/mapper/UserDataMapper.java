package com.sustbbgz.virtualspringbootbackend.mapper;

import com.sustbbgz.virtualspringbootbackend.entity.UserData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserDataMapper {
    List<UserData> findAll();

    UserData findById(Long id);

    void save(UserData userData);

    void update(UserData userData);


}
