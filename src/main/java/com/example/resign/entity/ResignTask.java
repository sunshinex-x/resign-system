package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 重签名任务实体类
 */
@Data
@TableName("resign_task")
public class ResignTask {

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务唯一标识
     */
    private String taskId;

    /**
     * 应用类型：IOS、ANDROID
     */
    private String appType;

    /**
     * 原始安装包URL
     */
    private String originalPackageUrl;

    /**
     * 证书URL
     */
    private String certificateUrl;

    /**
     * 证书密码
     */
    private String certificatePassword;

    /**
     * Provisioning Profile URL（兼容现有数据库字段）
     */
    private String provisioningProfileUrl;
    
    /**
     * Bundle ID（兼容现有数据库字段）
     */
    private String bundleId;
    
    /**
     * 嵌套应用包名映射（兼容现有数据库字段）
     */
    private String nestedAppProfiles;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 重签名后的安装包URL
     */
    private String resignedPackageUrl;

    /**
     * 任务状态：PENDING、PROCESSING、SUCCESS、FAILED
     */
    private String status;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 回调URL
     */
    private String callbackUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    // 注意：以下字段在当前数据库表中不存在，需要数据库升级后才能使用
    // 暂时注释掉，避免SQL错误
    
    // /**
    //  * 应用权限信息（JSON格式）
    //  */
    // private String permissions;
    
    // /**
    //  * 签名类型：DEVELOPMENT, DISTRIBUTION, ADHOC
    //  */
    // private String signType;
    
    // /**
    //  * 处理耗时（毫秒）
    //  */
    // private Long processingTime;
    
    // /**
    //  * 原始文件大小（字节）
    //  */
    // private Long originalFileSize;
    
    // /**
    //  * 重签名后文件大小（字节）
    //  */
    // private Long resignedFileSize;
}