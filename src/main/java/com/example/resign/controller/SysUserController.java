package com.example.resign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.model.common.Result;
import com.example.resign.model.dto.ChangePasswordDTO;
import com.example.resign.model.dto.SysUserDTO;
import com.example.resign.model.dto.UserProfileDTO;
import com.example.resign.model.vo.SysUserVO;
import com.example.resign.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 系统用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    public Result<IPage<SysUserVO>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer status) {
        
        Page<SysUserVO> page = new Page<>(current, size);
        IPage<SysUserVO> result = sysUserService.pageUsers(page, username, email, status);
        return Result.success(result);
    }

    /**
     * 根据用户ID查询用户详情
     */
    @GetMapping("/{userId}")
    public Result<SysUserVO> getUserById(@PathVariable Long userId) {
        SysUserVO user = sysUserService.getUserById(userId);
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<SysUserVO> createUser(@Valid @RequestBody SysUserDTO userDTO) {
        SysUserVO user = sysUserService.createUser(userDTO);
        return Result.success(user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    public Result<SysUserVO> updateUser(@PathVariable Long userId, @Valid @RequestBody SysUserDTO userDTO) {
        SysUserVO user = sysUserService.updateUser(userId, userDTO);
        return Result.success(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<Boolean> deleteUser(@PathVariable Long userId) {
        boolean result = sysUserService.deleteUser(userId);
        return Result.success(result);
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteUsers(@RequestBody List<Long> userIds) {
        boolean result = sysUserService.batchDeleteUsers(userIds);
        return Result.success(result);
    }

    /**
     * 切换用户状态
     */
    @PutMapping("/{userId}/status")
    public Result<Boolean> toggleUserStatus(@PathVariable Long userId, @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        boolean result = sysUserService.toggleUserStatus(userId, status);
        return Result.success(result);
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{userId}/password")
    public Result<Boolean> resetPassword(@PathVariable Long userId, @RequestBody Map<String, String> params) {
        String password = params.get("password");
        boolean result = sysUserService.resetPassword(userId, password);
        return Result.success(result);
    }
    
    /**
     * 获取当前用户信息
     */
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
    @PutMapping("/profile")
    public Result<SysUserVO> updateProfile(@Valid @RequestBody UserProfileDTO profileDTO) {
        // TODO: 从JWT token中获取当前用户ID
        Long userId = 1L;
        SysUserVO user = sysUserService.updateProfile(userId, profileDTO);
        return Result.success(user);
    }
    
    /**
     * 更新个人信息（兼容旧版本）
     */
    @PutMapping("/profile/legacy")
    public Result<SysUserVO> updateProfileLegacy(@RequestBody Map<String, String> params) {
        // TODO: 从JWT token中获取当前用户ID
        Long userId = 1L;
        SysUserVO user = sysUserService.updateProfile(userId, params);
        return Result.success(user);
    }
    
    /**
     * 修改个人密码
     */
    @PutMapping("/password")
    public Result<Boolean> changePassword(@Valid @RequestBody ChangePasswordDTO passwordDTO) {
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
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("avatar") MultipartFile avatar) {
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