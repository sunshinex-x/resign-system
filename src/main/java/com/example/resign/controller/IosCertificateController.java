package com.example.resign.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.IosCertificate;
import com.example.resign.model.common.Result;
import com.example.resign.service.IosCertificateService;
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
 * iOS证书管理Controller
 */
@Tag(name = "iOS证书管理", description = "iOS证书管理相关接口")
@Slf4j
@RestController
@RequestMapping("/api/ios/certificate")
@CrossOrigin(origins = "*")
public class IosCertificateController {

    @Autowired
    private IosCertificateService certificateService;

    /**
     * 上传iOS证书
     */
    @Operation(summary = "上传iOS证书", description = "上传iOS开发或发布证书文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功")
    })
    @PostMapping("/upload")
    public Result<IosCertificate> uploadCertificate(
            @Parameter(description = "证书文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "证书名称") @RequestParam("name") String name,
            @Parameter(description = "证书密码") @RequestParam("password") String password,
            @Parameter(description = "团队ID") @RequestParam(value = "teamId", required = false) String teamId,
            @Parameter(description = "主应用Bundle ID") @RequestParam(value = "bundleId", required = false) String bundleId,
            @Parameter(description = "证书类型") @RequestParam(value = "certificateType", required = false) String certificateType,
            @Parameter(description = "证书描述") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "描述文件列表") @RequestParam(value = "profiles", required = false) List<MultipartFile> profiles) {
        
        try {
            IosCertificate certificate = certificateService.uploadCertificate(
                file, name, password, teamId, bundleId, certificateType, description, profiles);
            
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
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/{id}")
    public Result<IosCertificate> getCertificate(
            @Parameter(description = "证书ID", example = "1") @PathVariable Long id) {
        try {
            IosCertificate certificate = certificateService.getCertificateById(id);
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
     * 分页查询证书列表
     */
    @Operation(summary = "分页查询证书列表", description = "分页查询iOS证书列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/list")
    public Result<Page<IosCertificate>> getCertificateList(
            @Parameter(description = "页码", example = "1") @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "页大小", example = "10") @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "证书状态") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "团队ID") @RequestParam(value = "teamId", required = false) String teamId,
            @Parameter(description = "证书类型") @RequestParam(value = "certificateType", required = false) String certificateType) {
        
        try {
            Page<IosCertificate> pageResult = certificateService.getCertificateList(
                page, size, status, teamId, certificateType);
            return Result.success("查询证书列表成功", pageResult);
            
        } catch (Exception e) {
            log.error("查询证书列表失败", e);
            return Result.error("查询证书列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新证书信息
     */
    @Operation(summary = "更新证书信息", description = "更新iOS证书的基本信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功")
    })
    @PutMapping("/{id}")
    public Result<Void> updateCertificate(
            @Parameter(description = "证书ID", example = "1") @PathVariable Long id,
            @Parameter(description = "证书信息") @RequestBody IosCertificate certificate) {
        
        try {
            certificate.setId(id);
            boolean success = certificateService.updateCertificate(certificate);
            
            if (success) {
                return Result.success("证书更新成功", null);
            } else {
                return Result.error("证书更新失败");
            }
            
        } catch (Exception e) {
            log.error("更新证书失败", e);
            return Result.error("更新证书失败: " + e.getMessage());
        }
    }

    /**
     * 删除证书
     */
    @Operation(summary = "删除证书", description = "删除指定的iOS证书")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功")
    })
    @DeleteMapping("/{id}")
    public Result<Void> deleteCertificate(
            @Parameter(description = "证书ID", example = "1") @PathVariable Long id) {
        try {
            boolean success = certificateService.deleteCertificate(id);
            
            if (success) {
                return Result.success("证书删除成功", null);
            } else {
                return Result.error("证书删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除证书失败", e);
            return Result.error("删除证书失败: " + e.getMessage());
        }
    }

    /**
     * 启用证书
     */
    @Operation(summary = "启用证书", description = "启用指定的iOS证书")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "启用成功")
    })
    @PostMapping("/{id}/enable")
    public Result<Void> enableCertificate(
            @Parameter(description = "证书ID", example = "1") @PathVariable Long id) {
        try {
            boolean success = certificateService.enableCertificate(id);
            
            if (success) {
                return Result.success("证书启用成功", null);
            } else {
                return Result.error("证书启用失败");
            }
            
        } catch (Exception e) {
            log.error("启用证书失败", e);
            return Result.error("启用证书失败: " + e.getMessage());
        }
    }

    /**
     * 禁用证书
     */
    @Operation(summary = "禁用证书", description = "禁用指定的iOS证书")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "禁用成功")
    })
    @PostMapping("/{id}/disable")
    public Result<Void> disableCertificate(
            @Parameter(description = "证书ID", example = "1") @PathVariable Long id) {
        try {
            boolean success = certificateService.disableCertificate(id);
            
            if (success) {
                return Result.success("证书禁用成功", null);
            } else {
                return Result.error("证书禁用失败");
            }
            
        } catch (Exception e) {
            log.error("禁用证书失败", e);
            return Result.error("禁用证书失败: " + e.getMessage());
        }
    }

    /**
     * 验证证书
     */
    @Operation(summary = "验证证书", description = "验证iOS证书的有效性")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "验证完成")
    })
    @PostMapping("/{id}/validate")
    public Result<Map<String, Object>> validateCertificate(
            @Parameter(description = "证书ID", example = "1") @PathVariable Long id) {
        try {
            boolean valid = certificateService.validateCertificate(id);
            
            Map<String, Object> data = new HashMap<>();
            data.put("valid", valid);
            return Result.success(valid ? "证书验证通过" : "证书验证失败", data);
            
        } catch (Exception e) {
            log.error("验证证书失败", e);
            return Result.error("验证证书失败: " + e.getMessage());
        }
    }

    /**
     * 获取即将过期的证书列表
     */
    @Operation(summary = "获取即将过期的证书列表", description = "获取即将过期的iOS证书列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/expiring")
    public Result<List<IosCertificate>> getExpiringCertificates() {
        try {
            List<IosCertificate> certificates = certificateService.getExpiringCertificates();
            
            return Result.success(certificates);
            
        } catch (Exception e) {
            log.error("获取即将过期证书列表失败", e);
            return Result.error("获取即将过期证书列表失败: " + e.getMessage());
        }
    }
}