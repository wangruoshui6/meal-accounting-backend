-- 创建数据库
CREATE DATABASE IF NOT EXISTS meal_accounting DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE meal_accounting;

-- 创建餐饮记录表
CREATE TABLE meal_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    breakfast DECIMAL(10,2) DEFAULT 0.00 COMMENT '早饭金额',
    lunch DECIMAL(10,2) DEFAULT 0.00 COMMENT '午饭金额',
    dinner DECIMAL(10,2) DEFAULT 0.00 COMMENT '晚饭金额',
    snack DECIMAL(10,2) DEFAULT 0.00 COMMENT '零食金额',
    drink DECIMAL(10,2) DEFAULT 0.00 COMMENT '饮料金额',
    other DECIMAL(10,2) DEFAULT 0.00 COMMENT '其他金额',
    total DECIMAL(10,2) DEFAULT 0.00 COMMENT '总金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_record_date (record_date),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐饮记录表';

-- 插入测试数据
INSERT INTO meal_records (record_date, breakfast, lunch, dinner, snack, drink, other, total) VALUES
('2024-01-15', 15.00, 25.00, 30.00, 10.00, 8.00, 5.00, 93.00),
('2024-01-16', 12.00, 28.00, 35.00, 15.00, 12.00, 0.00, 102.00),
('2024-01-17', 18.00, 22.00, 28.00, 8.00, 6.00, 3.00, 85.00);

