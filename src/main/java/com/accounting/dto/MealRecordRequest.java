package com.accounting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MealRecordRequest {
    
    @JsonProperty("date")
    private LocalDate recordDate;
    
    private BigDecimal breakfast;
    
    private BigDecimal lunch;
    
    private BigDecimal dinner;
    
    private BigDecimal snack;
    
    private BigDecimal drink;
    
    private BigDecimal other;
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
    
    private BigDecimal other;
    
    private BigDecimal total;
    
    private String createTime;
}
