package com.accounting.service;

import com.accounting.dto.MealRecordRequest;
import com.accounting.entity.MealRecord;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class MealRecordService {

    @Autowired
    private com.accounting.mapper.MealRecordMapper mealRecordMapper;

    /**
     * 保存或更新餐饮记录
     */
    public MealRecord saveOrUpdate(MealRecordRequest request) {
        // 检查是否已存在该日期的记录
        QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("record_date", request.getRecordDate());
        MealRecord existingRecord = mealRecordMapper.selectOne(queryWrapper);

        MealRecord record;
        if (existingRecord != null) {
            // 更新现有记录
            record = existingRecord;
        } else {
            // 创建新记录
            record = new MealRecord();
            record.setRecordDate(request.getRecordDate());
            record.setCreateTime(LocalDateTime.now());
        }

        // 设置餐饮数据
        record.setBreakfast(request.getBreakfast() != null ? request.getBreakfast() : BigDecimal.ZERO);
        record.setLunch(request.getLunch() != null ? request.getLunch() : BigDecimal.ZERO);
        record.setDinner(request.getDinner() != null ? request.getDinner() : BigDecimal.ZERO);
        record.setSnack(request.getSnack() != null ? request.getSnack() : BigDecimal.ZERO);
        record.setDrink(request.getDrink() != null ? request.getDrink() : BigDecimal.ZERO);
        record.setOther(request.getOther() != null ? request.getOther() : BigDecimal.ZERO);

        // 计算总计
        BigDecimal total = record.getBreakfast()
                .add(record.getLunch())
                .add(record.getDinner())
                .add(record.getSnack())
                .add(record.getDrink())
                .add(record.getOther());
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
        QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("record_date", date);
        return mealRecordMapper.selectOne(queryWrapper);
    }

    /**
     * 删除指定日期的记录
     */
    public boolean deleteByDate(LocalDate date) {
        QueryWrapper<MealRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("record_date", date);
        return mealRecordMapper.delete(queryWrapper) > 0;
    }
}

