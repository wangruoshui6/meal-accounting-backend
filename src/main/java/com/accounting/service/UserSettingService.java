package com.accounting.service;

import com.accounting.entity.UserSetting;
import com.accounting.mapper.UserSettingMapper;
import com.accounting.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserSettingService {
    
    @Autowired
    private UserSettingMapper userSettingMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String DEFAULT_MEAL_ITEMS_KEY = "default_meal_items";
    private static final String CACHE_PREFIX = "user_setting:";
    private static final long CACHE_TIME = 24; // 缓存24小时
    
    /**
     * 获取用户默认餐饮项目（带Redis缓存）
     */
    public List<String> getDefaultMealItems() {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        String cacheKey = CACHE_PREFIX + currentUserId + ":" + DEFAULT_MEAL_ITEMS_KEY;
        
        try {
            // 1. 先查Redis缓存
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
            if (cachedValue != null) {
                System.out.println("从Redis缓存读取默认菜品设置: 用户ID=" + currentUserId);
                String[] items = objectMapper.readValue(cachedValue.toString(), String[].class);
                return Arrays.asList(items);
            }
            
            // 2. Redis中没有，查询数据库
            System.out.println("Redis缓存未命中，查询数据库: 用户ID=" + currentUserId);
            QueryWrapper<UserSetting> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", currentUserId)
                       .eq("setting_key", DEFAULT_MEAL_ITEMS_KEY);
            UserSetting setting = userSettingMapper.selectOne(queryWrapper);
            
            List<String> mealItems;
            if (setting != null && setting.getSettingValue() != null) {
                mealItems = Arrays.asList(objectMapper.readValue(setting.getSettingValue(), String[].class));
            } else {
                // 如果没有找到，返回默认值
                mealItems = Arrays.asList("早饭", "午饭", "晚饭", "零食", "饮料");
            }
            
            // 3. 存入Redis缓存（24小时过期）
            try {
                String jsonValue = objectMapper.writeValueAsString(mealItems);
                redisTemplate.opsForValue().set(cacheKey, jsonValue, CACHE_TIME, TimeUnit.HOURS);
                System.out.println("默认菜品设置已存入Redis缓存: 用户ID=" + currentUserId);
            } catch (Exception e) {
                System.err.println("存入Redis缓存失败: " + e.getMessage());
                // 缓存失败不影响返回结果
            }
            
            return mealItems;
        } catch (Exception e) {
            System.err.println("获取默认餐饮项目失败: " + e.getMessage());
            return Arrays.asList("早饭", "午饭", "晚饭", "零食", "饮料");
        }
    }
    
    /**
     * 保存用户默认餐饮项目（保存后更新Redis缓存）
     */
    public boolean saveDefaultMealItems(List<String> mealItems) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        String cacheKey = CACHE_PREFIX + currentUserId + ":" + DEFAULT_MEAL_ITEMS_KEY;
        
        try {
            String jsonValue = objectMapper.writeValueAsString(mealItems);
            
            QueryWrapper<UserSetting> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", currentUserId)
                       .eq("setting_key", DEFAULT_MEAL_ITEMS_KEY);
            UserSetting existingSetting = userSettingMapper.selectOne(queryWrapper);
            
            boolean success = false;
            if (existingSetting != null) {
                // 更新现有设置
                existingSetting.setSettingValue(jsonValue);
                existingSetting.setUpdatedAt(LocalDateTime.now());
                success = userSettingMapper.updateById(existingSetting) > 0;
            } else {
                // 创建新设置
                UserSetting newSetting = new UserSetting();
                newSetting.setUserId(currentUserId);
                newSetting.setSettingKey(DEFAULT_MEAL_ITEMS_KEY);
                newSetting.setSettingValue(jsonValue);
                newSetting.setCreatedAt(LocalDateTime.now());
                newSetting.setUpdatedAt(LocalDateTime.now());
                success = userSettingMapper.insert(newSetting) > 0;
            }
            
            // 保存成功后，更新Redis缓存
            if (success) {
                try {
                    redisTemplate.opsForValue().set(cacheKey, jsonValue, CACHE_TIME, TimeUnit.HOURS);
                    System.out.println("默认菜品设置已保存并更新Redis缓存: 用户ID=" + currentUserId);
                } catch (Exception e) {
                    System.err.println("更新Redis缓存失败: " + e.getMessage());
                    // 缓存更新失败不影响保存结果
                }
            }
            
            return success;
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
