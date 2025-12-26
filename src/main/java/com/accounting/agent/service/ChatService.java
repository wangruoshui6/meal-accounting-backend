package com.accounting.agent.service;

import com.accounting.agent.dto.ChatMessage;
import com.accounting.agent.dto.ChatRequest;
import com.accounting.agent.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天服务 - 处理用户与 AI 助手的对话
 */
@Service
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    @Autowired
    private BailianApiService bailianApiService;
    
    @Autowired
    private AnalysisService analysisService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 仅使用 Redis 保存聊天记录，不再持久化到数据库
    private static final String REDIS_CHAT_HISTORY_PREFIX = "chat:history:";
    private static final int MAX_CONTEXT_LENGTH = 10; // 用于 AI 上下文的最多轮数（不影响 Redis 存储）
    private static final Duration HISTORY_EXPIRE = Duration.ofDays(30); // Redis 缓存 30 天过期
    
    /**
     * 处理聊天请求
     * 
     * @param userId 用户ID
     * @param request 聊天请求
     * @return 聊天响应
     */
    public ChatResponse chat(Long userId, ChatRequest request) {
        try {
            // 获取对话历史
            List<ChatMessage> history = getChatHistory(userId);
            
            // 构建系统提示词
            String systemPrompt = buildSystemPrompt(request.getIncludeContext());
            
            // 构建消息列表
            List<ChatMessage> messages = new ArrayList<>();
            
            // 添加系统提示词
            if (!history.isEmpty() || systemPrompt != null) {
                messages.add(new ChatMessage("system", systemPrompt != null ? systemPrompt : "你是一个专业的饮食健康助手，帮助用户分析餐饮消费和提供健康建议。"));
            }
            
            // 添加历史对话（用于 AI 上下文，最多保留最近几轮以提高效率）
            // 注意：这里只影响发送给 AI 的上下文，不影响 Redis 中保存的完整历史
            int historyStart = Math.max(0, history.size() - MAX_CONTEXT_LENGTH);
            for (int i = historyStart; i < history.size(); i++) {
                messages.add(history.get(i));
            }
            
            // 添加用户当前消息
            messages.add(new ChatMessage("user", request.getMessage()));
            
            // 调用 AI API
            String aiResponse = bailianApiService.chat(messages);
            
            // 保存对话历史到 Redis（保存所有记录，不截断）
            ChatMessage userMessage = new ChatMessage("user", request.getMessage());
            ChatMessage assistantMessage = new ChatMessage("assistant", aiResponse);
            saveChatHistory(userId, userMessage, assistantMessage);
            
            return ChatResponse.success(aiResponse);
            
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage(), e);
            return ChatResponse.error("配置错误: " + e.getMessage());
        } catch (IOException e) {
            logger.error("API 调用失败: {}", e.getMessage(), e);
            // 返回更详细的错误信息，方便调试
            String errorMsg = e.getMessage();
            if (errorMsg.contains("API 错误")) {
                return ChatResponse.error(errorMsg);
            }
            return ChatResponse.error("AI 服务暂时不可用: " + errorMsg);
        } catch (Exception e) {
            logger.error("聊天服务异常: {}", e.getMessage(), e);
            logger.error("异常堆栈:", e);
            return ChatResponse.error("服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(Boolean includeContext) {
        if (includeContext == null || !includeContext) {
            return "你是一个专业的饮食健康助手，帮助用户分析餐饮消费和提供健康建议。";
        }
        
        // 包含用户数据上下文
        String dataSummary = analysisService.getRecentWeekSummary();
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的饮食健康助手，专门帮助用户分析餐饮消费和提供健康建议。\n\n");
        prompt.append("以下是用户的近期餐饮数据：\n");
        prompt.append(dataSummary).append("\n");
        prompt.append("请基于这些数据，为用户提供个性化的分析和建议。");
        
        return prompt.toString();
    }
    
    /**
     * 获取对话历史（仅从 Redis 获取，如果没有则返回空列表）
     */
    @SuppressWarnings("unchecked")
    private List<ChatMessage> getChatHistory(Long userId) {
        try {
            String key = REDIS_CHAT_HISTORY_PREFIX + userId;
            Object historyObj = redisTemplate.opsForValue().get(key);
            if (historyObj != null) {
                return (List<ChatMessage>) historyObj;
            }
        } catch (Exception e) {
            logger.warn("获取对话历史失败", e);
        }
        return new ArrayList<>();
    }
    
    /**
     * 保存对话历史（仅保存到 Redis，保存所有记录，不截断，30天过期）
     */
    private void saveChatHistory(Long userId, ChatMessage userMessage, ChatMessage assistantMessage) {
        try {
            String key = REDIS_CHAT_HISTORY_PREFIX + userId;
            List<ChatMessage> history = getChatHistory(userId);
            
            // 添加新消息（不截断，保存所有历史记录）
            history.add(userMessage);
            history.add(assistantMessage);
            
            // 保存到 Redis（所有记录，30天过期）
            redisTemplate.opsForValue().set(key, history, HISTORY_EXPIRE);
            
            logger.debug("保存聊天记录成功，用户ID: {}, 当前记录数: {}", userId, history.size());
        } catch (Exception e) {
            logger.error("保存对话历史失败", e);
            // 即使保存失败也不影响主流程，只记录日志
        }
    }
    
    /**
     * 清空对话历史（仅清除 Redis）
     */
    public void clearHistory(Long userId) {
        try {
            String key = REDIS_CHAT_HISTORY_PREFIX + userId;
            redisTemplate.delete(key);
            logger.info("清除 Redis 聊天记录缓存成功，用户ID: {}", userId);
        } catch (Exception e) {
            logger.error("清空对话历史失败", e);
            throw new RuntimeException("清空对话历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有对话历史（用于前端显示，返回 Redis 中的所有记录）
     */
    public List<ChatMessage> getAllChatHistory(Long userId) {
        // 返回 Redis 中的所有历史记录（不截断）
        List<ChatMessage> history = getChatHistory(userId);
        logger.debug("获取所有聊天历史，用户ID: {}, 记录数: {}", userId, history.size());
        return history;
    }
}

