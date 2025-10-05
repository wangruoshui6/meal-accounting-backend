-- 最终数据库设置脚本 - 完全移除other字段
-- 在DataGrip中执行此脚本

-- 删除现有数据库（如果存在）
DROP DATABASE IF EXISTS meal_accounting;

-- 创建新数据库
CREATE DATABASE meal_accounting DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE meal_accounting;

-- 创建餐饮记录表（不包含other字段）
CREATE TABLE meal_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
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
    UNIQUE KEY uk_record_date (record_date),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐饮记录表';

-- 插入测试数据（包含动态项目示例）
INSERT INTO meal_records (record_date, breakfast, lunch, dinner, snack, drink, custom_items, total) VALUES
('2024-09-24', 15.00, 25.00, 30.00, 10.00, 8.00, '{"咖啡": 12.00, "水果": 8.50}', 108.50),
('2024-09-23', 12.00, 28.00, 35.00, 15.00, 12.00, '{"夜宵": 20.00, "零食": 5.00}', 127.00),
('2024-09-22', 18.00, 22.00, 28.00, 8.00, 6.00, NULL, 82.00);

-- 验证表结构
DESCRIBE meal_records;

-- 查看测试数据
SELECT * FROM meal_records;
