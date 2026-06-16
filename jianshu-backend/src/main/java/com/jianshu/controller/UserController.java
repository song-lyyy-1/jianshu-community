package com.jianshu.controller;

import com.jianshu.dto.Result;
import com.jianshu.entity.User;
import com.jianshu.service.UserService;
import com.jianshu.util.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public Result<Map<String, Object>> profile() {
        Long userId = UserContext.getUserId();
        User user = userService.getUserById(userId);
        Map<String, Object> stats = userService.getUserStats(userId);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("nickname", user.getNickname());
        userMap.put("avatar", user.getAvatar());
        userMap.put("email", user.getEmail());
        userMap.put("bio", user.getBio());
        userMap.put("createdAt", user.getCreatedAt());

        Map<String, Object> data = new HashMap<>();
        data.put("user", userMap);
        data.put("stats", stats);
        return Result.success(data);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Long userId = UserContext.getUserId();
        Map<String, Object> data = userService.getUserStats(userId);
        return Result.success(data);
    }

    @PutMapping("/update")
    public Result<User> update(@RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        String nickname = params.get("nickname");
        String avatar = params.get("avatar");
        String bio = params.get("bio");
        User updatedUser = userService.updateUserInfo(userId, nickname, avatar, bio);
        return Result.success(updatedUser);
    }
}