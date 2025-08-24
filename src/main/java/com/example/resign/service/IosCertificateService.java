package com.example.resign.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.IosCertificate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * iOS证书管理Service接口
 */
public interface IosCertificateService {

    /**
     * 上传并保存iOS证书
     */
    IosCertificate uploadCertificate(MultipartFile file, String name, String password, 
                                   String teamId, String bundleId, String certificateType, String description);

    /**
     * 根据ID获取证书信息
     */
    IosCertificate getCertificateById(Long id);

    /**
     * 分页查询证书列表
     */
    Page<IosCertificate> getCertificateList(int page, int size, String status, String teamId, String certificateType);

    /**
     * 根据状态查询证书列表
     */
    List<IosCertificate> getCertificatesByStatus(String status);

    /**
     * 根据团队ID查询证书列表
     */
    List<IosCertificate> getCertificatesByTeamId(String teamId);

    /**
     * 更新证书信息
     */
    boolean updateCertificate(IosCertificate certificate);

    /**
     * 删除证书
     */
    boolean deleteCertificate(Long id);

    /**
     * 启用证书
     */
    boolean enableCertificate(Long id);

    /**
     * 禁用证书
     */
    boolean disableCertificate(Long id);

    /**
     * 验证证书有效性
     */
    boolean validateCertificate(Long id);

    /**
     * 检查证书是否过期
     */
    boolean isCertificateExpired(Long id);

    /**
     * 获取即将过期的证书列表（30天内过期）
     */
    List<IosCertificate> getExpiringCertificates();
}