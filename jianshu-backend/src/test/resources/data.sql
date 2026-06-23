-- 测试初始数据
-- 用户 (密码BCrypt加密: test123, 哈希值: $2b$12$pGFOuFxzJrm2YfYunTS3buu8UKbnUsh5EZABCvy4hQioWhDTdr0g2)
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `email`, `bio`) VALUES
('zhangsan', '$2b$12$pGFOuFxzJrm2YfYunTS3buu8UKbnUsh5EZABCvy4hQioWhDTdr0g2', '张三', 'https://picsum.photos/seed/user1/200/200', 'zhangsan@example.com', '热爱写作，专注技术分享'),
('lisi', '$2b$12$pGFOuFxzJrm2YfYunTS3buu8UKbnUsh5EZABCvy4hQioWhDTdr0g2', '李四', 'https://picsum.photos/seed/user2/200/200', 'lisi@example.com', '读书破万卷，下笔如有神'),
('wangwu', '$2b$12$pGFOuFxzJrm2YfYunTS3buu8UKbnUsh5EZABCvy4hQioWhDTdr0g2', '王五', 'https://picsum.photos/seed/user3/200/200', 'wangwu@example.com', '生活不止眼前的代码');

-- 文章 (2篇已发布 + 1篇草稿)
INSERT INTO `article` (`user_id`, `title`, `content`, `summary`, `cover_image`, `status`, `view_count`, `like_count`, `favorite_count`) VALUES
(1, '《三国演义》', '作者：元末明初·罗贯中。全书以东汉末年到西晋初年近百年乱世为背景，是我国第一部长篇章回体历史演义小说。故事始于黄巾起义，终于三国归晋，核心讲述魏、蜀、吴三大割据势力争霸天下的全过程。', '全书以东汉末年到西晋初年近百年乱世为背景，是我国第一部长篇章回体历史演义小说。', 'https://picsum.photos/seed/article1/800/400', 1, 256, 32, 18),
(2, '《红楼梦》', '作者：清代·曹雪芹（后四十回多认为高鹗续写）。古典小说艺术成就最高峰，世情小说代表。以贾、史、王、薛四大家族的兴衰沉浮为大背景，主线讲述贾宝玉、林黛玉、薛宝钗三人的爱情悲剧。', '古典小说艺术成就最高峰，世情小说代表。', 'https://picsum.photos/seed/article2/800/400', 1, 189, 28, 12),
(1, '我的草稿文章', '这是一篇未发布的草稿文章，仅供作者本人查看。', '这是一篇未发布的草稿文章，仅供作者本人查看。', 'https://picsum.photos/seed/article3/800/400', 0, 0, 0, 0);

-- 点赞数据
INSERT INTO `article_like` (`user_id`, `article_id`) VALUES
(2, 1),
(3, 1),
(1, 2),
(3, 2);

-- 收藏数据
INSERT INTO `article_favorite` (`user_id`, `article_id`) VALUES
(2, 1),
(1, 2),
(3, 2);
