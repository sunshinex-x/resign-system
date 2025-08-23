package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * iOS Profile描述文件实体类
 */
@Data
@TableName("ios_profile")
public class IosProfile {

    /**
     * Profile ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Profile名称
     */
    private String name;

    /**
     * 关联的iOS证书ID
     */
    private Long certificateId;

    /**
     * Bundle ID
     */
    private String bundleId;

    /**
     * Profile文件URL（mobileprovision格式）
     */
    private String fileUrl;

    /**
     * 开发者团队ID
     */
    private String teamId;

    /**
     * Profile类型：DEVELOPMENT, DISTRIBUTION, ADHOC
     */
    private String profileType;

    /**
     * Profile过期时间
     */
    private LocalDateTime expireDate;

    /**
     * Profile描述
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