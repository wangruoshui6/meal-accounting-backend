# AI 饮食助手集成说明

## 📋 功能概述

已成功集成阿里云百炼（Bailian）AI 服务，实现智能饮食助手功能：
- ✅ 与 AI 助手对话聊天
- ✅ 基于用户餐饮数据提供个性化分析
- ✅ 提供饮食健康建议
- ✅ 对话历史管理（Redis 缓存）

## 🔧 配置步骤

### 1. 配置 API Key

**方式一：环境变量（推荐）**

在系统环境变量中设置：
```bash
# Windows
set BAILIAN_API_KEY=your-api-key-here

# Linux/Mac
export BAILIAN_API_KEY=your-api-key-here
```

**方式二：配置文件**

编辑 `src/main/resources/application.yml`：
```yaml
aliyun:
  bailian:
    api-key: sk-your-actual-api-key-here  # 替换为你的实际 API Key
```

⚠️ **安全提示**：
- 不要将 API Key 提交到 Git 仓库
- 生产环境务必使用环境变量
- 建议将 `application.yml` 中的 API Key 添加到 `.gitignore`

### 2. 配置参数说明

在 `application.yml` 中可以调整以下参数：

```yaml
aliyun:
  bailian:
    api-key: ${BAILIAN_API_KEY:your-api-key-here}  # API Key
    api-url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation  # API 端点
    model: qwen-turbo  # 模型名称（可选：qwen-turbo, qwen-plus, qwen-max）
    timeout: 30000  # 请求超时（毫秒）
    max-tokens: 2000  # 最大 token 数
    temperature: 0.7  # 温度参数（0-1，越高越随机）
```

### 3. 启动项目

```bash
mvn clean compile
mvn spring-boot:run
```

## 📡 API 接口

### 1. 发送聊天消息

**请求**
```http
POST /api/chat/message
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "message": "帮我分析一下最近的饮食消费",
  "includeContext": true
}
```

**响应**
```json
{
  "success": true,
  "message": "成功",
  "data": {
    "success": true,
    "content": "根据您最近一周的餐饮数据...",
    "message": "成功"
  }
}
```

### 2. 清空对话历史

**请求**
```http
POST /api/chat/clear-history
Authorization: Bearer {jwt-token}
```

**响应**
```json
{
  "success": true,
  "message": "对话历史已清空"
}
```

### 3. 测试接口

**请求**
```http
GET /api/chat/test
```

## 🏗️ 项目结构

```
com.accounting.agent/
├── controller/
│   └── ChatController.java          # 聊天控制器
├── service/
│   ├── BailianApiService.java       # 阿里云百炼 API 调用
│   ├── ChatService.java              # 聊天服务（对话管理）
│   └── AnalysisService.java         # 数据分析服务（数据摘要）
├── dto/
│   ├── ChatRequest.java              # 聊天请求 DTO
│   ├── ChatResponse.java             # 聊天响应 DTO
│   └── ChatMessage.java             # 消息 DTO
└── config/
    └── BailianConfig.java            # 配置类
```

## 🔍 功能特性

### 1. 智能上下文
- 自动获取用户最近一周的餐饮数据
- 将数据摘要作为上下文传递给 AI
- AI 可以基于实际数据提供个性化建议

### 2. 对话历史
- 使用 Redis 缓存对话历史
- 最多保留 10 轮对话
- 24 小时自动过期

### 3. 错误处理
- API 调用失败自动重试
- 友好的错误提示
- 完整的日志记录

## 🧪 测试

### 使用 Postman 测试

1. **获取 JWT Token**（先登录）
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

2. **发送聊天消息**
```http
POST /api/chat/message
Authorization: Bearer {从登录获取的token}
Content-Type: application/json

{
  "message": "你好，请帮我分析一下最近的饮食情况",
  "includeContext": true
}
```

### 使用 curl 测试

```bash
# 1. 登录获取 token
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' \
  | jq -r '.data.token')

# 2. 发送聊天消息
curl -X POST http://localhost:8080/api/chat/message \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "帮我分析一下最近的饮食消费",
    "includeContext": true
  }'
```

## ⚠️ 注意事项

1. **API Key 安全**
   - 不要将 API Key 硬编码在代码中
   - 使用环境变量或配置文件（不提交到 Git）

2. **成本控制**
   - 阿里云百炼有免费额度，超出后按量付费
   - 建议实现限流机制（可后续添加）

3. **API 格式**
   - 如果遇到 API 调用错误，可能需要根据阿里云百炼的最新文档调整请求格式
   - 当前实现基于标准 REST API 格式

4. **模型选择**
   - `qwen-turbo`: 快速响应，适合简单对话
   - `qwen-plus`: 平衡性能和能力
   - `qwen-max`: 最强能力，但响应较慢

## 🐛 常见问题

### 1. API Key 未配置
**错误**: `请配置阿里云百炼 API Key`
**解决**: 检查环境变量或配置文件中的 API Key

### 2. API 调用失败
**错误**: `API 请求失败: 401`
**解决**: 检查 API Key 是否正确，是否有权限

### 3. 响应解析失败
**错误**: `无法解析 API 响应`
**解决**: 可能是 API 格式变化，需要查看日志中的实际响应

## 📝 后续优化建议

1. **限流机制**: 限制每个用户的每日对话次数
2. **缓存优化**: 缓存常见问题的答案
3. **WebSocket**: 实现实时流式响应
4. **多轮对话优化**: 改进上下文管理策略
5. **数据分析增强**: 添加更多维度的数据分析

---

**集成完成！** 🎉

现在你可以在前端调用 `/api/chat/message` 接口，实现与 AI 饮食助手的对话功能。

