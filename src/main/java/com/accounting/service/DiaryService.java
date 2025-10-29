package com.accounting.service;

import com.accounting.entity.Diary;
import com.accounting.mapper.DiaryMapper;
import com.accounting.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日记服务类
 */
@Service
public class DiaryService {
    
    @Autowired
    private DiaryMapper diaryMapper;
    
    /**
     * 保存或更新日记
     */
    public void saveOrUpdateDiary(String itemName, String content, LocalDate recordDate) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 查询是否已存在该日期的该项目的日记
        QueryWrapper<Diary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId)
                   .eq("record_date", recordDate)
                   .eq("item_name", itemName);
        
        Diary existingDiary = diaryMapper.selectOne(queryWrapper);
        
        if (existingDiary != null) {
            // 更新现有日记
            existingDiary.setContent(content);
            existingDiary.setUpdateTime(LocalDateTime.now());
            diaryMapper.updateById(existingDiary);
        } else {
            // 创建新日记
            Diary diary = new Diary();
            diary.setUserId(currentUserId);
            diary.setRecordDate(recordDate);
            diary.setItemName(itemName);
            diary.setContent(content);
            diary.setCreateTime(LocalDateTime.now());
            diary.setUpdateTime(LocalDateTime.now());
            diaryMapper.insert(diary);
        }
    }
    
    /**
     * 获取指定日期和项目的日记
     */
    public String getDiaryContent(String itemName, LocalDate recordDate) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        QueryWrapper<Diary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId)
                   .eq("record_date", recordDate)
                   .eq("item_name", itemName);
        
        Diary diary = diaryMapper.selectOne(queryWrapper);
        return diary != null ? diary.getContent() : "";
    }
    
    /**
     * 获取指定日期的所有日记
     */
    public List<Diary> getDiariesByDate(LocalDate recordDate) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        QueryWrapper<Diary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId)
                   .eq("record_date", recordDate)
                   .orderByAsc("item_name");
        
        return diaryMapper.selectList(queryWrapper);
    }
    
    /**
     * 删除指定日期和项目的日记
     */
    public void deleteDiary(String itemName, LocalDate recordDate) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        QueryWrapper<Diary> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId)
                   .eq("record_date", recordDate)
                   .eq("item_name", itemName);
        
        diaryMapper.delete(queryWrapper);
    }
}
