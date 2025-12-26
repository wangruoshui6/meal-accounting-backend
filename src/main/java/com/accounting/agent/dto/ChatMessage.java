package com.accounting.agent.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息 DTO
 */
@Data
public class ChatMessage {
    /**
     * 消息角色：user 或 assistant
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息时间
     */
    private LocalDateTime timestamp;
    
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
}

