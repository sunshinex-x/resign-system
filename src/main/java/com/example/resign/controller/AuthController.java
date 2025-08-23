package com.example.resign.controller;

import com.example.resign.model.common.Result;
import com.example.resign.model.dto.LoginDTO;
import com.example.resign.model.vo.LoginVO;
import com.example.resign.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "用户认证", description = "用户登录、登出和身份验证相关接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户通过用户名和密码进行登录认证")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "400", description = "用户名或密码错误"),
        @ApiResponse(responseCode = "401", description = "认证失败")
    })
    @PostMapping("/login")
    public Result<LoginVO> login(
            @Parameter(description = "登录信息", required = true) @Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "用户退出登录，清除认证信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登出成功"),
        @ApiResponse(responseCode = "401", description = "未认证")
    })
    @PostMapping("/logout")
    public Result<Boolean> logout(
            @Parameter(description = "认证令牌", required = true) @RequestHeader("Authorization") String token) {
        boolean result = authService.logout(token);
        return Result.success(result);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取用户信息", description = "根据认证令牌获取当前登录用户的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未认证或令牌无效")
    })
    @GetMapping("/userinfo")
    public Result<LoginVO> getUserInfo(
            @Parameter(description = "认证令牌", required = true) @RequestHeader("Authorization") String token) {
        LoginVO userInfo = authService.getUserInfo(token);
        return Result.success(userInfo);
    }
}