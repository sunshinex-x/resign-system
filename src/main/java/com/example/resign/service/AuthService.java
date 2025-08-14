package com.example.resign.service;

import com.example.resign.model.dto.LoginDTO;
import com.example.resign.model.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);
    
    /**
     * 用户登出
     */
    boolean logout(String token);
    
    /**
     * 获取用户信息
     */
    LoginVO getUserInfo(String token);
}