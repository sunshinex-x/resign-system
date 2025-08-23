package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 证书与Profile关联表实体类
 */
@Data
@TableName("certificate_profile_relation")
public class CertificateProfileRelation {

    /**
     * 关联ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * iOS证书ID
     */
    private Long certificateId;

    /**
     * iOS Profile ID
     */
    private Long profileId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}