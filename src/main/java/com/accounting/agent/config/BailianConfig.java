package com.accounting.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云百炼配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.bailian")
public class BailianConfig {
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * API 端点
     */
    private String apiUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    
    /**
     * 模型名称
     */
    private String model = "qwen-turbo";
    
    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;
    
    /**
     * 最大 token 数
     */
    private Integer maxTokens = 2000;
    
    /**
     * 温度参数
     */
    private Double temperature = 0.7;
}

