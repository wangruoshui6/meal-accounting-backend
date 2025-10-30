package com.accounting.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试Redis连接
     */
    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> testRedis() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 测试Redis连接
            redisTemplate.opsForValue().set("test_key", "test_value");
            String value = (String) redisTemplate.opsForValue().get("test_key");
            
            if ("test_value".equals(value)) {
                response.put("success", true);
                response.put("message", "Redis连接正常");
                response.put("test_value", value);
            } else {
                response.put("success", false);
                response.put("message", "Redis连接异常");
            }
            
            // 清理测试数据
            redisTemplate.delete("test_key");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Redis连接失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
