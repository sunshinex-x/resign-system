package com.example.resign.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resign.entity.SysPermission;
import com.example.resign.mapper.SysPermissionMapper;
import com.example.resign.model.dto.SysPermissionDTO;
import com.example.resign.model.vo.SysPermissionVO;
import com.example.resign.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public List<SysPermissionVO> getPermissionTree() {
        List<SysPermissionVO> allPermissions = sysPermissionMapper.selectPermissionTree();
        return buildPermissionTree(allPermissions);
    }

    @Override
    public SysPermissionVO getPermissionById(Long permissionId) {
        SysPermission permission = getById(permissionId);
        if (permission == null) {
            return null;
        }
        
        SysPermissionVO vo = new SysPermissionVO();
        BeanUtil.copyProperties(permission, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysPermissionVO createPermission(SysPermissionDTO permissionDTO) {
        // 检查权限编码是否已存在
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getCode, permissionDTO.getCode());
        if (count(wrapper) > 0) {
            throw new RuntimeException("权限编码已存在");
        }

        // 创建权限
        SysPermission permission = new SysPermission();
        BeanUtil.copyProperties(permissionDTO, permission);
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());

        save(permission);

        SysPermissionVO vo = new SysPermissionVO();
        BeanUtil.copyProperties(permission, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysPermissionVO updatePermission(Long permissionId, SysPermissionDTO permissionDTO) {
        SysPermission permission = getById(permissionId);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }

        // 检查权限编码是否已被其他权限使用
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getCode, permissionDTO.getCode())
               .ne(SysPermission::getId, permissionId);
        if (count(wrapper) > 0) {
            throw new RuntimeException("权限编码已被其他权限使用");
        }

        // 更新权限信息
        BeanUtil.copyProperties(permissionDTO, permission);
        permission.setUpdateTime(LocalDateTime.now());

        updateById(permission);

        SysPermissionVO vo = new SysPermissionVO();
        BeanUtil.copyProperties(permission, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(Long permissionId) {
        // 检查是否有子权限
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getParentId, permissionId);
        if (count(wrapper) > 0) {
            throw new RuntimeException("存在子权限，无法删除");
        }

        // 删除权限
        return removeById(permissionId);
    }

    /**
     * 构建权限树
     */
    private List<SysPermissionVO> buildPermissionTree(List<SysPermissionVO> allPermissions) {
        // 按父ID分组
        Map<Long, List<SysPermissionVO>> permissionMap = allPermissions.stream()
                .collect(Collectors.groupingBy(permission -> permission.getParentId() == null ? 0L : permission.getParentId()));

        // 构建树结构
        List<SysPermissionVO> rootPermissions = permissionMap.getOrDefault(0L, new ArrayList<>());
        for (SysPermissionVO permission : rootPermissions) {
            buildChildren(permission, permissionMap);
        }

        return rootPermissions;
    }

    /**
     * 递归构建子权限
     */
    private void buildChildren(SysPermissionVO parent, Map<Long, List<SysPermissionVO>> permissionMap) {
        List<SysPermissionVO> children = permissionMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            parent.setChildren(children);
            for (SysPermissionVO child : children) {
                buildChildren(child, permissionMap);
            }
        }
    }
}