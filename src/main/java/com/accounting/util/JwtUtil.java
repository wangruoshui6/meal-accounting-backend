package com.accounting.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    
    @Autowired
    private com.accounting.service.JwtCacheService jwtCacheService;
    
    private static final String SECRET = "mySecretKey123456789012345678901234567890";
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24小时
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }
    
    /**
     * 生成JWT token
     */
    public String generateToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("userId", userId);
        String token = createToken(claims, username);
        
        // 缓存token
        jwtCacheService.cacheToken(token, userId, username);
        
        return token;
    }
    
    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 获取token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * 从token中获取指定信息
     */
    public <T> T getClaimFromToken(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * 从token中获取所有信息
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 检查token是否过期
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 验证token
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }
    
    /**
     * 简单验证token（不需要用户名）
     */
    public Boolean validateToken(String token) {
        try {
            // 先检查缓存
            if (jwtCacheService.isTokenCached(token)) {
                // 刷新缓存时间
                jwtCacheService.refreshTokenCache(token);
                System.out.println("JWT token验证成功（缓存）: " + token);
                return true;
            }
            
            // 缓存中没有，进行常规验证
            boolean isValid = !isTokenExpired(token);
            if (isValid) {
                // 验证成功，重新缓存
                Long userId = extractUserId(token);
                String username = extractUsername(token);
                jwtCacheService.cacheToken(token, userId, username);
                System.out.println("JWT token验证成功（重新缓存）: " + token);
            } else {
                System.out.println("JWT token验证失败: " + token);
            }
            
            return isValid;
        } catch (Exception e) {
            System.out.println("JWT token验证异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 从token中提取用户ID
     */
    public Long extractUserId(String token) {
        return getUserIdFromToken(token);
    }
    
    /**
     * 从token中提取用户名
     */
    public String extractUsername(String token) {
        return getUsernameFromToken(token);
    }
    
    /**
     * 登出时删除token缓存
     */
    public void logout(String token) {
        jwtCacheService.removeToken(token);
        System.out.println("用户登出，token已从缓存删除: " + token);
    }
    
    /**
     * 强制用户登出（删除用户所有token）
     */
    public void forceLogout(Long userId) {
        jwtCacheService.removeUserTokens(userId);
        System.out.println("强制用户登出，所有token已从缓存删除: " + userId);
    }
}
