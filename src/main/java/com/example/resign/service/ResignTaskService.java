package com.example.resign.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.ResignTask;
import com.example.resign.model.dto.ResignTaskCreateDTO;
import com.example.resign.model.dto.ResignTaskDTO;
import com.example.resign.model.dto.ResignTaskQueryDTO;
import com.example.resign.model.vo.PackageInfoVO;
import com.example.resign.model.vo.ResignTaskVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 重签名任务服务接口
 */
public interface ResignTaskService {

    /**
     * 创建重签名任务
     *
     * @param resignTaskDTO 任务信息
     * @return 任务视图对象
     */
    ResignTaskVO createTask(ResignTaskDTO resignTaskDTO);

    /**
     * 根据任务ID查询任务
     *
     * @param taskId 任务ID
     * @return 任务视图对象
     */
    ResignTaskVO getTaskById(String taskId);

    /**
     * 处理重签名任务
     *
     * @param task 任务实体
     */
    void processTask(ResignTask task);

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 任务状态
     * @param resignedPackageUrl 重签名后的安装包URL
     * @param failReason 失败原因
     * @return 是否更新成功
     */
    boolean updateTaskStatus(String taskId, String status, String resignedPackageUrl, String failReason);

    /**
     * 重试失败的任务
     *
     * @param taskId 任务ID
     * @return 是否重试成功
     */
    boolean retryTask(String taskId);
    
    /**
     * 分页查询任务列表
     *
     * @param page 分页参数
     * @param appType 应用类型，可为空
     * @param status 任务状态，可为空
     * @return 分页结果
     */
    IPage<ResignTaskVO> pageTask(Page<ResignTask> page, String appType, String status);
    
    /**
     * 高级分页查询任务列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<ResignTaskVO> pageTaskAdvanced(ResignTaskQueryDTO queryDTO);
    
    /**
     * 统计各状态任务数量
     *
     * @return 状态统计结果
     */
    Map<String, Long> countTaskByStatus();
    
    /**
     * 批量删除任务
     *
     * @param taskIds 任务ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteTasks(List<String> taskIds);

    /**
     * 解析包信息
     *
     * @param file    安装包文件
     * @param appType 应用类型
     * @return 包信息
     */
    PackageInfoVO parsePackage(MultipartFile file, String appType);

    /**
     * 创建重签名任务（文件上传）
     *
     * @param createDTO 创建任务DTO
     * @return 任务视图对象
     */
    ResignTaskVO createTaskWithFiles(ResignTaskCreateDTO createDTO);

}