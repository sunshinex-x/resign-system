package com.example.resign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.IosCertificate;
import com.example.resign.entity.IosProfile;
import com.example.resign.entity.IosResignTask;
import com.example.resign.mapper.IosResignTaskMapper;
import com.example.resign.service.IosCertificateService;
import com.example.resign.service.IosProfileService;
import com.example.resign.service.IosResignTaskService;
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
 * iOS重签名任务Service实现类
 */
@Slf4j
@Service
public class IosResignTaskServiceImpl implements IosResignTaskService {

    @Autowired
    private IosResignTaskMapper taskMapper;

    @Autowired
    private IosCertificateService certificateService;

    @Autowired
    private IosProfileService profileService;

    @Autowired
    private ResignTaskMessageService messageService;

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Value("${resign.script.ios:/scripts/ios_resign.sh}")
    private String iosResignScript;

    private static final Pattern BUNDLE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+$");

    @Override
    public IosResignTask createResignTask(MultipartFile ipaFile, Long certificateId,
                                        String bundleId, String appName, String appVersion,
                                        String buildVersion, String callbackUrl, String description) {
        try {
            // 验证证书是否存在
            IosCertificate certificate = certificateService.getCertificateById(certificateId);
            if (certificate == null || !"ACTIVE".equals(certificate.getStatus())) {
                throw new RuntimeException("证书不存在或已禁用");
            }

            // 验证Bundle ID格式
            if (StringUtils.hasText(bundleId) && !validateBundleId(bundleId)) {
                throw new RuntimeException("Bundle ID格式不正确");
            }

            // 保存原始IPA文件
            String originalFileName = ipaFile.getOriginalFilename();
            if (originalFileName == null || !originalFileName.endsWith(".ipa")) {
                throw new RuntimeException("只支持IPA格式的文件");
            }

            String fileName = UUID.randomUUID().toString() + ".ipa";
            String filePath = uploadPath + "/ipa/original/" + fileName;

            // 确保目录存在
            File dir = new File(uploadPath + "/ipa/original/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File destFile = new File(filePath);
            ipaFile.transferTo(destFile);

            // 解析IPA文件信息
            Map<String, Object> ipaInfo = parseIpaInfo(ipaFile);

            // 创建任务记录
            IosResignTask task = new IosResignTask();
            task.setTaskId(UUID.randomUUID().toString());
            task.setOriginalIpaUrl(filePath);
            task.setCertificateId(certificateId);
            task.setBundleConfig(bundleId);
            task.setAppName(appName != null ? appName : (String) ipaInfo.get("appName"));
            task.setAppVersion(appVersion != null ? appVersion : (String) ipaInfo.get("appVersion"));
            task.setAppBuild(buildVersion != null ? buildVersion : (String) ipaInfo.get("buildVersion"));
            task.setBundleIds((String) ipaInfo.get("bundleIds"));
            task.setPermissions((String) ipaInfo.get("permissions"));
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
            messageService.sendIosResignTaskMessage(task.getId());

            log.info("iOS重签名任务创建成功: {}", task.getTaskId());
            return task;

        } catch (IOException e) {
            log.error("创建重签名任务失败", e);
            throw new RuntimeException("创建重签名任务失败: " + e.getMessage());
        }
    }

    @Override
    public boolean executeResignTask(Long taskId) {
        IosResignTask task = taskMapper.selectById(taskId);
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

    private void executeResignTaskAsync(IosResignTask task) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 更新任务状态为处理中
            updateTaskStatus(task.getId(), "PROCESSING", null);

            // 获取证书信息
            IosCertificate certificate = certificateService.getCertificateById(task.getCertificateId());
            if (certificate == null) {
                throw new RuntimeException("证书不存在");
            }

            // 查找匹配的Profile
            String bundleId = StringUtils.hasText(task.getBundleConfig()) ? 
                            task.getBundleConfig() : extractMainBundleId(task.getBundleIds());
            
            List<IosProfile> profiles = profileService.getProfilesByCertificateId(certificate.getId());
            IosProfile matchingProfile = findMatchingProfile(bundleId, profiles);
            
            if (matchingProfile == null) {
                throw new RuntimeException("找不到匹配的Provisioning Profile");
            }

            // 执行重签名脚本
            String outputFileName = "resigned_" + UUID.randomUUID().toString() + ".ipa";
            String outputPath = uploadPath + "/ipa/resigned/" + outputFileName;

            // 确保输出目录存在
            File outputDir = new File(uploadPath + "/ipa/resigned/");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            boolean success = executeResignScript(
                task.getOriginalIpaUrl(),
                outputPath,
                certificate.getFileUrl(),
                certificate.getPassword(),
                matchingProfile.getFileUrl(),
                task.getBundleConfig(),
                task.getAppName(),
                task.getAppVersion(),
                task.getAppBuild()
            );

            if (success) {
                // 更新任务状态为成功
                task.setResignedIpaUrl(outputPath);
                task.setStatus("SUCCESS");
                task.setProcessingTime(System.currentTimeMillis() - startTime);
                task.setUpdateTime(LocalDateTime.now());
                taskMapper.updateById(task);

                log.info("iOS重签名任务执行成功: {}", task.getTaskId());

                // 发送回调通知
                if (StringUtils.hasText(task.getCallbackUrl())) {
                    sendCallback(task);
                }
            } else {
                throw new RuntimeException("重签名脚本执行失败");
            }

        } catch (Exception e) {
            log.error("iOS重签名任务执行失败: {}", task.getTaskId(), e);
            
            // 更新任务状态为失败
            task.setStatus("FAILED");
            task.setFailReason(e.getMessage());
            task.setRetryCount(task.getRetryCount() + 1);
            task.setProcessingTime(System.currentTimeMillis() - startTime);
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    private boolean executeResignScript(String originalIpa, String outputIpa, String certificateFile,
                                      String certificatePassword, String profileFile, String bundleId,
                                      String appName, String appVersion, String buildVersion) {
        try {
            // 获取脚本文件路径
            ClassPathResource scriptResource = new ClassPathResource("scripts/ios_resign.sh");
            String scriptPath;
            
            if (scriptResource.exists()) {
                // 如果在classpath中找到脚本，复制到临时目录
                Path tempScript = Files.createTempFile("ios_resign", ".sh");
                Files.copy(scriptResource.getInputStream(), tempScript, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                tempScript.toFile().setExecutable(true);
                scriptPath = tempScript.toString();
            } else {
                // 使用绝对路径
                scriptPath = iosResignScript;
            }

            // 构建命令
            List<String> command = new ArrayList<>();
            command.add("/bin/bash");
            command.add(scriptPath);
            command.add(originalIpa);
            command.add(outputIpa);
            command.add(certificateFile);
            command.add(certificatePassword);
            command.add(profileFile);
            
            if (StringUtils.hasText(bundleId)) {
                command.add(bundleId);
            } else {
                command.add("");
            }
            
            if (StringUtils.hasText(appName)) {
                command.add(appName);
            } else {
                command.add("");
            }
            
            if (StringUtils.hasText(appVersion)) {
                command.add(appVersion);
            } else {
                command.add("");
            }
            
            if (StringUtils.hasText(buildVersion)) {
                command.add(buildVersion);
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
                File outputFile = new File(outputIpa);
                if (outputFile.exists() && outputFile.length() > 0) {
                    log.info("重签名成功，输出文件: {}, 大小: {}", outputIpa, outputFile.length());
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
    public IosResignTask getTaskById(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    @Override
    public Page<IosResignTask> getTaskList(int page, int size, String status, Long certificateId,
                                         String appName, String createBy) {
        Page<IosResignTask> pageParam = new Page<>(page, size);
        QueryWrapper<IosResignTask> queryWrapper = new QueryWrapper<>();
        
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
    public List<IosResignTask> getTasksByStatus(String status) {
        return taskMapper.selectByStatus(status);
    }

    @Override
    public List<IosResignTask> getPendingTasks() {
        return taskMapper.selectPendingTasks();
    }

    @Override
    public boolean updateTaskStatus(Long taskId, String status, String failureReason) {
        IosResignTask task = new IosResignTask();
        task.setId(taskId);
        task.setStatus(status);
        task.setFailReason(failureReason);
        task.setUpdateTime(LocalDateTime.now());
        return taskMapper.updateById(task) > 0;
    }

    @Override
    public boolean retryTask(Long taskId) {
        IosResignTask task = taskMapper.selectById(taskId);
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
        IosResignTask task = taskMapper.selectById(taskId);
        if (task != null) {
            // 删除相关文件
            deleteTaskFiles(task);
            // 删除数据库记录
            return taskMapper.deleteById(taskId) > 0;
        }
        return false;
    }

    @Override
    public Map<String, Object> parseIpaInfo(MultipartFile ipaFile) {
        Map<String, Object> info = new HashMap<>();
        
        try (ZipInputStream zis = new ZipInputStream(ipaFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith("/Info.plist") && entry.getName().contains(".app/")) {
                    // 解析Info.plist文件
                    // 这里需要使用plist解析库，简化处理
                    info.put("appName", "Unknown App");
                    info.put("appVersion", "1.0.0");
                    info.put("buildVersion", "1");
                    info.put("bundleIds", "com.example.app");
                    info.put("permissions", "");
                    break;
                }
            }
        } catch (IOException e) {
            log.error("解析IPA文件失败", e);
        }
        
        return info;
    }

    @Override
    public boolean validateBundleId(String bundleId) {
        return StringUtils.hasText(bundleId) && BUNDLE_ID_PATTERN.matcher(bundleId).matches();
    }

    @Override
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        QueryWrapper<IosResignTask> queryWrapper = new QueryWrapper<>();
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
        List<IosResignTask> pendingTasks = getPendingTasks();
        log.info("开始处理待处理任务，数量: {}", pendingTasks.size());
        
        for (IosResignTask task : pendingTasks) {
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
        QueryWrapper<IosResignTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("create_time", expireTime)
                   .in("status", "SUCCESS", "FAILED", "CANCELLED");
        
        List<IosResignTask> expiredTasks = taskMapper.selectList(queryWrapper);
        log.info("开始清理过期任务，数量: {}", expiredTasks.size());
        
        for (IosResignTask task : expiredTasks) {
            deleteTask(task.getId());
        }
    }

    @Override
    public byte[] downloadResignedIpa(Long taskId) {
        IosResignTask task = taskMapper.selectById(taskId);
        if (task != null && StringUtils.hasText(task.getResignedIpaUrl())) {
            try {
                Path filePath = Paths.get(task.getResignedIpaUrl());
                return Files.readAllBytes(filePath);
            } catch (IOException e) {
                log.error("读取重签名文件失败: {}", task.getResignedIpaUrl(), e);
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> getTaskProgress(Long taskId) {
        IosResignTask task = taskMapper.selectById(taskId);
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

    // 辅助方法
    private String extractMainBundleId(String bundleIdList) {
        if (StringUtils.hasText(bundleIdList)) {
            String[] bundleIds = bundleIdList.split(",");
            return bundleIds[0].trim();
        }
        return null;
    }

    private IosProfile findMatchingProfile(String bundleId, List<IosProfile> profiles) {
        if (!StringUtils.hasText(bundleId) || profiles.isEmpty()) {
            return null;
        }
        
        // 精确匹配
        for (IosProfile profile : profiles) {
            if (bundleId.equals(profile.getBundleId())) {
                return profile;
            }
        }
        
        // 通配符匹配
        for (IosProfile profile : profiles) {
            String profileBundleId = profile.getBundleId();
            if (profileBundleId != null && profileBundleId.endsWith("*")) {
                String prefix = profileBundleId.substring(0, profileBundleId.length() - 1);
                if (bundleId.startsWith(prefix)) {
                    return profile;
                }
            }
        }
        
        return null;
    }

    private void sendCallback(IosResignTask task) {
        // TODO: 实现回调通知逻辑
        log.info("发送回调通知: {}, URL: {}", task.getTaskId(), task.getCallbackUrl());
    }

    private void deleteTaskFiles(IosResignTask task) {
        try {
            if (StringUtils.hasText(task.getOriginalIpaUrl())) {
                Files.deleteIfExists(Paths.get(task.getOriginalIpaUrl()));
            }
            if (StringUtils.hasText(task.getResignedIpaUrl())) {
                Files.deleteIfExists(Paths.get(task.getResignedIpaUrl()));
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