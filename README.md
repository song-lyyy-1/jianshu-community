# 简书问答社区 — 移动端应用

仿简书风格的简易移动端问答/创作社区，前后端完整实现，可联调运行。

## 技术栈

| 层面 | 技术 |
|------|------|
| 前端 | Vue3 + Vite + Vant4 + Vue Router + Pinia |
| 后端 | Spring Boot 3.2 + MyBatis-Plus 3.5 + JWT |
| 数据库 | MySQL 8.x |
| 博文格式 | Markdown (md-editor-v3) |

## 功能清单

- ✅ 首页（博文列表，下拉刷新+上拉加载+搜索）
- ✅ 博文详情（纯文本渲染 + 点赞/收藏）
- ✅ 登录/注册（JWT Token认证）
- ✅ 个人中心（统计：文章数、浏览量、点赞量、收藏量）
- ✅ 我的博文列表（全部/已发布/草稿筛选）
- ✅ 我点赞的博文列表
- ✅ 我收藏的博文列表
- ✅ 发表博文（纯文本编辑 + 草稿功能）

## 快速启动

### 1. 初始化数据库

```bash
# 启动MySQL
service mysql start

# 设置root密码
mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root'; FLUSH PRIVILEGES;"

# 创建数据库并初始化
mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS jianshu DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -proot jianshu < jianshu-backend/src/main/resources/sql/init.sql
```

### 2. 启动后端

```bash
cd jianshu-backend
mvn spring-boot:run
# 后端运行在 http://localhost:8080
```

### 3. 启动前端

```bash
cd jianshu-frontend
unset NODE_OPTIONS  # 如果遇到preload模块错误
pnpm install
npx vite --host 0.0.0.0 --port 5173
# 前端运行在 http://localhost:5173
```

### 4. 测试账号

| 用户名 | 密码 | 昵称 |
|--------|------|------|
| zhangsan | test123 | 张三 |
| lisi | test123 | 李四 |
| wangwu | test123 | 王五 |

## 项目结构

```
jianshu-community/
├── jianshu-frontend/          # Vue3前端
│   ├── src/
│   │   ├── api/               # 5个API模块
│   │   ├── components/        # 5个可复用组件
│   │   ├── layouts/           # 主布局+TabBar
│   │   ├── router/            # 11条路由+守卫
│   │   ├── stores/            # Pinia状态管理
│   │   ├── utils/             # axios封装+token工具
│   │   ├── views/             # 10个页面视图
│   │   ├── assets/styles/     # 全局样式(简书配色)
│   │   ├── App.vue
│   │   └── main.js
│   ├── vite.config.js         # @别名 + /api代理
│   └── package.json
│
├── jianshu-backend/           # Spring Boot后端
│   ├── src/main/java/com/jianshu/
│   │   ├── config/            # CORS + MyBatis-Plus + JWT注册
│   │   ├── controller/        # 5个Controller(15个接口)
│   │   ├── entity/            # 4个Entity
│   │   ├── mapper/            # 4个Mapper
│   │   ├── service/           # 4个Service+Impl
│   │   ├── dto/               # Result + Login/Register/Article DTO
│   │   ├── filter/            # JWT认证过滤器
│   │   ├── util/              # JwtUtil + UserContext
│   │   └── JianshuApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml    # MySQL + JWT + MyBatis-Plus配置
│   │   └── sql/init.sql       # 4张表DDL + 测试数据
│   └── pom.xml
│
└── README.md
```

## API接口一览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | /api/auth/login | 登录 |
| 认证 | POST | /api/auth/register | 注册 |
| 博文 | GET | /api/article/list | 博文列表(分页+搜索) |
| 博文 | GET | /api/article/detail/{id} | 博文详情 |
| 博文 | POST | /api/article/create | 创建博文 |
| 博文 | PUT | /api/article/update/{id} | 更新博文 |
| 博文 | DELETE | /api/article/delete/{id} | 删除博文 |
| 博文 | GET | /api/article/my | 我的博文列表 |
| 点赞 | POST | /api/like/toggle/{id} | 点赞/取消切换 |
| 点赞 | GET | /api/like/my | 我点赞的博文 |
| 收藏 | POST | /api/favorite/toggle/{id} | 收藏/取消切换 |
| 收藏 | GET | /api/favorite/my | 我收藏的博文 |
| 用户 | GET | /api/user/profile | 个人中心 |
| 用户 | GET | /api/user/stats | 统计数据 |
| 用户 | PUT | /api/user/update | 更新个人信息 |

## 数据库设计（4张核心表）

- **user** — 用户表(用户名BCrypt密码+昵称头像简介)
- **article** — 博文表(标题+内容+摘要+状态0草稿/1已发布+冗余计数)
- **article_like** — 点赞表(联合唯一索引user_id+article_id)
- **article_favorite** — 收藏表(联合唯一索引user_id+article_id)