package com.example.resign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.SysRole;
import com.example.resign.model.common.Result;
import com.example.resign.model.dto.SysRoleDTO;
import com.example.resign.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统角色控制器
 */
@Tag(name = "系统角色管理", description = "系统角色管理相关接口")
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    /**
     * 分页查询角色列表
     */
    @Operation(summary = "分页查询角色列表", description = "分页查询系统角色列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/list")
    public Result<IPage<SysRole>> getRoleList(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        
        Page<SysRole> page = new Page<>(current, size);
        IPage<SysRole> result = sysRoleService.pageRoles(page);
        return Result.success(result);
    }

    /**
     * 查询所有角色
     */
    @Operation(summary = "查询所有角色", description = "查询所有可用的系统角色")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/all")
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> roles = sysRoleService.getAllRoles();
        return Result.success(roles);
    }

    /**
     * 根据角色ID查询角色详情
     */
    @Operation(summary = "根据角色ID查询角色详情", description = "根据角色ID获取角色详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/{roleId}")
    public Result<SysRole> getRoleById(
            @Parameter(description = "角色ID") @PathVariable Long roleId) {
        SysRole role = sysRoleService.getRoleById(roleId);
        return Result.success(role);
    }

    /**
     * 创建角色
     */
    @Operation(summary = "创建角色", description = "创建新的系统角色")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功")
    })
    @PostMapping
    public Result<SysRole> createRole(
            @Parameter(description = "角色信息") @Valid @RequestBody SysRoleDTO roleDTO) {
        SysRole role = sysRoleService.createRole(roleDTO);
        return Result.success(role);
    }

    /**
     * 更新角色
     */
    @Operation(summary = "更新角色", description = "更新指定角色的信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功")
    })
    @PutMapping("/{roleId}")
    public Result<SysRole> updateRole(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Parameter(description = "角色信息") @Valid @RequestBody SysRoleDTO roleDTO) {
        SysRole role = sysRoleService.updateRole(roleId, roleDTO);
        return Result.success(role);
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色", description = "删除指定的系统角色")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功")
    })
    @DeleteMapping("/{roleId}")
    public Result<Boolean> deleteRole(
            @Parameter(description = "角色ID") @PathVariable Long roleId) {
        boolean result = sysRoleService.deleteRole(roleId);
        return Result.success(result);
    }

    /**
     * 获取角色权限
     */
    @Operation(summary = "获取角色权限", description = "获取指定角色的权限列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/{roleId}/permissions")
    public Result<List<Long>> getRolePermissions(
            @Parameter(description = "角色ID") @PathVariable Long roleId) {
        List<Long> permissionIds = sysRoleService.getRolePermissions(roleId);
        return Result.success(permissionIds);
    }

    /**
     * 设置角色权限
     */
    @Operation(summary = "设置角色权限", description = "为指定角色设置权限")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "设置成功")
    })
    @PutMapping("/{roleId}/permissions")
    public Result<Boolean> setRolePermissions(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Parameter(description = "权限参数") @RequestBody Map<String, List<Long>> params) {
        List<Long> permissionIds = params.get("permissionIds");
        boolean result = sysRoleService.setRolePermissions(roleId, permissionIds);
        return Result.success(result);
    }
}