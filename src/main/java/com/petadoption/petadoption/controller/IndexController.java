package com.petadoption.petadoption.controller;

import com.petadoption.petadoption.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class IndexController {

    @GetMapping("/")
    @Operation(summary = "API 欢迎页面", description = "宠物领养系统 API 欢迎页面")
    public ApiResponse<Map<String, Object>> index() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "宠物领养系统 API");
        data.put("version", "1.0.0");
        data.put("description", "宠物领养系统后端 API 服务");
        data.put("docs", "/api/doc.html");
        data.put("status", "running");
        
        return ApiResponse.success(data);
    }
}