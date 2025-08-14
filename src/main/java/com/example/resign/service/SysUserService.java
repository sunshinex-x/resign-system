package com.example.resign.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.model.dto.ChangePasswordDTO;
import com.example.resign.model.dto.SysUserDTO;
import com.example.resign.model.dto.UserProfileDTO;
import com.example.resign.model.vo.SysUserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 系统用户服务接口
 */
public interface SysUserService {

    /**
     * 分页查询用户列表
     *
     * @param page     分页参数
     * @param username 用户名
     * @param email    邮箱
     * @param status   状态
     * @return 用户列表
     */
    IPage<SysUserVO> pageUsers(Page<SysUserVO> page, String username, String email, Integer status);

    /**
     * 根据用户ID查询用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    SysUserVO getUserById(Long userId);

    /**
     * 创建用户
     *
     * @param userDTO 用户信息
     * @return 用户详情
     */
    SysUserVO createUser(SysUserDTO userDTO);

    /**
     * 更新用户
     *
     * @param userId  用户ID
     * @param userDTO 用户信息
     * @return 用户详情
     */
    SysUserVO updateUser(Long userId, SysUserDTO userDTO);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID列表
     * @return 是否成功
     */
    boolean batchDeleteUsers(List<Long> userIds);

    /**
     * 切换用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean toggleUserStatus(Long userId, Integer status);

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String password);
    
    /**
     * 更新个人信息
     *
     * @param userId 用户ID
     * @param params 更新参数
     * @return 用户信息
     */
    SysUserVO updateProfile(Long userId, Map<String, String> params);
    
    /**
     * 更新个人信息（使用DTO）
     *
     * @param userId     用户ID
     * @param profileDTO 个人信息DTO
     * @return 用户信息
     */
    SysUserVO updateProfile(Long userId, UserProfileDTO profileDTO);
    
    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    SysUserVO getCurrentUserProfile(Long userId);
    
    /**
     * 修改个人密码
     *
     * @param userId      用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 修改个人密码（使用DTO）
     *
     * @param userId        用户ID
     * @param passwordDTO   密码DTO
     * @return 是否成功
     */
    boolean changePassword(Long userId, ChangePasswordDTO passwordDTO);
    
    /**
     * 上传头像
     *
     * @param userId 用户ID
     * @param avatar 头像文件
     * @return 头像URL
     */
    String uploadAvatar(Long userId, MultipartFile avatar);
}