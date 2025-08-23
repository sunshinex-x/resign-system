package com.example.resign.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.IosProfile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * iOS Profile管理Service接口
 */
public interface IosProfileService {

    /**
     * 上传并保存iOS Profile
     */
    IosProfile uploadProfile(MultipartFile file, String name, Long certificateId, 
                           String bundleId, String teamId, String profileType, String description);

    /**
     * 根据ID获取Profile信息
     */
    IosProfile getProfileById(Long id);

    /**
     * 分页查询Profile列表
     */
    Page<IosProfile> getProfileList(int page, int size, String status, Long certificateId, 
                                  String bundleId, String teamId, String profileType);

    /**
     * 根据证书ID查询Profile列表
     */
    List<IosProfile> getProfilesByCertificateId(Long certificateId);

    /**
     * 根据Bundle ID查询Profile列表
     */
    List<IosProfile> getProfilesByBundleId(String bundleId);

    /**
     * 根据状态查询Profile列表
     */
    List<IosProfile> getProfilesByStatus(String status);

    /**
     * 更新Profile信息
     */
    boolean updateProfile(IosProfile profile);

    /**
     * 删除Profile
     */
    boolean deleteProfile(Long id);

    /**
     * 启用Profile
     */
    boolean enableProfile(Long id);

    /**
     * 禁用Profile
     */
    boolean disableProfile(Long id);

    /**
     * 验证Profile有效性
     */
    boolean validateProfile(Long id);

    /**
     * 检查Profile是否过期
     */
    boolean isProfileExpired(Long id);

    /**
     * 获取即将过期的Profile列表（30天内过期）
     */
    List<IosProfile> getExpiringProfiles();

    /**
     * 解析Profile文件获取Bundle ID
     */
    String parseBundleIdFromProfile(MultipartFile file);

    /**
     * 解析Profile文件获取团队ID
     */
    String parseTeamIdFromProfile(MultipartFile file);
}