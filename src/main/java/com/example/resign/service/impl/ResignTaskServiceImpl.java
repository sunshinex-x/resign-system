package com.example.resign.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resign.config.RabbitMQConfig;
import com.example.resign.entity.ResignTask;
import com.example.resign.entity.TaskBundleProfile;
import com.example.resign.enums.AppType;
import com.example.resign.enums.TaskStatus;
import com.example.resign.mapper.ResignTaskMapper;
import com.example.resign.mapper.TaskBundleProfileMapper;
import com.example.resign.model.dto.*;
import com.example.resign.model.vo.PackageInfoVO;
import com.example.resign.model.vo.ResignTaskVO;
import com.example.resign.entity.TaskProfile;
import com.example.resign.mapper.TaskProfileMapper;
import com.example.resign.service.IosResignService;
import com.example.resign.service.IosPackageParseService;
import com.example.resign.service.AndroidResignService;
import com.example.resign.config.DatabaseFallbackConfig;
import org.springframework.web.multipart.MultipartFile;
import com.example.resign.service.FileService;
import com.example.resign.service.ResignTaskService;
import com.example.resign.util.InMemoryMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 重签名任务服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResignTaskServiceImpl extends ServiceImpl<ResignTaskMapper, ResignTask> implements ResignTaskService {

    @Override
    public IPage<ResignTaskVO> pageTask(Page<ResignTask> page, String appType, String status) {
        // 构建查询条件
        LambdaQueryWrapper<ResignTask> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(appType)) {
            queryWrapper.eq(ResignTask::getAppType, appType);
        }
        if (StrUtil.isNotBlank(status)) {
            queryWrapper.eq(ResignTask::getStatus, status);
        }
        queryWrapper.orderByDesc(ResignTask::getCreateTime);
        
        // 执行分页查询
        Page<ResignTask> taskPage = page(page, queryWrapper);
        
        // 转换为VO对象
        IPage<ResignTaskVO> resultPage = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        List<ResignTaskVO> records = taskPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        resultPage.setRecords(records);
        
        return resultPage;
    }
    
    @Override
    public IPage<ResignTaskVO> pageTaskAdvanced(ResignTaskQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<ResignTask> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StrUtil.isNotBlank(queryDTO.getAppType())) {
            queryWrapper.eq(ResignTask::getAppType, queryDTO.getAppType());
        }
        if (StrUtil.isNotBlank(queryDTO.getStatus())) {
            queryWrapper.eq(ResignTask::getStatus, queryDTO.getStatus());
        }
        if (StrUtil.isNotBlank(queryDTO.getTaskId())) {
            queryWrapper.eq(ResignTask::getTaskId, queryDTO.getTaskId());
        }
        
        // 时间范围查询
        if (StrUtil.isNotBlank(queryDTO.getStartTime())) {
            queryWrapper.ge(ResignTask::getCreateTime, queryDTO.getStartTime());
        }
        if (StrUtil.isNotBlank(queryDTO.getEndTime())) {
            queryWrapper.le(ResignTask::getCreateTime, queryDTO.getEndTime());
        }
        
        // 默认按创建时间倒序
        queryWrapper.orderByDesc(ResignTask::getCreateTime);
        
        // 执行分页查询
        Page<ResignTask> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<ResignTask> taskPage = page(page, queryWrapper);
        
        // 转换为VO对象
        IPage<ResignTaskVO> resultPage = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        List<ResignTaskVO> records = taskPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        resultPage.setRecords(records);
        
        return resultPage;
    }

    private final RabbitTemplate rabbitTemplate;
    private final FileService fileService;
    private final IosResignService iosResignService;
    private final IosPackageParseService iosPackageParseService;
    private final AndroidResignService androidResignService;
    private final DatabaseFallbackConfig.DatabaseHealthChecker databaseHealthChecker;
    private final TaskProfileMapper taskProfileMapper;
    private final TaskBundleProfileMapper taskBundleProfileMapper;

    @Value("${resign.task.retry-count}")
    private Integer maxRetryCount;
    
    /**
     * 将实体转换为VO对象
     */
    private ResignTaskVO convertToVO(ResignTask task) {
        ResignTaskVO vo = new ResignTaskVO();
        BeanUtil.copyProperties(task, vo);
        return vo;
    }
    
    @Override
    public Map<String, Long> countTaskByStatus() {
        Map<String, Long> result = new HashMap<>();
        
        // 查询各状态的任务数量
        LambdaQueryWrapper<ResignTask> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(ResignTask::getStatus, TaskStatus.PENDING.getStatus());
        long pendingCount = count(pendingWrapper);
        
        LambdaQueryWrapper<ResignTask> processingWrapper = new LambdaQueryWrapper<>();
        processingWrapper.eq(ResignTask::getStatus, TaskStatus.PROCESSING.getStatus());
        long processingCount = count(processingWrapper);
        
        LambdaQueryWrapper<ResignTask> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.eq(ResignTask::getStatus, TaskStatus.SUCCESS.getStatus());
        long successCount = count(successWrapper);
        
        LambdaQueryWrapper<ResignTask> failedWrapper = new LambdaQueryWrapper<>();
        failedWrapper.eq(ResignTask::getStatus, TaskStatus.FAILED.getStatus());
        long failedCount = count(failedWrapper);
        
        // 查询总数
        long totalCount = count();
        
        // 按前端期望的格式返回
        result.put("totalCount", totalCount);
        result.put("pendingCount", pendingCount);
        result.put("processingCount", processingCount);
        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteTasks(List<String> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return false;
        }
        
        // 构建查询条件
        LambdaQueryWrapper<ResignTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ResignTask::getTaskId, taskIds);
        
        // 查询任务列表
        List<ResignTask> tasks = list(queryWrapper);
        if (tasks.isEmpty()) {
            return false;
        }
        
        // 删除任务
        boolean result = remove(queryWrapper);
        
        // 删除MinIO中的文件
        if (result) {
            tasks.forEach(task -> {
                try {
                    // 删除原始安装包
                    if (StrUtil.isNotBlank(task.getOriginalPackageUrl())) {
                        String objectName = fileService.getObjectNameFromUrl(task.getOriginalPackageUrl());
                        fileService.deleteFile(objectName);
                    }
                    
                    // 删除重签名后的安装包
                    if (StrUtil.isNotBlank(task.getResignedPackageUrl())) {
                        String objectName = fileService.getObjectNameFromUrl(task.getResignedPackageUrl());
                        fileService.deleteFile(objectName);
                    }
                    
                    // 删除证书
                    if (StrUtil.isNotBlank(task.getCertificateUrl())) {
                        String objectName = fileService.getObjectNameFromUrl(task.getCertificateUrl());
                        fileService.deleteFile(objectName);
                    }
                } catch (Exception e) {
                    log.error("删除任务文件失败: {}", task.getTaskId(), e);
                }
            });
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResignTaskVO createTask(ResignTaskDTO resignTaskDTO) {
        // 创建任务实体
        ResignTask task = new ResignTask();
        task.setTaskId(IdUtil.fastSimpleUUID());
        task.setAppType(resignTaskDTO.getAppType());
        task.setOriginalPackageUrl(resignTaskDTO.getOriginalPackageUrl());
        task.setCertificateUrl(resignTaskDTO.getCertificateUrl());
        task.setCertificatePassword(resignTaskDTO.getCertificatePassword());
        task.setStatus(TaskStatus.PENDING.getStatus());
        task.setRetryCount(0);
        task.setCallbackUrl(resignTaskDTO.getCallbackUrl());
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        // 保存任务
        save(task);

        // 发送消息到队列
        rabbitTemplate.convertAndSend(RabbitMQConfig.RESIGN_TASK_EXCHANGE, RabbitMQConfig.RESIGN_TASK_ROUTING_KEY, task.getTaskId());

        // 返回任务视图对象
        return convertToVO(task);
    }

    @Override
    public ResignTaskVO getTaskById(String taskId) {
        // 查询任务
        ResignTask task = getOne(new LambdaQueryWrapper<ResignTask>().eq(ResignTask::getTaskId, taskId));
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 返回任务视图对象
        return convertToVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTask(ResignTask task) {
        try {
            log.info("开始处理重签名任务: {}", task.getTaskId());
            
            // 更新任务状态为处理中
            updateTaskStatus(task.getTaskId(), TaskStatus.PROCESSING.getStatus(), null, null);

            // 根据应用类型选择对应的重签名服务
            AppType appType = AppType.fromString(task.getAppType());

            // 1. 从MinIO下载原始文件和证书文件
            MultipartFile originalPackageFile = downloadFileFromMinio(task.getOriginalPackageUrl());
            MultipartFile certificateFile = downloadFileFromMinio(task.getCertificateUrl());
            
            // 2. 构建ResignRequestDTO
            ResignRequestDTO resignRequest = new ResignRequestDTO();
            resignRequest.setTaskId(task.getTaskId());
            resignRequest.setAppType(task.getAppType());
            resignRequest.setOriginalPackageFile(originalPackageFile);
            resignRequest.setCertificateFile(certificateFile);
            resignRequest.setCertificatePassword(task.getCertificatePassword());
            
            // 3. 处理Profile文件（iOS需要）
            if (AppType.IOS.name().equals(task.getAppType())) {
                Map<String, MultipartFile> bundleIdProfileMap = downloadProfileFiles(task);
                resignRequest.setBundleIdProfileMap(bundleIdProfileMap);
                resignRequest.setBundleIds(new ArrayList<>(bundleIdProfileMap.keySet()));
            }
            
            // 4. 设置输出文件名
            String originalFileName = originalPackageFile.getOriginalFilename();
            String extension = originalFileName != null ? 
                originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
            resignRequest.setOutputFileName("resigned_" + task.getTaskId() + extension);

            // 5. 根据平台类型执行重签名
            ResignResultDTO result = executeResign(appType, resignRequest);
            
            // 6. 处理重签名结果
            if (result.isSuccess()) {
                // 更新任务状态为成功
                updateTaskStatus(task.getTaskId(), TaskStatus.SUCCESS.getStatus(), 
                               result.getResignedFileUrl(), null);
                log.info("重签名任务完成: {}, 文件URL: {}", task.getTaskId(), result.getResignedFileUrl());
            } else {
                // 更新任务状态为失败
                updateTaskStatus(task.getTaskId(), TaskStatus.FAILED.getStatus(), 
                               null, result.getErrorMessage());
                log.error("重签名任务失败: {}, 错误: {}", task.getTaskId(), result.getErrorMessage());
            }
            
            // 7. 发送回调通知
            sendCallback(task.getTaskId(), task.getCallbackUrl());

        } catch (Exception e) {
            log.error("处理重签名任务失败: {}", task.getTaskId(), e);

            // 更新任务状态为失败
            updateTaskStatus(task.getTaskId(), TaskStatus.FAILED.getStatus(), null, e.getMessage());

            // 如果重试次数未达到最大值，则重新发送到队列
            if (task.getRetryCount() < maxRetryCount) {
                try {
                    rabbitTemplate.convertAndSend(RabbitMQConfig.RESIGN_TASK_EXCHANGE, 
                                                RabbitMQConfig.RESIGN_TASK_ROUTING_KEY, task.getTaskId());
                } catch (Exception mqException) {
                    log.error("发送重试消息到队列失败: {}", task.getTaskId(), mqException);
                }
            } else {
                // 发送失败回调通知
                sendCallback(task.getTaskId(), task.getCallbackUrl());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTaskStatus(String taskId, String status, String resignedPackageUrl, String failReason) {
        // 查询任务
        ResignTask task = getOne(new LambdaQueryWrapper<ResignTask>().eq(ResignTask::getTaskId, taskId));
        if (task == null) {
            return false;
        }

        // 更新任务状态
        task.setStatus(status);
        if (StrUtil.isNotBlank(resignedPackageUrl)) {
            task.setResignedPackageUrl(resignedPackageUrl);
        }
        if (StrUtil.isNotBlank(failReason)) {
            task.setFailReason(failReason);
        }
        if (TaskStatus.PROCESSING.getStatus().equals(status)) {
            task.setRetryCount(task.getRetryCount() + 1);
        }
        task.setUpdateTime(LocalDateTime.now());

        // 更新任务
        return updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean retryTask(String taskId) {
        // 查询任务
        ResignTask task = getOne(new LambdaQueryWrapper<ResignTask>().eq(ResignTask::getTaskId, taskId));
        if (task == null) {
            return false;
        }

        // 只有失败状态的任务才能重试
        if (!TaskStatus.FAILED.getStatus().equals(task.getStatus())) {
            return false;
        }

        // 重置任务状态
        task.setStatus(TaskStatus.PENDING.getStatus());
        task.setRetryCount(0);
        task.setFailReason(null);
        task.setUpdateTime(LocalDateTime.now());

        // 更新任务
        boolean updated = updateById(task);
        if (updated) {
            // 发送消息到队列
            rabbitTemplate.convertAndSend(RabbitMQConfig.RESIGN_TASK_EXCHANGE, RabbitMQConfig.RESIGN_TASK_ROUTING_KEY, task.getTaskId());
        }

        return updated;
    }

    /**
     * 发送回调通知
     *
     * @param taskId      任务ID
     * @param callbackUrl 回调URL
     */
    private void sendCallback(String taskId, String callbackUrl) {
        if (StrUtil.isBlank(callbackUrl)) {
            return;
        }

        try {
            // 查询最新任务状态
            ResignTask task = getOne(new LambdaQueryWrapper<ResignTask>().eq(ResignTask::getTaskId, taskId));
            if (task == null) {
                return;
            }

            // 构建回调参数
            Map<String, Object> params = new HashMap<>();
            params.put("taskId", task.getTaskId());
            params.put("status", task.getStatus());
            params.put("originalPackageUrl", task.getOriginalPackageUrl());
            params.put("resignedPackageUrl", task.getResignedPackageUrl());
            params.put("failReason", task.getFailReason());

            // 发送HTTP请求
            HttpUtil.post(callbackUrl, params, 10000);
        } catch (Exception e) {
            log.error("发送回调通知失败: {}", callbackUrl, e);
        }
    }

    @Override
    public PackageInfoVO parsePackage(MultipartFile file) {
        try {
            log.info("开始解析包信息: {}", file.getOriginalFilename());
            
            // 自动检测应用类型并解析包信息
            PackageInfoDTO packageInfoDTO = parsePackageByType(file);
            
            // 转换为VO
            PackageInfoVO packageInfoVO = new PackageInfoVO();
            BeanUtil.copyProperties(packageInfoDTO, packageInfoVO);
            
            log.info("包信息解析完成: 应用类型={}, 应用名称={}", 
                    packageInfoDTO.getAppType(), packageInfoDTO.getAppName());
            
            return packageInfoVO;
            
        } catch (Exception e) {
            log.error("解析包信息失败", e);
            throw new RuntimeException("解析包信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResignTaskVO createTaskWithFiles(ResignTaskCreateDTO createDTO) {
        try {
            log.info("开始创建重签名任务");
            
            // 自动检测应用类型
            String appType = detectAppType(createDTO.getOriginalPackageFile());
            
            // 验证证书文件
            if (!validateCertificateByType(appType, createDTO.getCertificateFile(), createDTO.getCertificatePassword())) {
                throw new RuntimeException("证书文件验证失败，请检查证书和密码");
            }
            
            // 上传原始文件
            String originalPackageUrl = uploadFile(createDTO.getOriginalPackageFile(), "packages");
            String certificateUrl = uploadFile(createDTO.getCertificateFile(), "certificates");
            
            // 处理Profile文件上传（iOS需要）
            String profileFileUrl = null;
            String bundleId = null;
            if (createDTO.getProfileFiles() != null && !createDTO.getProfileFiles().isEmpty()) {
                // 暂时只处理第一个Profile文件，兼容现有数据库结构
                MultipartFile profileFile = createDTO.getProfileFiles().get(0);
                List<String> bundleIdList = createDTO.getBundleIds();
                
                bundleId = (bundleIdList != null && !bundleIdList.isEmpty()) ? 
                    bundleIdList.get(0) : "unknown.bundle.id";
                
                // 上传Profile文件
                profileFileUrl = uploadFile(profileFile, "profiles");
            }
            
            // 创建任务实体
            ResignTask task = new ResignTask();
            task.setTaskId(IdUtil.fastSimpleUUID());
            task.setAppType(appType);
            task.setOriginalPackageUrl(originalPackageUrl);
            task.setCertificateUrl(certificateUrl);
            task.setCertificatePassword(createDTO.getCertificatePassword());
            task.setProvisioningProfileUrl(profileFileUrl);
            task.setBundleId(bundleId);
            task.setDescription(createDTO.getDescription());
            task.setStatus(TaskStatus.PENDING.getStatus());
            task.setRetryCount(0);
            task.setCallbackUrl(createDTO.getCallbackUrl());
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            // 保存任务
            save(task);

            // 发送消息到队列进行异步处理
            rabbitTemplate.convertAndSend(RabbitMQConfig.RESIGN_TASK_EXCHANGE, 
                                        RabbitMQConfig.RESIGN_TASK_ROUTING_KEY, task.getTaskId());

            log.info("重签名任务创建成功: {}", task.getTaskId());
            
            // 返回任务视图对象
            return convertToVO(task);
            
        } catch (Exception e) {
            log.error("创建重签名任务失败", e);
            throw new RuntimeException("创建重签名任务失败: " + e.getMessage());
        }
    }

    /**
     * 解析iOS包信息
     */
    private PackageInfoVO parseIosPackage(MultipartFile file, PackageInfoVO packageInfo) {
        // 这里应该实现IPA文件的解析逻辑
        // 由于需要复杂的解析库，这里提供一个简化的实现
        packageInfo.setAppName("iOS应用");
        packageInfo.setPackageName("com.example.ios");
        packageInfo.setVersionName("1.0.0");
        packageInfo.setVersionCode("1");
        packageInfo.setMinSdkVersion("iOS 12.0");
        packageInfo.setTargetSdkVersion("iOS 16.0");
        packageInfo.setSignature("iOS签名信息");
        packageInfo.setPermissions(Arrays.asList("相机", "相册", "位置"));
        
        return packageInfo;
    }

    /**
     * 解析Android包信息
     */
    private PackageInfoVO parseAndroidPackage(MultipartFile file, PackageInfoVO packageInfo) {
        // 这里应该实现APK文件的解析逻辑
        // 由于需要复杂的解析库，这里提供一个简化的实现
        packageInfo.setAppName("Android应用");
        packageInfo.setPackageName("com.example.android");
        packageInfo.setVersionName("1.0.0");
        packageInfo.setVersionCode("1");
        packageInfo.setMinSdkVersion("21");
        packageInfo.setTargetSdkVersion("33");
        packageInfo.setSignature("Android签名信息");
        packageInfo.setPermissions(Arrays.asList("CAMERA", "READ_EXTERNAL_STORAGE", "ACCESS_FINE_LOCATION"));
        
        return packageInfo;
    }
    
    /**
     * 上传文件到MinIO
     */
    private String uploadFile(MultipartFile file, String folder) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String objectName = folder + "/" + IdUtil.fastSimpleUUID() + extension;
            
            return fileService.uploadFile(file.getInputStream(), objectName, file.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 从MinIO下载文件并转换为MultipartFile
     */
    private MultipartFile downloadFileFromMinio(String fileUrl) {
        try {
            String objectName = fileService.getObjectNameFromUrl(fileUrl);
            InputStream inputStream = fileService.downloadFile(objectName);
            
            // 创建临时文件
            String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
            return new InMemoryMultipartFile(fileName, inputStream);
            
        } catch (Exception e) {
            throw new RuntimeException("从MinIO下载文件失败: " + fileUrl, e);
        }
    }
    
    /**
     * 下载Profile文件（iOS和鸿蒙）
     */
    private Map<String, MultipartFile> downloadProfileFiles(ResignTask task) {
        Map<String, MultipartFile> profileFiles = new HashMap<>();
        
        try {
            // 首先从TaskBundleProfile表中获取所有的Bundle ID和Profile文件URL映射
            LambdaQueryWrapper<TaskBundleProfile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TaskBundleProfile::getTaskId, task.getTaskId());
            queryWrapper.isNotNull(TaskBundleProfile::getProfileUrl);
            
            List<TaskBundleProfile> bundleProfiles = taskBundleProfileMapper.selectList(queryWrapper);
            
            if (bundleProfiles != null && !bundleProfiles.isEmpty()) {
                // 从TaskBundleProfile表中获取映射关系
                for (TaskBundleProfile bundleProfile : bundleProfiles) {
                    String profileUrl = bundleProfile.getProfileUrl();
                    String bundleId = bundleProfile.getBundleId();
                    
                    if (StrUtil.isNotBlank(profileUrl) && StrUtil.isNotBlank(bundleId)) {
                        try {
                            MultipartFile profileFile = downloadFileFromMinio(profileUrl);
                            profileFiles.put(bundleId, profileFile);
                            log.info("已下载Profile文件: taskId={}, bundleId={}", task.getTaskId(), bundleId);
                        } catch (Exception e) {
                            log.warn("下载Profile文件失败: taskId={}, bundleId={}", task.getTaskId(), bundleId, e);
                        }
                    }
                }
            } else {
                // 兼容旧版本：如果TaskBundleProfile表中没有数据，尝试使用ResignTask中的字段
                if (StrUtil.isNotBlank(task.getProvisioningProfileUrl()) && StrUtil.isNotBlank(task.getBundleId())) {
                    MultipartFile profileFile = downloadFileFromMinio(task.getProvisioningProfileUrl());
                    profileFiles.put(task.getBundleId(), profileFile);
                    log.info("使用旧版字段下载Profile文件: taskId={}, bundleId={}", task.getTaskId(), task.getBundleId());
                }
            }
        } catch (Exception e) {
            log.error("下载Profile文件过程中发生错误: {}", task.getTaskId(), e);
        }
        
        return profileFiles;
    }
    
    /**
     * 根据平台类型执行重签名
     */
    private ResignResultDTO executeResign(AppType appType, ResignRequestDTO request) {
        switch (appType) {
            case IOS:
                return iosResignService.resign(request);
            case ANDROID:
                return androidResignService.resign(request);
            default:
                throw new RuntimeException("不支持的应用类型: " + appType);
        }
    }

    /**
     * 根据文件类型解析包信息
     */
    private PackageInfoDTO parsePackageByType(MultipartFile file) {
        // 检测文件类型
        if (iosPackageParseService.isSupported(file)) {
            return iosPackageParseService.parsePackageInfo(file);
        } else if (androidResignService.isSupported(file)) {
            return androidResignService.parsePackageInfo(file);
        } else {
            throw new RuntimeException("无法识别的应用包类型: " + file.getOriginalFilename());
        }
    }

    /**
     * 检测应用类型
     */
    private String detectAppType(MultipartFile file) {
        if (iosPackageParseService.isSupported(file)) {
            return AppType.IOS.name();
        } else if (androidResignService.isSupported(file)) {
            return AppType.ANDROID.name();
        } else {
            throw new RuntimeException("无法识别的应用包类型: " + file.getOriginalFilename());
        }
    }
    
    /**
     * 根据应用类型验证证书
     */
    private boolean validateCertificateByType(String appType, MultipartFile certificateFile, String password) {
        switch (AppType.fromString(appType)) {
            case IOS:
                return iosResignService.validateCertificate(certificateFile, password);
            case ANDROID:
                return androidResignService.validateCertificate(certificateFile, password);
            default:
                return false;
        }
    }
    
    @Override
    public PackageParseResultDTO parsePackageV2(MultipartFile file) {
        try {
            log.info("开始解析包信息V2: {}", file.getOriginalFilename());
            
            // 检测应用类型
            String appType = detectAppType(file);
            
            PackageParseResultDTO result = new PackageParseResultDTO();
            result.setAppType(appType);
            
            // 根据应用类型解析包信息
            if (AppType.IOS.name().equals(appType)) {
                parseIosPackageV2(file, result);
            }
            
            log.info("包信息解析完成V2: 应用类型={}, Bundle ID数量={}", 
                    result.getAppType(), result.getBundleInfos().size());
            
            return result;
            
        } catch (Exception e) {
            log.error("解析包信息失败V2", e);
            throw new RuntimeException("解析包信息失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResignTaskVO createTaskV2(MultipartFile originalPackageFile, MultipartFile certificateFile, 
                                   String certificatePassword, String callbackUrl, String description) {
        try {
            log.info("开始创建重签名任务V2");
            
            // 解析包信息
            PackageParseResultDTO parseResult = parsePackageV2(originalPackageFile);
            
            // 验证证书文件
            if (!validateCertificateByType(parseResult.getAppType(), certificateFile, certificatePassword)) {
                throw new RuntimeException("证书文件验证失败，请检查证书和密码");
            }
            
            // 上传原始文件和证书
            String originalPackageUrl = uploadFile(originalPackageFile, "packages");
            String certificateUrl = uploadFile(certificateFile, "certificates");
            
            // 创建任务实体
            ResignTask task = new ResignTask();
            task.setTaskId(IdUtil.fastSimpleUUID());
            task.setAppType(parseResult.getAppType());
            task.setOriginalPackageUrl(originalPackageUrl);
            task.setCertificateUrl(certificateUrl);
            task.setCertificatePassword(certificatePassword);
            task.setDescription(description);
            task.setStatus(TaskStatus.PENDING.getStatus());
            task.setRetryCount(0);
            task.setCallbackUrl(callbackUrl);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            
            // 兼容旧版本：如果只有一个Bundle ID，也设置到ResignTask中
            if (parseResult.getBundleInfos() != null && !parseResult.getBundleInfos().isEmpty()) {
                // 设置主应用的Bundle ID到任务实体中（兼容旧版本）
                PackageParseResultDTO.BundleInfo mainBundleInfo = parseResult.getBundleInfos().stream()
                    .filter(bundleInfo -> Boolean.TRUE.equals(bundleInfo.getIsMainApp()))
                    .findFirst()
                    .orElse(parseResult.getBundleInfos().get(0));
                
                task.setBundleId(mainBundleInfo.getBundleId());
            }

            // 保存任务
            save(task);
            
            // 保存Bundle ID信息（等待用户上传Profile文件）
            if (parseResult.getBundleInfos() != null) {
                for (PackageParseResultDTO.BundleInfo bundleInfo : parseResult.getBundleInfos()) {
                    TaskBundleProfile bundleProfile = new TaskBundleProfile();
                    bundleProfile.setTaskId(task.getTaskId());
                    bundleProfile.setBundleId(bundleInfo.getBundleId());
                    bundleProfile.setAppName(bundleInfo.getAppName());
                    bundleProfile.setVersion(bundleInfo.getVersion());
                    // profileUrl暂时为空，等待用户上传
                    
                    taskBundleProfileMapper.insert(bundleProfile);
                    
                    log.info("已保存Bundle ID信息: taskId={}, bundleId={}, isMainApp={}", 
                             task.getTaskId(), bundleInfo.getBundleId(), bundleInfo.getIsMainApp());
                }
            }

            log.info("重签名任务V2创建成功: {}", task.getTaskId());
            
            return convertToVO(task);
            
        } catch (Exception e) {
            log.error("创建重签名任务V2失败", e);
            throw new RuntimeException("创建重签名任务失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addBundleProfile(String taskId, String bundleId, MultipartFile profileFile) {
        try {
            log.info("为任务添加Bundle Profile: taskId={}, bundleId={}", taskId, bundleId);
            
            // 查询任务
            ResignTask task = getOne(new LambdaQueryWrapper<ResignTask>().eq(ResignTask::getTaskId, taskId));
            if (task == null) {
                throw new RuntimeException("任务不存在: " + taskId);
            }
            
            // 上传Profile文件
            String profileUrl = uploadFile(profileFile, "profiles");
            
            // 更新Bundle Profile记录
            LambdaQueryWrapper<TaskBundleProfile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TaskBundleProfile::getTaskId, taskId);
            queryWrapper.eq(TaskBundleProfile::getBundleId, bundleId);
            
            TaskBundleProfile bundleProfile = taskBundleProfileMapper.selectOne(queryWrapper);
            if (bundleProfile == null) {
                throw new RuntimeException("Bundle ID不存在: " + bundleId);
            }
            
            bundleProfile.setProfileUrl(profileUrl);
            taskBundleProfileMapper.updateById(bundleProfile);
            
            // 检查是否所有Bundle ID都已上传Profile文件
            LambdaQueryWrapper<TaskBundleProfile> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(TaskBundleProfile::getTaskId, taskId);
            checkWrapper.isNull(TaskBundleProfile::getProfileUrl);
            
            long pendingCount = taskBundleProfileMapper.selectCount(checkWrapper);
            
            // 如果所有Profile文件都已上传，则开始执行重签名任务
            if (pendingCount == 0) {
                log.info("所有Profile文件已上传完成，开始执行重签名任务: {}", taskId);
                rabbitTemplate.convertAndSend(RabbitMQConfig.RESIGN_TASK_EXCHANGE, 
                                            RabbitMQConfig.RESIGN_TASK_ROUTING_KEY, taskId);
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("添加Bundle Profile失败: taskId={}, bundleId={}", taskId, bundleId, e);
            throw new RuntimeException("添加Bundle Profile失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析iOS包信息V2
     */
    private void parseIosPackageV2(MultipartFile file, PackageParseResultDTO result) {
        try {
            // 直接使用IosPackageParseService的parsePackageInfoV2方法获取真实的包解析结果
            PackageParseResultDTO parseResult = iosPackageParseService.parsePackageInfoV2(file);
            
            // 将解析结果复制到result对象
            result.setAppName(parseResult.getAppName());
            result.setVersion(parseResult.getVersion());
            result.setBundleInfos(parseResult.getBundleInfos());
            
            log.info("iOS包解析完成: 应用名称={}, 版本={}, Bundle ID数量={}", 
                    result.getAppName(), result.getVersion(), result.getBundleInfos().size());
        } catch (Exception e) {
            log.error("解析iOS包信息失败", e);
            throw new RuntimeException("解析iOS包信息失败: " + e.getMessage());
        }
    }
}