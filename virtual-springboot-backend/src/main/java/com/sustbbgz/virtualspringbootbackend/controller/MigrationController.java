package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.service.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/migration")
public class MigrationController {

    @Autowired
    private MigrationService migrationService;

    @PostMapping("/all-to-cos")
    public ResponseEntity<Map<String, Object>> migrateAllToCos() {
        Map<String, Object> result = migrationService.migrateAllToCos();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/models-to-cos")
    public ResponseEntity<Map<String, Object>> migrateModelsToCos() {
        Map<String, Object> result = migrationService.migrateLocalModelsToCos();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/scenes-to-cos")
    public ResponseEntity<Map<String, Object>> migrateScenesToCos() {
        Map<String, Object> result = migrationService.migrateScenesToCos();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMigrationStatus() {
        Map<String, Object> status = migrationService.getMigrationStatus();
        return ResponseEntity.ok(status);
    }
}
