package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * iOS签名证书实体类
 */
@Data
@TableName("ios_certificate")
public class IosCertificate {

    /**
     * 证书ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 证书名称
     */
    private String name;

    /**
     * 证书文件URL（p12格式）
     */
    private String fileUrl;

    /**
     * 证书密码
     */
    private String password;

    /**
     * 开发者团队ID
     */
    private String teamId;

    /**
     * 证书类型：DEVELOPMENT, DISTRIBUTION, ADHOC
     */
    private String certificateType;

    /**
     * 证书过期时间
     */
    private LocalDateTime expireDate;

    /**
     * 证书描述
     */
    private String description;

    /**
     * 状态：ACTIVE, INACTIVE, EXPIRED
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;
}