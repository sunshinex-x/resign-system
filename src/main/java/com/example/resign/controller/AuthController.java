package com.example.resign.controller;

import com.example.resign.model.common.Result;
import com.example.resign.model.dto.LoginDTO;
import com.example.resign.model.vo.LoginVO;
import com.example.resign.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Boolean> logout(@RequestHeader("Authorization") String token) {
        boolean result = authService.logout(token);
        return Result.success(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    public Result<LoginVO> getUserInfo(@RequestHeader("Authorization") String token) {
        LoginVO userInfo = authService.getUserInfo(token);
        return Result.success(userInfo);
    }
}