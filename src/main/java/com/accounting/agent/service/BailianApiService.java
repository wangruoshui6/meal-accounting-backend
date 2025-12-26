package com.accounting.agent.service;

import com.accounting.agent.config.BailianConfig;
import com.accounting.agent.dto.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 阿里云百炼 API 调用服务
 */
@Service
public class BailianApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(BailianApiService.class);
    
    @Autowired
    private BailianConfig bailianConfig;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient httpClient;
    
    public BailianApiService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 调用阿里云百炼 API 进行对话
     * 
     * @param messages 对话消息列表
     * @return AI 回复内容
     */
    public String chat(List<ChatMessage> messages) throws IOException {
        if (bailianConfig.getApiKey() == null || bailianConfig.getApiKey().equals("your-api-key-here")) {
            throw new IllegalArgumentException("请配置阿里云百炼 API Key");
        }
        
        // 构建请求体 - 根据阿里云百炼 API 文档格式
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", bailianConfig.getModel());
        
        // 构建 input.messages 数组
        Map<String, Object> input = new HashMap<>();
        List<Map<String, String>> messageList = new ArrayList<>();
        for (ChatMessage msg : messages) {
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("role", msg.getRole());
            messageMap.put("content", msg.getContent());
            messageList.add(messageMap);
        }
        input.put("messages", messageList);
        requestBody.put("input", input);
        
        // 构建 parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_tokens", bailianConfig.getMaxTokens());
        parameters.put("temperature", bailianConfig.getTemperature());
        requestBody.put("parameters", parameters);
        
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        logger.info("=== 阿里云百炼 API 请求 ===");
        logger.info("URL: {}", bailianConfig.getApiUrl());
        logger.info("Model: {}", bailianConfig.getModel());
        logger.info("请求体: {}", requestBodyJson);
        
        // 构建 HTTP 请求
        RequestBody body = RequestBody.create(
                requestBodyJson,
                MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(bailianConfig.getApiUrl())
                .post(body)
                .addHeader("Authorization", "Bearer " + bailianConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .build();
        
        // 发送请求
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "未知错误";
                logger.error("API 请求失败: code={}, body={}", response.code(), errorBody);
                throw new IOException("API 请求失败: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            logger.info("API 响应状态码: {}", response.code());
            logger.info("API 响应体: {}", responseBody);
            
            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // 检查是否有错误码
            if (jsonNode.has("code")) {
                String code = jsonNode.get("code").asText();
                if (!code.equals("Success")) {
                    String errorMsg = jsonNode.has("message") ? 
                        jsonNode.get("message").asText() : "API 返回错误";
                    logger.error("API 返回错误: code={}, message={}", code, errorMsg);
                    throw new IOException("API 错误 [" + code + "]: " + errorMsg);
                }
            }
            
            // 提取回复内容 - 尝试多种响应格式
            String content = null;
            
            // 格式1: output.choices[0].message.content
            JsonNode output = jsonNode.path("output");
            if (!output.isMissingNode() && output.has("choices")) {
                JsonNode choices = output.get("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode choice = choices.get(0);
                    JsonNode message = choice.path("message");
                    if (message.has("content")) {
                        content = message.get("content").asText();
                    }
                }
            }
            
            // 格式2: output.text (备用格式)
            if (content == null && output.has("text")) {
                content = output.get("text").asText();
            }
            
            // 格式3: 直接 content 字段
            if (content == null && jsonNode.has("content")) {
                content = jsonNode.get("content").asText();
            }
            
            if (content != null && !content.isEmpty()) {
                logger.info("成功提取回复内容，长度: {}", content.length());
                return content;
            }
            
            // 如果所有格式都失败，记录完整响应并抛出异常
            logger.error("无法解析 API 响应，响应结构: {}", jsonNode.toPrettyString());
            throw new IOException("无法解析 API 响应，请查看日志了解详情");
        }
    }
}

