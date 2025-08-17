package com.example.resign.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * iOS权限处理工具类
 * 处理iOS应用的权限和功能配置
 */
@Slf4j
public class IosPermissionHandler {

    // 常见的iOS权限映射
    private static final Map<String, String> PERMISSION_MAPPING = new HashMap<>();
    
    static {
        // 相机权限
        PERMISSION_MAPPING.put("NSCameraUsageDescription", "相机");
        // 相册权限
        PERMISSION_MAPPING.put("NSPhotoLibraryUsageDescription", "相册");
        // 位置权限
        PERMISSION_MAPPING.put("NSLocationWhenInUseUsageDescription", "位置(使用时)");
        PERMISSION_MAPPING.put("NSLocationAlwaysAndWhenInUseUsageDescription", "位置(始终)");
        // 麦克风权限
        PERMISSION_MAPPING.put("NSMicrophoneUsageDescription", "麦克风");
        // 通讯录权限
        PERMISSION_MAPPING.put("NSContactsUsageDescription", "通讯录");
        // 日历权限
        PERMISSION_MAPPING.put("NSCalendarsUsageDescription", "日历");
        // 提醒事项权限
        PERMISSION_MAPPING.put("NSRemindersUsageDescription", "提醒事项");
        // 蓝牙权限
        PERMISSION_MAPPING.put("NSBluetoothPeripheralUsageDescription", "蓝牙");
        // Face ID权限
        PERMISSION_MAPPING.put("NSFaceIDUsageDescription", "Face ID");
        // 健康数据权限
        PERMISSION_MAPPING.put("NSHealthShareUsageDescription", "健康数据读取");
        PERMISSION_MAPPING.put("NSHealthUpdateUsageDescription", "健康数据写入");
    }

    /**
     * 从Info.plist中提取权限信息
     */
    public static List<String> extractPermissions(Path infoPlistPath) {
        List<String> permissions = new ArrayList<>();
        
        try {
            if (!Files.exists(infoPlistPath)) {
                return permissions;
            }
            
            String content = Files.readString(infoPlistPath);
            
            // 提取权限描述
            for (Map.Entry<String, String> entry : PERMISSION_MAPPING.entrySet()) {
                String key = entry.getKey();
                String description = entry.getValue();
                
                if (content.contains("<key>" + key + "</key>")) {
                    permissions.add(description);
                }
            }
            
            // 提取URL Schemes
            List<String> urlSchemes = extractUrlSchemes(content);
            if (!urlSchemes.isEmpty()) {
                permissions.add("URL Schemes: " + String.join(", ", urlSchemes));
            }
            
            // 提取后台模式
            List<String> backgroundModes = extractBackgroundModes(content);
            if (!backgroundModes.isEmpty()) {
                permissions.add("后台模式: " + String.join(", ", backgroundModes));
            }
            
        } catch (IOException e) {
            log.warn("读取Info.plist失败: {}", infoPlistPath, e);
        }
        
        return permissions;
    }

    /**
     * 提取URL Schemes
     */
    private static List<String> extractUrlSchemes(String plistContent) {
        List<String> schemes = new ArrayList<>();
        
        Pattern pattern = Pattern.compile(
            "<key>CFBundleURLTypes</key>\\s*<array>(.*?)</array>", 
            Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(plistContent);
        
        if (matcher.find()) {
            String urlTypesContent = matcher.group(1);
            Pattern schemePattern = Pattern.compile("<string>([^<]+)</string>");
            Matcher schemeMatcher = schemePattern.matcher(urlTypesContent);
            
            while (schemeMatcher.find()) {
                String scheme = schemeMatcher.group(1);
                if (!scheme.startsWith("http") && !scheme.equals("file")) {
                    schemes.add(scheme);
                }
            }
        }
        
        return schemes;
    }

    /**
     * 提取后台模式
     */
    private static List<String> extractBackgroundModes(String plistContent) {
        List<String> modes = new ArrayList<>();
        
        Pattern pattern = Pattern.compile(
            "<key>UIBackgroundModes</key>\\s*<array>(.*?)</array>", 
            Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(plistContent);
        
        if (matcher.find()) {
            String modesContent = matcher.group(1);
            Pattern modePattern = Pattern.compile("<string>([^<]+)</string>");
            Matcher modeMatcher = modePattern.matcher(modesContent);
            
            while (modeMatcher.find()) {
                String mode = modeMatcher.group(1);
                modes.add(translateBackgroundMode(mode));
            }
        }
        
        return modes;
    }

    /**
     * 翻译后台模式
     */
    private static String translateBackgroundMode(String mode) {
        switch (mode) {
            case "audio":
                return "音频播放";
            case "location":
                return "位置更新";
            case "voip":
                return "网络电话";
            case "newsstand-content":
                return "报刊杂志下载";
            case "external-accessory":
                return "外部配件通信";
            case "bluetooth-central":
                return "蓝牙中心";
            case "bluetooth-peripheral":
                return "蓝牙外设";
            case "background-fetch":
                return "后台应用刷新";
            case "remote-notification":
                return "远程通知";
            case "background-processing":
                return "后台处理";
            default:
                return mode;
        }
    }

    /**
     * 检查Profile文件是否包含必要的权限
     */
    public static boolean validateProfilePermissions(Path profilePath, List<String> requiredPermissions) {
        try {
            if (!Files.exists(profilePath)) {
                return false;
            }
            
            String content = Files.readString(profilePath);
            
            // 检查Entitlements
            Pattern entitlementsPattern = Pattern.compile(
                "<key>Entitlements</key>\\s*<dict>(.*?)</dict>", 
                Pattern.DOTALL
            );
            Matcher matcher = entitlementsPattern.matcher(content);
            
            if (matcher.find()) {
                String entitlementsContent = matcher.group(1);
                
                // 检查是否包含必要的权限
                for (String permission : requiredPermissions) {
                    if (!entitlementsContent.contains(permission)) {
                        log.warn("Profile文件缺少权限: {}", permission);
                        return false;
                    }
                }
            }
            
            return true;
            
        } catch (IOException e) {
            log.error("验证Profile权限失败: {}", profilePath, e);
            return false;
        }
    }

    /**
     * 从Profile文件中提取Bundle ID
     */
    public static String extractBundleIdFromProfile(Path profilePath) {
        try {
            if (!Files.exists(profilePath)) {
                return null;
            }
            
            String content = Files.readString(profilePath);
            
            // 提取应用标识符
            Pattern pattern = Pattern.compile(
                "<key>application-identifier</key>\\s*<string>([^<]+)</string>"
            );
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                String appId = matcher.group(1);
                // 移除Team ID前缀 (格式: TEAM_ID.bundle.id)
                int dotIndex = appId.indexOf('.');
                if (dotIndex > 0) {
                    return appId.substring(dotIndex + 1);
                }
                return appId;
            }
            
        } catch (IOException e) {
            log.error("从Profile提取Bundle ID失败: {}", profilePath, e);
        }
        
        return null;
    }

    /**
     * 检查Profile文件是否支持通配符
     */
    public static boolean isWildcardProfile(Path profilePath) {
        String bundleId = extractBundleIdFromProfile(profilePath);
        return bundleId != null && bundleId.contains("*");
    }

    /**
     * 检查Bundle ID是否匹配Profile
     */
    public static boolean matchesBundleId(String bundleId, Path profilePath) {
        String profileBundleId = extractBundleIdFromProfile(profilePath);
        
        if (profileBundleId == null) {
            return false;
        }
        
        // 精确匹配
        if (profileBundleId.equals(bundleId)) {
            return true;
        }
        
        // 通配符匹配
        if (profileBundleId.endsWith("*")) {
            String prefix = profileBundleId.substring(0, profileBundleId.length() - 1);
            return bundleId.startsWith(prefix);
        }
        
        return false;
    }
}