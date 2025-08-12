package com.example.resign.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resign.config.RabbitMQConfig;
import com.example.resign.entity.ResignTask;
import com.example.resign.enums.AppType;
import com.example.resign.enums.TaskStatus;
import com.example.resign.mapper.ResignTaskMapper;
import com.example.resign.model.dto.ResignTaskDTO;
import com.example.resign.model.dto.ResignTaskQueryDTO;
import com.example.resign.model.vo.ResignTaskVO;
import com.example.resign.service.FileService;
import com.example.resign.service.ResignService;
import com.example.resign.service.ResignTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

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
    private final ResignService resignService;

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
        
        // 获取所有状态枚举值
        Arrays.stream(TaskStatus.values()).forEach(status -> {
            // 构建查询条件
            LambdaQueryWrapper<ResignTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ResignTask::getStatus, status.name());
            
            // 查询数量
            long count = count(queryWrapper);
            result.put(status.name(), count);
        });
        
        // 查询总数
        long total = count();
        result.put("TOTAL", total);
        
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
            // 更新任务状态为处理中
            updateTaskStatus(task.getTaskId(), TaskStatus.PROCESSING.getStatus(), null, null);

            // 下载原始安装包和证书
            InputStream packageInputStream = fileService.downloadFileFromUrl(task.getOriginalPackageUrl());
            InputStream certificateInputStream = fileService.downloadFileFromUrl(task.getCertificateUrl());

            // 根据应用类型进行重签名
            InputStream resignedInputStream;
            String contentType;
            String fileExtension;

            AppType appType = AppType.fromString(task.getAppType());
            switch (appType) {
                case IOS:
                    resignedInputStream = resignService.resignIosApp(packageInputStream, certificateInputStream, task.getCertificatePassword());
                    contentType = "application/octet-stream";
                    fileExtension = ".ipa";
                    break;
                case ANDROID:
                    resignedInputStream = resignService.resignAndroidApp(packageInputStream, certificateInputStream, task.getCertificatePassword());
                    contentType = "application/vnd.android.package-archive";
                    fileExtension = ".apk";
                    break;
                case HARMONY:
                    resignedInputStream = resignService.resignHarmonyApp(packageInputStream, certificateInputStream, task.getCertificatePassword());
                    contentType = "application/octet-stream";
                    fileExtension = ".hap";
                    break;
                default:
                    throw new RuntimeException("不支持的应用类型: " + task.getAppType());
            }

            // 上传重签名后的安装包到MinIO
            String objectName = "resigned/" + task.getTaskId() + fileExtension;
            String resignedPackageUrl = fileService.uploadFile(resignedInputStream, objectName, contentType);

            // 更新任务状态为成功
            updateTaskStatus(task.getTaskId(), TaskStatus.SUCCESS.getStatus(), resignedPackageUrl, null);

            // 发送回调通知
            sendCallback(task.getTaskId(), task.getCallbackUrl());
        } catch (Exception e) {
            log.error("处理重签名任务失败: {}", task.getTaskId(), e);

            // 更新任务状态为失败
            updateTaskStatus(task.getTaskId(), TaskStatus.FAILED.getStatus(), null, e.getMessage());

            // 如果重试次数未达到最大值，则重新发送到队列
            if (task.getRetryCount() < maxRetryCount) {
                rabbitTemplate.convertAndSend(RabbitMQConfig.RESIGN_TASK_EXCHANGE, RabbitMQConfig.RESIGN_TASK_ROUTING_KEY, task.getTaskId());
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

    // 已在123行定义了相同的convertToVO方法，此处删除重复定义
}