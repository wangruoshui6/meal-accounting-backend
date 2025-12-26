package com.accounting.agent.dto;

import lombok.Data;

/**
 * 聊天响应 DTO
 */
@Data
public class ChatResponse {
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * AI 回复内容
     */
    private String content;
    
    /**
     * 错误信息（如果有）
     */
    private String error;
    
    public static ChatResponse success(String content) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(true);
        response.setContent(content);
        response.setMessage("成功");
        return response;
    }
    
    public static ChatResponse error(String error) {
        ChatResponse response = new ChatResponse();
        response.setSuccess(false);
        response.setError(error);
        response.setMessage("失败");
        return response;
    }
}

