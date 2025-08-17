package com.example.resign.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 包解析结果DTO
 */
@Data
public class PackageParseResultDTO {
    
    /**
     * 应用类型
     */
    private String appType;
    
    /**
     * 应用名称
     */
    private String appName;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * Bundle ID列表
     */
    private List<BundleInfo> bundleInfos;
    
    @Data
    public static class BundleInfo {
        /**
         * Bundle ID
         */
        private String bundleId;
        
        /**
         * 应用名称
         */
        private String appName;
        
        /**
         * 版本号
         */
        private String version;
        
        /**
         * 是否为主应用
         */
        private Boolean isMainApp;
    }
}