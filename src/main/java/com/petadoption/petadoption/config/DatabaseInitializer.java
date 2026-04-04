package com.petadoption.petadoption.config;

import com.petadoption.petadoption.service.HealthStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final HealthStatusService healthStatusService;

    @Autowired
    public DatabaseInitializer(JdbcTemplate jdbcTemplate, HealthStatusService healthStatusService) {
        this.jdbcTemplate = jdbcTemplate;
        this.healthStatusService = healthStatusService;
    }

    @Override
    public void run(String... args) throws Exception {
        createHealthStatusTable();
        createPetHealthStatusTable();
        healthStatusService.initHealthStatuses();
        migrateHealthStatusData();
    }

    private void migrateHealthStatusData() {
        // 首先检查关联表是否有数据
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pet_health_status", Integer.class
        );
        
        // 如果有数据则不处理
        if (count != null && count > 0) {
            return;
        }
        
        // 尝试从旧的 health_status 字段迁移
        String migrateSql = "INSERT INTO pet_health_status (pet_id, health_status_id) " +
                "SELECT p.id, hs.id FROM pet_pet p " +
                "JOIN health_status hs ON hs.code COLLATE utf8mb4_unicode_ci = p.health_status COLLATE utf8mb4_unicode_ci " +
                "WHERE p.health_status IS NOT NULL AND p.health_status != '' AND p.deleted = 0";
        
        int migrated = jdbcTemplate.update(migrateSql);
        if (migrated > 0) {
            System.out.println("Migrated " + migrated + " health status records from old field");
        }
        
        // 为还没有健康状况的宠物添加默认健康状况
        String checkSql = "SELECT COUNT(*) FROM pet_health_status";
        Integer newCount = jdbcTemplate.queryForObject(checkSql, Integer.class);
        
        if (newCount == null || newCount == 0) {
            // 为所有已发布的宠物添加默认的"健康"状态
            String defaultSql = "INSERT INTO pet_health_status (pet_id, health_status_id) " +
                    "SELECT p.id, hs.id FROM pet_pet p " +
                    "CROSS JOIN health_status hs " +
                    "WHERE hs.code = 'healthy' AND p.status = 1 AND p.deleted = 0 " +
                    "AND NOT EXISTS (SELECT 1 FROM pet_health_status phs WHERE phs.pet_id = p.id)";
            
            int defaultMigrated = jdbcTemplate.update(defaultSql);
            if (defaultMigrated > 0) {
                System.out.println("Added default health status for " + defaultMigrated + " pets");
            }
        }
    }

    private void createHealthStatusTable() {
        List<String> tables = jdbcTemplate.queryForList(
                "SHOW TABLES LIKE 'health_status'", String.class
        );

        if (tables.isEmpty()) {
            String sql = "CREATE TABLE `health_status` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`code` VARCHAR(50) NOT NULL," +
                "`name` VARCHAR(100) NOT NULL," +
                "`description` VARCHAR(255)," +
                "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_code` (`code`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            jdbcTemplate.execute(sql);
            System.out.println("Created health_status table");
        }
    }

    private void createPetHealthStatusTable() {
        List<String> tables = jdbcTemplate.queryForList(
                "SHOW TABLES LIKE 'pet_health_status'", String.class
        );

        if (tables.isEmpty()) {
            String sql = "CREATE TABLE `pet_health_status` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`pet_id` BIGINT NOT NULL," +
                "`health_status_id` BIGINT NOT NULL," +
                "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)," +
                "KEY `idx_pet_id` (`pet_id`)," +
                "KEY `idx_health_status_id` (`health_status_id`)," +
                "CONSTRAINT `fk_pet_health_status_pet` FOREIGN KEY (`pet_id`) REFERENCES `pet_pet` (`id`) ON DELETE CASCADE," +
                "CONSTRAINT `fk_pet_health_status_health` FOREIGN KEY (`health_status_id`) REFERENCES `health_status` (`id`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            jdbcTemplate.execute(sql);
            System.out.println("Created pet_health_status table");
        }
    }
}