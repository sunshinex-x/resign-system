package com.example.resign.enums;

import lombok.Getter;

/**
 * 应用类型枚举
 */
@Getter
public enum AppType {
    
    IOS("IOS", ".ipa"),
    ANDROID("ANDROID", ".apk"),
    HARMONY("HARMONY", ".hap");
    
    private final String type;
    private final String extension;
    
    AppType(String type, String extension) {
        this.type = type;
        this.extension = extension;
    }
    
    public static AppType fromString(String type) {
        for (AppType appType : AppType.values()) {
            if (appType.getType().equalsIgnoreCase(type)) {
                return appType;
            }
        }
        throw new IllegalArgumentException("Unknown app type: " + type);
    }
    
    public static AppType fromExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".ipa")) {
            return IOS;
        } else if (lowerFileName.endsWith(".apk")) {
            return ANDROID;
        } else if (lowerFileName.endsWith(".hap")) {
            return HARMONY;
        }
        
        return null;
    }
}