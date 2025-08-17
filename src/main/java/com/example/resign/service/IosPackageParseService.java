package com.example.resign.service;

import com.example.resign.model.dto.PackageInfoDTO;
import com.example.resign.model.dto.PackageParseResultDTO;
import com.example.resign.util.IosPermissionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * iOS包解析服务
 * 专门负责解析iOS应用包（.ipa文件）的信息
 */
@Slf4j
@Service
public class IosPackageParseService {

    /**
     * 检查文件是否为iOS应用包
     */
    public boolean isSupported(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }
        String fileName = file.getOriginalFilename().toLowerCase();
        return fileName.endsWith(".ipa");
    }

    /**
     * 解析iOS包信息
     */
    public PackageInfoDTO parsePackageInfo(MultipartFile file) {
        log.info("开始解析iOS包信息: {}", file.getOriginalFilename());

        try {
            PackageInfoDTO packageInfo = new PackageInfoDTO();
            packageInfo.setAppType("IOS");
            packageInfo.setFileSize(file.getSize());

            // 创建临时文件
            Path tempFile = Files.createTempFile("ios_parse_", ".ipa");
            file.transferTo(tempFile.toFile());

            try (ZipFile zipFile = new ZipFile(tempFile.toFile())) {
                log.info("ZIP文件打开成功，条目数量: {}", zipFile.size());

                // 查找Info.plist文件
                ZipEntry infoPlistEntry = findInfoPlistEntry(zipFile);
                if (infoPlistEntry != null) {
                    log.info("找到Info.plist文件: {}", infoPlistEntry.getName());
                    parseInfoPlist(zipFile, infoPlistEntry, packageInfo);
                } else {
                    log.warn("未找到Info.plist文件，列出所有条目:");
                    zipFile.stream().limit(20).forEach(entry ->
                        log.warn("  - {}", entry.getName()));
                }

                // 查找embedded.mobileprovision文件获取Bundle IDs
                List<String> bundleIds = extractBundleIds(zipFile);
                packageInfo.setBundleIds(bundleIds);
                log.info("解析到Bundle IDs: {}", bundleIds);

                if (!bundleIds.isEmpty()) {
                    packageInfo.setPackageName(bundleIds.get(0)); // 主Bundle ID
                }
            }

            // 清理临时文件
            Files.deleteIfExists(tempFile);

            log.info("iOS包信息解析完成: {}", packageInfo.getAppName());
            return packageInfo;

        } catch (Exception e) {
            log.error("解析iOS包信息失败", e);
            throw new RuntimeException("解析iOS包信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析iOS包信息V2，返回详细的Bundle ID信息
     */
    public PackageParseResultDTO parsePackageInfoV2(MultipartFile file) {
        log.info("开始解析iOS包信息V2: {}", file.getOriginalFilename());
        
        try {
            PackageParseResultDTO result = new PackageParseResultDTO();
            result.setAppType("IOS");

            // 创建临时文件
            Path tempFile = Files.createTempFile("ios_parse_v2_", ".ipa");
            file.transferTo(tempFile.toFile());

            try (ZipFile zipFile = new ZipFile(tempFile.toFile())) {
                // 查找主应用的Info.plist文件
                ZipEntry mainInfoPlistEntry = findInfoPlistEntry(zipFile);
                if (mainInfoPlistEntry != null) {
                    try (InputStream is = zipFile.getInputStream(mainInfoPlistEntry)) {
                        String content = new String(is.readAllBytes());
                        String appName = extractPlistValue(content, "CFBundleDisplayName");
                        if (appName == null) {
                            appName = extractPlistValue(content, "CFBundleName");
                        }
                        result.setAppName(appName);
                        
                        String versionName = extractPlistValue(content, "CFBundleShortVersionString");
                        String versionCode = extractPlistValue(content, "CFBundleVersion");
                        result.setVersion(versionName != null ? versionName : versionCode);
                    }
                }

                // 解析所有Bundle ID信息
                List<PackageParseResultDTO.BundleInfo> bundleInfos = new ArrayList<>();
                
                // 1. 解析主应用Bundle ID
                if (mainInfoPlistEntry != null) {
                    try (InputStream is = zipFile.getInputStream(mainInfoPlistEntry)) {
                        String content = new String(is.readAllBytes());
                        String mainBundleId = extractPlistValue(content, "CFBundleIdentifier");
                        if (mainBundleId != null) {
                            PackageParseResultDTO.BundleInfo mainInfo = new PackageParseResultDTO.BundleInfo();
                            mainInfo.setBundleId(mainBundleId);
                            mainInfo.setAppName(result.getAppName());
                            mainInfo.setVersion(result.getVersion());
                            mainInfo.setIsMainApp(true);
                            bundleInfos.add(mainInfo);
                        }
                    } catch (Exception e) {
                        log.warn("解析主应用Bundle ID失败", e);
                    }
                }
                
                // 2. 解析插件Bundle ID
                zipFile.stream()
                        .filter(entry -> entry.getName().contains("PlugIns/") && 
                                      entry.getName().endsWith("Info.plist"))
                        .forEach(entry -> {
                            try (InputStream is = zipFile.getInputStream(entry)) {
                                String content = new String(is.readAllBytes());
                                String bundleId = extractPlistValue(content, "CFBundleIdentifier");
                                String pluginName = extractPlistValue(content, "CFBundleDisplayName");
                                if (pluginName == null) {
                                    pluginName = extractPlistValue(content, "CFBundleName");
                                }
                                String pluginVersion = extractPlistValue(content, "CFBundleShortVersionString");
                                if (pluginVersion == null) {
                                    pluginVersion = extractPlistValue(content, "CFBundleVersion");
                                }
                                
                                if (bundleId != null) {
                                    // 检查是否已存在相同的Bundle ID
                                    boolean exists = bundleInfos.stream()
                                            .anyMatch(info -> bundleId.equals(info.getBundleId()));
                                    
                                    if (!exists) {
                                        PackageParseResultDTO.BundleInfo pluginInfo = new PackageParseResultDTO.BundleInfo();
                                        pluginInfo.setBundleId(bundleId);
                                        pluginInfo.setAppName(pluginName != null ? pluginName : "插件");
                                        pluginInfo.setVersion(pluginVersion != null ? pluginVersion : result.getVersion());
                                        pluginInfo.setIsMainApp(false);
                                        bundleInfos.add(pluginInfo);
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("解析插件Bundle ID失败", e);
                            }
                        });
                
                result.setBundleInfos(bundleInfos);
            }

            // 清理临时文件
            Files.deleteIfExists(tempFile);

            log.info("iOS包信息V2解析完成: {}, 发现 {} 个Bundle ID", result.getAppName(), result.getBundleInfos().size());
            return result;

        } catch (Exception e) {
            log.error("解析iOS包信息V2失败", e);
            throw new RuntimeException("解析iOS包信息V2失败: " + e.getMessage());
        }
    }

    /**
     * 查找主应用的Info.plist文件
     */
    private ZipEntry findInfoPlistEntry(ZipFile zipFile) {
        // 查找Payload/AppName.app/Info.plist文件
        return zipFile.stream()
                .filter(entry -> {
                    String name = entry.getName();
                    return name.startsWith("Payload/") && 
                           name.endsWith(".app/Info.plist") && 
                           !entry.isDirectory();
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 解析Info.plist文件内容
     */
    private void parseInfoPlist(ZipFile zipFile, ZipEntry entry, PackageInfoDTO packageInfo) {
        try (InputStream is = zipFile.getInputStream(entry)) {
            log.info("正在解析Info.plist文件: {}", entry.getName());
            
            // 读取文件内容
            byte[] bytes = is.readAllBytes();
            String content = new String(bytes);
            
            // 检查是否为二进制plist
            if (bytes.length > 8 && bytes[0] == 'b' && bytes[1] == 'p' && bytes[2] == 'l' && bytes[3] == 'i') {
                log.warn("检测到二进制plist格式，当前仅支持XML格式的plist文件");
                return;
            }
            
            log.debug("Info.plist内容长度: {} 字符", content.length());
            
            // 提取应用名称
            String appName = extractPlistValue(content, "CFBundleDisplayName");
            if (appName == null) {
                appName = extractPlistValue(content, "CFBundleName");
            }
            packageInfo.setAppName(appName);
            log.info("解析到应用名称: {}", appName);
            
            // 提取版本信息
            String versionName = extractPlistValue(content, "CFBundleShortVersionString");
            String versionCode = extractPlistValue(content, "CFBundleVersion");
            packageInfo.setVersionName(versionName);
            packageInfo.setVersionCode(versionCode);
            log.info("解析到版本信息: versionName={}, versionCode={}", versionName, versionCode);
            
            // 提取Bundle ID
            String bundleId = extractPlistValue(content, "CFBundleIdentifier");
            if (bundleId != null) {
                packageInfo.setPackageName(bundleId);
                log.info("解析到Bundle ID: {}", bundleId);
            }
            
            // 提取权限信息
            try {
                Path tempInfoPlist = Files.createTempFile("info_", ".plist");
                Files.write(tempInfoPlist, content.getBytes());
                List<String> permissions = IosPermissionHandler.extractPermissions(tempInfoPlist);
                packageInfo.setPermissions(permissions);
                Files.deleteIfExists(tempInfoPlist);
                log.info("解析到权限数量: {}", permissions != null ? permissions.size() : 0);
            } catch (Exception e) {
                log.warn("提取权限信息失败", e);
            }
            
        } catch (Exception e) {
            log.error("解析Info.plist失败: {}", entry.getName(), e);
        }
    }

    /**
     * 从plist内容中提取指定key的值
     */
    public String extractPlistValue(String content, String key) {
        try {
            // 尝试多种模式匹配
            String[] patterns = {
                "<key>" + key + "</key>\\s*<string>([^<]+)</string>",
                "<key>" + key + "</key>\\s*<string>\\s*([^<\\s]+)\\s*</string>",
                "<key>" + key + "</key>[\\s\\n]*<string>([^<]*)</string>"
            };
            
            for (String pattern : patterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher m = p.matcher(content);
                if (m.find()) {
                    String value = m.group(1).trim();
                    if (!value.isEmpty()) {
                        return value;
                    }
                }
            }
            
            log.debug("未找到key: {} 的值", key);
            return null;
        } catch (Exception e) {
            log.warn("解析plist值失败, key: {}", key, e);
            return null;
        }
    }

    /**
     * 提取所有Bundle ID
     */
    private List<String> extractBundleIds(ZipFile zipFile) {
        List<String> bundleIds = new ArrayList<>();
        
        // 从主应用的Info.plist提取主Bundle ID
        ZipEntry infoPlistEntry = findInfoPlistEntry(zipFile);
        if (infoPlistEntry != null) {
            try (InputStream is = zipFile.getInputStream(infoPlistEntry)) {
                String content = new String(is.readAllBytes());
                String mainBundleId = extractPlistValue(content, "CFBundleIdentifier");
                if (mainBundleId != null) {
                    bundleIds.add(mainBundleId);
                }
            } catch (Exception e) {
                log.warn("提取主Bundle ID失败", e);
            }
        }
        
        // 从插件中提取额外的Bundle ID
        zipFile.stream()
                .filter(entry -> entry.getName().contains("PlugIns/") && 
                               entry.getName().endsWith("Info.plist"))
                .forEach(entry -> {
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        String content = new String(is.readAllBytes());
                        String bundleId = extractPlistValue(content, "CFBundleIdentifier");
                        if (bundleId != null && !bundleIds.contains(bundleId)) {
                            bundleIds.add(bundleId);
                        }
                    } catch (Exception e) {
                        log.warn("提取插件Bundle ID失败", e);
                    }
                });
        
        return bundleIds;
    }
    
    /**
     * 检查Bundle ID是否匹配Profile文件
     */
    public boolean matchesBundleId(String bundleId, Path profileFile) {
        return IosPermissionHandler.matchesBundleId(bundleId, profileFile);
    }
    
    /**
     * 检查Profile文件是否为通配符Profile
     */
    public boolean isWildcardProfile(Path profileFile) {
        return IosPermissionHandler.isWildcardProfile(profileFile);
    }
}