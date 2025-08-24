package com.example.resign.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resign.entity.SignConfig;
import com.example.resign.mapper.SignConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 签名配置服务实现类
 */
public interface SignConfigService  {

    /**
     * 分页查询签名配置
     */
    public IPage<SignConfig> getSignConfigPage(int current, int size, String appName, String packageName, String appType, Integer status);

    /**
     * 根据ID获取签名配置
     */
    public SignConfig getSignConfigById(Long id);

    /**
     * 创建签名配置
     */
    public boolean createSignConfig(SignConfig signConfig);

    /**
     * 更新签名配置
     */
    public boolean updateSignConfig(SignConfig signConfig);

    /**
     * 删除签名配置
     */
    public boolean deleteSignConfig(Long id);

    /**
     * 批量删除签名配置
     */
    public boolean batchDeleteSignConfig(List<Long> ids);

    /**
     * 启用/禁用签名配置
     */
    public boolean toggleSignConfigStatus(Long id, Integer status);

    /**
     * 根据包名获取签名配置
     */
    public SignConfig getSignConfigByPackageNameAndType(String packageName, String appType);

    /**
     * 根据应用类型获取所有启用的签名配置
     */
    public List<SignConfig> getEnabledSignConfigsByType(String appType);

    /**
     * 根据包名自动创建或获取签名配置
     */
    public SignConfig autoGetOrCreateSignConfig(String packageName, String appType);
}