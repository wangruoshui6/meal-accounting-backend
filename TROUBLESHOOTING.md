# 问题排查指南

## ❌ 404 错误排查

### 问题：Request failed with status code 404

**原因分析：**
404 错误表示请求的路径不存在，通常不是 API Key 的问题（API Key 错误会返回 401/403）。

### 排查步骤

#### 1. 检查后端服务是否启动

```bash
# 检查后端是否在运行
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

**如果没有运行，启动后端：**
```bash
cd meal-accounting-backend
mvn spring-boot:run
```

#### 2. 检查前端 API 地址配置

打开 `meal-accounting/src/api/request.ts`，确认：
- **本地开发**：`baseURL: 'http://localhost:8080/api'`
- **生产环境**：`baseURL: 'http://101.201.254.71/api'`

**如果配置错误，修改为本地地址：**
```typescript
const request = axios.create({
  baseURL: 'http://localhost:8080/api',  // 本地开发
  timeout: 10000
})
```

#### 3. 检查后端路由是否正确

确认 `ChatController` 已正确创建：
- 文件路径：`src/main/java/com/accounting/agent/controller/ChatController.java`
- 注解：`@RequestMapping("/api/chat")`
- 方法：`@PostMapping("/message")`

**完整路径应该是：** `POST http://localhost:8080/api/chat/message`

#### 4. 测试后端接口

**使用浏览器测试：**
```
GET http://localhost:8080/api/chat/test
```

**使用 curl 测试：**
```bash
curl http://localhost:8080/api/chat/test
```

**使用 Postman 测试：**
1. 先登录获取 Token：`POST http://localhost:8080/api/auth/login`
2. 发送聊天消息：`POST http://localhost:8080/api/chat/message`
   - Headers: `Authorization: Bearer {token}`
   - Body: `{"message": "你好", "includeContext": true}`

#### 5. 检查后端日志

查看后端控制台输出，确认：
- ✅ Spring Boot 启动成功
- ✅ 没有编译错误
- ✅ ChatController 已加载
- ✅ 收到请求日志

**正常启动日志应该包含：**
```
Started MealAccountingBackendApplication in X.XXX seconds
```

#### 6. 检查跨域配置

如果前端和后端在不同端口，确认后端已配置 CORS：
```java
@CrossOrigin(origins = "*")
```

#### 7. 检查 JWT 拦截器

确认 `/api/chat/**` 路径没有被拦截器错误拦截。

查看 `WebConfig.java`：
```java
registry.addInterceptor(jwtInterceptor)
    .addPathPatterns("/api/**")
    .excludePathPatterns("/api/auth/**");
```

## ✅ 常见问题解决

### 问题 1: 后端服务未启动
**解决：**
```bash
cd meal-accounting-backend
mvn clean compile
mvn spring-boot:run
```

### 问题 2: 端口冲突
**解决：** 修改 `application.yml` 中的端口：
```yaml
server:
  port: 8081  # 改为其他端口
```

### 问题 3: 前端请求地址错误
**解决：** 修改 `src/api/request.ts` 中的 `baseURL`

### 问题 4: 后端代码未编译
**解决：**
```bash
cd meal-accounting-backend
mvn clean package -DskipTests
```

### 问题 5: 生产服务器代码未更新
**解决：** 如果使用生产服务器，需要：
1. 重新编译后端代码
2. 部署到服务器
3. 重启服务

## 🔍 调试技巧

### 1. 浏览器开发者工具
- 打开 Network 标签
- 查看请求的完整 URL
- 查看请求和响应头
- 查看错误详情

### 2. 后端日志
- 查看控制台输出
- 检查是否有异常堆栈
- 确认请求是否到达后端

### 3. 测试接口
先测试简单的 GET 接口：
```
GET http://localhost:8080/api/chat/test
```

如果这个也返回 404，说明：
- 后端服务未启动，或
- 路由配置有问题

## 📝 检查清单

- [ ] 后端服务已启动（端口 8080）
- [ ] 前端 API 地址配置正确（本地开发用 localhost:8080）
- [ ] ChatController 文件存在且正确
- [ ] 后端编译无错误
- [ ] 浏览器控制台查看实际请求 URL
- [ ] 后端日志查看是否收到请求
- [ ] 测试接口 `/api/chat/test` 是否可访问

---

**如果以上都检查无误，仍然 404，请提供：**
1. 浏览器 Network 标签的请求详情
2. 后端启动日志
3. 前端和后端的完整错误信息

