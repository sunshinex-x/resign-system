package com.example.resign.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.IosProfile;
import com.example.resign.model.common.Result;
import com.example.resign.service.IosProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * iOS Profile管理Controller
 */
@Tag(name = "iOS Profile管理", description = "iOS Profile管理相关接口")
@Slf4j
@RestController
@RequestMapping("/api/ios/profile")
@CrossOrigin(origins = "*")
public class IosProfileController {

    @Autowired
    private IosProfileService profileService;

    /**
     * 上传iOS Profile
     */
    @Operation(summary = "上传iOS Profile", description = "上传iOS开发或发布Profile文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功")
    })
    @PostMapping("/upload")
    public Result<IosProfile> uploadProfile(
            @Parameter(description = "Profile文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Profile名称") @RequestParam("name") String name,
            @Parameter(description = "关联证书ID") @RequestParam("certificateId") Long certificateId,
            @Parameter(description = "Bundle ID") @RequestParam(value = "bundleId", required = false) String bundleId,
            @Parameter(description = "团队ID") @RequestParam(value = "teamId", required = false) String teamId,
            @Parameter(description = "Profile类型") @RequestParam(value = "profileType", required = false) String profileType,
            @Parameter(description = "Profile描述") @RequestParam(value = "description", required = false) String description) {
        
        try {
            IosProfile profile = profileService.uploadProfile(
                file, name, certificateId, bundleId, teamId, profileType, description);
            
            return Result.success("Profile上传成功", profile);
            
        } catch (Exception e) {
            log.error("Profile上传失败", e);
            return Result.error("Profile上传失败: " + e.getMessage());
        }
    }

    /**
     * 解析Profile文件信息
     */
    @Operation(summary = "解析Profile文件信息", description = "解析上传的Profile文件获取Bundle ID和Team ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "解析成功")
    })
    @PostMapping("/parse")
    public Result<Map<String, String>> parseProfile(
            @Parameter(description = "Profile文件") @RequestParam("file") MultipartFile file) {
        try {
            String bundleId = profileService.parseBundleIdFromProfile(file);
            String teamId = profileService.parseTeamIdFromProfile(file);
            
            Map<String, String> profileInfo = new HashMap<>();
            profileInfo.put("bundleId", bundleId);
            profileInfo.put("teamId", teamId);
            
            return Result.success("解析Profile文件成功", profileInfo);
            
        } catch (Exception e) {
            log.error("解析Profile文件失败", e);
            return Result.error("解析Profile文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取Profile详情
     */
    @Operation(summary = "根据ID获取Profile", description = "根据Profile ID获取详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/{id}")
    public Result<IosProfile> getProfile(
            @Parameter(description = "Profile ID") @PathVariable Long id) {
        try {
            IosProfile profile = profileService.getProfileById(id);
            if (profile != null) {
                return Result.success("获取Profile详情成功", profile);
            } else {
                return Result.error("Profile不存在");
            }
        } catch (Exception e) {
            log.error("获取Profile详情失败", e);
            return Result.error("获取Profile详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询Profile列表
     */
    @Operation(summary = "分页获取Profile列表", description = "分页查询Profile列表，支持多条件筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/list")
    public Result<Page<IosProfile>> getProfileList(
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "状态") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "证书ID") @RequestParam(value = "certificateId", required = false) Long certificateId,
            @Parameter(description = "Bundle ID") @RequestParam(value = "bundleId", required = false) String bundleId,
            @Parameter(description = "团队ID") @RequestParam(value = "teamId", required = false) String teamId,
            @Parameter(description = "Profile类型") @RequestParam(value = "profileType", required = false) String profileType) {
        
        try {
            Page<IosProfile> pageResult = profileService.getProfileList(
                page, size, status, certificateId, bundleId, teamId, profileType);
            
            return Result.success("查询Profile列表成功", pageResult);
            
        } catch (Exception e) {
            log.error("查询Profile列表失败", e);
            return Result.error("查询Profile列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据证书ID查询Profile列表
     */
    @Operation(summary = "根据证书ID获取Profile列表", description = "获取指定证书关联的所有Profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/certificate/{certificateId}")
    public Result<List<IosProfile>> getProfilesByCertificateId(
            @Parameter(description = "证书ID") @PathVariable Long certificateId) {
        try {
            List<IosProfile> profiles = profileService.getProfilesByCertificateId(certificateId);
            
            return Result.success("查询Profile列表成功", profiles);
            
        } catch (Exception e) {
            log.error("根据证书ID查询Profile列表失败", e);
            return Result.error("查询Profile列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据Bundle ID查询Profile列表
     */
    @Operation(summary = "根据Bundle ID获取Profile列表", description = "获取指定Bundle ID的所有Profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/bundle/{bundleId}")
    public Result<List<IosProfile>> getProfilesByBundleId(
            @Parameter(description = "Bundle ID") @PathVariable String bundleId) {
        try {
            List<IosProfile> profiles = profileService.getProfilesByBundleId(bundleId);
            
            return Result.success("查询Profile列表成功", profiles);
            
        } catch (Exception e) {
            log.error("根据Bundle ID查询Profile列表失败", e);
            return Result.error("查询Profile列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新Profile信息
     */
    @Operation(summary = "更新Profile信息", description = "更新指定Profile的信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功")
    })
    @PutMapping("/{id}")
    public Result<Void> updateProfile(
            @Parameter(description = "Profile ID") @PathVariable Long id,
            @Parameter(description = "Profile信息") @RequestBody IosProfile profile) {
        
        try {
            profile.setId(id);
            boolean success = profileService.updateProfile(profile);
            
            if (success) {
                return Result.success("Profile更新成功", null);
            } else {
                return Result.error("Profile更新失败");
            }
            
        } catch (Exception e) {
            log.error("更新Profile失败", e);
            return Result.error("更新Profile失败: " + e.getMessage());
        }
    }



    /**
     * 启用Profile
     */
    @Operation(summary = "启用Profile", description = "启用指定的Profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "启用成功")
    })
    @PostMapping("/{id}/enable")
    public Result<Void> enableProfile(
            @Parameter(description = "Profile ID") @PathVariable Long id) {
        try {
            boolean success = profileService.enableProfile(id);
            
            if (success) {
                return Result.success("Profile启用成功", null);
            } else {
                return Result.error("Profile启用失败");
            }
            
        } catch (Exception e) {
            log.error("启用Profile失败", e);
            return Result.error("启用Profile失败: " + e.getMessage());
        }
    }

    /**
     * 禁用Profile
     */
    @Operation(summary = "禁用Profile", description = "禁用指定的Profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "禁用成功")
    })
    @PostMapping("/{id}/disable")
    public Result<Void> disableProfile(
            @Parameter(description = "Profile ID") @PathVariable Long id) {
        try {
            boolean success = profileService.disableProfile(id);
            
            if (success) {
                return Result.success("Profile禁用成功", null);
            } else {
                return Result.error("Profile禁用失败");
            }
            
        } catch (Exception e) {
            log.error("禁用Profile失败", e);
            return Result.error("禁用Profile失败: " + e.getMessage());
        }
    }

    /**
     * 验证Profile
     */
    @Operation(summary = "验证Profile有效性", description = "验证指定Profile的有效性")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "验证完成")
    })
    @PostMapping("/{id}/validate")
    public Result<Map<String, Object>> validateProfile(
            @Parameter(description = "Profile ID") @PathVariable Long id) {
        try {
            boolean valid = profileService.validateProfile(id);
            
            Map<String, Object> data = new HashMap<>();
            data.put("valid", valid);
            
            return Result.success(valid ? "Profile验证通过" : "Profile验证失败", data);
            
        } catch (Exception e) {
            log.error("验证Profile失败", e);
            return Result.error("验证Profile失败: " + e.getMessage());
        }
    }

    /**
     * 获取即将过期的Profile列表
     */
    @Operation(summary = "获取即将过期的Profile列表", description = "获取即将过期的Profile列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/expiring")
    public Result<List<IosProfile>> getExpiringProfiles() {
        try {
            List<IosProfile> profiles = profileService.getExpiringProfiles();
            
            return Result.success("获取即将过期Profile列表成功", profiles);
            
        } catch (Exception e) {
            log.error("获取即将过期Profile列表失败", e);
            return Result.error("获取即将过期Profile列表失败: " + e.getMessage());
        }
    }

    /**
     * 下载Profile文件
     */
    @Operation(summary = "下载Profile文件", description = "根据Profile ID下载mobileprovision文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "下载成功")
    })
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadProfile(
            @Parameter(description = "Profile ID") @PathVariable Long id) {
        try {
            // 获取Profile信息
            IosProfile profile = profileService.getProfileById(id);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 检查文件是否存在
            File file = new File(profile.getFileUrl());
            if (!file.exists()) {
                log.error("Profile文件不存在: {}", profile.getFileUrl());
                return ResponseEntity.notFound().build();
            }
            
            // 创建文件资源
            Resource resource = new FileSystemResource(file);
            
            // 设置文件名，使用Profile名称 + .mobileprovision后缀
            String fileName = profile.getName() + ".mobileprovision";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            
            log.info("下载Profile文件: {} (ID: {})", fileName, id);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("下载Profile文件失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}