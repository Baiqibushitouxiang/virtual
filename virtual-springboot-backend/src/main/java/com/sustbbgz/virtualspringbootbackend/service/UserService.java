package com.sustbbgz.virtualspringbootbackend.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sustbbgz.virtualspringbootbackend.common.Constants;
import com.sustbbgz.virtualspringbootbackend.common.RoleEnum;
import com.sustbbgz.virtualspringbootbackend.controller.dto.UserDTO;
import com.sustbbgz.virtualspringbootbackend.entity.User;
import com.sustbbgz.virtualspringbootbackend.entity.UserData;
import com.sustbbgz.virtualspringbootbackend.exception.ServiceException;
import com.sustbbgz.virtualspringbootbackend.mapper.UserDataMapper;
import com.sustbbgz.virtualspringbootbackend.mapper.UserMapper;
import com.sustbbgz.virtualspringbootbackend.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class UserService {
    private static final Log LOG = Log.get();
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserDataMapper userDataMapper;

    public UserDTO login(UserDTO userDTO) {
        // 用户密码 md5加密
        userDTO.setPassword(SecureUtil.md5(userDTO.getPassword()));
        User user = getUserInfo(userDTO);
        if (user != null) {
            BeanUtil.copyProperties(user, userDTO, true);
            // 设置token
            String token = TokenUtils.genToken(user.getId().toString(), user.getPassword());
            userDTO.setToken(token);
            String role = user.getRole(); // ROLE_ADMIN
            return userDTO;
        } else {
            throw new ServiceException(Constants.CODE_600, "用户名或密码错误");
        }
    }

    // 注册时只需要参数username，password, phone, email，其余参数都是自动生成
    public User register(UserDTO userDTO) {
        // 用户密码 md5加密
        userDTO.setPassword(SecureUtil.md5(userDTO.getPassword()));
        User user = getUserInfo(userDTO);
        if (user == null) {
            user = new User();
            BeanUtil.copyProperties(userDTO, user, true);
            // 默认一个普通用户的角色
            user.setRole(RoleEnum.USER.toString());
            if (user.getUsername() == null) {
                user.setUsername(user.getUsername());
            }
            save(user);  // 把 copy完之后的用户对象存储到数据库
            // 注册用户时自动创建用户数据
            UserData userData = new UserData();
            userData.setUser(user);
            userData.setUserInfo("{}");
            userDataMapper.save(userData);
        } else {
            throw new ServiceException(Constants.CODE_600, "用户已存在");
        }
        return user;
    }


    public boolean save(User user) {
        return userMapper.save(user);
    }


    private User getUserInfo(UserDTO userDTO) {
        User user;
        try {
            user = findByUsernameAndPassword(userDTO); // 从数据库查询用户信息
        } catch (Exception e) {
            LOG.error(e);
            throw new ServiceException(Constants.CODE_500, "系统错误");
        }
        return user;
    }


    public List<User> findAll() {
        return userMapper.findAll();
    }


    public Page<User> findPage(Page<User> page, String username, String email, String address) {
        return userMapper.findPage(page, username, email, address);
    }

    public User findById(Long id) {
        return userMapper.findById(id);
    }


    public User update(User user) {
        userMapper.update(user);
        return user;
    }


    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }


    public User findByUsernameAndPassword(UserDTO userDTO) {
        return userMapper.findByUsernameAndPassword(userDTO);
    }


    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

}