package com.accounting.controller;

import com.accounting.dto.UserSettingRequest;
import com.accounting.service.UserSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class UserSettingController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSettingController.class);
    
    @Autowired
    private UserSettingService userSettingService;
    
    /**
     * 获取用户默认餐饮项目
     */
    @GetMapping("/default-meal-items")
    public ResponseEntity<Map<String, Object>> getDefaultMealItems() {
        logger.info("获取用户默认餐饮项目");
        
        try {
            List<String> mealItems = userSettingService.getDefaultMealItems();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", mealItems);
            response.put("message", "获取成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取默认餐饮项目失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 保存用户默认餐饮项目
     */
    @PostMapping("/default-meal-items")
    public ResponseEntity<Map<String, Object>> saveDefaultMealItems(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> mealItems = (List<String>) request.get("mealItems");
        
        logger.info("保存用户默认餐饮项目: mealItems={}", mealItems);
        
        try {
            if (mealItems == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = userSettingService.saveDefaultMealItems(mealItems);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "保存成功" : "保存失败");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("保存默认餐饮项目失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取用户设置
     */
    @GetMapping("/{userId}/{settingKey}")
    public ResponseEntity<Map<String, Object>> getUserSetting(@PathVariable String userId, @PathVariable String settingKey) {
        logger.info("获取用户设置: userId={}, settingKey={}", userId, settingKey);
        
        try {
            String settingValue = userSettingService.getUserSetting(userId, settingKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", settingValue);
            response.put("message", "获取成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户设置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 保存用户设置
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveUserSetting(@RequestBody UserSettingRequest request) {
        logger.info("保存用户设置: userId={}, settingKey={}", request.getUserId(), request.getSettingKey());
        
        try {
            if (request.getUserId() == null || request.getSettingKey() == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "参数不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = userSettingService.saveUserSetting(
                request.getUserId(), 
                request.getSettingKey(), 
                request.getSettingValue()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "保存成功" : "保存失败");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("保存用户设置失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
