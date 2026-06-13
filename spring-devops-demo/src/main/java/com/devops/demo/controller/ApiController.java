package com.devops.demo.controller;

import com.devops.demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private TaskService taskService;

    @Value("${app.version:2.0.0}")
    private String appVersion;

    @Value("${app.environment:development}")
    private String environment;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "status",      "UP",
                "version",     appVersion,
                "environment", environment,
                "timestamp",   Instant.now().toString(),
                "tasks", Map.of(
                        "total",     taskService.getTotalCount(),
                        "completed", taskService.getCompletedCount(),
                        "pending",   taskService.getTotalCount() - taskService.getCompletedCount()
                )
        ));
    }
}