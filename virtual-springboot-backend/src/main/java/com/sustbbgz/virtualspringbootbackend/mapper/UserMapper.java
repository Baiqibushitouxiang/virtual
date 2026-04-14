package com.sustbbgz.virtualspringbootbackend.mapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sustbbgz.virtualspringbootbackend.controller.dto.UserDTO;
import com.sustbbgz.virtualspringbootbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

@Mapper
public interface UserMapper{
    List<User> findAll();

    User findById(Long id);

    boolean save(User user);

    void update(User user);

    void deleteById(Long id);

    User findByUsernameAndPassword(UserDTO userDTO);

    User findByUsername(String username);

    Page<User> findPage(Page<User> page, @Param("username") String username, @Param("email") String email, @Param("address") String address);
}
