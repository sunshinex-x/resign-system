package com.example.resign.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.resign.entity.Profile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Profile文件管理服务
 */
public interface ProfileService extends IService<Profile> {
    
    /**
     * 上传Profile文件
     */
    Profile uploadProfile(String name, Long certificateId, String bundleId, MultipartFile file, String description);
    
    /**
     * 分页查询Profile文件
     */
    IPage<Profile> pageList(Page<Profile> page, String bundleId);
    
    /**
     * 根据Bundle ID获取Profile文件列表
     */
    List<Profile> getProfilesByBundleId(String bundleId);
    
    /**
     * 根据证书ID获取Profile文件列表
     */
    List<Profile> getProfilesByCertificateId(Long certificateId);
    
    /**
     * 删除Profile文件
     */
    boolean deleteById(Long id);
    
    /**
     * 验证Profile文件
     */
    boolean validateProfile(Long profileId, String bundleId);
}