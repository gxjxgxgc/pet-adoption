# 宠物领养系统后端

## 项目介绍

宠物领养系统后端是一个基于 Spring Boot 的 RESTful API 服务，为前端提供宠物领养相关的业务功能支持。系统采用分层架构，包括控制器、服务层、数据访问层等，实现了宠物管理、用户管理、领养管理等核心功能。

## 技术栈

- **框架**：Spring Boot 3.5.11
- **数据库**：MySQL 8.0+
- **ORM**：MyBatis-Plus 3.5.15
- **认证**：JWT (JSON Web Token)
- **API文档**：SpringDoc OpenAPI + Knife4j
- **构建工具**：Maven 3.9+
- **Java版本**：Java 17+

## 项目结构

```
pet-adoption/
├── src/
│   ├── main/
│   │   ├── java/com/petadoption/petadoption/
│   │   │   ├── config/         # 配置类
│   │   │   ├── controller/     # 控制器
│   │   │   ├── dto/            # 数据传输对象
│   │   │   ├── entity/         # 实体类
│   │   │   ├── exception/      # 异常处理
│   │   │   ├── mapper/         # 数据访问
│   │   │   ├── response/       # 响应码和枚举
│   │   │   ├── security/       # 安全相关
│   │   │   ├── service/        # 业务逻辑
│   │   │   └── PetAdoptionApplication.java  # 应用入口
│   │   └── resources/
│   │       ├── mapper/         # MyBatis XML
│   │       ├── application.yml # 应用配置
│   │       └── schema.sql      # 数据库脚本
│   └── test/                   # 测试代码
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## 核心功能

1. **用户管理**：注册、登录、获取用户信息
2. **宠物管理**：发布、查询、更新、删除宠物信息
3. **领养管理**：提交领养申请、审核领养申请
4. **评论管理**：对宠物发表评论
5. **收藏管理**：收藏/取消收藏宠物

## 快速开始

### 环境要求

- JDK 17 或更高版本
- MySQL 8.0 或更高版本
- Maven 3.9 或更高版本

### 数据库配置

1. 创建数据库：
   ```sql
   CREATE DATABASE pet_adoption_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 执行数据库脚本：
   ```bash
   mysql -u root -p pet_adoption_system < src/main/resources/schema.sql
   ```

### 应用配置

修改 `src/main/resources/application.yml` 文件：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pet_adoption_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的数据库密码

jwt:
  secret: your_jwt_secret_key  # 修改为你的JWT密钥
  expiration: 86400000  # 24小时（毫秒）
```

### 构建和运行

1. 构建项目：
   ```bash
   mvn clean package
   ```

2. 运行项目：
   ```bash
   mvn spring-boot:run
   ```

3. 访问 API 文档：
   - Swagger UI: http://localhost:8080/api/swagger-ui.html
   - Knife4j: http://localhost:8080/api/doc.html

## API 接口

### 用户相关

- `POST /api/users/register` - 用户注册
- `POST /api/users/login` - 用户登录
- `GET /api/users/info` - 获取用户信息
- `POST /api/users/logout` - 用户登出

### 宠物相关

- `GET /api/pets/list` - 获取宠物列表
- `GET /api/pets/{id}` - 获取宠物详情
- `POST /api/pets` - 创建宠物
- `PUT /api/pets/{id}` - 更新宠物
- `DELETE /api/pets/{id}` - 删除宠物
- `GET /api/pets/search` - 搜索宠物
- `GET /api/pets/hot` - 获取热门宠物

### 领养相关

- `POST /api/adoptions` - 提交领养申请
- `GET /api/adoptions/list` - 获取领养列表
- `PUT /api/adoptions/{id}/status` - 更新领养状态

### 评论相关

- `POST /api/comments` - 发表评论
- `GET /api/comments/pet/{petId}` - 获取宠物评论

### 收藏相关

- `POST /api/favorites/{petId}` - 收藏宠物
- `DELETE /api/favorites/{petId}` - 取消收藏
- `GET /api/favorites` - 获取收藏列表

## 错误码

| 错误码 | 描述 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | 用户名已存在 |
| 1002 | 密码错误 |
| 1003 | 宠物不存在 |

## 开发指南

### 代码规范

- 遵循 Spring 代码风格
- 使用驼峰命名法
- 为关键方法和类添加 Javadoc 注释
- 统一使用自定义异常处理
- 使用 SLF4J 进行日志记录

### 测试

- 为核心服务编写单元测试
- 为 API 接口编写集成测试
- 确保测试覆盖率达到 60% 以上

### 部署

- 生产环境建议使用 Docker 容器化部署
- 配置环境变量管理敏感信息
- 启用 HTTPS 确保数据传输安全

## 许可证

MIT
