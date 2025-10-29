package com.accounting.service;

import com.accounting.entity.User;
import com.accounting.mapper.UserMapper;
import com.accounting.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 用户注册
     */
    public Map<String, Object> register(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查用户名是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            User existingUser = userMapper.selectOne(queryWrapper);
            
            if (existingUser != null) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }
            
            // 创建新用户
            User user = new User(username, passwordEncoder.encode(password)); // 使用构造函数，自动设置所有字段
            
            int insertResult = userMapper.insert(user);
            
            if (insertResult > 0) {
                result.put("success", true);
                result.put("message", "注册成功");
                result.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "nickname", user.getNickname()
                ));
            } else {
                result.put("success", false);
                result.put("message", "注册失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "注册失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            User user = userMapper.selectOne(queryWrapper);
            
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
                return result;
            }
            
            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
                return result;
            }
            
            // 生成JWT token
            String token = jwtUtil.generateToken(username, user.getId());
            
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("token", token);
            result.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "nickname", user.getNickname()
            ));
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "登录失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }
    
    /**
     * 根据ID查找用户
     */
    public User findById(Long id) {
        return userMapper.selectById(id);
    }
    
    /**
     * 获取JwtUtil实例
     */
    public JwtUtil getJwtUtil() {
        return jwtUtil;
    }
}
