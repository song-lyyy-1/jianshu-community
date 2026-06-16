package com.jianshu.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jianshu.dto.LoginDTO;
import com.jianshu.dto.RegisterDTO;
import com.jianshu.entity.Article;
import com.jianshu.entity.ArticleFavorite;
import com.jianshu.entity.ArticleLike;
import com.jianshu.entity.User;
import com.jianshu.mapper.ArticleFavoriteMapper;
import com.jianshu.mapper.ArticleLikeMapper;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.mapper.UserMapper;
import com.jianshu.service.UserService;
import com.jianshu.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleFavoriteMapper articleFavoriteMapper;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, ArticleMapper articleMapper,
                           ArticleLikeMapper articleLikeMapper, ArticleFavoriteMapper articleFavoriteMapper,
                           JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
        this.articleLikeMapper = articleLikeMapper;
        this.articleFavoriteMapper = articleFavoriteMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 由于User实体password字段有@TableField(select=false)，需要单独查询密码
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, loginDTO.getUsername())
        );
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 单独查询密码字段进行BCrypt比对
        User userWithPassword = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, user.getId())
                        .select(User::getId, User::getPassword)
        );

        if (!BCrypt.checkpw(loginDTO.getPassword(), userWithPassword.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", buildUserMap(user));
        return result;
    }

    @Override
    public Map<String, Object> register(RegisterDTO registerDTO) {
        // 检查用户名唯一
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // BCrypt加密密码
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setAvatar("");
        user.setEmail("");
        user.setBio("");
        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", buildUserMap(user));
        return result;
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 文章数
        Long articleCount = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getUserId, userId).eq(Article::getStatus, 1)
        );
        stats.put("articleCount", articleCount);

        // 总浏览数
        LambdaQueryWrapper<Article> viewQuery = new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId).eq(Article::getStatus, 1);
        Long totalViews = articleMapper.selectList(viewQuery).stream()
                .mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0).sum();
        stats.put("totalViews", totalViews);

        // 获赞数（别人对我的文章点赞）
        Long totalLikes = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getUserId, userId).eq(Article::getStatus, 1)
        ).stream().mapToLong(a -> a.getLikeCount() != null ? a.getLikeCount() : 0).sum();
        stats.put("totalLikes", totalLikes);

        // 收藏数（别人对我的文章收藏）
        Long totalFavorites = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getUserId, userId).eq(Article::getStatus, 1)
        ).stream().mapToLong(a -> a.getFavoriteCount() != null ? a.getFavoriteCount() : 0).sum();
        stats.put("totalFavorites", totalFavorites);

        return stats;
    }

    @Override
    public User updateUserInfo(Long userId, String nickname, String avatar, String bio) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);
        if (nickname != null) {
            updateWrapper.set(User::getNickname, nickname);
        }
        if (avatar != null) {
            updateWrapper.set(User::getAvatar, avatar);
        }
        if (bio != null) {
            updateWrapper.set(User::getBio, bio);
        }
        userMapper.update(null, updateWrapper);
        return userMapper.selectById(userId);
    }

    private Map<String, Object> buildUserMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("avatar", user.getAvatar());
        map.put("email", user.getEmail());
        map.put("bio", user.getBio());
        map.put("createdAt", user.getCreatedAt());
        return map;
    }
}