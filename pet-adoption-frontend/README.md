# 宠物领养系统前端

## 项目介绍

宠物领养系统前端是一个基于 Vue 3 的单页应用，为用户提供友好的宠物领养平台界面。系统采用现代化的前端技术栈，实现了宠物浏览、领养申请、用户管理等功能，为用户提供流畅的交互体验。

## 技术栈

- **框架**：Vue 3
- **构建工具**：Vite 7.3.1
- **UI组件库**：Element Plus
- **状态管理**：Pinia
- **路由**：Vue Router
- **HTTP客户端**：Axios
- **日期处理**：Day.js
- **工具库**：Lodash ES

## 项目结构

```
pet-adoption-frontend/
├── public/            # 静态资源
├── src/
│   ├── api/           # API 调用
│   ├── assets/        # 静态资源
│   │   └── styles/    # 样式文件
│   ├── components/    # 通用组件
│   ├── router/        # 路由配置
│   ├── stores/        # 状态管理
│   ├── utils/         # 工具函数
│   ├── views/         # 页面组件
│   │   ├── admin/     # 管理员页面
│   │   ├── adoption/  # 领养相关页面
│   │   ├── error/     # 错误页面
│   │   ├── home/      # 首页
│   │   ├── pet/       # 宠物相关页面
│   │   └── user/      # 用户相关页面
│   ├── App.vue        # 根组件
│   ├── main.js        # 入口文件
│   └── style.css      # 全局样式
├── .gitignore
├── index.html
├── package.json
└── vite.config.js
```

## 核心功能

1. **首页**：展示热门宠物和最新宠物
2. **宠物列表**：分页展示宠物，支持筛选和搜索
3. **宠物详情**：查看宠物详细信息，支持评论和收藏
4. **用户管理**：注册、登录、个人中心
5. **领养管理**：提交领养申请，查看领养状态
6. **管理员后台**：宠物管理、用户管理、领养审核

## 快速开始

### 环境要求

- Node.js 16.0 或更高版本
- npm 7.0 或更高版本

### 安装依赖

```bash
npm install
```

### 开发模式运行

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 页面路由

### 公共页面

- `/` - 首页
- `/pets` - 宠物列表
- `/pets/{id}` - 宠物详情
- `/login` - 登录
- `/register` - 注册

### 需要登录的页面

- `/user/profile` - 个人中心
- `/adoption/apply/{petId}` - 提交领养申请
- `/adoption/my` - 我的领养申请

### 管理员页面

- `/admin` - 管理员后台
- `/admin/pets` - 宠物管理
- `/admin/users` - 用户管理
- `/admin/adoptions` - 领养审核

## API 调用

前端通过 axios 调用后端 API，API 基础路径为 `/api`。主要 API 调用包括：

- **用户相关**：注册、登录、获取用户信息
- **宠物相关**：获取宠物列表、获取宠物详情、发布宠物、更新宠物、删除宠物
- **领养相关**：提交领养申请、获取领养列表、更新领养状态
- **评论相关**：发表评论、获取宠物评论
- **收藏相关**：收藏宠物、取消收藏、获取收藏列表

## 开发指南

### 代码规范

- 遵循 Vue 代码风格
- 使用 kebab-case 命名组件
- 为关键组件和方法添加注释
- 使用 Pinia 进行状态管理
- 使用 Vue Router 进行路由管理

### 组件设计

- 通用组件放在 `components` 目录
- 页面组件放在 `views` 目录
- 组件命名使用 PascalCase
- 样式使用 scoped 或 CSS Modules

### 状态管理

- 使用 Pinia 进行状态管理
- 按功能模块划分 store
- 遵循单向数据流原则
- 状态持久化使用 localStorage

## 性能优化

- 图片懒加载
- 组件懒加载
- 路由懒加载
- 缓存热点数据
- 减少 HTTP 请求

## 浏览器兼容性

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## 许可证

MIT

