package com.example.resign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.IosCertificate;
import com.example.resign.mapper.IosCertificateMapper;
import com.example.resign.service.IosCertificateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * iOS证书管理Service实现类
 */
@Slf4j
@Service
public class IosCertificateServiceImpl implements IosCertificateService {

    @Autowired
    private IosCertificateMapper certificateMapper;

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Override
    public IosCertificate uploadCertificate(MultipartFile file, String name, String password,
                                          String teamId, String certificateType, String description) {
        try {
            // 验证文件格式
            if (!file.getOriginalFilename().endsWith(".p12")) {
                throw new RuntimeException("只支持p12格式的证书文件");
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + ".p12";
            String filePath = uploadPath + "/certificates/ios/" + fileName;

            // 确保目录存在
            File dir = new File(uploadPath + "/certificates/ios/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File destFile = new File(filePath);
            file.transferTo(destFile);

            // 创建证书记录
            IosCertificate certificate = new IosCertificate();
            certificate.setName(name);
            certificate.setFileUrl(filePath);
            certificate.setPassword(password);
            certificate.setTeamId(teamId);
            certificate.setCertificateType(certificateType);
            certificate.setDescription(description);
            certificate.setStatus("ACTIVE");
            certificate.setCreateTime(LocalDateTime.now());
            certificate.setUpdateTime(LocalDateTime.now());
            // TODO: 从当前登录用户获取
            certificate.setCreateBy("system");
            certificate.setUpdateBy("system");

            // 保存到数据库
            certificateMapper.insert(certificate);
            
            log.info("iOS证书上传成功: {}", certificate.getName());
            return certificate;
            
        } catch (IOException e) {
            log.error("证书文件保存失败", e);
            throw new RuntimeException("证书文件保存失败: " + e.getMessage());
        }
    }

    @Override
    public IosCertificate getCertificateById(Long id) {
        return certificateMapper.selectById(id);
    }

    @Override
    public Page<IosCertificate> getCertificateList(int page, int size, String status, String teamId, String certificateType) {
        Page<IosCertificate> pageParam = new Page<>(page, size);
        QueryWrapper<IosCertificate> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (StringUtils.hasText(teamId)) {
            queryWrapper.eq("team_id", teamId);
        }
        if (StringUtils.hasText(certificateType)) {
            queryWrapper.eq("certificate_type", certificateType);
        }
        
        queryWrapper.orderByDesc("create_time");
        return certificateMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public List<IosCertificate> getCertificatesByStatus(String status) {
        return certificateMapper.selectByStatus(status);
    }

    @Override
    public List<IosCertificate> getCertificatesByTeamId(String teamId) {
        return certificateMapper.selectByTeamId(teamId);
    }

    @Override
    public boolean updateCertificate(IosCertificate certificate) {
        certificate.setUpdateTime(LocalDateTime.now());
        // TODO: 从当前登录用户获取
        certificate.setUpdateBy("system");
        return certificateMapper.updateById(certificate) > 0;
    }

    @Override
    public boolean deleteCertificate(Long id) {
        IosCertificate certificate = certificateMapper.selectById(id);
        if (certificate != null) {
            // 删除文件
            File file = new File(certificate.getFileUrl());
            if (file.exists()) {
                file.delete();
            }
            // 删除数据库记录
            return certificateMapper.deleteById(id) > 0;
        }
        return false;
    }

    @Override
    public boolean enableCertificate(Long id) {
        IosCertificate certificate = new IosCertificate();
        certificate.setId(id);
        certificate.setStatus("ACTIVE");
        certificate.setUpdateTime(LocalDateTime.now());
        return certificateMapper.updateById(certificate) > 0;
    }

    @Override
    public boolean disableCertificate(Long id) {
        IosCertificate certificate = new IosCertificate();
        certificate.setId(id);
        certificate.setStatus("INACTIVE");
        certificate.setUpdateTime(LocalDateTime.now());
        return certificateMapper.updateById(certificate) > 0;
    }

    @Override
    public boolean validateCertificate(Long id) {
        // TODO: 实现证书验证逻辑
        // 可以通过调用系统命令验证p12证书的有效性
        return true;
    }

    @Override
    public boolean isCertificateExpired(Long id) {
        IosCertificate certificate = certificateMapper.selectById(id);
        if (certificate != null && certificate.getExpireDate() != null) {
            return certificate.getExpireDate().isBefore(LocalDateTime.now());
        }
        return false;
    }

    @Override
    public List<IosCertificate> getExpiringCertificates() {
        QueryWrapper<IosCertificate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE")
                   .isNotNull("expire_date")
                   .le("expire_date", LocalDateTime.now().plusDays(30))
                   .ge("expire_date", LocalDateTime.now());
        return certificateMapper.selectList(queryWrapper);
    }
}