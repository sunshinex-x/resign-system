package com.example.resign.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resign.entity.SysRole;
import com.example.resign.entity.SysRolePermission;
import com.example.resign.mapper.SysRoleMapper;
import com.example.resign.mapper.SysRolePermissionMapper;
import com.example.resign.model.dto.SysRoleDTO;
import com.example.resign.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统角色服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public IPage<SysRole> pageRoles(Page<SysRole> page) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysRole::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public List<SysRole> getAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1);
        wrapper.orderByDesc(SysRole::getCreateTime);
        return list(wrapper);
    }

    @Override
    public SysRole getRoleById(Long roleId) {
        return getById(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole createRole(SysRoleDTO roleDTO) {
        // 检查角色编码是否已存在
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, roleDTO.getRoleCode());
        if (count(wrapper) > 0) {
            throw new RuntimeException("角色编码已存在");
        }

        // 创建角色
        SysRole role = new SysRole();
        BeanUtil.copyProperties(roleDTO, role);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        role.setCreateBy("admin");
        role.setUpdateBy("admin");

        save(role);
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole updateRole(Long roleId, SysRoleDTO roleDTO) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 检查角色编码是否已被其他角色使用
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, roleDTO.getRoleCode())
               .ne(SysRole::getId, roleId);
        if (count(wrapper) > 0) {
            throw new RuntimeException("角色编码已被其他角色使用");
        }

        // 更新角色信息
        BeanUtil.copyProperties(roleDTO, role);
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateBy("admin");

        updateById(role);
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        // 删除角色权限关联
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        sysRolePermissionMapper.delete(wrapper);

        // 删除角色
        return removeById(roleId);
    }

    @Override
    public List<Long> getRolePermissions(Long roleId) {
        return sysRoleMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setRolePermissions(Long roleId, List<Long> permissionIds) {
        // 删除原有权限
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        sysRolePermissionMapper.delete(wrapper);

        // 添加新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermission.setCreateTime(LocalDateTime.now());
                sysRolePermissionMapper.insert(rolePermission);
            }
        }

        return true;
    }
}