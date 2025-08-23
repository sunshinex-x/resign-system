package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resign.entity.AndroidResignTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Android重签名任务Mapper接口
 */
@Mapper
public interface AndroidResignTaskMapper extends BaseMapper<AndroidResignTask> {

    /**
     * 根据任务状态查询任务列表
     */
    List<AndroidResignTask> selectByStatus(@Param("status") String status);

    /**
     * 根据证书ID查询任务列表
     */
    List<AndroidResignTask> selectByCertificateId(@Param("certificateId") Long certificateId);

    /**
     * 根据应用名称查询任务列表
     */
    List<AndroidResignTask> selectByAppName(@Param("appName") String appName);

    /**
     * 根据包名查询任务列表
     */
    List<AndroidResignTask> selectByPackageName(@Param("packageName") String packageName);

    /**
     * 根据创建人查询任务列表
     */
    List<AndroidResignTask> selectByCreateBy(@Param("createBy") String createBy);

    /**
     * 查询待处理的任务列表
     */
    List<AndroidResignTask> selectPendingTasks();
}