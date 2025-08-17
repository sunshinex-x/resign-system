package com.example.resign.service;

import com.example.resign.model.dto.PackageInfoDTO;
import com.example.resign.model.dto.PackageParseResultDTO;
import com.example.resign.model.dto.ResignRequestDTO;
import com.example.resign.model.dto.ResignResultDTO;
import com.example.resign.util.IosSigningTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * iOS重签名服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IosResignService {

    private final FileService fileService;
    private final IosPackageParseService iosPackageParseService;

    @Value("${resign.temp-dir:/tmp/resign}")
    private String tempDir;

    @Value("${resign.tools.ios.sign-tool:/usr/bin/codesign}")
    private String codeSignTool;

    /**
     * 检查文件是否为iOS应用包
     */
    public boolean isSupported(MultipartFile file) {
        return iosPackageParseService.isSupported(file);
    }
    
    /**
     * 解析iOS包信息V2，返回详细的Bundle ID信息
     */
    public PackageParseResultDTO parsePackageInfoV2(MultipartFile file) {
        return iosPackageParseService.parsePackageInfoV2(file);
    }

    /**
     * 执行iOS重签名
     */
    public ResignResultDTO resign(ResignRequestDTO request) {
        log.info("开始iOS重签名任务: {}", request.getTaskId());
        long startTime = System.currentTimeMillis();

        try {
            // 创建工作目录
            Path workDir = createWorkDirectory(request.getTaskId());
            
            // 保存原始文件
            Path originalIpa = saveOriginalFile(request.getOriginalPackageFile(), workDir);
            Path certificateFile = saveCertificateFile(request.getCertificateFile(), workDir);
            Map<String, Path> profileFiles = saveProfileFiles(request, workDir);

            // 解压IPA
            Path extractDir = workDir.resolve("extracted");
            extractIpa(originalIpa, extractDir);

            // 查找.app目录
            Path appDir = findAppDirectory(extractDir);
            if (appDir == null) {
                throw new RuntimeException("未找到.app目录");
            }

            // 替换Provisioning Profile
            replaceProvisioningProfiles(appDir, profileFiles);

            // 删除原有签名
            removeExistingSignature(appDir);

            // 重新签名
            resignApp(appDir, certificateFile, request.getCertificatePassword());

            // 重新打包IPA
            Path resignedIpa = repackageIpa(extractDir, workDir, request.getOutputFileName());

            // 上传到MinIO
            String objectName = "resigned/" + request.getTaskId() + "/" + resignedIpa.getFileName().toString();
            String fileUrl = uploadResignedFile(resignedIpa, objectName);

            // 清理工作目录
            cleanupWorkDirectory(workDir);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("iOS重签名完成，耗时: {}ms", processingTime);

            ResignResultDTO result = ResignResultDTO.success(fileUrl, objectName, Files.size(resignedIpa));
            result.setProcessingTime(processingTime);
            return result;

        } catch (Exception e) {
            log.error("iOS重签名失败", e);
            return ResignResultDTO.failure("iOS重签名失败: " + e.getMessage());
        }
    }

    /**
     * 验证证书文件
     */
    public boolean validateCertificate(MultipartFile certificateFile, String password) {
        try {
            // 创建临时文件
            Path tempCert = Files.createTempFile("ios_cert_", ".p12");
            certificateFile.transferTo(tempCert.toFile());

            // 使用openssl验证证书
            ProcessBuilder pb = new ProcessBuilder(
                "openssl", "pkcs12", "-info", "-in", tempCert.toString(),
                "-passin", "pass:" + password, "-noout"
            );
            Process process = pb.start();
            int exitCode = process.waitFor();

            // 清理临时文件
            Files.deleteIfExists(tempCert);

            return exitCode == 0;

        } catch (Exception e) {
            log.error("验证iOS证书失败", e);
            return false;
        }
    }

    /**
     * 验证Profile文件
     */
    public boolean validateProfile(MultipartFile profileFile, String bundleId) {
        try {
            // 创建临时文件
            Path tempProfile = Files.createTempFile("ios_profile_", ".mobileprovision");
            profileFile.transferTo(tempProfile.toFile());

            // 使用包解析服务验证
            boolean isValid = iosPackageParseService.matchesBundleId(bundleId, tempProfile);
            
            if (isValid) {
                log.info("Profile文件验证成功: {} 匹配 Bundle ID: {}", 
                        profileFile.getOriginalFilename(), bundleId);
            } else {
                log.warn("Profile文件验证失败: {} 不匹配 Bundle ID: {}", 
                        profileFile.getOriginalFilename(), bundleId);
            }

            // 清理临时文件
            Files.deleteIfExists(tempProfile);

            return isValid;

        } catch (Exception e) {
            log.error("验证iOS Profile文件失败", e);
            return false;
        }
    }

    // ==================== 私有方法 ====================

    private Path createWorkDirectory(String taskId) throws IOException {
        Path workDir = Paths.get(tempDir, "ios", taskId);
        Files.createDirectories(workDir);
        return workDir;
    }

    private Path saveOriginalFile(MultipartFile file, Path workDir) throws IOException {
        Path filePath = workDir.resolve("original.ipa");
        file.transferTo(filePath.toFile());
        return filePath;
    }

    private Path saveCertificateFile(MultipartFile file, Path workDir) throws IOException {
        Path filePath = workDir.resolve("certificate.p12");
        file.transferTo(filePath.toFile());
        return filePath;
    }

    private Map<String, Path> saveProfileFiles(ResignRequestDTO request, Path workDir) throws IOException {
        Map<String, Path> profileFiles = new HashMap<>();
        
        if (request.getBundleIdProfileMap() != null) {
            for (Map.Entry<String, MultipartFile> entry : request.getBundleIdProfileMap().entrySet()) {
                String bundleId = entry.getKey();
                MultipartFile profileFile = entry.getValue();
                
                Path filePath = workDir.resolve("profile_" + bundleId.replace(".", "_") + ".mobileprovision");
                profileFile.transferTo(filePath.toFile());
                profileFiles.put(bundleId, filePath);
            }
        }
        
        return profileFiles;
    }



    private void extractIpa(Path ipaFile, Path extractDir) throws IOException {
        Files.createDirectories(extractDir);
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(ipaFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = extractDir.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath);
                }
                zis.closeEntry();
            }
        }
    }

    private Path findAppDirectory(Path extractDir) throws IOException {
        return Files.walk(extractDir)
                .filter(path -> path.toString().endsWith(".app") && Files.isDirectory(path))
                .findFirst()
                .orElse(null);
    }

    private void replaceProvisioningProfiles(Path appDir, Map<String, Path> profileFiles) throws IOException {
        if (profileFiles.isEmpty()) {
            log.warn("没有提供Profile文件，跳过Profile替换");
            return;
        }
        
        // 1. 获取主应用的Bundle ID
        String mainBundleId = getMainBundleId(appDir);
        log.info("主应用Bundle ID: {}", mainBundleId);
        
        // 2. 替换主应用的embedded.mobileprovision
        Path mainProfile = findMatchingProfile(mainBundleId, profileFiles);
        if (mainProfile != null) {
            Path embeddedProfile = appDir.resolve("embedded.mobileprovision");
            Files.copy(mainProfile, embeddedProfile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.info("已替换主应用Profile: {}", mainBundleId);
        } else {
            log.warn("未找到主应用的匹配Profile: {}", mainBundleId);
            // 使用第一个可用的Profile作为备选
            if (!profileFiles.isEmpty()) {
                Path fallbackProfile = profileFiles.values().iterator().next();
                Path embeddedProfile = appDir.resolve("embedded.mobileprovision");
                Files.copy(fallbackProfile, embeddedProfile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.info("使用备选Profile替换主应用Profile");
            }
        }
        
        // 3. 替换插件的embedded.mobileprovision
        Path pluginsDir = appDir.resolve("PlugIns");
        if (Files.exists(pluginsDir)) {
            Files.walk(pluginsDir, 1)
                    .filter(path -> path.getFileName().toString().endsWith(".appex"))
                    .forEach(pluginDir -> {
                        try {
                            String pluginBundleId = getPluginBundleId(pluginDir);
                            log.info("处理插件Bundle ID: {}", pluginBundleId);
                            
                            Path pluginProfile = findMatchingProfile(pluginBundleId, profileFiles);
                            if (pluginProfile != null) {
                                Path pluginEmbeddedProfile = pluginDir.resolve("embedded.mobileprovision");
                                Files.copy(pluginProfile, pluginEmbeddedProfile, 
                                         java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                log.info("已替换插件Profile: {}", pluginBundleId);
                            } else {
                                log.warn("未找到插件的匹配Profile: {}", pluginBundleId);
                                // 对于插件，如果没有匹配的Profile，可以尝试使用通配符Profile
                                Path wildcardProfile = findWildcardProfile(profileFiles);
                                if (wildcardProfile != null) {
                                    Path pluginEmbeddedProfile = pluginDir.resolve("embedded.mobileprovision");
                                    Files.copy(wildcardProfile, pluginEmbeddedProfile, 
                                             java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                    log.info("使用通配符Profile替换插件Profile: {}", pluginBundleId);
                                }
                            }
                        } catch (IOException e) {
                            log.warn("替换插件Profile失败: {}", pluginDir, e);
                        }
                    });
        }
    }

    /**
     * 获取主应用的Bundle ID
     */
    private String getMainBundleId(Path appDir) {
        try {
            Path infoPlist = appDir.resolve("Info.plist");
            if (Files.exists(infoPlist)) {
                String content = Files.readString(infoPlist);
                return iosPackageParseService.extractPlistValue(content, "CFBundleIdentifier");
            }
        } catch (Exception e) {
            log.warn("获取主应用Bundle ID失败", e);
        }
        return null;
    }

    /**
     * 获取插件的Bundle ID
     */
    private String getPluginBundleId(Path pluginDir) {
        try {
            Path infoPlist = pluginDir.resolve("Info.plist");
            if (Files.exists(infoPlist)) {
                String content = Files.readString(infoPlist);
                return iosPackageParseService.extractPlistValue(content, "CFBundleIdentifier");
            }
        } catch (Exception e) {
            log.warn("获取插件Bundle ID失败: {}", pluginDir, e);
        }
        return null;
    }

    /**
     * 查找匹配的Profile文件
     */
    private Path findMatchingProfile(String bundleId, Map<String, Path> profileFiles) {
        if (bundleId == null) {
            return null;
        }
        
        // 首先尝试精确匹配
        for (Map.Entry<String, Path> entry : profileFiles.entrySet()) {
            if (iosPackageParseService.matchesBundleId(bundleId, entry.getValue())) {
                log.info("找到匹配的Profile: {} -> {}", bundleId, entry.getKey());
                return entry.getValue();
            }
        }
        
        // 如果没有精确匹配，查找通配符Profile
        for (Map.Entry<String, Path> entry : profileFiles.entrySet()) {
            if (iosPackageParseService.isWildcardProfile(entry.getValue())) {
                log.info("使用通配符Profile: {} -> {}", bundleId, entry.getKey());
                return entry.getValue();
            }
        }
        
        return null;
    }

    /**
     * 查找通配符Profile文件
     */
    private Path findWildcardProfile(Map<String, Path> profileFiles) {
        for (Map.Entry<String, Path> entry : profileFiles.entrySet()) {
            if (iosPackageParseService.isWildcardProfile(entry.getValue())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void removeExistingSignature(Path appDir) throws IOException {
        // 删除_CodeSignature目录
        Path codeSignDir = appDir.resolve("_CodeSignature");
        if (Files.exists(codeSignDir)) {
            Files.walk(codeSignDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("删除签名文件失败: {}", path, e);
                        }
                    });
        }
    }

    private void resignApp(Path appDir, Path certificateFile, String password) throws IOException, InterruptedException {
        // 提取证书身份
        String identity = extractCertificateIdentity(certificateFile, password);
        
        // 使用改进的签名工具
        IosSigningTool.signApplication(appDir, identity, password);
        
        // 验证签名
        if (!IosSigningTool.verifySignature(appDir)) {
            throw new RuntimeException("签名验证失败");
        }
        
        log.info("iOS应用签名和验证成功: {}", appDir);
    }
    
    /**
     * 提取证书身份
     */
    private String extractCertificateIdentity(Path certificateFile, String password) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
            "openssl", "pkcs12", "-in", certificateFile.toString(),
            "-passin", "pass:" + password, "-nokeys", "-clcerts"
        );
        
        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        boolean finished = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("提取证书身份超时");
        }
        
        if (process.exitValue() != 0) {
            throw new RuntimeException("提取证书身份失败");
        }
        
        // 从输出中提取证书名称
        String content = output.toString();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("subject=.*?CN=([^,/]+)");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String identity = matcher.group(1).trim();
            log.info("提取到证书身份: {}", identity);
            return identity;
        }
        
        throw new RuntimeException("无法从证书中提取身份信息");
    }

    private Path repackageIpa(Path extractDir, Path workDir, String outputFileName) throws IOException {
        Path outputIpa = workDir.resolve(outputFileName != null ? outputFileName : "resigned.ipa");
        
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputIpa))) {
            Files.walk(extractDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            String entryName = extractDir.relativize(path).toString();
                            ZipEntry entry = new ZipEntry(entryName);
                            zos.putNextEntry(entry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("打包IPA失败", e);
                        }
                    });
        }
        
        return outputIpa;
    }

    private String uploadResignedFile(Path resignedFile, String objectName) throws IOException {
        try (InputStream is = Files.newInputStream(resignedFile)) {
            return fileService.uploadFile(is, objectName, "application/octet-stream");
        }
    }

    private void cleanupWorkDirectory(Path workDir) {
        try {
            Files.walk(workDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("清理工作目录失败: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("清理工作目录失败: {}", workDir, e);
        }
    }
}