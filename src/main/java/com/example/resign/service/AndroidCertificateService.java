package com.example.resign.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.AndroidCertificate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Android证书管理Service接口
 */
public interface AndroidCertificateService {

    /**
     * 上传并保存Android证书
     */
    AndroidCertificate uploadCertificate(MultipartFile file, String name, String password, 
                                       String keyAlias, String keyPassword, String description);

    /**
     * 根据ID获取证书信息
     */
    AndroidCertificate getCertificateById(Long id);

    /**
     * 分页查询证书列表
     */
    Page<AndroidCertificate> getCertificateList(int page, int size, String status, String keyAlias);

    /**
     * 根据状态查询证书列表
     */
    List<AndroidCertificate> getCertificatesByStatus(String status);

    /**
     * 根据密钥别名查询证书列表
     */
    List<AndroidCertificate> getCertificatesByKeyAlias(String keyAlias);

    /**
     * 获取有效的证书列表
     */
    List<AndroidCertificate> getActiveCertificates();

    /**
     * 更新证书信息
     */
    boolean updateCertificate(AndroidCertificate certificate);

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
    List<AndroidCertificate> getExpiringCertificates();

    /**
     * 验证证书密码和密钥密码
     */
    boolean validateCertificatePassword(Long id, String password, String keyPassword);
}