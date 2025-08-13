package com.example.resign.service;

import com.example.resign.model.dto.SysPermissionDTO;
import com.example.resign.model.vo.SysPermissionVO;

import java.util.List;

/**
 * 系统权限服务接口
 */
public interface SysPermissionService {

    /**
     * 查询权限树
     *
     * @return 权限树
     */
    List<SysPermissionVO> getPermissionTree();

    /**
     * 根据权限ID查询权限详情
     *
     * @param permissionId 权限ID
     * @return 权限详情
     */
    SysPermissionVO getPermissionById(Long permissionId);

    /**
     * 创建权限
     *
     * @param permissionDTO 权限信息
     * @return 权限详情
     */
    SysPermissionVO createPermission(SysPermissionDTO permissionDTO);

    /**
     * 更新权限
     *
     * @param permissionId  权限ID
     * @param permissionDTO 权限信息
     * @return 权限详情
     */
    SysPermissionVO updatePermission(Long permissionId, SysPermissionDTO permissionDTO);

    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @return 是否成功
     */
    boolean deletePermission(Long permissionId);
}