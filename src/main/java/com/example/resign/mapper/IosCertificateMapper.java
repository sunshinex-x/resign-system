package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resign.entity.IosCertificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * iOS证书Mapper接口
 */
@Mapper
public interface IosCertificateMapper extends BaseMapper<IosCertificate> {

    /**
     * 根据状态查询证书列表
     */
    List<IosCertificate> selectByStatus(@Param("status") String status);

    /**
     * 根据团队ID查询证书列表
     */
    List<IosCertificate> selectByTeamId(@Param("teamId") String teamId);

    /**
     * 根据证书类型查询证书列表
     */
    List<IosCertificate> selectByCertificateType(@Param("certificateType") String certificateType);
}