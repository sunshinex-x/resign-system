package com.example.resign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resign.entity.SignConfig;
import com.example.resign.mapper.SignConfigMapper;
import com.example.resign.service.SignConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SignConfigServiceImpl
 *
 * @author 枫叶fy
 * @date 2025/8/24 23:27
 * @description
 */
@Slf4j
@Service
public class SignConfigServiceImpl extends ServiceImpl<SignConfigMapper, SignConfig> implements SignConfigService {

    /**
     * 分页查询签名配置
     */
    public IPage<SignConfig> getSignConfigPage(int current, int size, String appName, String packageName, String appType, Integer status) {
        Page<SignConfig> page = new Page<>(current, size);
        QueryWrapper<SignConfig> queryWrapper = new QueryWrapper<>();

        // 构建查询条件
        if (StringUtils.hasText(appName)) {
            queryWrapper.like("app_name", appName);
        }
        if (StringUtils.hasText(packageName)) {
            queryWrapper.like("package_name", packageName);
        }
        if (StringUtils.hasText(appType)) {
            queryWrapper.eq("app_type", appType);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc("create_time");

        return this.page(page, queryWrapper);
    }

    /**
     * 根据ID获取签名配置
     */
    public SignConfig getSignConfigById(Long id) {
        return this.getById(id);
    }

    /**
     * 创建签名配置
     */
    public boolean createSignConfig(SignConfig signConfig) {
        // 检查包名和应用类型的唯一性
        QueryWrapper<SignConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("package_name", signConfig.getPackageName())
                .eq("app_type", signConfig.getAppType());
        SignConfig existing = this.getOne(queryWrapper);
        if (existing != null) {
            throw new RuntimeException("该包名在" + signConfig.getAppType() + "平台下已存在签名配置");
        }

        signConfig.setCreateTime(LocalDateTime.now());
        signConfig.setUpdateTime(LocalDateTime.now());
        if (signConfig.getStatus() == null) {
            signConfig.setStatus(1); // 默认启用
        }

        boolean result = this.save(signConfig);
        if (result) {
            log.info("创建签名配置成功: {}", signConfig.getAppName());
        }
        return result;
    }

    /**
     * 更新签名配置
     */
    public boolean updateSignConfig(SignConfig signConfig) {
        SignConfig existing = this.getById(signConfig.getId());
        if (existing == null) {
            throw new RuntimeException("签名配置不存在");
        }

        // 检查包名和应用类型的唯一性（排除当前记录）
        QueryWrapper<SignConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("package_name", signConfig.getPackageName())
                .eq("app_type", signConfig.getAppType())
                .ne("id", signConfig.getId());
        SignConfig duplicate = this.getOne(queryWrapper);
        if (duplicate != null) {
            throw new RuntimeException("该包名在" + signConfig.getAppType() + "平台下已存在签名配置");
        }

        signConfig.setUpdateTime(LocalDateTime.now());

        boolean result = this.updateById(signConfig);
        if (result) {
            log.info("更新签名配置成功: {}", signConfig.getAppName());
        }
        return result;
    }

    /**
     * 删除签名配置
     */
    public boolean deleteSignConfig(Long id) {
        SignConfig signConfig = this.getById(id);
        if (signConfig == null) {
            throw new RuntimeException("签名配置不存在");
        }

        boolean result = this.removeById(id);
        if (result) {
            log.info("删除签名配置成功: {}", signConfig.getAppName());
        }
        return result;
    }

    /**
     * 批量删除签名配置
     */
    public boolean batchDeleteSignConfig(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("删除ID列表不能为空");
        }

        boolean result = this.removeByIds(ids);
        if (result) {
            log.info("批量删除签名配置成功，数量: {}", ids.size());
        }
        return result;
    }

    /**
     * 启用/禁用签名配置
     */
    public boolean toggleSignConfigStatus(Long id, Integer status) {
        SignConfig signConfig = this.getById(id);
        if (signConfig == null) {
            throw new RuntimeException("签名配置不存在");
        }

        signConfig.setStatus(status);
        signConfig.setUpdateTime(LocalDateTime.now());

        boolean result = this.updateById(signConfig);
        if (result) {
            String action = status == 1 ? "启用" : "禁用";
            log.info("{}签名配置成功: {}", action, signConfig.getAppName());
        }
        return result;
    }

    /**
     * 根据包名和应用类型获取签名配置
     */
    public SignConfig getSignConfigByPackageNameAndType(String packageName, String appType) {
        QueryWrapper<SignConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("package_name", packageName)
                .eq("app_type", appType)
                .eq("status", 1); // 只查询启用的配置
        return this.getOne(queryWrapper);
    }

    /**
     * 根据应用类型获取所有启用的签名配置
     */
    public List<SignConfig> getEnabledSignConfigsByType(String appType) {
        QueryWrapper<SignConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_type", appType)
                .eq("status", 1)
                .orderByDesc("create_time");
        return this.list(queryWrapper);
    }

    /**
     * 根据包名自动创建或获取签名配置
     */
    public SignConfig autoGetOrCreateSignConfig(String packageName, String appType) {
        // 先尝试获取现有配置
        SignConfig existingConfig = getSignConfigByPackageNameAndType(packageName, appType);
        if (existingConfig != null) {
            return existingConfig;
        }

        // 如果不存在，创建新的配置
        SignConfig newConfig = new SignConfig();
        newConfig.setPackageName(packageName);
        newConfig.setAppType(appType);
        newConfig.setAppName(packageName); // 默认使用包名作为应用名
        newConfig.setStatus(1); // 默认启用
        newConfig.setCreateTime(LocalDateTime.now());
        newConfig.setUpdateTime(LocalDateTime.now());

        // 保存新配置
        if (this.save(newConfig)) {
            return newConfig;
        } else {
            throw new RuntimeException("创建签名配置失败");
        }
    }
}
