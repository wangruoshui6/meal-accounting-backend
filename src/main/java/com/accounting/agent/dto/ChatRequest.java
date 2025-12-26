package com.accounting.agent.dto;

import lombok.Data;
import java.util.List;

/**
 * 聊天请求 DTO
 */
@Data
public class ChatRequest {
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 对话历史（可选）
     */
    private List<ChatMessage> history;
    
    /**
     * 是否包含用户数据上下文
     */
    private Boolean includeContext = true;
}

