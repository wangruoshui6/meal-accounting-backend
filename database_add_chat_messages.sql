-- ============================================
-- 添加聊天记录表
-- ============================================

USE meal_accounting;

-- 创建聊天记录表 (chat_messages)
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) NOT NULL COMMENT '消息角色(user/assistant/system)',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- 创建索引
    INDEX idx_user_id (user_id),
    INDEX idx_user_create_time (user_id, create_time),
    -- 外键约束：删除用户时同时删除其聊天记录
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天记录表';

-- 验证表结构
DESCRIBE chat_messages;

