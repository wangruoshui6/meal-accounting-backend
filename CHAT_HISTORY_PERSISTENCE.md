# 聊天记录持久化说明

## ✅ 已实现功能

### 1. 数据库持久化存储
- ✅ 创建了 `chat_messages` 表
- ✅ 所有聊天记录保存到数据库
- ✅ 支持最多 100 条历史记录（自动清理旧数据）

### 2. Redis 缓存优化
- ✅ 使用 Redis 缓存最近 10 轮对话（用于 AI 上下文）
- ✅ 缓存过期时间：7 天
- ✅ 提升查询性能

### 3. 前端历史记录加载
- ✅ 页面加载时自动从数据库获取历史记录
- ✅ 退出后重新进入，历史记录自动恢复

## 📋 使用步骤

### 1. 执行数据库脚本

```bash
# 执行 SQL 脚本创建聊天记录表
mysql -u root -p meal_accounting < database_add_chat_messages.sql
```

或者在 MySQL 客户端中执行：
```sql
USE meal_accounting;
SOURCE database_add_chat_messages.sql;
```

### 2. 重启后端服务

```bash
cd meal-accounting-backend
mvn clean compile
mvn spring-boot:run
```

### 3. 测试功能

1. **发送消息**：聊天记录会自动保存到数据库
2. **退出登录**：关闭浏览器或退出登录
3. **重新登录**：再次进入聊天页面
4. **验证**：历史记录应该自动加载显示

## 🗄️ 数据库表结构

### chat_messages 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 消息ID（主键，自增） |
| user_id | BIGINT | 用户ID（外键） |
| role | VARCHAR(20) | 消息角色（user/assistant/system） |
| content | TEXT | 消息内容 |
| create_time | DATETIME | 创建时间 |

**索引：**
- `idx_user_id`: 用户ID索引
- `idx_user_create_time`: 用户ID+创建时间联合索引

**外键约束：**
- 删除用户时自动删除其所有聊天记录

## 🔧 技术实现

### 存储策略

1. **数据库存储**（持久化）
   - 所有消息保存到 `chat_messages` 表
   - 最多保留 100 条记录（自动清理）
   - 按时间排序，保留最新的记录

2. **Redis 缓存**（性能优化）
   - 缓存最近 10 轮对话（用于 AI 上下文）
   - 过期时间：7 天
   - 提升查询速度

### 数据流程

```
用户发送消息
    ↓
保存到数据库（持久化）
    ↓
更新 Redis 缓存（性能优化）
    ↓
返回 AI 回复
    ↓
保存 AI 回复到数据库
    ↓
更新 Redis 缓存
```

### 历史记录加载

```
前端页面加载
    ↓
调用 GET /api/chat/history
    ↓
后端从数据库查询（最多100条）
    ↓
返回给前端
    ↓
前端显示历史记录
```

## 📊 数据管理

### 自动清理机制

- **数据库**：超过 100 条记录时，自动删除最旧的记录
- **Redis**：7 天后自动过期

### 手动清理

- **清空所有记录**：调用 `POST /api/chat/clear-history`
- **数据库清理**：直接执行 SQL `DELETE FROM chat_messages WHERE user_id = ?`

## 🐛 常见问题

### 1. 历史记录没有加载

**检查：**
- 数据库表是否创建成功
- 后端服务是否重启
- 浏览器控制台是否有错误

**解决：**
```sql
-- 检查表是否存在
SHOW TABLES LIKE 'chat_messages';

-- 检查是否有数据
SELECT COUNT(*) FROM chat_messages WHERE user_id = YOUR_USER_ID;
```

### 2. 历史记录丢失

**可能原因：**
- 数据库表未创建
- 数据保存失败（查看后端日志）
- 手动清空了记录

**解决：**
- 确认表已创建
- 查看后端日志确认保存是否成功
- 检查是否有异常错误

### 3. 性能问题

**优化建议：**
- Redis 缓存已启用，查询速度应该很快
- 如果数据量很大，可以考虑分页加载
- 定期清理旧数据

## 📝 API 接口

### 获取历史记录

```http
GET /api/chat/history
Authorization: Bearer {token}
```

**响应：**
```json
{
  "success": true,
  "data": [
    {
      "role": "user",
      "content": "你好",
      "timestamp": "2024-01-01T10:00:00"
    },
    {
      "role": "assistant",
      "content": "你好！我是你的AI饮食助手...",
      "timestamp": "2024-01-01T10:00:01"
    }
  ]
}
```

### 清空历史记录

```http
POST /api/chat/clear-history
Authorization: Bearer {token}
```

**响应：**
```json
{
  "success": true,
  "message": "对话历史已清空"
}
```

## ✅ 验证清单

- [ ] 数据库表已创建
- [ ] 后端服务已重启
- [ ] 发送消息后，数据库中有记录
- [ ] 退出后重新进入，历史记录能加载
- [ ] 清空历史功能正常

---

**现在聊天记录会永久保存到数据库，退出后重新进入也能看到历史记录！** 🎉

