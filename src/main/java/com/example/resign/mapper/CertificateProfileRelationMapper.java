package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resign.entity.CertificateProfileRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 证书与Profile关联表Mapper接口
 */
@Mapper
public interface CertificateProfileRelationMapper extends BaseMapper<CertificateProfileRelation> {

    /**
     * 根据证书ID查询关联的Profile ID列表
     */
    List<Long> selectProfileIdsByCertificateId(@Param("certificateId") Long certificateId);

    /**
     * 根据Profile ID查询关联的证书ID列表
     */
    List<Long> selectCertificateIdsByProfileId(@Param("profileId") Long profileId);

    /**
     * 根据证书ID查询所有关联记录
     */
    List<CertificateProfileRelation> selectByCertificateId(@Param("certificateId") Long certificateId);

    /**
     * 根据Profile ID查询所有关联记录
     */
    List<CertificateProfileRelation> selectByProfileId(@Param("profileId") Long profileId);
}