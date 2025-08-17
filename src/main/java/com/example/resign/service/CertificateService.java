package com.example.resign.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.resign.entity.Certificate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 证书管理服务
 */
public interface CertificateService extends IService<Certificate> {
    
    /**
     * 上传证书文件
     */
    Certificate uploadCertificate(String name, String platform, MultipartFile file, 
                                String password, String description);
    
    /**
     * 分页查询证书
     */
    IPage<Certificate> pageList(Page<Certificate> page, String platform);
    
    /**
     * 根据平台获取证书列表
     */
    List<Certificate> getCertificatesByPlatform(String platform);
    
    /**
     * 删除证书文件
     */
    boolean deleteById(Long id);
    
    /**
     * 验证证书文件
     */
    boolean validateCertificate(Long certificateId, String password);
}