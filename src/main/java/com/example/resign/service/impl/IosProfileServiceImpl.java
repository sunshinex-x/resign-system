package com.example.resign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.IosProfile;
import com.example.resign.mapper.IosProfileMapper;
import com.example.resign.service.IosProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * iOS Profile管理Service实现类
 */
@Slf4j
@Service
public class IosProfileServiceImpl implements IosProfileService {

    @Autowired
    private IosProfileMapper profileMapper;

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Override
    public IosProfile uploadProfile(MultipartFile file, String name, Long certificateId,
                                  String bundleId, String teamId, String profileType, String description) {
        try {
            // 验证文件格式
            if (!file.getOriginalFilename().endsWith(".mobileprovision")) {
                throw new RuntimeException("只支持mobileprovision格式的Profile文件");
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + ".mobileprovision";
            String filePath = uploadPath + "/profiles/ios/" + fileName;

            // 确保目录存在
            File dir = new File(uploadPath + "/profiles/ios/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File destFile = new File(filePath);
            file.transferTo(destFile);

            // 如果没有提供Bundle ID，尝试从文件中解析
            if (!StringUtils.hasText(bundleId)) {
                bundleId = parseBundleIdFromProfile(file);
            }

            // 如果没有提供团队ID，尝试从文件中解析
            if (!StringUtils.hasText(teamId)) {
                teamId = parseTeamIdFromProfile(file);
            }

            // 创建Profile记录
            IosProfile profile = new IosProfile();
            profile.setName(name);
            profile.setCertificateId(certificateId);
            profile.setBundleId(bundleId);
            profile.setFileUrl(filePath);
            profile.setTeamId(teamId);
            profile.setProfileType(profileType);
            profile.setDescription(description);
            profile.setStatus("ACTIVE");
            profile.setCreateTime(LocalDateTime.now());
            profile.setUpdateTime(LocalDateTime.now());
            // TODO: 从当前登录用户获取
            profile.setCreateBy("system");
            profile.setUpdateBy("system");

            // 保存到数据库
            profileMapper.insert(profile);
            
            log.info("iOS Profile上传成功: {}", profile.getName());
            return profile;
            
        } catch (IOException e) {
            log.error("Profile文件保存失败", e);
            throw new RuntimeException("Profile文件保存失败: " + e.getMessage());
        }
    }

    @Override
    public IosProfile getProfileById(Long id) {
        return profileMapper.selectById(id);
    }

    @Override
    public Page<IosProfile> getProfileList(int page, int size, String status, Long certificateId,
                                         String bundleId, String teamId, String profileType) {
        Page<IosProfile> pageParam = new Page<>(page, size);
        QueryWrapper<IosProfile> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (certificateId != null) {
            queryWrapper.eq("certificate_id", certificateId);
        }
        if (StringUtils.hasText(bundleId)) {
            queryWrapper.like("bundle_id", bundleId);
        }
        if (StringUtils.hasText(teamId)) {
            queryWrapper.eq("team_id", teamId);
        }
        if (StringUtils.hasText(profileType)) {
            queryWrapper.eq("profile_type", profileType);
        }
        
        queryWrapper.orderByDesc("create_time");
        return profileMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public List<IosProfile> getProfilesByCertificateId(Long certificateId) {
        return profileMapper.selectByCertificateId(certificateId);
    }

    @Override
    public List<IosProfile> getProfilesByBundleId(String bundleId) {
        return profileMapper.selectByBundleId(bundleId);
    }

    @Override
    public List<IosProfile> getProfilesByStatus(String status) {
        return profileMapper.selectByStatus(status);
    }

    @Override
    public boolean updateProfile(IosProfile profile) {
        profile.setUpdateTime(LocalDateTime.now());
        // TODO: 从当前登录用户获取
        profile.setUpdateBy("system");
        return profileMapper.updateById(profile) > 0;
    }

    @Override
    public boolean deleteProfile(Long id) {
        IosProfile profile = profileMapper.selectById(id);
        if (profile != null) {
            // 删除文件
            File file = new File(profile.getFileUrl());
            if (file.exists()) {
                file.delete();
            }
            // 删除数据库记录
            return profileMapper.deleteById(id) > 0;
        }
        return false;
    }

    @Override
    public boolean enableProfile(Long id) {
        IosProfile profile = new IosProfile();
        profile.setId(id);
        profile.setStatus("ACTIVE");
        profile.setUpdateTime(LocalDateTime.now());
        return profileMapper.updateById(profile) > 0;
    }

    @Override
    public boolean disableProfile(Long id) {
        IosProfile profile = new IosProfile();
        profile.setId(id);
        profile.setStatus("INACTIVE");
        profile.setUpdateTime(LocalDateTime.now());
        return profileMapper.updateById(profile) > 0;
    }

    @Override
    public boolean validateProfile(Long id) {
        // TODO: 实现Profile验证逻辑
        // 可以通过调用系统命令验证mobileprovision文件的有效性
        return true;
    }

    @Override
    public boolean isProfileExpired(Long id) {
        IosProfile profile = profileMapper.selectById(id);
        if (profile != null && profile.getExpireDate() != null) {
            return profile.getExpireDate().isBefore(LocalDateTime.now());
        }
        return false;
    }

    @Override
    public List<IosProfile> getExpiringProfiles() {
        QueryWrapper<IosProfile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE")
                   .isNotNull("expire_date")
                   .le("expire_date", LocalDateTime.now().plusDays(30))
                   .ge("expire_date", LocalDateTime.now());
        return profileMapper.selectList(queryWrapper);
    }

    @Override
    public String parseBundleIdFromProfile(MultipartFile file) {
        try {
            // 使用security命令解析mobileprovision文件
            File tempFile = File.createTempFile("profile", ".mobileprovision");
            file.transferTo(tempFile);
            
            ProcessBuilder pb = new ProcessBuilder("security", "cms", "-D", "-i", tempFile.getAbsolutePath());
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            // 使用正则表达式提取Bundle ID
            Pattern pattern = Pattern.compile("<key>application-identifier</key>\\s*<string>([^<]+)</string>");
            Matcher matcher = pattern.matcher(content.toString());
            if (matcher.find()) {
                String appId = matcher.group(1);
                // 移除团队ID前缀，只保留Bundle ID
                if (appId.contains(".")) {
                    return appId.substring(appId.indexOf(".") + 1);
                }
                return appId;
            }
            
            tempFile.delete();
            
        } catch (Exception e) {
            log.error("解析Profile文件Bundle ID失败", e);
        }
        return null;
    }

    @Override
    public String parseTeamIdFromProfile(MultipartFile file) {
        try {
            // 使用security命令解析mobileprovision文件
            File tempFile = File.createTempFile("profile", ".mobileprovision");
            file.transferTo(tempFile);
            
            ProcessBuilder pb = new ProcessBuilder("security", "cms", "-D", "-i", tempFile.getAbsolutePath());
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            // 使用正则表达式提取团队ID
            Pattern pattern = Pattern.compile("<key>TeamIdentifier</key>\\s*<array>\\s*<string>([^<]+)</string>");
            Matcher matcher = pattern.matcher(content.toString());
            if (matcher.find()) {
                return matcher.group(1);
            }
            
            tempFile.delete();
            
        } catch (Exception e) {
            log.error("解析Profile文件团队ID失败", e);
        }
        return null;
    }
}