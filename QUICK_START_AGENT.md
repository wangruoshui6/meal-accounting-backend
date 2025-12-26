# AI 饮食助手快速开始指南

## 🚀 5 分钟快速集成

### 步骤 1: 配置 API Key

**方式一：环境变量（推荐）**
```bash
# Windows PowerShell
$env:BAILIAN_API_KEY="sk-your-api-key-here"

# Windows CMD
set BAILIAN_API_KEY=sk-your-api-key-here

# Linux/Mac
export BAILIAN_API_KEY=sk-your-api-key-here
```

**方式二：直接修改配置文件**
编辑 `src/main/resources/application.yml`，找到：
```yaml
aliyun:
  bailian:
    api-key: ${BAILIAN_API_KEY:your-api-key-here}
```
将 `your-api-key-here` 替换为你的实际 API Key。

### 步骤 2: 启动项目

```bash
cd meal-accounting-backend
mvn clean compile
mvn spring-boot:run
```

### 步骤 3: 测试接口

**1. 先登录获取 Token**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"your-username","password":"your-password"}'
```

**2. 发送聊天消息**
```bash
curl -X POST http://localhost:8080/api/chat/message \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好，请帮我分析一下最近的饮食情况",
    "includeContext": true
  }'
```

## ✅ 验证清单

- [ ] API Key 已配置（环境变量或配置文件）
- [ ] 项目已成功启动（无错误日志）
- [ ] 可以访问 `/api/chat/test` 接口
- [ ] 登录后可以发送聊天消息
- [ ] AI 返回了回复内容

## 🐛 常见问题快速排查

### 问题 1: "请配置阿里云百炼 API Key"
**解决**: 检查环境变量或配置文件中的 API Key 是否正确设置

### 问题 2: "API 请求失败: 401"
**解决**: API Key 无效或已过期，请检查阿里云控制台

### 问题 3: "无法解析 API 响应"
**解决**: 查看日志中的实际响应，可能需要调整 API 请求格式

## 📚 更多信息

详细文档请查看 `AGENT_README.md`

