package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resign.entity.AndroidCertificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Android证书Mapper接口
 */
@Mapper
public interface AndroidCertificateMapper extends BaseMapper<AndroidCertificate> {

    /**
     * 根据状态查询证书列表
     */
    List<AndroidCertificate> selectByStatus(@Param("status") String status);

    /**
     * 根据密钥别名查询证书
     */
    List<AndroidCertificate> selectByKeyAlias(@Param("keyAlias") String keyAlias);

    /**
     * 查询有效的证书列表（状态为ACTIVE且未过期）
     */
    List<AndroidCertificate> selectActiveCertificates();
}