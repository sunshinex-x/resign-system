package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resign.entity.IosProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * iOS Profile Mapper接口
 */
@Mapper
public interface IosProfileMapper extends BaseMapper<IosProfile> {

    /**
     * 根据证书ID查询Profile列表
     */
    List<IosProfile> selectByCertificateId(@Param("certificateId") Long certificateId);

    /**
     * 根据Bundle ID查询Profile列表
     */
    List<IosProfile> selectByBundleId(@Param("bundleId") String bundleId);

    /**
     * 根据状态查询Profile列表
     */
    List<IosProfile> selectByStatus(@Param("status") String status);

    /**
     * 根据团队ID查询Profile列表
     */
    List<IosProfile> selectByTeamId(@Param("teamId") String teamId);

    /**
     * 根据Profile类型查询Profile列表
     */
    List<IosProfile> selectByProfileType(@Param("profileType") String profileType);
}