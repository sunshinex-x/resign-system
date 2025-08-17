package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.resign.entity.Certificate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 证书Mapper
 */
@Mapper
public interface CertificateMapper extends BaseMapper<Certificate> {
}