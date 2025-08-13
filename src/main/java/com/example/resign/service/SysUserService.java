package com.example.resign.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.model.dto.SysUserDTO;
import com.example.resign.model.vo.SysUserVO;

import java.util.List;

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
}