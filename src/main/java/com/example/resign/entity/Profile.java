package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Profile描述文件管理实体
 */
@Data
@TableName("profile")
public class Profile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Profile名称
     */
    private String name;

    /**
     * 关联的证书ID
     */
    private Long certificateId;

    /**
     * Bundle ID
     */
    private String bundleId;

    /**
     * Profile文件URL
     */
    private String fileUrl;

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