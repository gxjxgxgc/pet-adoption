-- 添加用户标签字段
ALTER TABLE pet_user ADD COLUMN tags VARCHAR(500) COMMENT '用户标签（多个用逗号分隔）';
