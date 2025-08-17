package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resign.entity.Profile;
import org.apache.ibatis.annotations.Mapper;

/**
 * Profile文件Mapper
 */
@Mapper
public interface ProfileMapper extends BaseMapper<Profile> {
}