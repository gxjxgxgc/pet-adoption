-- 创建健康状况表
CREATE TABLE `health_status` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(255),
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建宠物健康状况关联表
CREATE TABLE `pet_health_status` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `pet_id` BIGINT NOT NULL,
  `health_status_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pet_id` (`pet_id`),
  KEY `idx_health_status_id` (`health_status_id`),
  CONSTRAINT `fk_pet_health_status_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet_pet` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pet_health_status_health` FOREIGN KEY (`health_status_id`) REFERENCES `health_status` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化健康状况数据
INSERT INTO `health_status` (`code`, `name`, `description`) VALUES
('healthy', '健康', '宠物身体健康'),
('vaccinated', '已疫苗', '宠物已接种疫苗'),
('sterilized', '已绝育', '宠物已绝育');

-- 迁移现有健康状况数据
INSERT INTO `pet_health_status` (`pet_id`, `health_status_id`, `create_time`)
SELECT 
    p.id AS pet_id,
    hs.id AS health_status_id,
    NOW() AS create_time
FROM 
    pet_pet p,
    health_status hs
WHERE 
    FIND_IN_SET(hs.code, p.health_status) > 0
    AND p.deleted = 0;

-- 验证迁移结果
SELECT 
    p.id,
    p.name,
    GROUP_CONCAT(hs.name SEPARATOR ',') AS health_statuses
FROM 
    pet_pet p
LEFT JOIN 
    pet_health_status phs ON p.id = phs.pet_id
LEFT JOIN 
    health_status hs ON phs.health_status_id = hs.id
WHERE 
    p.deleted = 0
GROUP BY 
    p.id, p.name
LIMIT 10;