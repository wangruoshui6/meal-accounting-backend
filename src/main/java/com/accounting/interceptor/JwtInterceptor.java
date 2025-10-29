package com.accounting.interceptor;

import com.accounting.util.JwtUtil;
import com.accounting.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器，用于验证token并设置用户上下文
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("=== JWT拦截器调试 ===");
        System.out.println("请求URI: " + request.getRequestURI());
        
        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization头: " + authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // 验证token
            if (jwtUtil.validateToken(token)) {
                try {
                    // 从token中提取用户信息
                    Long userId = jwtUtil.extractUserId(token);
                    String username = jwtUtil.extractUsername(token);
                    
                    // 设置用户上下文
                    UserContext.setUserId(userId);
                    UserContext.setUsername(username);
                    
                    System.out.println("用户ID: " + userId);
                    System.out.println("用户名: " + username);
                    System.out.println("JWT验证成功，放行");
                    
                    return true;
                } catch (Exception e) {
                    // token解析失败
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return false;
                }
            }
        }
        
        // 对于不需要认证的接口（如登录、注册），直接放行
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/")) {
            return true;
        }
        
        // 其他接口需要认证
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清除用户上下文
        UserContext.clear();
    }
}
