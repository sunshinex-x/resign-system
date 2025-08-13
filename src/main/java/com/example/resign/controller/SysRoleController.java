package com.example.resign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.SysRole;
import com.example.resign.model.common.Result;
import com.example.resign.model.dto.SysRoleDTO;
import com.example.resign.service.SysRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统角色控制器
 */
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    /**
     * 分页查询角色列表
     */
    @GetMapping("/list")
    public Result<IPage<SysRole>> getRoleList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<SysRole> page = new Page<>(current, size);
        IPage<SysRole> result = sysRoleService.pageRoles(page);
        return Result.success(result);
    }

    /**
     * 查询所有角色
     */
    @GetMapping("/all")
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> roles = sysRoleService.getAllRoles();
        return Result.success(roles);
    }

    /**
     * 根据角色ID查询角色详情
     */
    @GetMapping("/{roleId}")
    public Result<SysRole> getRoleById(@PathVariable Long roleId) {
        SysRole role = sysRoleService.getRoleById(roleId);
        return Result.success(role);
    }

    /**
     * 创建角色
     */
    @PostMapping
    public Result<SysRole> createRole(@Valid @RequestBody SysRoleDTO roleDTO) {
        SysRole role = sysRoleService.createRole(roleDTO);
        return Result.success(role);
    }

    /**
     * 更新角色
     */
    @PutMapping("/{roleId}")
    public Result<SysRole> updateRole(@PathVariable Long roleId, @Valid @RequestBody SysRoleDTO roleDTO) {
        SysRole role = sysRoleService.updateRole(roleId, roleDTO);
        return Result.success(role);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    public Result<Boolean> deleteRole(@PathVariable Long roleId) {
        boolean result = sysRoleService.deleteRole(roleId);
        return Result.success(result);
    }

    /**
     * 获取角色权限
     */
    @GetMapping("/{roleId}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        List<Long> permissionIds = sysRoleService.getRolePermissions(roleId);
        return Result.success(permissionIds);
    }

    /**
     * 设置角色权限
     */
    @PutMapping("/{roleId}/permissions")
    public Result<Boolean> setRolePermissions(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> params) {
        List<Long> permissionIds = params.get("permissionIds");
        boolean result = sysRoleService.setRolePermissions(roleId, permissionIds);
        return Result.success(result);
    }
}