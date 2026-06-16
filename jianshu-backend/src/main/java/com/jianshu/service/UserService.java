package com.jianshu.service;

import com.jianshu.dto.LoginDTO;
import com.jianshu.dto.RegisterDTO;
import com.jianshu.entity.User;

import java.util.Map;

public interface UserService {

    Map<String, Object> login(LoginDTO loginDTO);

    Map<String, Object> register(RegisterDTO registerDTO);

    User getUserById(Long id);

    Map<String, Object> getUserStats(Long userId);

    User updateUserInfo(Long userId, String nickname, String avatar, String bio);
}