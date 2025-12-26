# 500 错误调试指南

## 🔍 问题排查步骤

### 1. 查看后端日志

**最重要！** 查看后端控制台的完整错误日志，应该能看到：
- 异常堆栈信息
- API 请求详情
- API 响应内容

### 2. 检查常见问题

#### 问题 1: API Key 配置错误
**症状：** 日志显示 "请配置阿里云百炼 API Key"
**解决：**
```yaml
# 检查 application.yml
aliyun:
  bailian:
    api-key: ${BAILIAN_API_KEY:sk-9a347114b9a54885a5e8008cab3203bd}
```

#### 问题 2: API 请求格式错误
**症状：** 日志显示 "API 错误" 或 "无法解析 API 响应"
**解决：** 查看日志中的"API 响应体"，确认响应格式

#### 问题 3: Redis 连接失败
**症状：** 日志显示 Redis 连接错误
**解决：**
```bash
# 检查 Redis 是否运行
redis-cli ping
# 应该返回 PONG
```

#### 问题 4: 数据库查询失败
**症状：** 日志显示 SQL 错误
**解决：** 检查数据库连接和查询语句

### 3. 测试 API Key

**使用 curl 直接测试阿里云 API：**

```bash
curl -X POST https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation \
  -H "Authorization: Bearer sk-9a347114b9a54885a5e8008cab3203bd" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen-turbo",
    "input": {
      "messages": [
        {
          "role": "user",
          "content": "你好"
        }
      ]
    },
    "parameters": {
      "max_tokens": 2000,
      "temperature": 0.7
    }
  }'
```

**如果这个请求失败，说明 API Key 有问题。**

### 4. 检查后端日志输出

现在代码已经添加了详细的日志，应该能看到：

```
=== 阿里云百炼 API 请求 ===
URL: https://dashscope.aliyuncs.com/...
Model: qwen-turbo
请求体: {...}
API 响应状态码: 200
API 响应体: {...}
```

**请复制这些日志信息，特别是：**
- 请求体内容
- 响应体内容
- 任何错误信息

## 🐛 常见错误及解决

### 错误 1: "请配置阿里云百炼 API Key"
**原因：** API Key 未正确读取
**解决：**
1. 检查 `application.yml` 配置
2. 确认环境变量 `BAILIAN_API_KEY`（如果使用）
3. 重启后端服务

### 错误 2: "API 错误 [InvalidApiKey]"
**原因：** API Key 无效或过期
**解决：**
1. 登录阿里云控制台
2. 检查 API Key 是否有效
3. 确认 API Key 是否正确复制（没有多余空格）

### 错误 3: "无法解析 API 响应"
**原因：** API 响应格式与预期不符
**解决：**
1. 查看日志中的"API 响应体"
2. 对比阿里云官方文档的响应格式
3. 可能需要调整解析逻辑

### 错误 4: Redis 连接失败
**原因：** Redis 服务未启动
**解决：**
```bash
# Windows
# 启动 Redis（如果使用 Windows 版本）

# Linux/Mac
redis-server

# 或使用 Docker
docker run -d -p 6379:6379 redis
```

### 错误 5: 数据库查询异常
**原因：** 数据库连接或查询问题
**解决：**
1. 检查数据库是否运行
2. 检查连接配置
3. 查看具体 SQL 错误

## 📋 调试检查清单

- [ ] 后端服务已启动
- [ ] 查看后端控制台日志
- [ ] API Key 配置正确
- [ ] Redis 服务运行正常（如果使用）
- [ ] 数据库连接正常
- [ ] 网络连接正常（能访问阿里云 API）
- [ ] 查看完整的错误堆栈

## 🔧 临时解决方案

如果急需测试，可以暂时禁用某些功能：

### 禁用 Redis（如果 Redis 有问题）

修改 `ChatService.java`：
```java
// 临时禁用 Redis
private List<ChatMessage> getChatHistory(Long userId) {
    return new ArrayList<>(); // 直接返回空列表
}
```

### 禁用数据分析（如果数据库查询有问题）

修改 `ChatService.java`：
```java
private String buildSystemPrompt(Boolean includeContext) {
    return "你是一个专业的饮食健康助手，帮助用户分析餐饮消费和提供健康建议。";
    // 暂时不包含用户数据
}
```

## 📞 获取帮助

**请提供以下信息：**

1. **后端完整错误日志**（最重要！）
   - 包括异常堆栈
   - API 请求和响应日志

2. **浏览器 Network 标签信息**
   - 请求 URL
   - 请求头
   - 响应状态码
   - 响应体

3. **环境信息**
   - 操作系统
   - Java 版本
   - Redis 版本（如果使用）
   - MySQL 版本

---

**请先查看后端日志，这是解决问题的关键！**

