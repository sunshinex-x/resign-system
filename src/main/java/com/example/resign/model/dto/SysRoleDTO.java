package com.example.resign.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 系统角色数据传输对象
 */
@Data
public class SysRoleDTO {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status = 1;
}