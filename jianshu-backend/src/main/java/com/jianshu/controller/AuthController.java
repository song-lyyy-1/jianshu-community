package com.jianshu.controller;

import com.jianshu.dto.LoginDTO;
import com.jianshu.dto.Result;
import com.jianshu.dto.RegisterDTO;
import com.jianshu.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> data = userService.login(loginDTO);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            Map<String, Object> data = userService.register(registerDTO);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}