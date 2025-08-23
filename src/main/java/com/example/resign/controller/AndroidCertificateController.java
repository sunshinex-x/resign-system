package com.example.resign.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.AndroidCertificate;
import com.example.resign.model.common.Result;
import com.example.resign.service.AndroidCertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Android证书管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/android/certificate")
@Tag(name = "Android证书管理", description = "Android证书的上传、查询、更新、删除等操作")
public class AndroidCertificateController {

    @Autowired
    private AndroidCertificateService certificateService;

    /**
     * 上传Android证书
     */
    @Operation(summary = "上传Android证书", description = "上传Android证书文件并保存证书信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "证书上传成功"),
            @ApiResponse(responseCode = "400", description = "证书上传失败")
    })
    @PostMapping("/upload")
    public Result<AndroidCertificate> uploadCertificate(
            @Parameter(description = "证书文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "证书名称", required = true) @RequestParam("name") String name,
            @Parameter(description = "证书密码", required = true) @RequestParam("password") String password,
            @Parameter(description = "密钥别名", required = true) @RequestParam("keyAlias") String keyAlias,
            @Parameter(description = "密钥密码", required = true) @RequestParam("keyPassword") String keyPassword,
            @Parameter(description = "证书描述") @RequestParam(value = "description", required = false) String description) {
        
        try {
            AndroidCertificate certificate = certificateService.uploadCertificate(
                file, name, password, keyAlias, keyPassword, description);
            
            return Result.success("证书上传成功", certificate);
            
        } catch (Exception e) {
            log.error("证书上传失败", e);
            return Result.error("证书上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取证书详情
     */
    @Operation(summary = "获取证书详情", description = "根据证书ID获取证书详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "获取失败")
    })
    @GetMapping("/{id}")
    public Result<AndroidCertificate> getCertificateDetail(
            @Parameter(description = "证书ID", required = true) @PathVariable Long id) {
        
        try {
            AndroidCertificate certificate = certificateService.getCertificateById(id);
            if (certificate != null) {
                return Result.success(certificate);
            } else {
                return Result.error("证书不存在");
            }
            
        } catch (Exception e) {
            log.error("获取证书详情失败", e);
            return Result.error("获取证书详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取证书列表
     */
    @Operation(summary = "获取证书列表", description = "分页查询证书列表，支持按状态和密钥别名筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "获取失败")
    })
    @GetMapping("/list")
    public Result<Map<String, Object>> getCertificateList(
            @Parameter(description = "页码", example = "1") @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "证书状态") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "密钥别名") @RequestParam(value = "keyAlias", required = false) String keyAlias) {
        
        try {
            Page<AndroidCertificate> pageResult = certificateService.getCertificateList(page, size, status, keyAlias);
            
            Map<String, Object> data = new HashMap<>();
            data.put("data", pageResult.getRecords());
            data.put("total", pageResult.getTotal());
            data.put("page", pageResult.getCurrent());
            data.put("size", pageResult.getSize());
            return Result.success(data);
            
        } catch (Exception e) {
            log.error("查询证书列表失败", e);
            return Result.error("查询证书列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用证书列表
     */
    @Operation(summary = "获取可用证书列表", description = "获取所有状态为可用的证书列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "获取失败")
    })
    @GetMapping("/active")
    public Result<List<AndroidCertificate>> getActiveCertificates() {
        try {
            List<AndroidCertificate> certificates = certificateService.getActiveCertificates();
            
            return Result.success(certificates);
            
        } catch (Exception e) {
            log.error("获取有效证书列表失败", e);
            return Result.error("获取有效证书列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新证书信息
     */
    @Operation(summary = "更新证书信息", description = "更新指定证书的基本信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "更新失败")
    })
    @PutMapping("/{id}")
    public Result<Void> updateCertificate(
            @Parameter(description = "证书ID", required = true) @PathVariable Long id,
            @Parameter(description = "证书信息", required = true) @RequestBody AndroidCertificate certificate) {
        
        try {
            certificate.setId(id);
            boolean success = certificateService.updateCertificate(certificate);
            
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新失败");
            }
            
        } catch (Exception e) {
            log.error("更新证书失败", e);
            return Result.error("更新证书失败: " + e.getMessage());
        }
    }

    /**
     * 删除证书
     */
    @Operation(summary = "删除证书", description = "删除指定的证书")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "删除失败")
    })
    @DeleteMapping("/{id}")
    public Result<Void> deleteCertificate(
            @Parameter(description = "证书ID", required = true) @PathVariable Long id) {
        
        try {
            boolean success = certificateService.deleteCertificate(id);
            
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除证书失败", e);
            return Result.error("删除证书失败: " + e.getMessage());
        }
    }

    /**
     * 启用证书
     */
    @Operation(summary = "启用证书", description = "启用指定的证书")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "启用成功"),
            @ApiResponse(responseCode = "400", description = "启用失败")
    })
    @PostMapping("/{id}/enable")
    public Result<Void> enableCertificate(
            @Parameter(description = "证书ID", required = true) @PathVariable Long id) {
        
        try {
            boolean success = certificateService.enableCertificate(id);
            
            if (success) {
                return Result.success();
            } else {
                return Result.error("启用失败");
            }
            
        } catch (Exception e) {
            log.error("启用证书失败", e);
            return Result.error("启用证书失败: " + e.getMessage());
        }
    }

    /**
     * 禁用证书
     */
    @Operation(summary = "禁用证书", description = "禁用指定的证书")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "禁用成功"),
            @ApiResponse(responseCode = "400", description = "禁用失败")
    })
    @PostMapping("/{id}/disable")
    public Result<Void> disableCertificate(
            @Parameter(description = "证书ID", required = true) @PathVariable Long id) {
        
        try {
            boolean success = certificateService.disableCertificate(id);
            
            if (success) {
                return Result.success();
            } else {
                return Result.error("禁用失败");
            }
            
        } catch (Exception e) {
            log.error("禁用证书失败", e);
            return Result.error("禁用证书失败: " + e.getMessage());
        }
    }

    /**
     * 验证证书
     */
    @Operation(summary = "验证证书", description = "验证指定证书的有效性")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "验证成功"),
            @ApiResponse(responseCode = "400", description = "验证失败")
    })
    @PostMapping("/{id}/validate")
    public Result<Map<String, Object>> validateCertificate(
            @Parameter(description = "证书ID", required = true) @PathVariable Long id) {
        
        try {
            boolean valid = certificateService.validateCertificate(id);
            
            Map<String, Object> data = new HashMap<>();
            data.put("valid", valid);
            data.put("message", valid ? "证书验证通过" : "证书验证失败");
            return Result.success(data);
            
        } catch (Exception e) {
            log.error("验证证书失败", e);
            return Result.error("验证证书失败: " + e.getMessage());
        }
    }

    /**
     * 获取即将过期的证书
     */
    @Operation(summary = "获取即将过期的证书", description = "获取即将过期的证书列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "400", description = "获取失败")
    })
    @GetMapping("/expiring")
    public Result<List<AndroidCertificate>> getExpiringCertificates() {
        try {
            List<AndroidCertificate> certificates = certificateService.getExpiringCertificates();
            
            return Result.success(certificates);
            
        } catch (Exception e) {
            log.error("获取即将过期证书失败", e);
            return Result.error("获取即将过期证书失败: " + e.getMessage());
        }
    }

    /**
     * 验证证书密码
     */
    @Operation(summary = "验证证书密码", description = "验证证书的密码和密钥密码是否正确")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "验证成功"),
            @ApiResponse(responseCode = "400", description = "验证失败")
    })
    @PostMapping("/{id}/validate-password")
    public Result<Map<String, Object>> validateCertificatePassword(
            @Parameter(description = "证书ID", required = true) @PathVariable Long id,
            @Parameter(description = "证书密码", required = true) @RequestParam("password") String password,
            @Parameter(description = "密钥密码", required = true) @RequestParam("keyPassword") String keyPassword) {
        
        try {
            boolean valid = certificateService.validateCertificatePassword(id, password, keyPassword);
            
            Map<String, Object> data = new HashMap<>();
            data.put("valid", valid);
            data.put("message", valid ? "密码验证通过" : "密码验证失败");
            return Result.success(data);
            
        } catch (Exception e) {
            log.error("验证证书密码失败", e);
            return Result.error("验证证书密码失败: " + e.getMessage());
        }
    }
}