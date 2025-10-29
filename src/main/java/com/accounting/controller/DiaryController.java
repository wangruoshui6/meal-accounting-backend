package com.accounting.controller;

import com.accounting.entity.Diary;
import com.accounting.service.DiaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日记控制器
 */
@RestController
@RequestMapping("/api/diary")
@CrossOrigin(origins = "*")
public class DiaryController {
    
    private static final Logger logger = LoggerFactory.getLogger(DiaryController.class);
    
    @Autowired
    private DiaryService diaryService;
    
    /**
     * 保存或更新日记
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveDiary(@RequestBody Map<String, Object> request) {
        logger.info("收到保存日记请求: {}", request);
        
        try {
            String itemName = (String) request.get("itemName");
            String content = (String) request.get("content");
            String dateStr = (String) request.get("date");
            
            if (itemName == null || content == null || dateStr == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "参数不完整");
                return ResponseEntity.badRequest().body(response);
            }
            
            LocalDate recordDate = LocalDate.parse(dateStr);
            diaryService.saveOrUpdateDiary(itemName, content, recordDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "保存成功");
            
            logger.info("日记保存成功: 项目={}, 日期={}", itemName, recordDate);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("保存日记失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取指定日期和项目的日记内容
     */
    @GetMapping("/content")
    public ResponseEntity<Map<String, Object>> getDiaryContent(
            @RequestParam String itemName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        logger.info("获取日记内容请求: 项目={}, 日期={}", itemName, date);
        
        try {
            String content = diaryService.getDiaryContent(itemName, date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", content);
            response.put("message", "获取成功");
            
            logger.info("获取日记内容成功: 项目={}, 日期={}", itemName, date);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取日记内容失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取指定日期的所有日记
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getDiariesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        logger.info("获取日记列表请求: 日期={}", date);
        
        try {
            List<Diary> diaries = diaryService.getDiariesByDate(date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", diaries);
            response.put("message", "获取成功");
            
            logger.info("获取日记列表成功: 日期={}, 数量={}", date, diaries.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取日记列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 删除日记
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteDiary(
            @RequestParam String itemName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        logger.info("删除日记请求: 项目={}, 日期={}", itemName, date);
        
        try {
            diaryService.deleteDiary(itemName, date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "删除成功");
            
            logger.info("删除日记成功: 项目={}, 日期={}", itemName, date);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("删除日记失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
