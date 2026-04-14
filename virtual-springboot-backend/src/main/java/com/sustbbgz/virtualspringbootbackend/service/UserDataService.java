package com.sustbbgz.virtualspringbootbackend.service;
import com.sustbbgz.virtualspringbootbackend.entity.User;
import com.sustbbgz.virtualspringbootbackend.entity.UserData;
import com.sustbbgz.virtualspringbootbackend.mapper.UserDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserDataService {
    @Autowired
    private UserDataMapper userDataMapper;

    public List<UserData> findAll() {
        return userDataMapper.findAll();
    }
    ;

    public UserData findById(Long id) {
        return userDataMapper.findById(id);
    }
    ;

    public void save(UserData userData) {
        userDataMapper.save(userData);
    }
    ;

    public void update(UserData userData) {
        userDataMapper.update(userData);
    }
}
