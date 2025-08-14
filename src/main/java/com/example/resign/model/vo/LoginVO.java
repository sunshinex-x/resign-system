package com.example.resign.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {
    
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private String token;
    private List<String> roles;
    private List<String> permissions;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
}