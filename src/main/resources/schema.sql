-- 宠物领养系统数据库表结构
-- 数据库：pet_adoption_system

-- 创建数据库
CREATE DATABASE IF NOT EXISTS pet_adoption_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pet_adoption_system;

-- 1. 用户表
DROP TABLE IF EXISTS pet_user;
CREATE TABLE pet_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID,主键',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（登录用）',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(500) COMMENT '头像URL',
    gender VARCHAR(10) COMMENT '性别：男/女',
    age INT COMMENT '年龄',
    birthday DATE COMMENT '出生日期',
    id_card VARCHAR(18) COMMENT '身份证号',
    is_verified TINYINT DEFAULT 0 COMMENT '实名认证：0-待审核，1-已认证，-1-拒绝',
    user_type VARCHAR(20) DEFAULT 'PERSONAL' COMMENT '用户类型：PERSONAL-个人，SHELTER-救助机构',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '系统角色：USER-普通用户，SHELTER-救助者，ADMIN-管理员',
    status TINYINT DEFAULT 1 COMMENT '账号状态：0-停用，1-正常，2-锁定',
    bio TEXT COMMENT '个人/机构简介',
    address VARCHAR(255) COMMENT '联系地址',
    credit_score INT DEFAULT 100 COMMENT '信用分（0-100）',
    favorite_count INT DEFAULT 0 COMMENT '收藏数量',
    adoption_count INT DEFAULT 0 COMMENT '领养数量',
    last_login_time DATETIME COMMENT '最后登录时间',
    tags VARCHAR(500) COMMENT '用户标签（多个用逗号分隔）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_user_type (user_type),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物领养系统-用户表';

-- 2. 宠物表
DROP TABLE IF EXISTS pet_pet;
CREATE TABLE pet_pet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '宠物ID',
    name VARCHAR(50) NOT NULL COMMENT '宠物名称',
    type VARCHAR(20) NOT NULL COMMENT '宠物类型：dog-狗，cat-猫，other-其他',
    breed VARCHAR(50) COMMENT '品种',
    age INT COMMENT '年龄（月）',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-公，2-母',
    color VARCHAR(20) COMMENT '颜色',
    weight DECIMAL(5,2) COMMENT '体重（kg）',
    description TEXT COMMENT '描述',
    images TEXT COMMENT '图片URL（多个用逗号分隔）',
    status TINYINT DEFAULT 1 COMMENT '状态：0-隐藏，1-待领养，2-领养中，3-已领养',
    health_status VARCHAR(20) DEFAULT 'healthy' COMMENT '健康状况：healthy-健康，vaccinated-已疫苗，sterilized-已绝育',
    sterilized TINYINT DEFAULT 0 COMMENT '是否绝育：0-否，1-是',
    owner_id BIGINT COMMENT '主人ID（个人用户）',
    shelter_id BIGINT COMMENT '收容所ID（机构用户）',
    publish_time DATETIME COMMENT '发布时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_gender (gender),
    INDEX idx_owner_id (owner_id),
    INDEX idx_shelter_id (shelter_id),
    INDEX idx_publish_time (publish_time),
    INDEX idx_view_count (view_count),
    INDEX idx_favorite_count (favorite_count),
    -- 注意：外键约束可根据实际需求选择是否启用
    -- FOREIGN KEY (owner_id) REFERENCES pet_user(id) ON DELETE SET NULL,
    -- FOREIGN KEY (shelter_id) REFERENCES pet_user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';

-- 3. 领养申请表
DROP TABLE IF EXISTS pet_adoption;
CREATE TABLE pet_adoption (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    pet_id BIGINT NOT NULL COMMENT '宠物ID',
    user_id BIGINT NOT NULL COMMENT '申请用户ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝，3-已取消，4-已完成',
    reason TEXT COMMENT '申请理由',
    contact_name VARCHAR(50) COMMENT '联系人',
    phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(200) COMMENT '居住地址',
    housing_type VARCHAR(20) COMMENT '住房类型',
    experience TINYINT DEFAULT 0 COMMENT '养宠经验：0-无经验，1-有经验',
    experience_desc TEXT COMMENT '经验描述',
    pet_plan TEXT COMMENT '养宠计划',
    work_info TEXT COMMENT '工作情况',
    family_info TEXT COMMENT '家庭情况',
    financial_status TEXT COMMENT '经济状况',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    review_comment TEXT COMMENT '审核意见',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    review_time DATETIME COMMENT '审核时间',
    reviewer_id BIGINT COMMENT '审核人ID',
    complete_time DATETIME COMMENT '完成时间',
    rating INT DEFAULT NULL COMMENT '评分(1-5)',
    comment TEXT DEFAULT NULL COMMENT '评价内容',
    rate_time DATETIME DEFAULT NULL COMMENT '评价时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_pet_id (pet_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_apply_time (apply_time),
    INDEX idx_reviewer_id (reviewer_id),
    -- 注意：外键约束可根据实际需求选择是否启用
    -- FOREIGN KEY (pet_id) REFERENCES pet_pet(id),
    -- FOREIGN KEY (user_id) REFERENCES pet_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领养申请表';

-- 4. 收藏表
DROP TABLE IF EXISTS pet_favorite;
CREATE TABLE pet_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    pet_id BIGINT NOT NULL COMMENT '宠物ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_pet (user_id, pet_id),
    INDEX idx_user_id (user_id),
    INDEX idx_pet_id (pet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 5. 评论表
DROP TABLE IF EXISTS pet_comment;
CREATE TABLE pet_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    pet_id BIGINT NOT NULL COMMENT '宠物ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID（NULL表示顶级评论）',
    reply_to_user_id BIGINT DEFAULT NULL COMMENT '被回复用户ID',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-待审核，1-已发布，2-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_pet_id (pet_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_create_time (create_time),
    INDEX idx_status (status)
    -- FOREIGN KEY (pet_id) REFERENCES pet_pet(id),
    -- FOREIGN KEY (user_id) REFERENCES pet_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 6. 健康状况字典表
DROP TABLE IF EXISTS health_status;
CREATE TABLE health_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '健康状况ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '健康状况代码',
    name VARCHAR(100) NOT NULL COMMENT '健康状况名称',
    description VARCHAR(255) COMMENT '健康状况描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康状况字典表';

-- 7. 宠物健康状况关联表
DROP TABLE IF EXISTS pet_health_status;
CREATE TABLE pet_health_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    pet_id BIGINT NOT NULL COMMENT '宠物ID',
    health_status_id BIGINT NOT NULL COMMENT '健康状况ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_pet_health (pet_id, health_status_id),
    INDEX idx_pet_id (pet_id),
    INDEX idx_health_status_id (health_status_id),
    FOREIGN KEY (pet_id) REFERENCES pet_pet(id) ON DELETE CASCADE,
    FOREIGN KEY (health_status_id) REFERENCES health_status(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物健康状况关联表';

-- 8. 用户活动日志表
CREATE TABLE IF NOT EXISTS activity_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    activity_type VARCHAR(50) NOT NULL COMMENT '活动类型: favorite-收藏, unfavorite-取消收藏, adoption-领养申请, adoption_approved-领养通过, adoption_rejected-领养拒绝, comment-评论, profile-个人信息更新, verification-实名认证',
    target_type VARCHAR(50) NOT NULL COMMENT '目标类型: pet-宠物, adoption-领养, comment-评论, user-用户',
    target_id BIGINT COMMENT '目标ID',
    description VARCHAR(500) COMMENT '活动描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户活动日志表';

-- 6. 插入测试数据

-- 插入健康状况字典数据
INSERT INTO health_status (code, name, description) VALUES 
('healthy', '健康', '宠物身体健康，无疾病'),
('vaccinated', '已疫苗', '已完成疫苗接种'),
('sterilized', '已绝育', '已完成绝育手术');

-- 插入管理员用户
INSERT INTO pet_user (username, password, email, phone, real_name, user_type, role, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@pet.com', '13800138000', '系统管理员', 'PERSONAL', 'ADMIN', 1);

-- 插入普通用户
INSERT INTO pet_user (username, password, email, phone, real_name, user_type, role, status) 
VALUES ('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user1@pet.com', '13800138001', '张三', 'PERSONAL', 'USER', 1),
       ('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user2@pet.com', '13800138002', '李四', 'PERSONAL', 'USER', 1);

-- 插入收容所用户
INSERT INTO pet_user (username, password, email, phone, real_name, user_type, role, status, bio, address) 
VALUES ('shelter1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'shelter1@pet.com', '13800138003', '爱心收容所', 'SHELTER', 'RESCUER', 1, '致力于为流浪动物提供温暖的家', '北京市朝阳区');

-- 插入测试宠物数据（收容所发布的宠物，shelter_id=4，owner_id=NULL）
INSERT INTO pet_pet (name, type, breed, age, gender, color, weight, description, images, status, health_status, sterilized, owner_id, shelter_id, publish_time) 
VALUES ('小白', 'cat', '英国短毛猫', 12, 2, '白色', 3.5, '性格温顺，喜欢和人亲近，已完成疫苗接种', '/uploads/20260314182221947_9ff412af.png', 1, 'healthy', 1, NULL, 4, NOW()),
       ('大黄', 'dog', '金毛', 18, 1, '金色', 25.0, '活泼好动，非常适合家庭饲养，已绝育', '/uploads/20260314184230764_02e62b72.png', 1, 'healthy', 1, NULL, 4, NOW()),
       ('小花', 'cat', '中华田园猫', 6, 2, '三花', 2.8, '可爱的小猫咪，喜欢玩耍，需要爱心家庭', '/uploads/20260314184942777_d0092d7e.png', 1, 'healthy', 0, NULL, 4, NOW()),
       ('旺财', 'dog', '柯基', 24, 1, '黄白相间', 12.5, '短腿萌宠，性格开朗，已绝育', '/uploads/20260314194036737_849eaeee.png', 1, 'healthy', 1, NULL, 4, NOW());

-- 插入宠物健康状况关联数据
INSERT INTO pet_health_status (pet_id, health_status_id) VALUES 
(1, 1), (1, 2), 
(2, 1), (2, 2), (2, 3), 
(3, 1), 
(4, 1), (4, 2), (4, 3);

-- 插入测试收藏数据
INSERT INTO pet_favorite (user_id, pet_id) VALUES (2, 1), (2, 2), (3, 3);

-- 插入测试评论数据
INSERT INTO pet_comment (pet_id, user_id, content) VALUES (1, 2, '这只猫咪好可爱！'), (1, 3, '希望能给它一个温暖的家'), (2, 2, '金毛真的很聪明');

-- 插入测试领养申请数据（shelter_id 字段应该删除，因为已经从 pet_id 关联）
INSERT INTO pet_adoption (pet_id, user_id, status, reason) VALUES (1, 2, 0, '我很喜欢这只猫咪，我有养猫经验，会好好照顾它');


-- =============================================
-- 数据验证查询
-- =============================================

-- 查看用户数据
SELECT '========== 用户数据 ==========' AS info;
SELECT id, username, email, phone, user_type, role,
       CASE status WHEN 0 THEN '停用' WHEN 1 THEN '正常' WHEN 2 THEN '锁定' END AS status_text,
       create_time
FROM pet_user
WHERE deleted = 0;

-- 查看宠物数据
SELECT '========== 宠物数据 ==========' AS info;
SELECT id, name, 
       CASE type WHEN 'dog' THEN '狗' WHEN 'cat' THEN '猫' ELSE '其他' END AS type_text,
       breed,
       CASE gender WHEN 0 THEN '未知' WHEN 1 THEN '公' WHEN 2 THEN '母' END AS gender_text,
       CONCAT(age, '个月') AS age_text,
       CASE status WHEN 0 THEN '隐藏' WHEN 1 THEN '待领养' WHEN 2 THEN '领养中' WHEN 3 THEN '已领养' END AS status_text,
       CASE health_status WHEN 'healthy' THEN '健康' WHEN 'vaccinated' THEN '已疫苗' WHEN 'sterilized' THEN '已绝育' END AS health_text,
       IF(sterilized=1, '是', '否') AS sterilized_text,
       owner_id, shelter_id
FROM pet_pet
WHERE deleted = 0;

-- 查看收藏数据
SELECT '========== 收藏数据 ==========' AS info;
SELECT f.id, u.username, p.name AS pet_name, f.create_time
FROM pet_favorite f
LEFT JOIN pet_user u ON f.user_id = u.id
LEFT JOIN pet_pet p ON f.pet_id = p.id;

-- 查看评论数据
SELECT '========== 评论数据 ==========' AS info;
SELECT c.id, p.name AS pet_name, u.username, c.content, c.create_time
FROM pet_comment c
LEFT JOIN pet_pet p ON c.pet_id = p.id
LEFT JOIN pet_user u ON c.user_id = u.id
WHERE c.deleted = 0
ORDER BY c.pet_id, c.create_time;

-- 查看健康状况字典数据
SELECT '========== 健康状况字典数据 ==========' AS info;
SELECT id, code, name, description
FROM health_status
ORDER BY id;

-- 查看宠物健康状况关联数据
SELECT '========== 宠物健康状况关联数据 ==========' AS info;
SELECT phs.id, p.name AS pet_name, hs.code, hs.name AS health_status_name
FROM pet_health_status phs
LEFT JOIN pet_pet p ON phs.pet_id = p.id
LEFT JOIN health_status hs ON phs.health_status_id = hs.id
ORDER BY phs.pet_id, phs.id;

-- 查看领养申请数据
SELECT '========== 领养申请数据 ==========' AS info;
SELECT a.id, p.name AS pet_name, u.username AS applicant,
       CASE a.status 
           WHEN 0 THEN '待审核' 
           WHEN 1 THEN '已通过' 
           WHEN 2 THEN '已拒绝' 
           WHEN 3 THEN '已取消' 
           WHEN 4 THEN '已完成' 
       END AS status_text,
       LEFT(a.reason, 30) AS reason_preview,
       a.create_time
FROM pet_adoption a
LEFT JOIN pet_pet p ON a.pet_id = p.id
LEFT JOIN pet_user u ON a.user_id = u.id
WHERE a.deleted = 0
ORDER BY a.create_time DESC;

-- 统计信息
SELECT '========== 统计信息 ==========' AS info;
SELECT 
    (SELECT COUNT(*) FROM pet_user WHERE deleted = 0) AS user_count,
    (SELECT COUNT(*) FROM pet_pet WHERE deleted = 0) AS pet_count,
    (SELECT COUNT(*) FROM pet_pet WHERE status = 1 AND deleted = 0) AS available_pet_count,
    (SELECT COUNT(*) FROM pet_favorite) AS favorite_count,
    (SELECT COUNT(*) FROM pet_comment WHERE deleted = 0) AS comment_count,
    (SELECT COUNT(*) FROM pet_adoption WHERE deleted = 0) AS adoption_count,
    (SELECT COUNT(*) FROM health_status) AS health_status_count,
    (SELECT COUNT(*) FROM pet_health_status) AS pet_health_status_count;