package com.accounting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("meal_records")
public class MealRecord {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("record_date")
    private LocalDate recordDate;
    
    private BigDecimal breakfast;
    
    private BigDecimal lunch;
    
    private BigDecimal dinner;
    
    private BigDecimal snack;
    
    private BigDecimal drink;
    
    private BigDecimal total;
    
    // 动态餐饮项目，以JSON格式存储
    @TableField("custom_items")
    private String customItems;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}

