package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resign.entity.IosResignTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * iOS重签名任务Mapper接口
 */
@Mapper
public interface IosResignTaskMapper extends BaseMapper<IosResignTask> {

    /**
     * 根据任务状态查询任务列表
     */
    List<IosResignTask> selectByStatus(@Param("status") String status);

    /**
     * 根据证书ID查询任务列表
     */
    List<IosResignTask> selectByCertificateId(@Param("certificateId") Long certificateId);

    /**
     * 根据应用名称查询任务列表
     */
    List<IosResignTask> selectByAppName(@Param("appName") String appName);

    /**
     * 根据创建人查询任务列表
     */
    List<IosResignTask> selectByCreateBy(@Param("createBy") String createBy);

    /**
     * 查询待处理的任务列表
     */
    List<IosResignTask> selectPendingTasks();
}