package com.example.resign.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resign.entity.SysUser;
import com.example.resign.entity.SysUserRole;
import com.example.resign.mapper.SysUserMapper;
import com.example.resign.mapper.SysUserRoleMapper;
import com.example.resign.model.dto.SysUserDTO;
import com.example.resign.model.vo.SysUserVO;
import com.example.resign.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public IPage<SysUserVO> pageUsers(Page<SysUserVO> page, String username, String email, Integer status) {
        return sysUserMapper.selectUserPage(page, username, email, status);
    }

    @Override
    public SysUserVO getUserById(Long userId) {
        return sysUserMapper.selectUserById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserVO createUser(SysUserDTO userDTO) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(SysUser::getUsername, userDTO.getUsername());
        if (count(usernameWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(SysUser::getEmail, userDTO.getEmail());
        if (count(emailWrapper) > 0) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        BeanUtil.copyProperties(userDTO, user);
        
        // 设置默认密码并加密
        if (StrUtil.isBlank(userDTO.getPassword())) {
            user.setPassword(DigestUtil.md5Hex("123456"));
        } else {
            user.setPassword(DigestUtil.md5Hex(userDTO.getPassword()));
        }
        
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setCreateBy("admin");
        user.setUpdateBy("admin");

        save(user);

        // 分配角色
        if (userDTO.getRoleId() != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(userDTO.getRoleId());
            userRole.setCreateTime(LocalDateTime.now());
            sysUserRoleMapper.insert(userRole);
        }

        return getUserById(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserVO updateUser(Long userId, SysUserDTO userDTO) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查邮箱是否已被其他用户使用
        LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(SysUser::getEmail, userDTO.getEmail())
                   .ne(SysUser::getId, userId);
        if (count(emailWrapper) > 0) {
            throw new RuntimeException("邮箱已被其他用户使用");
        }

        // 更新用户信息
        BeanUtil.copyProperties(userDTO, user, "password");
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy("admin");

        updateById(user);

        // 更新用户角色
        if (userDTO.getRoleId() != null) {
            // 删除原有角色
            LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUserRole::getUserId, userId);
            sysUserRoleMapper.delete(wrapper);

            // 分配新角色
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(userDTO.getRoleId());
            userRole.setCreateTime(LocalDateTime.now());
            sysUserRoleMapper.insert(userRole);
        }

        return getUserById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        // 删除用户角色关联
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        sysUserRoleMapper.delete(wrapper);

        // 删除用户
        return removeById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }

        // 删除用户角色关联
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysUserRole::getUserId, userIds);
        sysUserRoleMapper.delete(wrapper);

        // 批量删除用户
        return removeByIds(userIds);
    }

    @Override
    public boolean toggleUserStatus(Long userId, Integer status) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy("admin");
        return updateById(user);
    }

    @Override
    public boolean resetPassword(Long userId, String password) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(DigestUtil.md5Hex(password));
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy("admin");
        return updateById(user);
    }
}