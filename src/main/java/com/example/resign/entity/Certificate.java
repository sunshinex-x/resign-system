package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 证书管理实体
 */
@Data
@TableName("certificate")
public class Certificate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 证书名称
     */
    private String name;

    /**
     * 平台类型：IOS, ANDROID
     */
    private String platform;

    /**
     * 证书文件URL
     */
    private String fileUrl;

    /**
     * 证书密码
     */
    private String password;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 状态：ACTIVE, INACTIVE
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
}