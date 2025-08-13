package com.example.resign.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统权限视图对象
 */
@Data
public class SysPermissionVO {

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限编码
     */
    private String code;

    /**
     * 权限类型：menu-菜单，button-按钮，api-接口
     */
    private String type;

    /**
     * 路径/接口地址
     */
    private String path;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 子权限列表
     */
    private List<SysPermissionVO> children;
}