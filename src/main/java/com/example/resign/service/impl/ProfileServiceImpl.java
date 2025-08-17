package com.example.resign.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resign.entity.Profile;
import com.example.resign.mapper.ProfileMapper;
import com.example.resign.service.ProfileService;
import com.example.resign.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Profile文件管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl extends ServiceImpl<ProfileMapper, Profile> 
        implements ProfileService {
    
    private final FileService fileService;
    
    @Override
    public Profile uploadProfile(String name, Long certificateId, String bundleId, MultipartFile file, String description) {
        try {
            // 上传文件到MinIO
            String fileUrl = uploadFile(file, "profiles");
            
            // 创建Profile记录
            Profile profile = new Profile();
            profile.setName(name);
            profile.setCertificateId(certificateId);
            profile.setBundleId(bundleId);
            profile.setFileUrl(fileUrl);
            profile.setDescription(description);
            profile.setStatus("ACTIVE");
            profile.setCreateTime(LocalDateTime.now());
            profile.setUpdateTime(LocalDateTime.now());
            
            save(profile);
            
            log.info("Profile文件上传成功: {}", name);
            return profile;
            
        } catch (Exception e) {
            log.error("Profile文件上传失败: {}", name, e);
            throw new RuntimeException("Profile文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public IPage<Profile> pageList(Page<Profile> page, String bundleId) {
        LambdaQueryWrapper<Profile> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(bundleId)) {
            queryWrapper.eq(Profile::getBundleId, bundleId);
        }
        
        queryWrapper.eq(Profile::getStatus, "ACTIVE");
        queryWrapper.orderByDesc(Profile::getCreateTime);
        
        return page(page, queryWrapper);
    }
    
    @Override
    public List<Profile> getProfilesByBundleId(String bundleId) {
        LambdaQueryWrapper<Profile> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(bundleId)) {
            queryWrapper.and(wrapper -> 
                wrapper.eq(Profile::getBundleId, bundleId)
                       .or()
                       .like(Profile::getBundleId, "*") // 通配符Profile
            );
        }
        
        queryWrapper.eq(Profile::getStatus, "ACTIVE");
        queryWrapper.orderByDesc(Profile::getCreateTime);
        
        return list(queryWrapper);
    }
    
    @Override
    public List<Profile> getProfilesByCertificateId(Long certificateId) {
        LambdaQueryWrapper<Profile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Profile::getCertificateId, certificateId);
        queryWrapper.eq(Profile::getStatus, "ACTIVE");
        queryWrapper.orderByDesc(Profile::getCreateTime);
        
        return list(queryWrapper);
    }
    
    @Override
    public boolean deleteById(Long id) {
        try {
            Profile entity = getById(id);
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
            log.error("删除Profile文件失败: {}", id, e);
            return false;
        }
    }
    
    @Override
    public boolean validateProfile(Long profileId, String bundleId) {
        // TODO: 实现Profile文件验证逻辑
        // 这里应该下载Profile文件并验证Bundle ID是否匹配
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