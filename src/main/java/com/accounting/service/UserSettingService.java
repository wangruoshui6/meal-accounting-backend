package com.accounting.service;

import com.accounting.entity.UserSetting;
import com.accounting.mapper.UserSettingMapper;
import com.accounting.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class UserSettingService {
    
    @Autowired
    private UserSettingMapper userSettingMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String DEFAULT_MEAL_ITEMS_KEY = "default_meal_items";
    
    /**
     * 获取用户默认餐饮项目
     */
    public List<String> getDefaultMealItems() {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        try {
            QueryWrapper<UserSetting> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", currentUserId)
                       .eq("setting_key", DEFAULT_MEAL_ITEMS_KEY);
            UserSetting setting = userSettingMapper.selectOne(queryWrapper);
            
            if (setting != null && setting.getSettingValue() != null) {
                return Arrays.asList(objectMapper.readValue(setting.getSettingValue(), String[].class));
            }
            
            // 如果没有找到，返回默认值
            return Arrays.asList("早饭", "午饭", "晚饭", "零食", "饮料");
        } catch (Exception e) {
            System.err.println("获取默认餐饮项目失败: " + e.getMessage());
            return Arrays.asList("早饭", "午饭", "晚饭", "零食", "饮料");
        }
    }
    
    /**
     * 保存用户默认餐饮项目
     */
    public boolean saveDefaultMealItems(List<String> mealItems) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        try {
            String jsonValue = objectMapper.writeValueAsString(mealItems);
            
            QueryWrapper<UserSetting> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", currentUserId)
                       .eq("setting_key", DEFAULT_MEAL_ITEMS_KEY);
            UserSetting existingSetting = userSettingMapper.selectOne(queryWrapper);
            
            if (existingSetting != null) {
                // 更新现有设置
                existingSetting.setSettingValue(jsonValue);
                existingSetting.setUpdatedAt(LocalDateTime.now());
                return userSettingMapper.updateById(existingSetting) > 0;
            } else {
                // 创建新设置
                UserSetting newSetting = new UserSetting();
                newSetting.setUserId(currentUserId);
                newSetting.setSettingKey(DEFAULT_MEAL_ITEMS_KEY);
                newSetting.setSettingValue(jsonValue);
                newSetting.setCreatedAt(LocalDateTime.now());
                newSetting.setUpdatedAt(LocalDateTime.now());
                return userSettingMapper.insert(newSetting) > 0;
            }
        } catch (JsonProcessingException e) {
            System.err.println("保存默认餐饮项目失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取用户设置
     */
    public String getUserSetting(String userId, String settingKey) {
        try {
            QueryWrapper<UserSetting> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("setting_key", settingKey);
            UserSetting setting = userSettingMapper.selectOne(queryWrapper);
            
            return setting != null ? setting.getSettingValue() : null;
        } catch (Exception e) {
            System.err.println("获取用户设置失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 保存用户设置
     */
    public boolean saveUserSetting(Long userId, String settingKey, String settingValue) {
        try {
            QueryWrapper<UserSetting> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("setting_key", settingKey);
            UserSetting existingSetting = userSettingMapper.selectOne(queryWrapper);
            
            if (existingSetting != null) {
                // 更新现有设置
                existingSetting.setSettingValue(settingValue);
                existingSetting.setUpdatedAt(LocalDateTime.now());
                return userSettingMapper.updateById(existingSetting) > 0;
            } else {
                // 创建新设置
                UserSetting newSetting = new UserSetting();
                newSetting.setUserId(userId);
                newSetting.setSettingKey(settingKey);
                newSetting.setSettingValue(settingValue);
                newSetting.setCreatedAt(LocalDateTime.now());
                newSetting.setUpdatedAt(LocalDateTime.now());
                return userSettingMapper.insert(newSetting) > 0;
            }
        } catch (Exception e) {
            System.err.println("保存用户设置失败: " + e.getMessage());
            return false;
        }
    }
}
