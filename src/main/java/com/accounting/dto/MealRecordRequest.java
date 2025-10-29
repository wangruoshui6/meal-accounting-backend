package com.accounting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
public class MealRecordRequest {
    
    @JsonProperty("recordDate")
    private LocalDate recordDate;
    
    private BigDecimal breakfast;
    
    private BigDecimal lunch;
    
    private BigDecimal dinner;
    
    private BigDecimal snack;
    
    private BigDecimal drink;
    
    // 动态餐饮项目
    private Map<String, BigDecimal> customItems;
}

@Data
class MealRecordResponse {
    
    private Long id;
    
    private LocalDate recordDate;
    
    private BigDecimal breakfast;
    
    private BigDecimal lunch;
    
    private BigDecimal dinner;
    
    private BigDecimal snack;
    
    private BigDecimal drink;
    
    private BigDecimal total;
    
    private String createTime;
}
