package com.example.resign.controller;

import com.example.resign.model.common.Result;
import com.example.resign.model.dto.SysPermissionDTO;
import com.example.resign.model.vo.SysPermissionVO;
import com.example.resign.service.SysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "系统权限管理", description = "系统权限管理相关接口")
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    /**
     * 查询权限树
     */
    @Operation(summary = "查询权限树", description = "获取系统权限的树形结构")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/tree")
    public Result<List<SysPermissionVO>> getPermissionTree() {
        List<SysPermissionVO> tree = sysPermissionService.getPermissionTree();
        return Result.success(tree);
    }

    /**
     * 查询权限列表（扁平结构）
     */
    @Operation(summary = "查询权限列表", description = "获取系统权限的扁平列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/list")
    public Result<List<SysPermissionVO>> getPermissionList() {
        List<SysPermissionVO> tree = sysPermissionService.getPermissionTree();
        return Result.success(tree);
    }

    /**
     * 根据权限ID查询权限详情
     */
    @Operation(summary = "查询权限详情", description = "根据权限ID获取权限详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "权限不存在")
    })
    @GetMapping("/{permissionId}")
    public Result<SysPermissionVO> getPermissionById(
            @Parameter(description = "权限ID", example = "1") @PathVariable Long permissionId) {
        SysPermissionVO permission = sysPermissionService.getPermissionById(permissionId);
        return Result.success(permission);
    }

    /**
     * 创建权限
     */
    @Operation(summary = "创建权限", description = "创建新的系统权限")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping
    public Result<SysPermissionVO> createPermission(
            @Parameter(description = "权限信息") @Valid @RequestBody SysPermissionDTO permissionDTO) {
        SysPermissionVO permission = sysPermissionService.createPermission(permissionDTO);
        return Result.success(permission);
    }

    /**
     * 更新权限
     */
    @Operation(summary = "更新权限", description = "更新指定权限的信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "404", description = "权限不存在")
    })
    @PutMapping("/{permissionId}")
    public Result<SysPermissionVO> updatePermission(
            @Parameter(description = "权限ID", example = "1") @PathVariable Long permissionId,
            @Parameter(description = "权限信息") @Valid @RequestBody SysPermissionDTO permissionDTO) {
        SysPermissionVO permission = sysPermissionService.updatePermission(permissionId, permissionDTO);
        return Result.success(permission);
    }

    /**
     * 删除权限
     */
    @Operation(summary = "删除权限", description = "删除指定的系统权限")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "权限不存在")
    })
    @DeleteMapping("/{permissionId}")
    public Result<Boolean> deletePermission(
            @Parameter(description = "权限ID", example = "1") @PathVariable Long permissionId) {
        boolean result = sysPermissionService.deletePermission(permissionId);
        return Result.success(result);
    }
}