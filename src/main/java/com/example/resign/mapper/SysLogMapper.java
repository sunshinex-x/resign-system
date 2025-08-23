package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统日志Mapper接口
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {

    /**
     * 分页查询系统日志
     *
     * @param page 分页参数
     * @param level 日志级别
     * @param keyword 关键词（搜索message、logger、username）
     * @param module 操作模块
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    IPage<SysLog> selectLogPage(Page<SysLog> page,
                                @Param("level") String level,
                                @Param("keyword") String keyword,
                                @Param("module") String module,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);

    /**
     * 根据日志级别统计数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> selectLogStatistics(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的日志
     *
     * @param beforeTime 时间点
     * @return 删除数量
     */
    int deleteLogsBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 批量插入日志
     *
     * @param logs 日志列表
     * @return 插入数量
     */
    int batchInsert(@Param("logs") List<SysLog> logs);
}