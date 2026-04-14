package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.entity.UserData;
import com.sustbbgz.virtualspringbootbackend.service.UserDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/userdata")
public class UserDataController {

    @Autowired
    private UserDataService userDataService;

    // 获取所有用户数据
    @GetMapping
    public List<UserData> findAll() {
        return userDataService.findAll();
    }

    // 根据 ID 获取用户数据
    @GetMapping("/{id}")
    public UserData findById(@PathVariable Long id) {
        return userDataService.findById(id);
    }

    // 添加用户数据
    @PostMapping("/save")
    public ResponseEntity<Void> addUserData(@RequestBody UserData userdata) {
        userDataService.save(userdata);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }


}
