package com.example.resign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.resign.entity.SysUser;
import com.example.resign.mapper.SysUserMapper;
import com.example.resign.model.dto.LoginDTO;
import com.example.resign.model.vo.LoginVO;
import com.example.resign.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证密码（这里简化处理，实际应该使用加密）
        String encryptedPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }
        
        // 生成token
        String token = UUID.randomUUID().toString().replace("-", "");
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp("127.0.0.1"); // 实际应该获取真实IP
        sysUserMapper.updateById(user);
        
        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken(token);
        loginVO.setRoles(Arrays.asList("admin")); // 简化处理
        loginVO.setPermissions(Arrays.asList("*:*:*")); // 简化处理
        
        return loginVO;
    }

    @Override
    public boolean logout(String token) {
        // 实际应该清除token缓存
        return true;
    }

    @Override
    public LoginVO getUserInfo(String token) {
        // 实际应该根据token获取用户信息
        // 这里简化处理，返回默认管理员信息
        LoginVO loginVO = new LoginVO();
        loginVO.setId(1L);
        loginVO.setUsername("admin");
        loginVO.setNickname("管理员");
        loginVO.setEmail("admin@example.com");
        loginVO.setPhone("13800138000");
        loginVO.setRoles(Arrays.asList("admin"));
        loginVO.setPermissions(Arrays.asList("*:*:*"));
        return loginVO;
    }
}