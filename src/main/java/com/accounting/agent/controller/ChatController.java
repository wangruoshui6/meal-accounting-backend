package com.accounting.agent.controller;

import com.accounting.agent.dto.ChatRequest;
import com.accounting.agent.dto.ChatResponse;
import com.accounting.agent.service.ChatService;
import com.accounting.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private ChatService chatService;
    
    /**
     * 发送聊天消息
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody ChatRequest request) {
        logger.info("收到聊天请求: {}", request.getMessage());
        
        try {
            // 获取当前用户ID
            Long userId = UserContext.getUserId();
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }
            
            // 验证请求
            if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 调用聊天服务
            ChatResponse chatResponse = chatService.chat(userId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", chatResponse.getSuccess());
            response.put("message", chatResponse.getMessage());
            response.put("data", chatResponse);
            
            if (chatResponse.getSuccess()) {
                logger.info("聊天请求处理成功");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("聊天请求处理失败: {}", chatResponse.getError());
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            logger.error("处理聊天请求异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 清空对话历史
     */
    @PostMapping("/clear-history")
    public ResponseEntity<Map<String, Object>> clearHistory() {
        logger.info("收到清空对话历史请求");
        
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }
            
            chatService.clearHistory(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "对话历史已清空");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("清空对话历史异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取所有对话历史
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getChatHistory() {
        logger.info("收到获取对话历史请求");
        
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }
            
            List<com.accounting.agent.dto.ChatMessage> history = chatService.getAllChatHistory(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", history);
            response.put("message", "获取成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取对话历史异常", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 测试接口
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "聊天服务正常运行");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}

