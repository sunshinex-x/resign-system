package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resign.entity.ResignTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 重签名任务数据访问接口
 */
@Mapper
public interface ResignTaskMapper extends BaseMapper<ResignTask> {
}