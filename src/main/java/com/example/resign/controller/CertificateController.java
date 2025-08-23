package com.example.resign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.Certificate;
import com.example.resign.entity.Profile;
import com.example.resign.model.common.Result;
import com.example.resign.service.CertificateService;
import com.example.resign.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 证书和Profile文件管理控制器
 */
@RestController
@RequestMapping("/api/certificate")
@RequiredArgsConstructor
@Tag(name = "证书和Profile管理", description = "证书和Profile文件管理接口")
public class CertificateController {
    
    private final CertificateService certificateService;
    private final ProfileService profileService;
    
    /**
     * 上传证书文件
     */
    @Operation(summary = "上传证书文件", description = "上传iOS或Android证书文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功"),
        @ApiResponse(responseCode = "400", description = "上传失败")
    })
    @PostMapping("/upload-certificate")
    public Result<Certificate> uploadCertificate(
            @Parameter(description = "证书名称", example = "iOS开发证书") @RequestParam("name") String name,
            @Parameter(description = "平台类型", example = "ios") @RequestParam("platform") String platform,
            @Parameter(description = "证书文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "证书密码") @RequestParam("password") String password,
            @Parameter(description = "证书描述") @RequestParam(value = "description", required = false) String description) {
        
        Certificate certificate = certificateService.uploadCertificate(
            name, platform, file, password, description);
        return Result.success(certificate);
    }
    
    /**
     * 上传Profile文件
     */
    @Operation(summary = "上传Profile文件", description = "上传iOS应用的Profile文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功"),
        @ApiResponse(responseCode = "400", description = "上传失败")
    })
    @PostMapping("/upload-profile")
    public Result<Profile> uploadProfile(
            @Parameter(description = "Profile名称", example = "MyApp Profile") @RequestParam("name") String name,
            @Parameter(description = "关联的证书ID", example = "1") @RequestParam("certificateId") Long certificateId,
            @Parameter(description = "应用Bundle ID", example = "com.example.myapp") @RequestParam("bundleId") String bundleId,
            @Parameter(description = "Profile文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Profile描述") @RequestParam(value = "description", required = false) String description) {
        
        Profile profile = profileService.uploadProfile(name, certificateId, bundleId, file, description);
        return Result.success(profile);
    }
    
    /**
     * 分页查询证书
     */
    @Operation(summary = "分页查询证书", description = "分页获取证书列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/certificates")
    public Result<IPage<Certificate>> pageCertificates(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "平台类型", example = "ios") @RequestParam(required = false) String platform) {
        
        Page<Certificate> page = new Page<>(current, size);
        IPage<Certificate> result = certificateService.pageList(page, platform);
        return Result.success(result);
    }
    
    /**
     * 分页查询Profile文件
     */
    @Operation(summary = "分页查询Profile文件", description = "分页获取Profile文件列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/profiles")
    public Result<IPage<Profile>> pageProfiles(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "应用Bundle ID", example = "com.example.myapp") @RequestParam(required = false) String bundleId) {
        
        Page<Profile> page = new Page<>(current, size);
        IPage<Profile> result = profileService.pageList(page, bundleId);
        return Result.success(result);
    }
    
    /**
     * 根据平台获取证书列表
     */
    @Operation(summary = "根据平台获取证书列表", description = "获取指定平台的所有证书")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/certificates/{platform}")
    public Result<List<Certificate>> getCertificatesByPlatform(
            @Parameter(description = "平台类型", example = "ios") @PathVariable String platform) {
        List<Certificate> certificates = certificateService.getCertificatesByPlatform(platform);
        return Result.success(certificates);
    }
    
    /**
     * 根据Bundle ID获取Profile文件列表
     */
    @Operation(summary = "根据Bundle ID获取Profile文件列表", description = "获取指定Bundle ID的所有Profile文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/profiles/bundle/{bundleId}")
    public Result<List<Profile>> getProfilesByBundleId(
            @Parameter(description = "应用Bundle ID", example = "com.example.myapp") @PathVariable String bundleId) {
        List<Profile> profiles = profileService.getProfilesByBundleId(bundleId);
        return Result.success(profiles);
    }
    
    /**
     * 根据证书ID获取Profile文件列表
     */
    @Operation(summary = "根据证书ID获取Profile文件列表", description = "获取指定证书关联的所有Profile文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/profiles/certificate/{certificateId}")
    public Result<List<Profile>> getProfilesByCertificateId(
            @Parameter(description = "证书ID", example = "1") @PathVariable Long certificateId) {
        List<Profile> profiles = profileService.getProfilesByCertificateId(certificateId);
        return Result.success(profiles);
    }
    
    /**
     * 删除证书文件
     */
    @Operation(summary = "删除证书文件", description = "根据ID删除指定证书文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功")
    })
    @DeleteMapping("/certificates/{id}")
    public Result<Boolean> deleteCertificate(
            @Parameter(description = "证书ID", example = "1") @PathVariable Long id) {
        boolean result = certificateService.deleteById(id);
        return Result.success(result);
    }
    
    /**
     * 删除Profile文件
     */
    @Operation(summary = "删除Profile文件", description = "根据ID删除指定Profile文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功")
    })
    @DeleteMapping("/profiles/{id}")
    public Result<Boolean> deleteProfile(
            @Parameter(description = "Profile文件ID", example = "1") @PathVariable Long id) {
        boolean result = profileService.deleteById(id);
        return Result.success(result);
    }
    
    /**
     * 验证证书文件
     */
    @Operation(summary = "验证证书文件", description = "验证指定证书文件的有效性")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "验证完成")
    })
    @PostMapping("/validate-certificate/{id}")
    public Result<Boolean> validateCertificate(
            @Parameter(description = "证书ID", example = "1") @PathVariable Long id,
            @Parameter(description = "证书密码") @RequestParam String password) {
        boolean result = certificateService.validateCertificate(id, password);
        return Result.success(result);
    }
    
    /**
     * 验证Profile文件
     */
    @Operation(summary = "验证Profile文件", description = "验证指定Profile文件的有效性")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "验证完成")
    })
    @PostMapping("/validate-profile/{id}")
    public Result<Boolean> validateProfile(
            @Parameter(description = "Profile文件ID", example = "1") @PathVariable Long id,
            @Parameter(description = "应用Bundle ID", example = "com.example.myapp") @RequestParam String bundleId) {
        boolean result = profileService.validateProfile(id, bundleId);
        return Result.success(result);
    }
}