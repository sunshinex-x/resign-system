package com.example.resign.controller;

import com.example.resign.model.common.Result;
import com.example.resign.model.dto.SysPermissionDTO;
import com.example.resign.model.vo.SysPermissionVO;
import com.example.resign.service.SysPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统权限控制器
 */
@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    /**
     * 查询权限树
     */
    @GetMapping("/tree")
    public Result<List<SysPermissionVO>> getPermissionTree() {
        List<SysPermissionVO> tree = sysPermissionService.getPermissionTree();
        return Result.success(tree);
    }

    /**
     * 查询权限列表（扁平结构）
     */
    @GetMapping("/list")
    public Result<List<SysPermissionVO>> getPermissionList() {
        List<SysPermissionVO> tree = sysPermissionService.getPermissionTree();
        return Result.success(tree);
    }

    /**
     * 根据权限ID查询权限详情
     */
    @GetMapping("/{permissionId}")
    public Result<SysPermissionVO> getPermissionById(@PathVariable Long permissionId) {
        SysPermissionVO permission = sysPermissionService.getPermissionById(permissionId);
        return Result.success(permission);
    }

    /**
     * 创建权限
     */
    @PostMapping
    public Result<SysPermissionVO> createPermission(@Valid @RequestBody SysPermissionDTO permissionDTO) {
        SysPermissionVO permission = sysPermissionService.createPermission(permissionDTO);
        return Result.success(permission);
    }

    /**
     * 更新权限
     */
    @PutMapping("/{permissionId}")
    public Result<SysPermissionVO> updatePermission(@PathVariable Long permissionId, @Valid @RequestBody SysPermissionDTO permissionDTO) {
        SysPermissionVO permission = sysPermissionService.updatePermission(permissionId, permissionDTO);
        return Result.success(permission);
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{permissionId}")
    public Result<Boolean> deletePermission(@PathVariable Long permissionId) {
        boolean result = sysPermissionService.deletePermission(permissionId);
        return Result.success(result);
    }
}