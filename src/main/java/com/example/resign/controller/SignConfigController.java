package com.example.resign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.resign.model.common.Result;
import com.example.resign.entity.SignConfig;
import com.example.resign.service.SignConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 签名配置控制器
 */
@Slf4j
@Tag(name = "签名配置管理", description = "签名配置管理相关接口")
@RestController
@RequestMapping("/api/sign-config")
public class SignConfigController {

    @Autowired
    private SignConfigService signConfigService;

    /**
     * 分页查询签名配置
     */
    @Operation(summary = "分页查询签名配置", description = "分页查询签名配置列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/page")
    public Result<IPage<SignConfig>> getSignConfigPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "应用名称") @RequestParam(required = false) String appName,
            @Parameter(description = "包名") @RequestParam(required = false) String packageName,
            @Parameter(description = "应用类型") @RequestParam(required = false) String appType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        try {
            IPage<SignConfig> page = signConfigService.getSignConfigPage(current, size, appName, packageName, appType, status);
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页查询签名配置失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取签名配置
     */
    @Operation(summary = "根据ID获取签名配置", description = "根据ID获取签名配置详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/{id}")
    public Result<SignConfig> getSignConfigById(@Parameter(description = "配置ID") @PathVariable Long id) {
        try {
            SignConfig signConfig = signConfigService.getSignConfigById(id);
            if (signConfig == null) {
                return Result.error("签名配置不存在");
            }
            return Result.success(signConfig);
        } catch (Exception e) {
            log.error("获取签名配置失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建签名配置
     */
    @Operation(summary = "创建签名配置", description = "创建新的签名配置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功")
    })
    @PostMapping
    public Result<String> createSignConfig(@Valid @RequestBody SignConfig signConfig) {
        try {
            boolean result = signConfigService.createSignConfig(signConfig);
            if (result) {
                return Result.success("创建成功");
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建签名配置失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新签名配置
     */
    @Operation(summary = "更新签名配置", description = "更新指定ID的签名配置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功")
    })
    @PutMapping("/{id}")
    public Result<String> updateSignConfig(
            @Parameter(description = "配置ID") @PathVariable Long id,
            @Valid @RequestBody SignConfig signConfig) {
        try {
            signConfig.setId(id);
            boolean result = signConfigService.updateSignConfig(signConfig);
            if (result) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新签名配置失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除签名配置
     */
    @Operation(summary = "删除签名配置", description = "删除指定ID的签名配置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功")
    })
    @DeleteMapping("/{id}")
    public Result<String> deleteSignConfig(@Parameter(description = "配置ID") @PathVariable Long id) {
        try {
            boolean result = signConfigService.deleteSignConfig(id);
            if (result) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除签名配置失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据包名获取签名配置
     */
    @Operation(summary = "根据包名获取签名配置", description = "根据应用包名自动获取对应的签名配置")
    @GetMapping("/by-package")
    public Result<SignConfig> getSignConfigByPackage(
            @Parameter(description = "应用包名", required = true) @RequestParam String packageName,
            @Parameter(description = "应用类型", required = true) @RequestParam String appType) {
        try {
            SignConfig config = signConfigService.getSignConfigByPackageNameAndType(packageName, appType);
            if (config != null) {
                return Result.success("获取成功", config);
            } else {
                return Result.error("未找到匹配的签名配置");
            }
        } catch (Exception e) {
            log.error("根据包名获取签名配置失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据包名自动创建或获取签名配置
     */
    @Operation(summary = "自动获取或创建签名配置", description = "根据包名自动获取签名配置，如果不存在则创建新的配置")
    @PostMapping("/auto-config")
    public Result<SignConfig> autoGetOrCreateSignConfig(
            @Parameter(description = "应用包名", required = true) @RequestParam String packageName,
            @Parameter(description = "应用类型", required = true) @RequestParam String appType) {
        try {
            SignConfig config = signConfigService.autoGetOrCreateSignConfig(packageName, appType);
            return Result.success("操作成功", config);
        } catch (Exception e) {
            log.error("自动获取或创建签名配置失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除签名配置
     */
    @Operation(summary = "批量删除签名配置", description = "批量删除多个签名配置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "批量删除成功")
    })
    @DeleteMapping("/batch")
    public Result<String> batchDeleteSignConfig(@RequestBody List<Long> ids) {
        try {
            boolean result = signConfigService.batchDeleteSignConfig(ids);
            if (result) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除签名配置失败", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用签名配置
     */
    @Operation(summary = "启用/禁用签名配置", description = "切换签名配置的启用状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功")
    })
    @PutMapping("/{id}/status")
    public Result<String> toggleSignConfigStatus(
            @Parameter(description = "配置ID") @PathVariable Long id,
            @Parameter(description = "状态：1-启用，0-禁用") @RequestParam Integer status) {
        try {
            boolean result = signConfigService.toggleSignConfigStatus(id, status);
            if (result) {
                String action = status == 1 ? "启用" : "禁用";
                return Result.success(action + "成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("切换签名配置状态失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 根据应用类型获取所有启用的签名配置
     */
    @Operation(summary = "根据应用类型获取所有启用的签名配置", description = "获取指定应用类型的所有启用配置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/enabled")
    public Result<List<SignConfig>> getEnabledSignConfigsByType(
            @Parameter(description = "应用类型") @RequestParam String appType) {
        try {
            List<SignConfig> signConfigs = signConfigService.getEnabledSignConfigsByType(appType);
            return Result.success(signConfigs);
        } catch (Exception e) {
            log.error("获取启用的签名配置失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}