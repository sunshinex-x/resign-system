package com.example.resign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.model.common.Result;
import com.example.resign.model.dto.SysUserDTO;
import com.example.resign.model.vo.SysUserVO;
import com.example.resign.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}