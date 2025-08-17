package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resign.entity.TaskProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务Profile关联Mapper
 */
@Mapper
public interface TaskProfileMapper extends BaseMapper<TaskProfile> {
}