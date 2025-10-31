-- ============================================
-- 餐饮记账系统数据库初始化脚本 (2.0版本)
-- ============================================
-- 注意：此脚本会删除所有现有表并重建，请确保已备份重要数据！

-- 删除现有数据库（如果存在）
DROP DATABASE IF EXISTS meal_accounting;

-- 创建新数据库
CREATE DATABASE meal_accounting DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE meal_accounting;

-- ============================================
-- 1. 创建用户表 (users)
-- ============================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    nickname VARCHAR(100) COMMENT '昵称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 创建餐饮记录表 (meal_records)
-- ============================================
CREATE TABLE meal_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    breakfast DECIMAL(10,2) DEFAULT 0.00 COMMENT '早饭金额',
    lunch DECIMAL(10,2) DEFAULT 0.00 COMMENT '午饭金额',
    dinner DECIMAL(10,2) DEFAULT 0.00 COMMENT '晚饭金额',
    snack DECIMAL(10,2) DEFAULT 0.00 COMMENT '零食金额',
    drink DECIMAL(10,2) DEFAULT 0.00 COMMENT '饮料金额',
    custom_items TEXT COMMENT '动态餐饮项目(JSON格式)',
    total DECIMAL(10,2) DEFAULT 0.00 COMMENT '总金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_record_date (record_date),
    INDEX idx_user_id (user_id),
    -- 同一用户同一日期只能有一条记录
    UNIQUE KEY uk_user_record_date (user_id, record_date),
    -- 外键约束：删除用户时同时删除其餐饮记录
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐饮记录表';

-- ============================================
-- 3. 创建用户设置表 (user_settings)
-- ============================================
CREATE TABLE user_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    setting_key VARCHAR(100) NOT NULL COMMENT '设置键',
    setting_value TEXT COMMENT '设置值(JSON格式)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 同一用户的同一设置键只能有一条记录
    UNIQUE KEY uk_user_setting (user_id, setting_key),
    -- 外键约束：删除用户时同时删除其设置
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';

-- ============================================
-- 4. 创建日记表 (diaries)
-- ============================================
CREATE TABLE diaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    item_name VARCHAR(50) NOT NULL COMMENT '项目名称',
    content TEXT COMMENT '日记内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 创建索引
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_user_item_date (user_id, item_name, record_date),
    -- 外键约束：删除用户时同时删除其日记
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    -- 唯一约束：同一用户同一日期同一项目只能有一条日记
    UNIQUE KEY uk_user_item_date (user_id, item_name, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日记表';

-- ============================================
-- 5. 验证表结构
-- ============================================
-- 查看所有表
SHOW TABLES;

-- 查看各表结构
DESCRIBE users;
DESCRIBE meal_records;
DESCRIBE user_settings;
DESCRIBE diaries;

-- ============================================
-- 脚本执行完成！
-- ============================================
-- 提示：
-- 1. 表结构已根据你提供的结构创建完成
-- 2. 所有表都包含 user_id 字段，支持多用户
-- 3. meal_records 表使用 DATETIME，users/user_settings 表使用 TIMESTAMP
-- 4. 所有表都设置了合适的外键约束和唯一约束
-- ============================================
