# 生产环境部署说明

## 部署流程

1. 代码推送到 GitHub
2. 服务器从 GitHub 拉取代码
3. 配置环境变量
4. 编译和启动应用

## 环境变量配置

### 方式1：使用环境变量文件（推荐）

在服务器上创建 `.env` 文件（不要提交到 Git）：

```bash
# 服务器配置
SERVER_PORT=8080

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=meal_accounting
DB_USER=root
DB_PASSWORD=your_database_password

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password  # 如果没有密码，留空
REDIS_DATABASE=0

# JWT 密钥（生产环境建议修改）
JWT_SECRET=your_jwt_secret_key_here

# 阿里云百炼 API Key
BAILIAN_API_KEY=sk-your-api-key-here
```

### 方式2：直接在启动命令中设置

```bash
export DB_PASSWORD=your_password
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET=your_jwt_secret
export BAILIAN_API_KEY=sk-your-api-key
```

## 部署步骤

### 1. 拉取代码

```bash
cd /path/to/your/project
git pull origin main  # 或 master，根据你的分支名
```

### 2. 设置环境变量

创建 `.env` 文件并填入实际配置：

```bash
nano .env  # 或使用 vim
# 填入上面的环境变量配置
```

### 3. 加载环境变量并启动

#### 方式A：使用 Maven 启动（开发/测试）

```bash
cd meal-accounting-backend
# 加载环境变量
export $(cat .env | xargs)
# 启动应用（使用生产环境配置）
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### 方式B：打包成 JAR 运行（生产环境推荐）

```bash
cd meal-accounting-backend

# 1. 编译打包
mvn clean package -DskipTests

# 2. 加载环境变量并启动
export $(cat .env | xargs)
java -jar target/meal-accounting-backend-*.jar --spring.profiles.active=prod
```

#### 方式C：使用 systemd 服务（推荐用于生产环境）

创建服务文件 `/etc/systemd/system/meal-accounting.service`：

```ini
[Unit]
Description=Meal Accounting Backend Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=your_user
WorkingDirectory=/path/to/meal-accounting-backend
EnvironmentFile=/path/to/.env
ExecStart=/usr/bin/java -jar /path/to/meal-accounting-backend/target/meal-accounting-backend-*.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable meal-accounting
sudo systemctl start meal-accounting
sudo systemctl status meal-accounting
```

### 4. 验证部署

```bash
# 检查服务是否运行
curl http://localhost:8080/api/meal/test

# 查看日志
tail -f logs/application.log  # 如果有日志文件
# 或
journalctl -u meal-accounting -f  # 如果使用 systemd
```

## 环境变量说明

| 变量名 | 说明 | 是否必需 | 默认值 |
|--------|------|----------|--------|
| `SERVER_PORT` | 服务器端口 | 否 | 8080 |
| `DB_HOST` | 数据库主机 | 否 | localhost |
| `DB_PORT` | 数据库端口 | 否 | 3306 |
| `DB_NAME` | 数据库名 | 否 | meal_accounting |
| `DB_USER` | 数据库用户名 | 否 | root |
| `DB_PASSWORD` | 数据库密码 | **是** | 无 |
| `REDIS_HOST` | Redis 主机 | 否 | localhost |
| `REDIS_PORT` | Redis 端口 | 否 | 6379 |
| `REDIS_PASSWORD` | Redis 密码 | 否 | 空 |
| `REDIS_DATABASE` | Redis 数据库索引 | 否 | 0 |
| `JWT_SECRET` | JWT 密钥 | 否 | 默认值（不安全） |
| `BAILIAN_API_KEY` | 阿里云百炼 API Key | **是** | 无 |

## 注意事项

1. **安全**：
   - `.env` 文件不要提交到 Git
   - 生产环境必须修改 `DB_PASSWORD`、`JWT_SECRET`
   - 建议使用强密码

2. **数据库**：
   - 确保数据库已创建：`CREATE DATABASE meal_accounting;`
   - 确保数据库用户有足够权限

3. **Redis**：
   - 确保 Redis 服务已启动
   - 如果 Redis 有密码，必须配置 `REDIS_PASSWORD`

4. **防火墙**：
   - 确保服务器防火墙开放了应用端口（8080 或 80）
   - 如果使用 Nginx 反向代理，确保 Nginx 配置正确

5. **日志**：
   - 生产环境日志级别为 `info`，减少日志输出
   - 建议配置日志轮转，避免日志文件过大

## 故障排查

### 应用无法启动

1. 检查环境变量是否配置正确：
   ```bash
   echo $DB_PASSWORD
   echo $BAILIAN_API_KEY
   ```

2. 检查数据库连接：
   ```bash
   mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD $DB_NAME
   ```

3. 检查 Redis 连接：
   ```bash
   redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD ping
   ```

### 401 错误

- 检查 JWT_SECRET 是否配置
- 检查 token 是否过期（30天有效期）

### 500 错误

- 查看应用日志
- 检查数据库和 Redis 连接
- 检查 API Key 是否正确

## 更新部署

当代码更新后：

```bash
# 1. 拉取最新代码
git pull origin main

# 2. 重新编译
mvn clean package -DskipTests

# 3. 重启服务
# 如果使用 systemd：
sudo systemctl restart meal-accounting

# 如果直接运行：
# 先停止旧进程，然后重新启动
```

