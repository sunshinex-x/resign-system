package com.example.resign.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 系统权限数据传输对象
 */
@Data
public class SysPermissionDTO {

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    private String name;

    /**
     * 权限编码
     */
    @NotBlank(message = "权限编码不能为空")
    private String code;

    /**
     * 权限类型：menu-菜单，button-按钮，api-接口
     */
    @NotBlank(message = "权限类型不能为空")
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
    @NotNull(message = "排序不能为空")
    private Integer sort;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status = 1;

    /**
     * 权限描述
     */
    private String description;
}