package com.accounting.controller;

import com.accounting.dto.MealRecordRequest;
import com.accounting.entity.MealRecord;
import com.accounting.service.MealRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/meal")
@CrossOrigin(origins = "*")
public class MealRecordController {

    @Autowired
    private MealRecordService mealRecordService;

    /**
     * 保存餐饮记录
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveMealRecord(@RequestBody MealRecordRequest request) {
        try {
            MealRecord record = mealRecordService.saveOrUpdate(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "保存成功");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据日期获取餐饮记录
     */
    @GetMapping("/get/{date}")
    public ResponseEntity<Map<String, Object>> getMealRecord(@PathVariable String date) {
        try {
            LocalDate recordDate = LocalDate.parse(date);
            MealRecord record = mealRecordService.getByDate(recordDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除指定日期的记录
     */
    @DeleteMapping("/delete/{date}")
    public ResponseEntity<Map<String, Object>> deleteMealRecord(@PathVariable String date) {
        try {
            LocalDate recordDate = LocalDate.parse(date);
            boolean deleted = mealRecordService.deleteByDate(recordDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "删除成功" : "记录不存在");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}

