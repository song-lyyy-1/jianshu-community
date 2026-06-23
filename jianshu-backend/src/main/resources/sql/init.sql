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

-- ===================== 测试数据 =====================

-- 用户 (密码BCrypt加密: test123)
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `email`, `bio`) VALUES
('zhangsan', '$2b$12$pGFOuFxzJrm2YfYunTS3buu8UKbnUsh5EZABCvy4hQioWhDTdr0g2', '张三', 'https://picsum.photos/seed/user1/200/200', 'zhangsan@example.com', '热爱写作，专注技术分享'),
('lisi', '$2b$12$pGFOuFxzJrm2YfYunTS3buu8UKbnUsh5EZABCvy4hQioWhDTdr0g2', '李四', 'https://picsum.photos/seed/user2/200/200', 'lisi@example.com', '读书破万卷，下笔如有神'),
('wangwu', '$2b$12$pGFOuFxzJrm2YfYunTS3buu8UKbnUsh5EZABCvy4hQioWhDTdr0g2', '王五', 'https://picsum.photos/seed/user3/200/200', 'wangwu@example.com', '生活不止眼前的代码');

-- 文章 (5篇已发布博文，含Markdown内容)
INSERT INTO `article` (`user_id`, `title`, `content`, `summary`, `cover_image`, `status`, `view_count`, `like_count`, `favorite_count`) VALUES
(1, '《三国演义》', '作者：元末明初・罗贯中。
全书以东汉末年到西晋初年近百年乱世为背景，是我国第一部长篇章回体历史演义小说。故事始于黄巾起义，终于三国归晋，核心讲述魏、蜀、吴三大割据势力争霸天下的全过程。书中塑造了刘备、曹操、诸葛亮、关羽、张飞、周瑜等经典人物，既描绘了赤壁之战、官渡之战、空城计等经典战争谋略，也刻画忠义、权谋、英雄壮志。作品将历史史实与文学虚构结合，宣扬忠义仁厚的价值观，展现乱世兴衰、分久必合的历史规律，是古典历史小说的巅峰之作。', '全书以东汉末年到西晋初年近百年乱世为背景，是我国第一部长篇章回体历史演义小说。', 'https://picsum.photos/seed/article1/800/400', 1, 256, 32, 18),

(2, '《红楼梦》', '作者：清代・曹雪芹（后四十回多认为高鹗续写）。
古典小说艺术成就最高峰，世情小说代表。以贾、史、王、薛四大家族的兴衰沉浮为大背景，主线讲述贾宝玉、林黛玉、薛宝钗三人的爱情悲剧。大观园中数百位女子命运各异，作者细致描绘贵族家庭日常起居、诗词宴游、人情世故，看似写儿女情长、家族琐事，实则暗藏封建贵族由盛转衰的必然命运，揭露封建礼教对人性的束缚与摧残。书中诗词、建筑、饮食、礼仪包罗万象，兼具文学美感与社会史料价值，暗含繁华落尽、万事皆空的人生慨叹，内涵深邃，后世衍生专门研究学问 "红学"。', '古典小说艺术成就最高峰，世情小说代表。', 'https://picsum.photos/seed/article2/800/400', 1, 189, 28, 12),

(1, '《西游记》', '作者：明代・吴承恩。
经典神魔浪漫主义长篇小说。讲述唐僧奉唐王之命，前往西天求取真经，途中收下孙悟空、猪八戒、沙和尚三位徒弟，师徒四人一路跋山涉水，历经九九八十一难，对抗各路妖魔鬼怪，最终抵达灵山取得真经、修成正果。主角孙悟空神通广大、敢于反抗天庭权威，大闹天宫的故事家喻户晓；全书融合神话、佛道文化，借神魔故事影射人间官场与社会百态，既有奇幻瑰丽的想象，又蕴含惩恶扬善、坚守初心、历经磨难方能修成大道的人生哲理，老少皆宜，流传极广。', '经典神魔浪漫主义长篇小说。', 'https://picsum.photos/seed/article3/800/400', 1, 342, 45, 25),

(3, '《水浒传》', '作者：元末明初・施耐庵（一说施耐庵、罗贯中合著）。
中国第一部白话长篇章回体英雄传奇小说。北宋末年，朝廷腐败、官逼民反，一百零八位来自各行各业的好汉，因遭受权贵欺压，陆续投奔梁山泊聚义。前期众人劫富济贫、替天行道，反抗贪官恶霸；后宋江接受朝廷招安，带领梁山好汉征讨辽国、平定叛乱，最终大半好汉战死、离散，落得悲凉结局。小说生动刻画武松、林冲、鲁智深、李逵、宋江等鲜活人物，歌颂底层百姓反抗压迫的侠义精神，也揭露封建王朝的腐朽黑暗，深刻反映古代社会底层民众的生存困境。', 'Docker容器化部署全流程，Dockerfile编写与Compose编排最佳实践', 'https://picsum.photos/seed/article4/800/400', 1, 178, 22, 15),

(2, '《诗经》', '# 《诗经》是我国第一部诗歌总集，收录西周初年至春秋中叶五百余年的诗歌共 305 篇，另有 6 篇有标题无内容的笙诗，先秦名为《诗》《诗三百》，西汉被尊为儒家经典，定名《诗经》。全书分为风、雅、颂三部分，国风共 160 篇，是各地民间歌谣，贴近百姓生活，描写爱情婚恋、农耕劳作、徭役苦难，文学价值最高；雅分为大雅、小雅，共 105 篇，是宫廷乐曲，多用于朝堂宴饮，记叙历史、评议时政；颂共 40 篇，是宗庙祭祀乐歌，风格庄重古朴，用来祭祀神明、歌颂先祖功绩。赋、比、兴是《诗经》三大表现手法，与风雅颂合称 "六义"，赋是直白叙事抒情，比是打比方托物喻情，兴是借景物起笔引出情感。全书以四言为主，多用重章叠句，语言简洁质朴、韵律和谐优美。《诗经》立足现实，真实还原周代民间风俗、社会百态与王朝风貌，开创了中国古典诗歌现实主义创作传统，积淀了先秦民族文化，不仅是古代诗歌的源头之作，更是儒家必读经典，千百年来深刻影响后世诗词创作，拥有极高的文学价值与历史研究价值。', '《诗经》是我国第一部诗歌总集，收录西周初年至春秋中叶五百余年的诗歌共 305 篇，另有 6 篇有标题无内容的笙诗，先秦名为《诗》《诗三百》，西汉被尊为儒家经典，定名《诗经》。', 'https://picsum.photos/seed/article5/800/400', 1, 145, 19, 8);

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
