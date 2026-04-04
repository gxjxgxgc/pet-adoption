# 宠物领养系统 - 健康状况多选联查功能修改清单

## 版本信息
- **修改日期**: 2026-03-14
- **修改版本**: v1.1.0
- **修改目标**: 实现健康状况多选联查功能，优化数据结构支持多个健康状况的联合筛选

---

## 一、问题背景

### 原有问题
1. 数据库中健康状况字段仅支持存储单一状态值（健康、已疫苗、已绝育中的一种）
2. 前端在对健康状况进行多条件联合筛选时无法正确返回匹配数据
3. 数据结构限制导致无法支持多选项组合筛选

### 解决方案
1. 创建健康状况字典表 `health_status`
2. 创建宠物健康状况关联表 `pet_health_status`（多对多关系）
3. 实现数据迁移，将旧数据迁移到新结构
4. 修改前后端代码支持新的数据结构

---

## 二、数据库变更

### 2.1 新增表结构

#### health_status（健康状况字典表）
```sql
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
```

#### pet_health_status（宠物健康状况关联表）
```sql
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
```

### 2.2 初始数据
```sql
INSERT INTO `health_status` (`code`, `name`, `description`) VALUES 
('healthy', '健康', '宠物身体健康'),
('vaccinated', '已疫苗', '宠物已接种疫苗'),
('sterilized', '已绝育', '宠物已绝育');
```

---

## 三、后端修改清单

### 3.1 实体类

| 文件 | 修改内容 | 状态 |
|------|----------|------|
| `entity/HealthStatus.java` | 新增健康状况实体类 | ✅ 完成 |
| `entity/PetHealthStatus.java` | 新增宠物健康状况关联实体类 | ✅ 完成 |
| `entity/Pet.java` | 移除healthStatus字段，添加healthStatuses关联 | ✅ 完成 |

### 3.2 Mapper层

| 文件 | 修改内容 | 状态 |
|------|----------|------|
| `mapper/HealthStatusMapper.java` | 新增健康状况Mapper接口 | ✅ 完成 |
| `mapper/PetHealthStatusMapper.java` | 新增宠物健康状况关联Mapper接口 | ✅ 完成 |
| `mapper/PetMapper.java` | 新增selectPetWithHealthStatuses方法 | ✅ 完成 |
| `resources/mapper/HealthStatusMapper.xml` | 实现selectByCode方法 | ✅ 完成 |
| `resources/mapper/PetMapper.xml` | 移除healthStatus字段映射，新增关联查询 | ✅ 完成 |

### 3.3 Service层

| 文件 | 修改内容 | 状态 |
|------|----------|------|
| `service/HealthStatusService.java` | 新增健康状况服务接口 | ✅ 完成 |
| `service/impl/HealthStatusServiceImpl.java` | 实现健康状况服务，包含初始化方法 | ✅ 完成 |
| `service/PetService.java` | 新增getPetWithHealthStatuses方法 | ✅ 完成 |
| `service/impl/PetServiceImpl.java` | 修改savePet支持健康状况关联，新增关联查询 | ✅ 完成 |

### 3.4 Controller层

| 文件 | 修改内容 | 状态 |
|------|----------|------|
| `controller/PetController.java` | 新增getHealthStatuses接口，修改getPetDetail调用关联查询 | ✅ 完成 |

### 3.5 配置类

| 文件 | 修改内容 | 状态 |
|------|----------|------|
| `config/DatabaseInitializer.java` | 新增数据库初始化类，自动创建表结构和迁移数据 | ✅ 完成 |

---

## 四、前端修改清单

### 4.1 API层

| 文件 | 修改内容 | 状态 |
|------|----------|------|
| `api/pet.js` | 新增getHealthStatuses API调用 | ✅ 完成 |

### 4.2 视图层

| 文件 | 修改内容 | 状态 |
|------|----------|------|
| `views/pet/List.vue` | 将健康状况筛选从多选改为单选，添加"全部"选项 | ✅ 完成 |
| `views/pet/Publish.vue` | 修改健康状况选择为多选，动态加载健康状况选项 | ✅ 完成 |
| `views/pet/Detail.vue` | 更新健康状况显示逻辑，遍历healthStatuses数组 | ✅ 完成 |

---

## 五、关键修改详情

### 5.1 PetMapper.xml 修改

**修改前：**
```xml
<select id="selectPetsByFilter" resultMap="BaseResultMap">
    SELECT p.* FROM pet_pet p WHERE p.deleted = 0
    ...
</select>
```

**修改后：**
```xml
<select id="selectPetsByFilter" resultMap="PetWithHealthStatusResultMap">
    SELECT DISTINCT p.*, 
           hs.id as hs_id, 
           hs.code as hs_code, 
           hs.name as hs_name, 
           hs.description as hs_description
    FROM pet_pet p
    LEFT JOIN pet_health_status phs ON p.id = phs.pet_id
    LEFT JOIN health_status hs ON phs.health_status_id = hs.id
    WHERE p.deleted = 0
    ...
    <if test="healthStatus != null and healthStatus != ''">
        AND p.id IN (
            SELECT phs2.pet_id FROM pet_health_status phs2
            JOIN health_status hs2 ON phs2.health_status_id = hs2.id
            WHERE FIND_IN_SET(hs2.code, #{healthStatus}) > 0
        )
    </if>
    ...
</select>
```

### 5.2 数据迁移逻辑

```java
private void migrateHealthStatusData() {
    Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM pet_health_status", Integer.class);
    
    if (count != null && count > 0) {
        return;
    }
    
    String sql = "INSERT INTO pet_health_status (pet_id, health_status_id) " +
            "SELECT p.id, hs.id FROM pet_pet p " +
            "JOIN health_status hs ON hs.code COLLATE utf8mb4_unicode_ci = p.health_status COLLATE utf8mb4_unicode_ci " +
            "WHERE p.health_status IS NOT NULL AND p.health_status != '' AND p.deleted = 0";
    
    int migrated = jdbcTemplate.update(sql);
    if (migrated > 0) {
        System.out.println("Migrated " + migrated + " health status records");
    }
}
```

### 5.3 前端筛选修改

**修改前（多选）：**
```vue
<el-checkbox-group v-model="filterForm.healthStatus" @change="handleSearch">
    <el-checkbox label="healthy">健康</el-checkbox>
    <el-checkbox label="vaccinated">已疫苗</el-checkbox>
    <el-checkbox label="sterilized">已绝育</el-checkbox>
</el-checkbox-group>
```

**修改后（单选）：**
```vue
<el-radio-group v-model="filterForm.healthStatus" @change="handleSearch">
    <el-radio label="">全部</el-radio>
    <el-radio label="healthy">健康</el-radio>
    <el-radio label="vaccinated">已疫苗</el-radio>
    <el-radio label="sterilized">已绝育</el-radio>
</el-radio-group>
```

---

## 六、问题修复记录

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| PetMapper.xml包含已删除字段 | 重构时未更新XML映射 | 移除healthStatus字段映射 |
| 前端发送格式与后端不匹配 | 前端发送code字符串，后端期望对象 | 修改savePet支持两种格式 |
| 数据库初始化逻辑重复 | 两处都有初始化逻辑 | 统一由DatabaseInitializer调用 |
| 宠物详情未加载健康状况 | 使用getById未关联查询 | 新增selectPetWithHealthStatuses方法 |
| 前端详情页显示逻辑过时 | 假设healthStatus是字符串 | 改为遍历healthStatuses数组 |
| 筛选功能无法工作 | 查询使用AND逻辑，且未返回健康状况 | 改为OR逻辑，添加关联查询 |
| 字符集冲突 | utf8mb4_unicode_ci vs utf8mb4_0900_ai_ci | 使用COLLATE指定字符集 |

---

## 七、测试验证

### 7.1 数据迁移验证
```
Migrated 4 health status records ✅
```

### 7.2 API测试
- 健康状况列表: `GET /api/pets/health-statuses` ✅
- 宠物列表筛选: `GET /api/pets/list?healthStatus=healthy` ✅
- 宠物详情: `GET /api/pets/{id}` ✅

### 7.3 前端测试
- 健康状况单选筛选 ✅
- 宠物详情页健康状况显示 ✅
- 发布宠物健康状况选择 ✅

---

## 八、部署说明

### 8.1 数据库迁移
应用启动时会自动执行以下操作：
1. 检查并创建 `health_status` 表
2. 检查并创建 `pet_health_status` 表
3. 初始化健康状况基础数据
4. 迁移旧数据到关联表

### 8.2 注意事项
- 迁移只会执行一次（检查关联表是否已有数据）
- 旧的 `health_status` 字段保留，但不建议继续使用
- 建议在低峰期执行迁移

---

## 九、后续优化建议

1. **性能优化**: 为关联表添加复合索引
2. **功能增强**: 支持批量设置健康状况
3. **数据清理**: 迁移完成后可考虑删除旧字段
4. **缓存优化**: 对健康状况字典数据进行缓存
