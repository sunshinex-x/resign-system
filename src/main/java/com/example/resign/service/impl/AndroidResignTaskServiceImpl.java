package com.example.resign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.AndroidCertificate;
import com.example.resign.entity.AndroidResignTask;
import com.example.resign.mapper.AndroidResignTaskMapper;
import com.example.resign.service.AndroidCertificateService;
import com.example.resign.service.AndroidResignTaskService;
import com.example.resign.service.ResignTaskMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Android重签名任务Service实现类
 */
@Slf4j
@Service
public class AndroidResignTaskServiceImpl implements AndroidResignTaskService {

    @Autowired
    private AndroidResignTaskMapper taskMapper;

    @Autowired
    private AndroidCertificateService certificateService;

    @Autowired
    private ResignTaskMessageService messageService;

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Value("${resign.script.android:/scripts/android_resign.sh}")
    private String androidResignScript;

    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z][a-zA-Z0-9_]*)*$");
    private static final Set<String> VALID_SIGNATURE_VERSIONS = Set.of("v1", "v2", "v1+v2");

    @Override
    public AndroidResignTask createResignTask(MultipartFile apkFile, Long certificateId,
                                            String packageName, String appName, String versionName,
                                            Integer versionCode, String signatureVersion,
                                            String callbackUrl, String description) {
        try {
            // 验证证书是否存在
            AndroidCertificate certificate = certificateService.getCertificateById(certificateId);
            if (certificate == null || !"ACTIVE".equals(certificate.getStatus())) {
                throw new RuntimeException("证书不存在或已禁用");
            }

            // 验证包名格式
            if (StringUtils.hasText(packageName) && !validatePackageName(packageName)) {
                throw new RuntimeException("包名格式不正确");
            }

            // 验证签名版本
            if (!validateSignatureVersion(signatureVersion)) {
                throw new RuntimeException("签名版本不支持，支持的版本: v1, v2, v1+v2");
            }

            // 保存原始APK文件
            String originalFileName = apkFile.getOriginalFilename();
            if (originalFileName == null || !originalFileName.endsWith(".apk")) {
                throw new RuntimeException("只支持APK格式的文件");
            }

            String fileName = UUID.randomUUID().toString() + ".apk";
            String filePath = uploadPath + "/apk/original/" + fileName;

            // 确保目录存在
            File dir = new File(uploadPath + "/apk/original/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File destFile = new File(filePath);
            apkFile.transferTo(destFile);

            // 解析APK文件信息
            Map<String, Object> apkInfo = parseApkInfo(apkFile);

            // 创建任务记录
            AndroidResignTask task = new AndroidResignTask();
            task.setTaskId(UUID.randomUUID().toString());
            task.setOriginalApkUrl(filePath);
            task.setCertificateId(certificateId);
            task.setPackageName(packageName != null ? packageName : (String) apkInfo.get("packageName"));
            task.setAppName(appName != null ? appName : (String) apkInfo.get("appName"));
            task.setAppVersion(versionName != null ? versionName : (String) apkInfo.get("versionName"));
            task.setPermissions((String) apkInfo.get("permissions"));
            task.setStatus("PENDING");
            task.setRetryCount(0);
            task.setOriginalFileSize(destFile.length());
            task.setCallbackUrl(callbackUrl);
            task.setDescription(description);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            // TODO: 从当前登录用户获取
            task.setCreateBy("system");
            task.setUpdateBy("system");

            // 保存到数据库
            taskMapper.insert(task);

            // 发送消息到队列进行异步处理
            messageService.sendAndroidResignTaskMessage(task.getId());

            log.info("Android重签名任务创建成功: {}", task.getTaskId());
            return task;

        } catch (IOException e) {
            log.error("创建重签名任务失败", e);
            throw new RuntimeException("创建重签名任务失败: " + e.getMessage());
        }
    }

    @Override
    public boolean executeResignTask(Long taskId) {
        AndroidResignTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return false;
        }

        if (!"PENDING".equals(task.getStatus()) && !"FAILED".equals(task.getStatus())) {
            log.warn("任务状态不允许执行: {}, 状态: {}", taskId, task.getStatus());
            return false;
        }

        // 异步执行重签名
        CompletableFuture.runAsync(() -> {
            executeResignTaskAsync(task);
        });

        return true;
    }

    private void executeResignTaskAsync(AndroidResignTask task) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 更新任务状态为处理中
            updateTaskStatus(task.getId(), "PROCESSING", null);

            // 获取证书信息
            AndroidCertificate certificate = certificateService.getCertificateById(task.getCertificateId());
            if (certificate == null) {
                throw new RuntimeException("证书不存在");
            }

            // 执行重签名脚本
            String outputFileName = "resigned_" + UUID.randomUUID().toString() + ".apk";
            String outputPath = uploadPath + "/apk/resigned/" + outputFileName;

            // 确保输出目录存在
            File outputDir = new File(uploadPath + "/apk/resigned/");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // 默认使用v1+v2签名
            boolean success = executeResignScript(
                task.getOriginalApkUrl(),
                outputPath,
                certificate.getFileUrl(),
                certificate.getPassword(),
                certificate.getKeyAlias(),
                certificate.getKeyPassword(),
                "v1+v2", // 默认签名版本
                task.getPackageName(),
                task.getAppName(),
                task.getAppVersion(),
                null // versionCode 字段在实体中不存在，传null
            );

            if (success) {
                // 更新任务状态为成功
                task.setResignedApkUrl(outputPath);
                task.setStatus("SUCCESS");
                task.setProcessingTime(System.currentTimeMillis() - startTime);
                task.setUpdateTime(LocalDateTime.now());
                taskMapper.updateById(task);

                log.info("Android重签名任务执行成功: {}", task.getTaskId());

                // 发送回调通知
                if (StringUtils.hasText(task.getCallbackUrl())) {
                    sendCallback(task);
                }
            } else {
                throw new RuntimeException("重签名脚本执行失败");
            }

        } catch (Exception e) {
            log.error("Android重签名任务执行失败: {}", task.getTaskId(), e);
            
            // 更新任务状态为失败
            task.setStatus("FAILED");
            task.setFailReason(e.getMessage());
            task.setRetryCount(task.getRetryCount() + 1);
            task.setProcessingTime(System.currentTimeMillis() - startTime);
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    private boolean executeResignScript(String originalApk, String outputApk, String certificateFile,
                                      String certificatePassword, String keyAlias, String keyPassword,
                                      String signatureVersion, String packageName, String appName,
                                      String versionName, Integer versionCode) {
        try {
            // 获取脚本文件路径
            ClassPathResource scriptResource = new ClassPathResource("scripts/android_resign.sh");
            String scriptPath;
            
            if (scriptResource.exists()) {
                // 如果在classpath中找到脚本，复制到临时目录
                Path tempScript = Files.createTempFile("android_resign", ".sh");
                Files.copy(scriptResource.getInputStream(), tempScript, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                tempScript.toFile().setExecutable(true);
                scriptPath = tempScript.toString();
            } else {
                // 使用绝对路径
                scriptPath = androidResignScript;
            }

            // 构建命令
            List<String> command = new ArrayList<>();
            command.add("/bin/bash");
            command.add(scriptPath);
            command.add(originalApk);
            command.add(outputApk);
            command.add(certificateFile);
            command.add(certificatePassword);
            command.add(keyAlias);
            command.add(keyPassword);
            command.add(signatureVersion);
            
            if (StringUtils.hasText(packageName)) {
                command.add(packageName);
            } else {
                command.add("");
            }
            
            if (StringUtils.hasText(appName)) {
                command.add(appName);
            } else {
                command.add("");
            }
            
            if (StringUtils.hasText(versionName)) {
                command.add(versionName);
            } else {
                command.add("");
            }
            
            if (versionCode != null) {
                command.add(versionCode.toString());
            }

            log.info("执行重签名命令: {}", String.join(" ", command));

            // 执行命令
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.info("脚本输出: {}", line);
                }
            }

            int exitCode = process.waitFor();
            log.info("重签名脚本执行完成，退出码: {}", exitCode);
            
            if (exitCode == 0) {
                // 检查输出文件是否存在
                File outputFile = new File(outputApk);
                if (outputFile.exists() && outputFile.length() > 0) {
                    log.info("重签名成功，输出文件: {}, 大小: {}", outputApk, outputFile.length());
                    return true;
                } else {
                    log.error("重签名失败，输出文件不存在或为空");
                    return false;
                }
            } else {
                log.error("重签名脚本执行失败，退出码: {}, 输出: {}", exitCode, output.toString());
                return false;
            }

        } catch (Exception e) {
            log.error("执行重签名脚本异常", e);
            return false;
        }
    }

    @Override
    public AndroidResignTask getTaskById(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    @Override
    public Page<AndroidResignTask> getTaskList(int page, int size, String status, Long certificateId,
                                             String appName, String createBy) {
        Page<AndroidResignTask> pageParam = new Page<>(page, size);
        QueryWrapper<AndroidResignTask> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (certificateId != null) {
            queryWrapper.eq("certificate_id", certificateId);
        }
        if (StringUtils.hasText(appName)) {
            queryWrapper.like("app_name", appName);
        }
        if (StringUtils.hasText(createBy)) {
            queryWrapper.eq("create_by", createBy);
        }
        
        queryWrapper.orderByDesc("create_time");
        return taskMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public List<AndroidResignTask> getTasksByStatus(String status) {
        return taskMapper.selectByStatus(status);
    }

    @Override
    public List<AndroidResignTask> getPendingTasks() {
        return taskMapper.selectPendingTasks();
    }

    @Override
    public boolean updateTaskStatus(Long taskId, String status, String failureReason) {
        AndroidResignTask task = new AndroidResignTask();
        task.setId(taskId);
        task.setStatus(status);
        task.setFailReason(failureReason);
        task.setUpdateTime(LocalDateTime.now());
        return taskMapper.updateById(task) > 0;
    }

    @Override
    public boolean retryTask(Long taskId) {
        AndroidResignTask task = taskMapper.selectById(taskId);
        if (task != null && "FAILED".equals(task.getStatus())) {
            task.setStatus("PENDING");
            task.setFailReason(null);
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
            
            // 重新执行任务
            return executeResignTask(taskId);
        }
        return false;
    }

    @Override
    public boolean cancelTask(Long taskId) {
        return updateTaskStatus(taskId, "CANCELLED", "用户取消");
    }

    @Override
    public boolean deleteTask(Long taskId) {
        AndroidResignTask task = taskMapper.selectById(taskId);
        if (task != null) {
            // 删除相关文件
            deleteTaskFiles(task);
            // 删除数据库记录
            return taskMapper.deleteById(taskId) > 0;
        }
        return false;
    }

    @Override
    public Map<String, Object> parseApkInfo(MultipartFile apkFile) {
        Map<String, Object> info = new HashMap<>();
        
        try (ZipInputStream zis = new ZipInputStream(apkFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("AndroidManifest.xml".equals(entry.getName())) {
                    // 解析AndroidManifest.xml文件
                    // 这里需要使用AAPT或其他工具解析二进制XML，简化处理
                    info.put("appName", "Unknown App");
                    info.put("packageName", "com.example.app");
                    info.put("versionName", "1.0.0");
                    info.put("versionCode", 1);
                    info.put("permissions", "");
                    break;
                }
            }
        } catch (IOException e) {
            log.error("解析APK文件失败", e);
        }
        
        return info;
    }

    @Override
    public boolean validatePackageName(String packageName) {
        return StringUtils.hasText(packageName) && PACKAGE_NAME_PATTERN.matcher(packageName).matches();
    }

    @Override
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        QueryWrapper<AndroidResignTask> queryWrapper = new QueryWrapper<>();
        stats.put("total", taskMapper.selectCount(queryWrapper));
        
        queryWrapper.eq("status", "SUCCESS");
        stats.put("success", taskMapper.selectCount(queryWrapper));
        
        queryWrapper.clear();
        queryWrapper.eq("status", "FAILED");
        stats.put("failed", taskMapper.selectCount(queryWrapper));
        
        queryWrapper.clear();
        queryWrapper.eq("status", "PENDING");
        stats.put("pending", taskMapper.selectCount(queryWrapper));
        
        queryWrapper.clear();
        queryWrapper.eq("status", "PROCESSING");
        stats.put("processing", taskMapper.selectCount(queryWrapper));
        
        return stats;
    }

    @Override
    public void processPendingTasks() {
        List<AndroidResignTask> pendingTasks = getPendingTasks();
        log.info("开始处理待处理任务，数量: {}", pendingTasks.size());
        
        for (AndroidResignTask task : pendingTasks) {
            try {
                executeResignTask(task.getId());
            } catch (Exception e) {
                log.error("处理任务失败: {}", task.getTaskId(), e);
            }
        }
    }

    @Override
    public void cleanupExpiredTasks(int daysToKeep) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(daysToKeep);
        QueryWrapper<AndroidResignTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("create_time", expireTime)
                   .in("status", "SUCCESS", "FAILED", "CANCELLED");
        
        List<AndroidResignTask> expiredTasks = taskMapper.selectList(queryWrapper);
        log.info("开始清理过期任务，数量: {}", expiredTasks.size());
        
        for (AndroidResignTask task : expiredTasks) {
            deleteTask(task.getId());
        }
    }

    @Override
    public byte[] downloadResignedApk(Long taskId) {
        AndroidResignTask task = taskMapper.selectById(taskId);
        if (task != null && StringUtils.hasText(task.getResignedApkUrl())) {
            try {
                Path filePath = Paths.get(task.getResignedApkUrl());
                return Files.readAllBytes(filePath);
            } catch (IOException e) {
                log.error("读取重签名文件失败: {}", task.getResignedApkUrl(), e);
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> getTaskProgress(Long taskId) {
        AndroidResignTask task = taskMapper.selectById(taskId);
        Map<String, Object> progress = new HashMap<>();
        
        if (task != null) {
            progress.put("taskId", task.getTaskId());
            progress.put("status", task.getStatus());
            progress.put("progress", getProgressPercentage(task.getStatus()));
            progress.put("message", getStatusMessage(task.getStatus()));
            progress.put("failureReason", task.getFailReason());
        }
        
        return progress;
    }

    @Override
    public boolean validateSignatureVersion(String signatureVersion) {
        return StringUtils.hasText(signatureVersion) && VALID_SIGNATURE_VERSIONS.contains(signatureVersion);
    }

    // 辅助方法
    private void sendCallback(AndroidResignTask task) {
        // TODO: 实现回调通知逻辑
        log.info("发送回调通知: {}, URL: {}", task.getTaskId(), task.getCallbackUrl());
    }

    private void deleteTaskFiles(AndroidResignTask task) {
        try {
            if (StringUtils.hasText(task.getOriginalApkUrl())) {
                Files.deleteIfExists(Paths.get(task.getOriginalApkUrl()));
            }
            if (StringUtils.hasText(task.getResignedApkUrl())) {
                Files.deleteIfExists(Paths.get(task.getResignedApkUrl()));
            }
        } catch (IOException e) {
            log.error("删除任务文件失败: {}", task.getTaskId(), e);
        }
    }

    private int getProgressPercentage(String status) {
        switch (status) {
            case "PENDING": return 0;
            case "PROCESSING": return 50;
            case "SUCCESS": return 100;
            case "FAILED": return 100;
            case "CANCELLED": return 100;
            default: return 0;
        }
    }

    private String getStatusMessage(String status) {
        switch (status) {
            case "PENDING": return "等待处理";
            case "PROCESSING": return "正在处理";
            case "SUCCESS": return "处理成功";
            case "FAILED": return "处理失败";
            case "CANCELLED": return "已取消";
            default: return "未知状态";
        }
    }
}