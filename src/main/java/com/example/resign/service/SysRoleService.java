package com.example.resign.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.SysRole;
import com.example.resign.model.dto.SysRoleDTO;

import java.util.List;

/**
 * 系统角色服务接口
 */
public interface SysRoleService {

    /**
     * 分页查询角色列表
     *
     * @param page 分页参数
     * @return 角色列表
     */
    IPage<SysRole> pageRoles(Page<SysRole> page);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<SysRole> getAllRoles();

    /**
     * 根据角色ID查询角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    SysRole getRoleById(Long roleId);

    /**
     * 创建角色
     *
     * @param roleDTO 角色信息
     * @return 角色详情
     */
    SysRole createRole(SysRoleDTO roleDTO);

    /**
     * 更新角色
     *
     * @param roleId  角色ID
     * @param roleDTO 角色信息
     * @return 角色详情
     */
    SysRole updateRole(Long roleId, SysRoleDTO roleDTO);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long roleId);

    /**
     * 获取角色权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getRolePermissions(Long roleId);

    /**
     * 设置角色权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    boolean setRolePermissions(Long roleId, List<Long> permissionIds);
}