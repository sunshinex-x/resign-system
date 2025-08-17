package com.example.resign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.Certificate;
import com.example.resign.entity.Profile;
import com.example.resign.model.common.Result;
import com.example.resign.service.CertificateService;
import com.example.resign.service.ProfileService;
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
public class CertificateController {
    
    private final CertificateService certificateService;
    private final ProfileService profileService;
    
    /**
     * 上传证书文件
     */
    @PostMapping("/upload-certificate")
    public Result<Certificate> uploadCertificate(
            @RequestParam("name") String name,
            @RequestParam("platform") String platform,
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password,
            @RequestParam(value = "description", required = false) String description) {
        
        Certificate certificate = certificateService.uploadCertificate(
            name, platform, file, password, description);
        return Result.success(certificate);
    }
    
    /**
     * 上传Profile文件
     */
    @PostMapping("/upload-profile")
    public Result<Profile> uploadProfile(
            @RequestParam("name") String name,
            @RequestParam("certificateId") Long certificateId,
            @RequestParam("bundleId") String bundleId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        
        Profile profile = profileService.uploadProfile(name, certificateId, bundleId, file, description);
        return Result.success(profile);
    }
    
    /**
     * 分页查询证书
     */
    @GetMapping("/certificates")
    public Result<IPage<Certificate>> pageCertificates(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String platform) {
        
        Page<Certificate> page = new Page<>(current, size);
        IPage<Certificate> result = certificateService.pageList(page, platform);
        return Result.success(result);
    }
    
    /**
     * 分页查询Profile文件
     */
    @GetMapping("/profiles")
    public Result<IPage<Profile>> pageProfiles(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String bundleId) {
        
        Page<Profile> page = new Page<>(current, size);
        IPage<Profile> result = profileService.pageList(page, bundleId);
        return Result.success(result);
    }
    
    /**
     * 根据平台获取证书列表
     */
    @GetMapping("/certificates/{platform}")
    public Result<List<Certificate>> getCertificatesByPlatform(@PathVariable String platform) {
        List<Certificate> certificates = certificateService.getCertificatesByPlatform(platform);
        return Result.success(certificates);
    }
    
    /**
     * 根据Bundle ID获取Profile文件列表
     */
    @GetMapping("/profiles/bundle/{bundleId}")
    public Result<List<Profile>> getProfilesByBundleId(@PathVariable String bundleId) {
        List<Profile> profiles = profileService.getProfilesByBundleId(bundleId);
        return Result.success(profiles);
    }
    
    /**
     * 根据证书ID获取Profile文件列表
     */
    @GetMapping("/profiles/certificate/{certificateId}")
    public Result<List<Profile>> getProfilesByCertificateId(@PathVariable Long certificateId) {
        List<Profile> profiles = profileService.getProfilesByCertificateId(certificateId);
        return Result.success(profiles);
    }
    
    /**
     * 删除证书文件
     */
    @DeleteMapping("/certificates/{id}")
    public Result<Boolean> deleteCertificate(@PathVariable Long id) {
        boolean result = certificateService.deleteById(id);
        return Result.success(result);
    }
    
    /**
     * 删除Profile文件
     */
    @DeleteMapping("/profiles/{id}")
    public Result<Boolean> deleteProfile(@PathVariable Long id) {
        boolean result = profileService.deleteById(id);
        return Result.success(result);
    }
    
    /**
     * 验证证书文件
     */
    @PostMapping("/validate-certificate/{id}")
    public Result<Boolean> validateCertificate(@PathVariable Long id, @RequestParam String password) {
        boolean result = certificateService.validateCertificate(id, password);
        return Result.success(result);
    }
    
    /**
     * 验证Profile文件
     */
    @PostMapping("/validate-profile/{id}")
    public Result<Boolean> validateProfile(@PathVariable Long id, @RequestParam String bundleId) {
        boolean result = profileService.validateProfile(id, bundleId);
        return Result.success(result);
    }
}