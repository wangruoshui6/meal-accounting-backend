package com.accounting.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String JWT_TOKEN_PREFIX = "jwt_token:";
    private static final String USER_TOKEN_PREFIX = "user_token:";
    private static final long TOKEN_CACHE_TIME = 24; // 24小时

    /**
     * 缓存JWT token
     */
    public void cacheToken(String token, Long userId, String username) {
        String tokenKey = JWT_TOKEN_PREFIX + token;
        String userKey = USER_TOKEN_PREFIX + userId;
        
        // 缓存token -> 用户信息
        redisTemplate.opsForValue().set(tokenKey, userId, TOKEN_CACHE_TIME, TimeUnit.HOURS);
        
        // 缓存用户ID -> token（用于单点登录控制）
        redisTemplate.opsForValue().set(userKey, token, TOKEN_CACHE_TIME, TimeUnit.HOURS);
        
        System.out.println("JWT token已缓存: " + token + " -> 用户ID: " + userId);
    }

    /**
     * 验证token是否在缓存中
     */
    public boolean isTokenCached(String token) {
        String tokenKey = JWT_TOKEN_PREFIX + token;
        return redisTemplate.hasKey(tokenKey);
    }

    /**
     * 从缓存中获取用户ID
     */
    public Long getUserIdFromCache(String token) {
        String tokenKey = JWT_TOKEN_PREFIX + token;
        Object userId = redisTemplate.opsForValue().get(tokenKey);
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * 获取用户的当前token
     */
    public String getUserToken(Long userId) {
        String userKey = USER_TOKEN_PREFIX + userId;
        Object token = redisTemplate.opsForValue().get(userKey);
        return token != null ? token.toString() : null;
    }

    /**
     * 删除token缓存
     */
    public void removeToken(String token) {
        String tokenKey = JWT_TOKEN_PREFIX + token;
        Object userId = redisTemplate.opsForValue().get(tokenKey);
        
        if (userId != null) {
            String userKey = USER_TOKEN_PREFIX + userId;
            redisTemplate.delete(userKey);
        }
        
        redisTemplate.delete(tokenKey);
        System.out.println("JWT token已从缓存删除: " + token);
    }

    /**
     * 删除用户的所有token（用于登出）
     */
    public void removeUserTokens(Long userId) {
        String userKey = USER_TOKEN_PREFIX + userId;
        Object token = redisTemplate.opsForValue().get(userKey);
        
        if (token != null) {
            String tokenKey = JWT_TOKEN_PREFIX + token;
            redisTemplate.delete(tokenKey);
        }
        
        redisTemplate.delete(userKey);
        System.out.println("用户所有token已从缓存删除: " + userId);
    }

    /**
     * 刷新token缓存时间
     */
    public void refreshTokenCache(String token) {
        String tokenKey = JWT_TOKEN_PREFIX + token;
        Object userId = redisTemplate.opsForValue().get(tokenKey);
        
        if (userId != null) {
            String userKey = USER_TOKEN_PREFIX + userId;
            redisTemplate.expire(tokenKey, TOKEN_CACHE_TIME, TimeUnit.HOURS);
            redisTemplate.expire(userKey, TOKEN_CACHE_TIME, TimeUnit.HOURS);
            System.out.println("JWT token缓存时间已刷新: " + token);
        }
    }
}
