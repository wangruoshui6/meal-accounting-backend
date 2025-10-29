package com.accounting.controller;

import com.accounting.dto.MealRecordRequest;
import com.accounting.dto.DeleteItemsRequest;
import com.accounting.entity.MealRecord;
import com.accounting.service.MealRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meal")
@CrossOrigin(origins = "*")
public class MealRecordController {

    private static final Logger logger = LoggerFactory.getLogger(MealRecordController.class);
    
    /**
     * 测试端点
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        logger.info("收到测试请求");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "后端服务正常运行");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Autowired
    private MealRecordService mealRecordService;

    /**
     * 保存餐饮记录
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveMealRecord(@RequestBody MealRecordRequest request) {
        logger.info("收到保存餐饮记录请求: {}", request);
        logger.info("请求详情 - 日期: {}, 早饭: {}, 午饭: {}, 晚饭: {}, 零食: {}, 饮料: {}, 动态项目: {}", 
            request.getRecordDate(), request.getBreakfast(), request.getLunch(), 
            request.getDinner(), request.getSnack(), request.getDrink(), request.getCustomItems());
        
        try {
            // 验证请求数据
            if (request == null) {
                throw new IllegalArgumentException("请求数据不能为空");
            }
            
            if (request.getRecordDate() == null) {
                throw new IllegalArgumentException("记录日期不能为空");
            }
            
            // 验证金额不能为负数
            if (request.getBreakfast() != null && request.getBreakfast().compareTo(BigDecimal.ZERO) < 0 ||
                request.getLunch() != null && request.getLunch().compareTo(BigDecimal.ZERO) < 0 ||
                request.getDinner() != null && request.getDinner().compareTo(BigDecimal.ZERO) < 0 ||
                request.getSnack() != null && request.getSnack().compareTo(BigDecimal.ZERO) < 0 ||
                request.getDrink() != null && request.getDrink().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("金额不能为负数");
            }
            
            // 验证动态项目金额不能为负数
            if (request.getCustomItems() != null) {
                for (Map.Entry<String, BigDecimal> entry : request.getCustomItems().entrySet()) {
                    if (entry.getValue() != null && entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("动态项目 \"" + entry.getKey() + "\" 的金额不能为负数");
                    }
                }
            }
            
            MealRecord record = mealRecordService.saveOrUpdate(request);
            logger.info("餐饮记录保存成功: ID={}, 日期={}", record.getId(), record.getRecordDate());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "保存成功");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("保存餐饮记录参数错误: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "参数错误: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("保存餐饮记录失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据日期获取餐饮记录
     */
    @GetMapping("/get/{date}")
    public ResponseEntity<Map<String, Object>> getMealRecord(@PathVariable String date) {
        logger.info("收到获取餐饮记录请求: 日期={}", date);
        
        try {
            // 验证日期格式
            if (date == null || date.trim().isEmpty()) {
                throw new IllegalArgumentException("日期不能为空");
            }
            
            LocalDate recordDate = LocalDate.parse(date);
            MealRecord record = mealRecordService.getByDate(recordDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", record);
            
            if (record != null) {
                logger.info("找到餐饮记录: ID={}, 日期={}", record.getId(), record.getRecordDate());
            } else {
                logger.info("未找到日期为 {} 的餐饮记录", date);
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("获取餐饮记录参数错误: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "参数错误: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("获取餐饮记录失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 删除指定日期的记录
     */
    @DeleteMapping("/delete/{date}")
    public ResponseEntity<Map<String, Object>> deleteMealRecord(@PathVariable String date) {
        logger.info("收到删除餐饮记录请求: 日期={}", date);
        
        try {
            // 验证日期格式
            if (date == null || date.trim().isEmpty()) {
                throw new IllegalArgumentException("日期不能为空");
            }
            
            LocalDate recordDate = LocalDate.parse(date);
            boolean deleted = mealRecordService.deleteByDate(recordDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "删除成功" : "记录不存在");
            
            if (deleted) {
                logger.info("餐饮记录删除成功: 日期={}", date);
            } else {
                logger.info("未找到要删除的餐饮记录: 日期={}", date);
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("删除餐饮记录参数错误: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "参数错误: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("删除餐饮记录失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 删除指定日期的动态项目
     */
    @PostMapping("/delete-items/{date}")
    public ResponseEntity<Map<String, Object>> deleteCustomItems(@PathVariable String date, @RequestBody DeleteItemsRequest request) {
        logger.info("收到删除动态项目请求: 日期={}, 项目={}", date, request.getItemNames());
        
        try {
            // 验证日期格式
            if (date == null || date.trim().isEmpty()) {
                throw new IllegalArgumentException("日期不能为空");
            }
            
            LocalDate recordDate = LocalDate.parse(date);
            
            // 获取要删除的项目名称列表
            java.util.List<String> itemNames = request.getItemNames();
            if (itemNames == null || itemNames.isEmpty()) {
                throw new IllegalArgumentException("要删除的项目名称不能为空");
            }
            
            boolean updated = mealRecordService.deleteCustomItems(recordDate, itemNames);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", updated);
            response.put("message", updated ? "动态项目删除成功" : "记录不存在或项目不存在");
            
            if (updated) {
                logger.info("动态项目删除成功: 日期={}, 项目={}", date, itemNames);
            } else {
                logger.info("未找到要删除的动态项目: 日期={}, 项目={}", date, itemNames);
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("删除动态项目参数错误: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "参数错误: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("删除动态项目失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 完全清空指定日期的所有数据
     */
    @PostMapping("/clear-all/{date}")
    public ResponseEntity<Map<String, Object>> clearAllData(@PathVariable String date) {
        logger.info("收到清空所有数据请求: 日期={}", date);
        
        try {
            if (date == null || date.trim().isEmpty()) {
                throw new IllegalArgumentException("日期不能为空");
            }
            
            LocalDate recordDate = LocalDate.parse(date);
            boolean cleared = mealRecordService.clearAllData(recordDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", cleared);
            response.put("message", cleared ? "所有数据已清空" : "清空失败");
            
            if (cleared) {
                logger.info("所有数据清空成功: 日期={}", date);
            } else {
                logger.info("清空失败: 日期={}", date);
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("清空数据参数错误: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "参数错误: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("清空数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清空失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取指定月份有记录的日期列表
     */
    @GetMapping("/record-dates/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getRecordDates(@PathVariable int year, @PathVariable int month) {
        logger.info("获取记录日期请求: 年={}, 月={}", year, month);
        
        try {
            List<String> recordDates = mealRecordService.getRecordDates(year, month);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", recordDates);
            response.put("message", "获取成功");
            
            logger.info("获取记录日期成功: 年={}, 月={}, 数量={}", year, month, recordDates.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取记录日期失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取用户统计概览
     */
    @GetMapping("/user-statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        logger.info("收到获取用户统计概览请求");
        
        try {
            Map<String, Object> statistics = mealRecordService.getUserStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            response.put("message", "获取成功");
            
            logger.info("获取用户统计概览成功: {}", statistics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取用户统计概览失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取指定日期范围的餐饮记录（用于统计）
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getMealRecordsByDateRange(
            @RequestParam String startDate, 
            @RequestParam String endDate) {
        logger.info("收到获取统计数据请求: 开始日期={}, 结束日期={}", startDate, endDate);
        
        try {
            // 验证日期格式
            if (startDate == null || startDate.trim().isEmpty() || 
                endDate == null || endDate.trim().isEmpty()) {
                throw new IllegalArgumentException("开始日期和结束日期不能为空");
            }
            
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("开始日期不能晚于结束日期");
            }
            
            List<MealRecord> records = mealRecordService.getRecordsByDateRange(start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", records);
            response.put("message", "获取成功");
            
            logger.info("获取统计数据成功: 开始日期={}, 结束日期={}, 记录数量={}", startDate, endDate, records.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("获取统计数据参数错误: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "参数错误: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("获取统计数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

