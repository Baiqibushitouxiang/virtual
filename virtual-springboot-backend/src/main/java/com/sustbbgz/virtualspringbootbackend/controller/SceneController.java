package com.sustbbgz.virtualspringbootbackend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/scenes")
public class SceneController {

    @PostMapping
    public ResponseEntity<String> saveScene(@RequestBody String sceneData) {
        // 这里可以将sceneData保存到数据库或文件
        System.out.println("收到场景数据: " + sceneData);
        // 假设保存成功
        return new ResponseEntity<>("场景保存成功", HttpStatus.CREATED);
    }
}