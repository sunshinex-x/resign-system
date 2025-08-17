package com.example.resign.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resign.entity.Certificate;
import com.example.resign.mapper.CertificateMapper;
import com.example.resign.service.CertificateService;
import com.example.resign.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 证书管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> 
        implements CertificateService {
    
    private final FileService fileService;
    
    @Override
    public Certificate uploadCertificate(String name, String platform, MultipartFile file, 
                                       String password, String description) {
        try {
            // 上传文件到MinIO
            String fileUrl = uploadFile(file, "certificates");
            
            // 创建证书记录
            Certificate certificate = new Certificate();
            certificate.setName(name);
            certificate.setPlatform(platform);
            certificate.setFileUrl(fileUrl);
            certificate.setPassword(password);
            certificate.setDescription(description);
            certificate.setStatus("ACTIVE");
            certificate.setCreateTime(LocalDateTime.now());
            certificate.setUpdateTime(LocalDateTime.now());
            
            save(certificate);
            
            log.info("证书上传成功: {}", name);
            return certificate;
            
        } catch (Exception e) {
            log.error("证书上传失败: {}", name, e);
            throw new RuntimeException("证书上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public IPage<Certificate> pageList(Page<Certificate> page, String platform) {
        LambdaQueryWrapper<Certificate> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(platform)) {
            queryWrapper.eq(Certificate::getPlatform, platform);
        }
        
        queryWrapper.eq(Certificate::getStatus, "ACTIVE");
        queryWrapper.orderByDesc(Certificate::getCreateTime);
        
        return page(page, queryWrapper);
    }
    
    @Override
    public List<Certificate> getCertificatesByPlatform(String platform) {
        LambdaQueryWrapper<Certificate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Certificate::getPlatform, platform);
        queryWrapper.eq(Certificate::getStatus, "ACTIVE");
        queryWrapper.orderByDesc(Certificate::getCreateTime);
        
        return list(queryWrapper);
    }
    
    @Override
    public boolean deleteById(Long id) {
        try {
            Certificate entity = getById(id);
            if (entity == null) {
                return false;
            }
            
            // 删除MinIO中的文件
            if (StrUtil.isNotBlank(entity.getFileUrl())) {
                String objectName = fileService.getObjectNameFromUrl(entity.getFileUrl());
                fileService.deleteFile(objectName);
            }
            
            // 软删除记录
            entity.setStatus("INACTIVE");
            entity.setUpdateTime(LocalDateTime.now());
            
            return updateById(entity);
            
        } catch (Exception e) {
            log.error("删除证书文件失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public boolean validateCertificate(Long certificateId, String password) {
        // TODO: 实现证书验证逻辑
        // 这里应该下载证书文件并验证密码是否正确
        return true;
    }
    
    /**
     * 上传文件到MinIO
     */
    private String uploadFile(MultipartFile file, String folder) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String objectName = folder + "/" + IdUtil.fastSimpleUUID() + extension;
            
            return fileService.uploadFile(file.getInputStream(), objectName, file.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
}