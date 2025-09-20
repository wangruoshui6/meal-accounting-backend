# 餐饮记账系统 - 后端

基于Spring Boot + MyBatis + MySQL的餐饮记账API服务，提供数据持久化和API接口支持。

## 🎯 项目简介

这是餐饮记账系统的后端服务，负责处理前端的数据请求，提供餐饮记录的增删改查功能，数据持久化存储到MySQL数据库中。

## ✨ 功能特点

- 🚀 **RESTful API**：标准的REST接口设计
- 💾 **数据持久化**：MySQL数据库存储
- 🔄 **自动计算**：自动计算总金额
- 📅 **日期管理**：支持按日期查询和管理
- 🛡️ **异常处理**：完善的错误处理机制
- 🌐 **跨域支持**：支持前端跨域请求
- 📊 **数据验证**：输入数据验证和格式化

## 🛠️ 技术栈

- **框架**: Spring Boot 2.7
- **ORM**: MyBatis Plus 3.5
- **数据库**: MySQL 8.0
- **构建工具**: Maven
- **Java版本**: JDK 11+
- **连接池**: HikariCP

## 📦 项目结构

```
meal-accounting-backend/
├── src/main/java/com/accounting/
│   ├── MealAccountingBackendApplication.java  # 启动类
│   ├── entity/
│   │   └── MealRecord.java                    # 实体类
│   ├── dto/
│   │   └── MealRecordDto.java                 # 数据传输对象
│   ├── mapper/
│   │   └── MealRecordMapper.java              # 数据访问层
│   ├── service/
│   │   └── MealRecordService.java             # 业务逻辑层
│   └── controller/
│       └── MealRecordController.java          # 控制器层
├── src/main/resources/
│   └── application.yml                        # 配置文件
├── database.sql                               # 数据库初始化脚本
└── pom.xml                                    # Maven配置
```

## 🚀 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+

### 数据库配置

1. **创建数据库**
```sql
CREATE DATABASE meal_accounting;
```

2. **执行初始化脚本**
```sql
-- 运行 database.sql 文件
USE meal_accounting;
-- 创建表和插入测试数据
```

3. **修改配置文件**
在 `src/main/resources/application.yml` 中修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meal_accounting?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 你的密码  # 修改这里
```

### 启动项目

```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动

## 📡 API接口文档

### 基础信息

- **基础URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **字符编码**: UTF-8

### 接口列表

#### 1. 保存餐饮记录

**请求**
```http
POST /api/meal/save
Content-Type: application/json

{
  "recordDate": "2024-01-15",
  "breakfast": 15.00,
  "lunch": 25.00,
  "dinner": 30.00,
  "snack": 10.00,
  "drink": 8.00,
  "other": 5.00
}
```

**响应**
```json
{
  "success": true,
  "message": "保存成功",
  "data": {
    "id": 1,
    "recordDate": "2024-01-15",
    "breakfast": 15.00,
    "lunch": 25.00,
    "dinner": 30.00,
    "snack": 10.00,
    "drink": 8.00,
    "other": 5.00,
    "total": 93.00,
    "createTime": "2024-01-15T10:30:00",
    "updateTime": "2024-01-15T10:30:00"
  }
}
```

#### 2. 获取餐饮记录

**请求**
```http
GET /api/meal/get/2024-01-15
```

**响应**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "recordDate": "2024-01-15",
    "breakfast": 15.00,
    "lunch": 25.00,
    "dinner": 30.00,
    "snack": 10.00,
    "drink": 8.00,
    "other": 5.00,
    "total": 93.00,
    "createTime": "2024-01-15T10:30:00",
    "updateTime": "2024-01-15T10:30:00"
  }
}
```

#### 3. 删除餐饮记录

**请求**
```http
DELETE /api/meal/delete/2024-01-15
```

**响应**
```json
{
  "success": true,
  "message": "删除成功"
}
```

## 🗄️ 数据库设计

### 表结构

**meal_records** - 餐饮记录表

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 记录ID | 主键，自增 |
| record_date | DATE | 记录日期 | 非空，唯一 |
| breakfast | DECIMAL(10,2) | 早饭金额 | 默认0.00 |
| lunch | DECIMAL(10,2) | 午饭金额 | 默认0.00 |
| dinner | DECIMAL(10,2) | 晚饭金额 | 默认0.00 |
| snack | DECIMAL(10,2) | 零食金额 | 默认0.00 |
| drink | DECIMAL(10,2) | 饮料金额 | 默认0.00 |
| other | DECIMAL(10,2) | 其他金额 | 默认0.00 |
| total | DECIMAL(10,2) | 总金额 | 默认0.00 |
| create_time | DATETIME | 创建时间 | 自动生成 |
| update_time | DATETIME | 更新时间 | 自动更新 |

### 索引

- `uk_record_date`: 记录日期唯一索引
- `idx_record_date`: 记录日期普通索引

## 🔧 配置说明

### 数据库连接配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meal_accounting?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### MyBatis配置

```yaml
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.accounting.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

## 🧪 测试

### 单元测试

```bash
mvn test
```

### API测试

可以使用Postman或其他API测试工具测试接口：

1. 导入API集合
2. 设置环境变量
3. 执行测试用例

## 📝 开发说明

### 添加新功能

1. **实体类**: 在 `entity` 包中添加新的实体
2. **DTO**: 在 `dto` 包中添加数据传输对象
3. **Mapper**: 在 `mapper` 包中添加数据访问接口
4. **Service**: 在 `service` 包中添加业务逻辑
5. **Controller**: 在 `controller` 包中添加API接口

### 代码规范

- 使用Lombok减少样板代码
- 遵循RESTful API设计原则
- 统一的异常处理
- 完善的日志记录

## 🚀 部署

### 打包

```bash
mvn clean package
```

### 运行JAR包

```bash
java -jar target/meal-accounting-backend-0.0.1-SNAPSHOT.jar
```

### Docker部署

```dockerfile
FROM openjdk:11-jre-slim
COPY target/meal-accounting-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue
- 发送邮件
- 微信联系

---

⭐ 如果这个项目对你有帮助，请给个Star支持一下！
