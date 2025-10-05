package com.accounting.service;

import com.accounting.dto.MealRecordRequest;
import com.accounting.entity.MealRecord;
import com.accounting.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MealRecordService {

    @Autowired
    private com.accounting.mapper.MealRecordMapper mealRecordMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 保存或更新餐饮记录
     */
    public MealRecord saveOrUpdate(MealRecordRequest request) {
        // 获取当前用户ID
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 检查是否已存在该日期的记录（仅限当前用户）
        QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("record_date", request.getRecordDate())
                   .eq("user_id", currentUserId);
        MealRecord existingRecord = mealRecordMapper.selectOne(queryWrapper);

        MealRecord record;
        if (existingRecord != null) {
            // 更新现有记录
            record = existingRecord;
        } else {
            // 创建新记录
            record = new MealRecord();
            record.setRecordDate(request.getRecordDate());
            record.setUserId(currentUserId);
            record.setCreateTime(LocalDateTime.now());
        }

        // 设置餐饮数据
        record.setBreakfast(request.getBreakfast() != null ? request.getBreakfast() : BigDecimal.ZERO);
        record.setLunch(request.getLunch() != null ? request.getLunch() : BigDecimal.ZERO);
        record.setDinner(request.getDinner() != null ? request.getDinner() : BigDecimal.ZERO);
        record.setSnack(request.getSnack() != null ? request.getSnack() : BigDecimal.ZERO);
        record.setDrink(request.getDrink() != null ? request.getDrink() : BigDecimal.ZERO);

        // 处理动态餐饮项目
        if (request.getCustomItems() != null && !request.getCustomItems().isEmpty()) {
            try {
                String customItemsJson = objectMapper.writeValueAsString(request.getCustomItems());
                System.out.println("动态项目JSON: " + customItemsJson);
                record.setCustomItems(customItemsJson);
            } catch (JsonProcessingException e) {
                System.err.println("序列化动态项目失败: " + e.getMessage());
                throw new RuntimeException("序列化动态项目失败", e);
            }
        } else {
            System.out.println("没有动态项目数据");
            record.setCustomItems(null);
        }

        // 计算总计（包括动态项目）
        BigDecimal total = record.getBreakfast()
                .add(record.getLunch())
                .add(record.getDinner())
                .add(record.getSnack())
                .add(record.getDrink());
        
        // 加上动态项目的金额
        if (request.getCustomItems() != null) {
            for (BigDecimal amount : request.getCustomItems().values()) {
                if (amount != null) {
                    total = total.add(amount);
                }
            }
        }
        
        record.setTotal(total);

        record.setUpdateTime(LocalDateTime.now());

        if (existingRecord != null) {
            mealRecordMapper.updateById(record);
        } else {
            mealRecordMapper.insert(record);
        }

        return record;
    }

    /**
     * 根据日期获取餐饮记录
     */
    public MealRecord getByDate(LocalDate date) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("record_date", date)
                   .eq("user_id", currentUserId);
        return mealRecordMapper.selectOne(queryWrapper);
    }

    /**
     * 删除指定日期的记录
     */
    public boolean deleteByDate(LocalDate date) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("record_date", date)
                   .eq("user_id", currentUserId);
        return mealRecordMapper.delete(queryWrapper) > 0;
    }

    /**
     * 删除指定日期的动态项目
     */
    @Transactional
    public boolean deleteCustomItems(LocalDate date, List<String> itemNames) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        System.out.println("=== 强制删除方法 ===");
        System.out.println("日期: " + date);
        System.out.println("要删除的项目: " + itemNames);
        
        try {
            // 先查询记录（仅限当前用户）
            QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("record_date", date)
                       .eq("user_id", currentUserId);
            MealRecord record = mealRecordMapper.selectOne(queryWrapper);
            
            if (record == null) {
                System.out.println("记录不存在");
                return false;
            }
            
            System.out.println("找到记录ID: " + record.getId());
            System.out.println("删除前custom_items: " + record.getCustomItems());
            
            // 解析现有动态项目
            Map<String, BigDecimal> customItems = new HashMap<>();
            if (record.getCustomItems() != null && !record.getCustomItems().trim().isEmpty()) {
                try {
                    customItems = objectMapper.readValue(record.getCustomItems(), 
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, BigDecimal.class));
                } catch (Exception e) {
                    System.out.println("解析custom_items失败: " + e.getMessage());
                }
            }
            
            System.out.println("删除前动态项目: " + customItems);
            
            // 删除指定项目
            boolean hasChanges = false;
            for (String itemName : itemNames) {
                if (customItems.containsKey(itemName)) {
                    customItems.remove(itemName);
                    hasChanges = true;
                    System.out.println("删除项目: " + itemName);
                }
            }
            
            System.out.println("删除后动态项目: " + customItems);
            
            // 更新记录
            if (customItems.isEmpty()) {
                record.setCustomItems(null);
                System.out.println("设置为NULL");
            } else {
                String updatedJson = objectMapper.writeValueAsString(customItems);
                record.setCustomItems(updatedJson);
                System.out.println("更新为: " + updatedJson);
            }
            
            // 如果没有变化，也要更新数据库
            if (!hasChanges) {
                System.out.println("没有找到要删除的项目，但继续更新数据库");
            }
            
            // 重新计算总计
            BigDecimal total = BigDecimal.ZERO;
            
            // 加上固定项目
            if (record.getBreakfast() != null) total = total.add(record.getBreakfast());
            if (record.getLunch() != null) total = total.add(record.getLunch());
            if (record.getDinner() != null) total = total.add(record.getDinner());
            if (record.getSnack() != null) total = total.add(record.getSnack());
            if (record.getDrink() != null) total = total.add(record.getDrink());
            
            // 加上剩余动态项目
            for (BigDecimal amount : customItems.values()) {
                if (amount != null) {
                    total = total.add(amount);
                }
            }
            
            record.setTotal(total);
            record.setUpdateTime(LocalDateTime.now());
            
            System.out.println("准备更新数据库");
            System.out.println("新的custom_items: " + record.getCustomItems());
            System.out.println("新的total: " + record.getTotal());
            
            // 强制更新
            int result = mealRecordMapper.updateById(record);
            System.out.println("数据库更新结果: " + result);
            
            // 立即验证
            MealRecord verifyRecord = mealRecordMapper.selectById(record.getId());
            System.out.println("验证结果 - custom_items: " + verifyRecord.getCustomItems());
            System.out.println("验证结果 - total: " + verifyRecord.getTotal());
            
            return result > 0;
            
        } catch (Exception e) {
            System.err.println("删除失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 完全清空指定日期的所有数据
     */
    @Transactional
    public boolean clearAllData(LocalDate date) {
        // 获取当前用户ID
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        System.out.println("=== 完全清空数据 ===");
        System.out.println("日期: " + date);
        System.out.println("用户ID: " + currentUserId);
        
        try {
            QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("record_date", date)
                       .eq("user_id", currentUserId);
            MealRecord record = mealRecordMapper.selectOne(queryWrapper);
            
            if (record == null) {
                System.out.println("记录不存在");
                return false;
            }
            
            System.out.println("找到记录ID: " + record.getId());
            System.out.println("清空前的customItems: " + record.getCustomItems());
            
            // 清空所有数据
            record.setBreakfast(BigDecimal.ZERO);
            record.setLunch(BigDecimal.ZERO);
            record.setDinner(BigDecimal.ZERO);
            record.setSnack(BigDecimal.ZERO);
            record.setDrink(BigDecimal.ZERO);
            record.setCustomItems(""); // 设置为空字符串而不是null
            record.setTotal(BigDecimal.ZERO);
            record.setUpdateTime(LocalDateTime.now());
            
            System.out.println("准备清空所有数据");
            System.out.println("清空后的customItems: " + record.getCustomItems());
            
            int result = mealRecordMapper.updateById(record);
            System.out.println("清空结果: " + result);
            
            // 验证清空结果
            MealRecord verifyRecord = mealRecordMapper.selectById(record.getId());
            System.out.println("验证结果 - customItems: " + verifyRecord.getCustomItems());
            
            return result > 0;
            
        } catch (Exception e) {
            System.err.println("清空数据失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
}


