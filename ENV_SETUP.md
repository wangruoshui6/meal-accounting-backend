# 环境变量配置详细指南

## ⚠️ 重要说明

**使用项目级别的环境变量不会影响服务器上的其他项目！**

我们使用以下方式之一：
- **方式1**：项目目录下的 `.env` 文件（推荐，最简单）
- **方式2**：systemd 服务的 `EnvironmentFile`（推荐用于生产环境）
- **方式3**：启动脚本中的 `export`（临时使用）

**不会使用系统级别的环境变量**（如 `/etc/environment`），所以不会影响其他项目。

---

## 方式1：使用 .env 文件（推荐，最简单）

### 步骤1：在服务器上进入项目目录

```bash
cd /path/to/meal-accounting-backend
# 例如：cd ~/project/meal-accounting-backend
```

### 步骤2：创建 .env 文件

```bash
nano .env
# 或使用 vim: vim .env
```

### 步骤3：填入环境变量

复制以下内容到 `.env` 文件，并根据实际情况修改：

```bash
# 服务器配置
SERVER_PORT=8080

# 数据库配置（请根据实际情况修改）
DB_HOST=localhost
DB_PORT=3306
DB_NAME=meal_accounting
DB_USER=root
DB_PASSWORD=你的数据库密码

# Redis 配置（请根据实际情况修改）
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=你的Redis密码
REDIS_DATABASE=0

# JWT 密钥（生产环境建议修改为强密钥，至少32个字符）
JWT_SECRET=mySecretKey123456789012345678901234567890

# 阿里云百炼 API Key（请填写实际的 API Key）
BAILIAN_API_KEY=sk-9a347114b9a54885a5e8008cab3203bd
```

### 步骤4：保存文件

- **nano**：按 `Ctrl + X`，然后按 `Y`，最后按 `Enter`
- **vim**：按 `Esc`，输入 `:wq`，按 `Enter`

### 步骤5：验证文件内容

```bash
cat .env
```

### 步骤6：测试加载环境变量

**方法A：使用改进的加载命令（推荐，能自动过滤注释）**

```bash
# 加载环境变量（自动过滤注释行和空行）
set -a
source <(grep -v '^#' .env | grep -v '^$' | sed 's/^/export /')
set +a

# 验证是否加载成功
echo $DB_PASSWORD
echo $BAILIAN_API_KEY
```

**方法B：使用简单的过滤命令**

```bash
# 加载环境变量（过滤注释和空行）
export $(grep -v '^#' .env | grep -v '^$' | xargs)

# 验证是否加载成功
echo $DB_PASSWORD
echo $BAILIAN_API_KEY
```

**方法C：创建加载脚本（最推荐）**

```bash
# 创建加载脚本
cat > load-env.sh << 'EOF'
#!/bin/bash
# 加载 .env 文件中的环境变量（自动过滤注释和空行）
set -a
[ -f .env ] && source <(grep -v '^#' .env | grep -v '^$' | sed 's/^/export /')
set +a
EOF

# 给脚本添加执行权限
chmod +x load-env.sh

# 使用脚本加载
source load-env.sh

# 验证
echo $DB_PASSWORD
```

如果能看到你填写的值，说明配置成功。

### 步骤7：启动应用（使用环境变量）

**方式A：每次启动前加载环境变量**

```bash
# 加载环境变量（使用改进的方法）
set -a
source <(grep -v '^#' .env | grep -v '^$' | sed 's/^/export /')
set +a

# 启动应用
java -jar target/meal-accounting-backend-*.jar --spring.profiles.active=prod
```

**方式B：使用启动脚本（推荐）**

创建 `start.sh` 脚本：

```bash
cat > start.sh << 'EOF'
#!/bin/bash
# 加载环境变量
set -a
[ -f .env ] && source <(grep -v '^#' .env | grep -v '^$' | sed 's/^/export /')
set +a

# 启动应用
java -jar target/meal-accounting-backend-*.jar --spring.profiles.active=prod
EOF

chmod +x start.sh

# 使用脚本启动
./start.sh
```

---

## 方式2：使用 systemd 服务（推荐用于生产环境）

这种方式更专业，适合长期运行的服务。

### 步骤1：创建 .env 文件（同上）

按照方式1的步骤1-4创建 `.env` 文件。

### 步骤2：创建 systemd 服务文件

```bash
sudo nano /etc/systemd/system/meal-accounting.service
```

### 步骤3：填入服务配置

```ini
[Unit]
Description=Meal Accounting Backend Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=你的用户名  # 例如：root 或 ubuntu
WorkingDirectory=/path/to/meal-accounting-backend  # 修改为实际路径
EnvironmentFile=/path/to/meal-accounting-backend/.env  # 指向 .env 文件
ExecStart=/usr/bin/java -jar /path/to/meal-accounting-backend/target/meal-accounting-backend-*.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

**重要**：修改以下内容：
- `User=你的用户名`：改为运行服务的用户
- `WorkingDirectory`：改为项目实际路径
- `EnvironmentFile`：改为 `.env` 文件的实际路径
- `ExecStart` 中的路径：改为 JAR 文件的实际路径

### 步骤4：保存并启用服务

```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 启用服务（开机自启）
sudo systemctl enable meal-accounting

# 启动服务
sudo systemctl start meal-accounting

# 查看服务状态
sudo systemctl status meal-accounting

# 查看日志
sudo journalctl -u meal-accounting -f
```

### 步骤5：管理服务

```bash
# 启动服务
sudo systemctl start meal-accounting

# 停止服务
sudo systemctl stop meal-accounting

# 重启服务
sudo systemctl restart meal-accounting

# 查看状态
sudo systemctl status meal-accounting

# 查看日志
sudo journalctl -u meal-accounting -f
```

---

## 方式3：使用启动脚本（推荐）

### 步骤1：创建启动脚本

```bash
cd /path/to/meal-accounting-backend
cat > start.sh << 'EOF'
#!/bin/bash

# 加载环境变量（自动过滤注释和空行）
set -a
[ -f .env ] && source <(grep -v '^#' .env | grep -v '^$' | sed 's/^/export /')
set +a

# 启动应用
java -jar target/meal-accounting-backend-*.jar --spring.profiles.active=prod
EOF
```

### 步骤2：给脚本添加执行权限

```bash
chmod +x start.sh
```

### 步骤3：运行脚本

```bash
./start.sh
```

---

## 环境变量说明

| 变量名 | 说明 | 是否必需 | 默认值 | 示例 |
|--------|------|----------|--------|------|
| `SERVER_PORT` | 服务器端口 | 否 | 8080 | `8080` |
| `DB_HOST` | 数据库主机 | 否 | localhost | `localhost` 或 `192.168.1.100` |
| `DB_PORT` | 数据库端口 | 否 | 3306 | `3306` |
| `DB_NAME` | 数据库名 | 否 | meal_accounting | `meal_accounting` |
| `DB_USER` | 数据库用户名 | 否 | root | `root` |
| `DB_PASSWORD` | 数据库密码 | **是** | 无 | `your_password` |
| `REDIS_HOST` | Redis 主机 | 否 | localhost | `localhost` |
| `REDIS_PORT` | Redis 端口 | 否 | 6379 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | 否 | 空 | `your_redis_password` |
| `REDIS_DATABASE` | Redis 数据库索引 | 否 | 0 | `0` |
| `JWT_SECRET` | JWT 密钥 | 否 | 默认值 | `your_secret_key` |
| `BAILIAN_API_KEY` | 阿里云 API Key | **是** | 无 | `sk-xxx...` |

---

## 常见问题

### Q1: 会影响其他项目吗？

**A: 不会！** `.env` 文件只在项目目录下，只影响当前项目。其他项目不会读取这个文件。

### Q2: 如何查看当前加载的环境变量？

```bash
# 查看所有环境变量
env | grep DB_
env | grep REDIS_
env | grep BAILIAN_
```

### Q3: 环境变量没有生效？

1. 检查 `.env` 文件是否存在：
   ```bash
   ls -la .env
   ```

2. 检查文件内容：
   ```bash
   cat .env
   ```

3. 检查是否有语法错误（每行应该是 `KEY=VALUE` 格式，不要有空格）：
   ```bash
   # 错误：DB_PASSWORD = password  （有空格）
   # 正确：DB_PASSWORD=password
   ```

4. 使用改进的方法重新加载环境变量（能自动过滤注释）：
   ```bash
   # 方法1：使用改进的命令
   set -a
   source <(grep -v '^#' .env | grep -v '^$' | sed 's/^/export /')
   set +a
   
   # 方法2：使用过滤命令
   export $(grep -v '^#' .env | grep -v '^$' | xargs)
   ```

5. 如果还是不行，检查 `.env` 文件格式：
   ```bash
   # 查看所有非注释行
   grep -v '^#' .env | grep -v '^$'
   ```

### Q4: 如何修改环境变量？

```bash
# 编辑 .env 文件
nano .env

# 修改后，如果使用 systemd，需要重启服务
sudo systemctl restart meal-accounting
```

### Q5: 如何保护 .env 文件？

```bash
# 设置文件权限（只有所有者可以读写）
chmod 600 .env

# 查看文件权限
ls -la .env
# 应该显示：-rw------- 1 user user ...
```

---

## 快速检查清单

- [ ] 创建了 `.env` 文件
- [ ] 填写了 `DB_PASSWORD`（必需）
- [ ] 填写了 `BAILIAN_API_KEY`（必需）
- [ ] 根据实际情况修改了数据库和 Redis 配置
- [ ] 测试加载环境变量成功
- [ ] 应用能正常启动
- [ ] 应用能连接数据库和 Redis

---

## 推荐配置流程

1. **开发/测试环境**：使用方式1（.env 文件 + 手动启动）
2. **生产环境**：使用方式2（systemd 服务）

这样既简单又专业，且不会影响其他项目！

