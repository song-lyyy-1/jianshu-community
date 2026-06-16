-- 简书问答社区数据库初始化脚本

CREATE DATABASE IF NOT EXISTS jianshu DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE jianshu;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '头像URL',
    `email` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '邮箱',
    `bio` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '个人简介',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 文章表
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `user_id` BIGINT NOT NULL COMMENT '作者ID',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `content` LONGTEXT NOT NULL COMMENT '内容(Markdown)',
    `summary` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '摘要',
    `cover_image` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '封面图URL',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0草稿 1已发布',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status_created_at` (`status`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 文章点赞表
DROP TABLE IF EXISTS `article_like`;
CREATE TABLE `article_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞表';

-- 文章收藏表
DROP TABLE IF EXISTS `article_favorite`;
CREATE TABLE `article_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章收藏表';

-- 评论表
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ===================== 测试数据 =====================

-- 用户 (密码BCrypt加密: test123)
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `email`, `bio`) VALUES
('zhangsan', '$2b$12$pGFOuFxzJrm2YfYunTS3buu8UKbnUsh5EZABCvy4hQioWhDTdr0g2', '张三', 'https://picsum.photos/seed/user1/200/200', 'zhangsan@example.com', '热爱写作，专注技术分享'),
('lisi', '$2b$12$pGFOuFxzJrm2YfYunTS3buu8UKbnUsh5EZABCvy4hQioWhDTdr0g2', '李四', 'https://picsum.photos/seed/user2/200/200', 'lisi@example.com', '读书破万卷，下笔如有神'),
('wangwu', '$2b$12$pGFOuFxzJrm2YfYunTS3buu8UKbnUsh5EZABCvy4hQioWhDTdr0g2', '王五', 'https://picsum.photos/seed/user3/200/200', 'wangwu@example.com', '生活不止眼前的代码');

-- 文章 (5篇已发布博文，含Markdown内容)
INSERT INTO `article` (`user_id`, `title`, `content`, `summary`, `cover_image`, `status`, `view_count`, `like_count`, `favorite_count`, `comment_count`) VALUES
(1, 'Spring Boot 3 入门指南', '# Spring Boot 3 入门指南\n\n## 简介\n\nSpring Boot 3 是 Spring 生态系统的重大更新，带来了许多新特性和改进。\n\n## 核心特性\n\n- **JDK 17 基线**：最低要求 Java 17\n- **Jakarta EE 9+**：从 javax 迁移到 jakarta 命名空间\n- **GraalVM 原生镜像支持**：开箱即用的 AOT 编译\n\n## 快速开始\n\n```xml\n<dependency>\n    <groupId>org.springframework.boot</groupId>\n    <artifactId>spring-boot-starter-web</artifactId>\n    <version>3.2.5</version>\n</dependency>\n```\n\n## 总结\n\nSpring Boot 3 是未来 Java 后端开发的方向，建议尽早迁移和学习。', 'Spring Boot 3 重大更新，JDK17基线、Jakarta EE迁移与GraalVM支持', 'https://picsum.photos/seed/article1/800/400', 1, 256, 32, 18, 8),

(2, '深入理解 Vue 3 组合式 API', '# 深入理解 Vue 3 组合式 API\n\n## 为什么需要组合式 API？\n\nVue 2 的选项式 API 在大型组件中存在以下问题：\n\n1. 逻辑分散，同一功能的代码被拆分到不同选项\n2. 代码复用依赖 Mixins，存在命名冲突风险\n3. TypeScript 支持不够完善\n\n## setup() 函数\n\n```javascript\nexport default {\n  setup() {\n    const count = ref(0)\n    const increment = () => count.value++\n    return { count, increment }\n  }\n}\n```\n\n## 响应式核心\n\n- `ref()`：基本类型响应式\n- `reactive()`：对象类型响应式\n- `computed()`：计算属性\n- `watch()` / `watchEffect()`：侦听器\n\n## 总结\n\n组合式 API 是 Vue 3 最重要的改进，让代码组织更加灵活和可维护。', 'Vue3组合式API详解，解决选项式API的痛点，提升代码组织与复用能力', 'https://picsum.photos/seed/article2/800/400', 1, 189, 28, 12, 5),

(1, 'MySQL 索引优化实战', '# MySQL 索引优化实战\n\n## 索引基础\n\n索引是提高查询性能的关键手段，但不当使用反而会降低性能。\n\n## 常见索引类型\n\n| 类型 | 说明 |\n|------|------|\n| 主键索引 | 唯一且不为空 |\n| 唯一索引 | 允许为空的唯一约束 |\n| 普通索引 | 最基本的索引 |\n| 组合索引 | 多列组合索引 |\n| 全文索引 | 文本搜索 |\n\n## 优化建议\n\n1. **遵循最左前缀原则**：组合索引查询必须从最左列开始\n2. **避免索引失效**：\n   - 不要在索引列上使用函数\n   - 避免隐式类型转换\n   - LIKE 查询不以通配符开头\n3. **覆盖索引**：查询字段全部包含在索引中\n\n## EXPLAIN 分析\n\n```sql\nEXPLAIN SELECT * FROM article WHERE user_id = 1 AND status = 1;\n```\n\n## 总结\n\n索引优化是后端开发必备技能，需要在实践中不断积累经验。', 'MySQL索引类型、优化建议与EXPLAIN分析，后端性能优化必备', 'https://picsum.photos/seed/article3/800/400', 1, 342, 45, 25, 12),

(3, 'Docker 容器化部署最佳实践', '# Docker 容器化部署最佳实践\n\n## 为什么要容器化？\n\n- 环境一致性\n- 快速部署和扩展\n- 资源隔离\n- CI/CD 友好\n\n## Dockerfile 编写\n\n```dockerfile\nFROM openjdk:17-jdk-slim\nWORKDIR /app\nCOPY target/*.jar app.jar\nEXPOSE 8080\nENTRYPOINT ["java", "-jar", "app.jar"]\n```\n\n## Docker Compose 编排\n\n```yaml\nversion: "3.8"\nservices:\n  app:\n    build: .\n    ports:\n      - "8080:8080"\n    depends_on:\n      - mysql\n  mysql:\n    image: mysql:8.0\n    environment:\n      MYSQL_ROOT_PASSWORD: root\n      MYSQL_DATABASE: jianshu\n```\n\n## 最佳实践\n\n1. 使用多阶段构建减小镜像体积\n2. 合理利用构建缓存\n3. 不要以 root 用户运行应用\n4. 健康检查必不可少\n\n## 总结\n\nDocker 让部署变得简单而可靠，掌握容器化技术是现代开发者的必修课。', 'Docker容器化部署全流程，Dockerfile编写与Compose编排最佳实践', 'https://picsum.photos/seed/article4/800/400', 1, 178, 22, 15, 6),

(2, 'Git 工作流与团队协作', '# Git 工作流与团队协作\n\n## 常见工作流\n\n### Git Flow\n\n适合发布周期较长的项目：\n\n- `main`：生产分支\n- `develop`：开发分支\n- `feature/*`：功能分支\n- `release/*`：发布分支\n- `hotfix/*`：热修复分支\n\n### GitHub Flow\n\n适合持续部署的项目：\n\n- `main`：始终可部署\n- 功能分支 + Pull Request\n\n## 提交规范\n\n```
feat: 新功能\nfix: 修复bug\ndocs: 文档更新\nstyle: 代码格式\nrefactor: 重构\ntest: 测试\nchore: 构建/工具\n```\n\n## Code Review 要点\n\n1. 逻辑正确性\n2. 代码风格一致性\n3. 安全性考虑\n4. 性能影响\n\n## 总结\n\n良好的 Git 工作流是团队高效协作的基础，选择合适的工作流并坚持执行至关重要。', 'Git工作流对比、提交规范与Code Review要点，提升团队协作效率', 'https://picsum.photos/seed/article5/800/400', 1, 145, 19, 8, 4);

-- 点赞数据
INSERT INTO `article_like` (`user_id`, `article_id`) VALUES
(2, 1), (3, 1),
(1, 2), (3, 2),
(1, 3), (2, 3),
(1, 4), (2, 4),
(1, 5), (3, 5);

-- 收藏数据
INSERT INTO `article_favorite` (`user_id`, `article_id`) VALUES
(2, 1),
(1, 3),
(3, 3),
(1, 4),
(2, 5);

-- 评论数据
INSERT INTO `comment` (`user_id`, `article_id`, `content`) VALUES
(2, 1, '写得很详细，对我帮助很大！'),
(3, 1, 'Spring Boot 3 确实是未来的方向'),
(1, 2, '组合式API确实比选项式更灵活'),
(3, 3, '索引优化这块讲得很透彻'),
(1, 3, 'EXPLAIN那部分可以再详细一些'),
(2, 4, 'Docker部署确实方便多了'),
(3, 5, 'Git Flow vs GitHub Flow 分析得很到位'),
(1, 5, '提交规范很重要，团队应该统一');
