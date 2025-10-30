package com.accounting.controller;

import com.accounting.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "用户名不能为空"
            ));
        }
        
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "密码不能为空"
            ));
        }
        
        // 验证用户名格式（1-6个中文汉字）
        if (!username.matches("^[\u4e00-\u9fa5]{1,6}$")) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "用户名只能包含1-6个中文汉字"
            ));
        }
        
        // 验证密码格式（6-20位字母数字特殊字符）
        if (!password.matches("^[A-Za-z0-9\\W_]{6,20}$")) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "密码必须为6-20位字母、数字或特殊字符"
            ));
        }
        
        Map<String, Object> result = userService.register(username, password);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "用户名不能为空"
            ));
        }
        
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "密码不能为空"
            ));
        }
        
        Map<String, Object> result = userService.login(username, password);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "无效的token格式"
            ));
        }
        
        String actualToken = token.substring(7); // 移除 "Bearer " 前缀
        
        try {
            // 删除token缓存
            userService.getJwtUtil().logout(actualToken);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "登出成功"
            ));
        } catch (Exception e) {
            logger.error("登出失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "登出失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 验证token
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "无效的token格式"
            ));
        }
        
        String actualToken = token.substring(7); // 移除 "Bearer " 前缀
        
        try {
            String username = userService.getJwtUtil().getUsernameFromToken(actualToken);
            Long userId = userService.getJwtUtil().getUserIdFromToken(actualToken);
            
            if (userService.getJwtUtil().validateToken(actualToken, username)) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "token有效",
                    "user", Map.of(
                        "id", userId,
                        "username", username
                    )
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "token无效或已过期"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "token验证失败: " + e.getMessage()
            ));
        }
    }
}
