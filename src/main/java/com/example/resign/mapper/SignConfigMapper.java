package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resign.entity.SignConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 签名配置 Mapper接口
 */
@Mapper
public interface SignConfigMapper extends BaseMapper<SignConfig> {

    /**
     * 根据应用类型查询配置列表
     */
    List<SignConfig> selectByAppType(@Param("appType") String appType);

    /**
     * 根据包名和应用类型查询配置
     */
    SignConfig selectByPackageNameAndType(@Param("packageName") String packageName, @Param("appType") String appType);

    /**
     * 根据状态查询配置列表
     */
    List<SignConfig> selectByStatus(@Param("status") Integer status);

    /**
     * 根据应用名称模糊查询
     */
    List<SignConfig> selectByAppNameLike(@Param("appName") String appName);
}