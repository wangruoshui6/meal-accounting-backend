package com.accounting.agent.service;

import com.accounting.entity.MealRecord;
import com.accounting.service.MealRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据分析服务 - 为 AI 提供用户数据上下文
 */
@Service
public class AnalysisService {
    
    @Autowired
    private MealRecordService mealRecordService;
    
    /**
     * 获取用户最近一段时间的餐饮数据摘要
     * 
     * @param days 最近多少天
     * @return 数据摘要文本
     */
    public String getUserDataSummary(int days) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);
            
            System.out.println("=== AnalysisService: 获取用户数据摘要 ===");
            System.out.println("查询日期范围: " + startDate + " 到 " + endDate);
            
            List<MealRecord> records = mealRecordService.getRecordsByDateRange(startDate, endDate);
            
            System.out.println("查询到的记录数: " + (records != null ? records.size() : 0));
            
            // 如果最近N天没有数据，尝试查询所有历史数据
            if (records == null || records.isEmpty()) {
                System.out.println("最近 " + days + " 天没有数据，尝试查询所有历史数据");
                // 查询所有历史数据（从一年前开始）
                LocalDate allStartDate = endDate.minusYears(1);
                records = mealRecordService.getRecordsByDateRange(allStartDate, endDate);
                
                if (records == null || records.isEmpty()) {
                    System.out.println("所有历史数据也为空");
                    return "您目前还没有任何餐饮记录。建议您开始记录每日的餐饮消费，这样我可以为您提供更准确的分析和建议。";
                } else {
                    System.out.println("找到历史数据: " + records.size() + " 条");
                    // 使用所有历史数据，但提示用户
                    StringBuilder summary = new StringBuilder();
                    summary.append("（注：您最近 ").append(days).append(" 天没有新记录，以下是您的历史数据）\n\n");
                    summary.append(buildSummaryContent(records, "历史"));
                    return summary.toString();
                }
            }
            
            return buildSummaryContent(records, "最近 " + days + " 天");
        } catch (Exception e) {
            System.err.println("获取数据摘要失败: " + e.getMessage());
            e.printStackTrace();
            return "获取数据时发生错误: " + e.getMessage();
        }
    }
    
    /**
     * 构建数据摘要内容
     */
    private String buildSummaryContent(List<MealRecord> records, String periodLabel) {
        // 统计数据
        BigDecimal totalAmount = BigDecimal.ZERO;
        int recordCount = records.size();
        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        categoryTotals.put("早饭", BigDecimal.ZERO);
        categoryTotals.put("午饭", BigDecimal.ZERO);
        categoryTotals.put("晚饭", BigDecimal.ZERO);
        categoryTotals.put("零食", BigDecimal.ZERO);
        categoryTotals.put("饮料", BigDecimal.ZERO);
        
        // 统计动态项目
        Map<String, BigDecimal> customItemsTotals = new HashMap<>();
        
        for (MealRecord record : records) {
            if (record.getTotal() != null) {
                totalAmount = totalAmount.add(record.getTotal());
            }
            if (record.getBreakfast() != null && record.getBreakfast().compareTo(BigDecimal.ZERO) > 0) {
                categoryTotals.put("早饭", categoryTotals.get("早饭").add(record.getBreakfast()));
            }
            if (record.getLunch() != null && record.getLunch().compareTo(BigDecimal.ZERO) > 0) {
                categoryTotals.put("午饭", categoryTotals.get("午饭").add(record.getLunch()));
            }
            if (record.getDinner() != null && record.getDinner().compareTo(BigDecimal.ZERO) > 0) {
                categoryTotals.put("晚饭", categoryTotals.get("晚饭").add(record.getDinner()));
            }
            if (record.getSnack() != null && record.getSnack().compareTo(BigDecimal.ZERO) > 0) {
                categoryTotals.put("零食", categoryTotals.get("零食").add(record.getSnack()));
            }
            if (record.getDrink() != null && record.getDrink().compareTo(BigDecimal.ZERO) > 0) {
                categoryTotals.put("饮料", categoryTotals.get("饮料").add(record.getDrink()));
            }
            
            // 处理动态项目
            if (record.getCustomItems() != null && !record.getCustomItems().trim().isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, BigDecimal> customItems = mapper.readValue(
                        record.getCustomItems(),
                        mapper.getTypeFactory().constructMapType(
                            java.util.Map.class, String.class, BigDecimal.class
                        )
                    );
                    for (Map.Entry<String, BigDecimal> entry : customItems.entrySet()) {
                        if (entry.getValue() != null && entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                            customItemsTotals.put(
                                entry.getKey(),
                                customItemsTotals.getOrDefault(entry.getKey(), BigDecimal.ZERO)
                                    .add(entry.getValue())
                            );
                        }
                    }
                } catch (Exception e) {
                    System.err.println("解析动态项目失败: " + e.getMessage());
                }
            }
        }
        
        BigDecimal avgDaily = recordCount > 0 ? 
            totalAmount.divide(BigDecimal.valueOf(recordCount), 2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        // 构建摘要文本
        StringBuilder summary = new StringBuilder();
        summary.append("【").append(periodLabel).append("餐饮数据摘要】\n");
        summary.append("记录天数: ").append(recordCount).append(" 天\n");
        summary.append("总消费: ¥").append(totalAmount.setScale(2, RoundingMode.HALF_UP)).append("\n");
        summary.append("日均消费: ¥").append(avgDaily).append("\n");
        summary.append("\n各项目消费:\n");
        
        // 固定项目
        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                summary.append("- ").append(entry.getKey())
                       .append(": ¥").append(entry.getValue().setScale(2, RoundingMode.HALF_UP)).append("\n");
            }
        }
        
        // 动态项目
        for (Map.Entry<String, BigDecimal> entry : customItemsTotals.entrySet()) {
            summary.append("- ").append(entry.getKey())
                   .append(": ¥").append(entry.getValue().setScale(2, RoundingMode.HALF_UP)).append("\n");
        }
        
        // 添加日期范围信息
        if (!records.isEmpty()) {
            LocalDate earliestDate = records.stream()
                .map(MealRecord::getRecordDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
            LocalDate latestDate = records.stream()
                .map(MealRecord::getRecordDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
            summary.append("\n数据日期范围: ").append(earliestDate).append(" 至 ").append(latestDate).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * 获取用户本月数据摘要
     */
    public String getCurrentMonthSummary() {
        LocalDate now = LocalDate.now();
        int daysInMonth = now.lengthOfMonth();
        return getUserDataSummary(daysInMonth);
    }
    
    /**
     * 获取用户最近一周数据摘要
     */
    public String getRecentWeekSummary() {
        return getUserDataSummary(7);
    }
}

