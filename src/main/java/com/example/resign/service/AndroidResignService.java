package com.example.resign.service;

import com.example.resign.model.dto.PackageInfoDTO;
import com.example.resign.model.dto.ResignRequestDTO;
import com.example.resign.model.dto.ResignResultDTO;
import com.example.resign.model.dto.PackageParseResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Android重签名服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AndroidResignService {

    private final FileService fileService;

    @Value("${resign.temp-dir:/tmp/resign}")
    private String tempDir;

    @Value("${resign.tools.android.aapt:/usr/local/bin/aapt}")
    private String aaptTool;

    @Value("${resign.tools.android.jarsigner:/usr/bin/jarsigner}")
    private String jarSignerTool;

    @Value("${resign.tools.android.zipalign:/usr/local/bin/zipalign}")
    private String zipAlignTool;
    
    @Value("${resign.tools.android.apksigner:/usr/local/bin/apksigner}")
    private String apkSignerTool;

    /**
     * 检查文件是否为Android应用包
     */
    public boolean isSupported(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }
        String fileName = file.getOriginalFilename().toLowerCase();
        return fileName.endsWith(".apk");
    }

    /**
     * 执行Android重签名
     */
    public ResignResultDTO resign(ResignRequestDTO request) {
        log.info("开始Android重签名任务: {}", request.getTaskId());
        long startTime = System.currentTimeMillis();

        try {
            // 创建工作目录
            Path workDir = createWorkDirectory(request.getTaskId());
            
            // 保存原始文件
            Path originalApk = saveOriginalFile(request.getOriginalPackageFile(), workDir);
            Path keystoreFile = saveKeystoreFile(request.getCertificateFile(), workDir);

            // 删除原有签名
            Path unsignedApk = removeExistingSignature(originalApk, workDir);

            // 重新签名（内部已包含对齐步骤）
            Path signedApk = resignApk(unsignedApk, keystoreFile, request.getCertificatePassword(), workDir);

            // 上传到MinIO
            String objectName = "resigned/" + request.getTaskId() + "/" + signedApk.getFileName().toString();
            String fileUrl = uploadResignedFile(signedApk, objectName);

            // 清理工作目录
            cleanupWorkDirectory(workDir);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Android重签名完成，耗时: {}ms", processingTime);

            ResignResultDTO result = ResignResultDTO.success(fileUrl, objectName, Files.size(signedApk));
            result.setProcessingTime(processingTime);
            return result;

        } catch (Exception e) {
            log.error("Android重签名失败", e);
            return ResignResultDTO.failure("Android重签名失败: " + e.getMessage());
        }
    }

    /**
     * 解析Android包信息
     */
    public PackageInfoDTO parsePackageInfo(MultipartFile file) {
        log.info("开始解析Android包信息: {}", file.getOriginalFilename());
        
        try {
            PackageInfoDTO packageInfo = new PackageInfoDTO();
            packageInfo.setAppType("ANDROID");
            packageInfo.setFileSize(file.getSize());

            // 创建临时文件
            Path tempFile = Files.createTempFile("android_parse_", ".apk");
            file.transferTo(tempFile.toFile());

            // 使用aapt解析APK信息
            ProcessBuilder pb = new ProcessBuilder(
                aaptTool, "dump", "badging", tempFile.toString()
            );
            
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            process.waitFor(10, TimeUnit.SECONDS);
            
            // 解析aapt输出
            String packageName = extractAaptValue(output, "package: name='([^']+)'");
            String versionName = extractAaptValue(output, "versionName='([^']+)'");
            String versionCode = extractAaptValue(output, "versionCode='([^']+)'");
            String appName = extractAaptValue(output, "application-label:'([^']+)'");
            
            packageInfo.setPackageName(packageName);
            packageInfo.setVersionName(versionName);
            packageInfo.setVersionCode(versionCode);
            packageInfo.setAppName(appName);
            
            // 清理临时文件
            Files.deleteIfExists(tempFile);
            
            log.info("Android包信息解析完成: {}", packageInfo.getAppName());
            return packageInfo;
            
        } catch (Exception e) {
            log.error("解析Android包信息失败", e);
            throw new RuntimeException("解析Android包信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 从aapt输出中提取值
     */
    private String extractAaptValue(String output, String pattern) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(output);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * 验证证书文件（Android使用keystore）
     */
    public boolean validateCertificate(MultipartFile certificateFile, String password) {
        try {
            // 创建临时文件
            Path tempKeystore = Files.createTempFile("android_keystore_", ".jks");
            certificateFile.transferTo(tempKeystore.toFile());

            // 使用keytool验证keystore
            ProcessBuilder pb = new ProcessBuilder(
                "keytool", "-list", "-keystore", tempKeystore.toString(),
                "-storepass", password
            );
            Process process = pb.start();
            int exitCode = process.waitFor();

            // 清理临时文件
            Files.deleteIfExists(tempKeystore);

            return exitCode == 0;

        } catch (Exception e) {
            log.error("验证Android证书失败", e);
            return false;
        }
    }

    // ==================== 私有方法 ====================

    private Path createWorkDirectory(String taskId) throws IOException {
        Path workDir = Paths.get(tempDir, "android", taskId);
        Files.createDirectories(workDir);
        return workDir;
    }

    private Path saveOriginalFile(MultipartFile file, Path workDir) throws IOException {
        Path filePath = workDir.resolve("original.apk");
        file.transferTo(filePath.toFile());
        return filePath;
    }

    private Path saveKeystoreFile(MultipartFile file, Path workDir) throws IOException {
        Path filePath = workDir.resolve("keystore.jks");
        file.transferTo(filePath.toFile());
        return filePath;
    }

    private Path removeExistingSignature(Path originalApk, Path workDir) throws IOException, InterruptedException {
        Path unsignedApk = workDir.resolve("unsigned.apk");
        
        // 使用zip命令删除META-INF目录
        ProcessBuilder pb = new ProcessBuilder(
            "zip", "-d", originalApk.toString(), "META-INF/*"
        );
        
        Process process = pb.start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("删除签名超时");
        }
        
        // 复制到unsigned.apk
        Files.copy(originalApk, unsignedApk, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        return unsignedApk;
    }

    private Path resignApk(Path unsignedApk, Path keystoreFile, String password, Path workDir) 
            throws IOException, InterruptedException {
        // 先进行zipalign对齐，再签名
        Path alignedApk = alignApk(unsignedApk, workDir);
        Path signedApk = workDir.resolve("signed.apk");
        
        // 复制对齐后的APK作为签名前的准备
        Files.copy(alignedApk, signedApk, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        // 使用apksigner进行签名，支持v1和v2签名
        ProcessBuilder pb = new ProcessBuilder(
            apkSignerTool,
            "sign",
            "--verbose",
            "--ks", keystoreFile.toString(),
            "--ks-pass", "pass:" + password,
            "--key-pass", "pass:" + password,
            "--v1-signing-enabled", "true",
            "--v2-signing-enabled", "true",
            "--out", signedApk.toString(),
            alignedApk.toString()
        );
        
        log.info("执行APK签名命令: {}", String.join(" ", pb.command()));
        
        Process process = pb.start();
        
        // 记录输出和错误信息
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        
        try (BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            
            String line;
            while ((line = outReader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            while ((line = errReader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }
        
        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("APK签名超时");
        }
        
        if (process.exitValue() != 0) {
            log.error("APK签名失败: {}\n{}", error.toString(), output.toString());
            throw new RuntimeException("APK签名失败: " + error.toString());
        }
        
        log.info("APK签名成功: {}\n{}", output.toString(), error.toString());
        return signedApk;
    }

    private Path alignApk(Path signedApk, Path workDir) throws IOException, InterruptedException {
        Path alignedApk = workDir.resolve("aligned.apk");
        
        // 使用zipalign对齐APK
        ProcessBuilder pb = new ProcessBuilder(
            zipAlignTool,
            "-v", "4",
            signedApk.toString(),
            alignedApk.toString()
        );
        
        Process process = pb.start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("APK对齐超时");
        }
        
        if (process.exitValue() != 0) {
            String error = new String(process.getErrorStream().readAllBytes());
            throw new RuntimeException("APK对齐失败: " + error);
        }
        
        return alignedApk;
    }

    private String uploadResignedFile(Path resignedFile, String objectName) throws IOException {
        try (InputStream is = Files.newInputStream(resignedFile)) {
            return fileService.uploadFile(is, objectName, "application/octet-stream");
        }
    }

    private void cleanupWorkDirectory(Path workDir) {
        try {
            Files.walk(workDir)
                    .sorted(java.util.Comparator.reverseOrder())
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