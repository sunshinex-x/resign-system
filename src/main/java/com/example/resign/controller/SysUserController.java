package com.example.resign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.model.common.Result;
import com.example.resign.model.dto.ChangePasswordDTO;
import com.example.resign.model.dto.SysUserDTO;
import com.example.resign.model.dto.UserProfileDTO;
import com.example.resign.model.vo.SysUserVO;
import com.example.resign.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 系统用户控制器
 */
@Tag(name = "系统用户管理", description = "系统用户管理相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表", description = "分页获取系统用户列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/list")
    public Result<IPage<SysUserVO>> getUserList(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名", example = "admin") @RequestParam(required = false) String username,
            @Parameter(description = "邮箱", example = "admin@example.com") @RequestParam(required = false) String email,
            @Parameter(description = "状态", example = "1") @RequestParam(required = false) Integer status) {
        
        Page<SysUserVO> page = new Page<>(current, size);
        IPage<SysUserVO> result = sysUserService.pageUsers(page, username, email, status);
        return Result.success(result);
    }

    /**
     * 根据用户ID查询用户详情
     */
    @Operation(summary = "查询用户详情", description = "根据用户ID获取用户详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/{userId}")
    public Result<SysUserVO> getUserById(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        SysUserVO user = sysUserService.getUserById(userId);
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户", description = "创建新的系统用户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功")
    })
    @PostMapping
    public Result<SysUserVO> createUser(
            @Parameter(description = "用户信息") @Valid @RequestBody SysUserDTO userDTO) {
        SysUserVO user = sysUserService.createUser(userDTO);
        return Result.success(user);
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户", description = "更新指定用户的信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功")
    })
    @PutMapping("/{userId}")
    public Result<SysUserVO> updateUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "用户信息") @Valid @RequestBody SysUserDTO userDTO) {
        SysUserVO user = sysUserService.updateUser(userId, userDTO);
        return Result.success(user);
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功")
    })
    @DeleteMapping("/{userId}")
    public Result<Boolean> deleteUser(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        boolean result = sysUserService.deleteUser(userId);
        return Result.success(result);
    }

    /**
     * 批量删除用户
     */
    @Operation(summary = "批量删除用户", description = "根据用户ID列表批量删除用户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功")
    })
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteUsers(
            @Parameter(description = "用户ID列表") @RequestBody List<Long> userIds) {
        boolean result = sysUserService.batchDeleteUsers(userIds);
        return Result.success(result);
    }

    /**
     * 切换用户状态
     */
    @Operation(summary = "切换用户状态", description = "启用或禁用指定用户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "状态切换成功")
    })
    @PutMapping("/{userId}/status")
    public Result<Boolean> toggleUserStatus(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "状态参数") @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        boolean result = sysUserService.toggleUserStatus(userId, status);
        return Result.success(result);
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置用户密码", description = "管理员重置指定用户的密码")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "重置成功")
    })
    @PutMapping("/{userId}/password")
    public Result<Boolean> resetPassword(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "密码参数") @RequestBody Map<String, String> params) {
        String password = params.get("password");
        boolean result = sysUserService.resetPassword(userId, password);
        return Result.success(result);
    }
    
    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的个人信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/profile")
    public Result<SysUserVO> getCurrentUserProfile() {
        // TODO: 从JWT token中获取当前用户ID
        Long userId = 1L;
        SysUserVO user = sysUserService.getCurrentUserProfile(userId);
        return Result.success(user);
    }
    
    /**
     * 更新个人信息
     */
    @Operation(summary = "更新个人信息", description = "更新当前用户的个人信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功")
    })
    @PutMapping("/profile")
    public Result<SysUserVO> updateProfile(
            @Parameter(description = "个人信息") @Valid @RequestBody UserProfileDTO profileDTO) {
        // TODO: 从JWT token中获取当前用户ID
        Long userId = 1L;
        SysUserVO user = sysUserService.updateProfile(userId, profileDTO);
        return Result.success(user);
    }
    
    /**
     * 更新个人信息（兼容旧版本）
     */
    @Operation(summary = "更新个人信息（兼容版本）", description = "更新当前用户的个人信息（兼容旧版本接口）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功")
    })
    @PutMapping("/profile/legacy")
    public Result<SysUserVO> updateProfileLegacy(
            @Parameter(description = "个人信息参数") @RequestBody Map<String, String> params) {
        // TODO: 从JWT token中获取当前用户ID
        Long userId = 1L;
        SysUserVO user = sysUserService.updateProfile(userId, params);
        return Result.success(user);
    }
    
    /**
     * 修改个人密码
     */
    @Operation(summary = "修改个人密码", description = "用户修改自己的登录密码")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "修改成功")
    })
    @PutMapping("/password")
    public Result<Boolean> changePassword(
            @Parameter(description = "密码修改信息") @Valid @RequestBody ChangePasswordDTO passwordDTO) {
        // TODO: 从JWT token中获取当前用户ID
        Long userId = 1L;
        
        // 检查新旧密码是否相同
        if (passwordDTO.getOldPassword().equals(passwordDTO.getNewPassword())) {
            return Result.error("新密码不能与原密码相同");
        }
        
        boolean result = sysUserService.changePassword(userId, passwordDTO);
        return Result.success(result);
    }
    
    /**
     * 上传头像
     */
    @Operation(summary = "上传头像", description = "上传用户头像文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功")
    })
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @Parameter(description = "头像文件") @RequestParam("avatar") MultipartFile avatar) {
        // TODO: 从JWT token中获取当前用户ID
        Long userId = 1L;
        
        // 参数验证
        if (avatar == null || avatar.isEmpty()) {
            return Result.error("请选择要上传的头像文件");
        }
        
        String avatarUrl = sysUserService.uploadAvatar(userId, avatar);
        Map<String, String> result = Map.of("avatarUrl", avatarUrl);
        return Result.success(result);
    }
}