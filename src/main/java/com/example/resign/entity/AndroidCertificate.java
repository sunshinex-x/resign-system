package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Android签名证书实体类
 */
@Data
@TableName("android_certificate")
public class AndroidCertificate {

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
     * 证书文件URL（jks格式）
     */
    private String fileUrl;

    /**
     * 证书密码
     */
    private String password;

    /**
     * 密钥别名
     */
    private String keyAlias;

    /**
     * 密钥密码
     */
    private String keyPassword;

    /**
     * 主应用包名
     */
    private String bundleId;

    /**
     * 证书过期时间
     */
    private LocalDateTime expireDate;

    /**
     * 证书主题名称
     */
    private String subject;

    /**
     * 证书签发者
     */
    private String issuer;

    /**
     * 证书序列号
     */
    private String serialNumber;

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