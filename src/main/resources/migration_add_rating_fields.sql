-- 为pet_adoption表添加评价相关字段
USE pet_adoption_system;

-- 添加rating字段
ALTER TABLE pet_adoption ADD COLUMN rating INT DEFAULT NULL COMMENT '评分(1-5)' AFTER complete_time;

-- 添加comment字段
ALTER TABLE pet_adoption ADD COLUMN comment TEXT DEFAULT NULL COMMENT '评价内容' AFTER rating;

-- 添加rate_time字段
ALTER TABLE pet_adoption ADD COLUMN rate_time DATETIME DEFAULT NULL COMMENT '评价时间' AFTER comment;
