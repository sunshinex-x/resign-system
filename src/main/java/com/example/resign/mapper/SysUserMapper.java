package com.example.resign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.SysUser;
import com.example.resign.model.vo.SysUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户Mapper接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 分页查询用户列表（包含角色信息）
     *
     * @param page     分页参数
     * @param username 用户名
     * @param email    邮箱
     * @param status   状态
     * @return 用户列表
     */
    IPage<SysUserVO> selectUserPage(Page<SysUserVO> page, 
                                   @Param("username") String username,
                                   @Param("email") String email, 
                                   @Param("status") Integer status);

    /**
     * 根据用户ID查询用户详情（包含角色信息）
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    SysUserVO selectUserById(@Param("userId") Long userId);
    
    /**
     * 根据邮箱查询用户（排除指定用户ID）
     *
     * @param email  邮箱
     * @param userId 排除的用户ID
     * @return 用户数量
     */
    int countByEmailExcludeUserId(@Param("email") String email, @Param("userId") Long userId);
    
    /**
     * 根据手机号查询用户（排除指定用户ID）
     *
     * @param phone  手机号
     * @param userId 排除的用户ID
     * @return 用户数量
     */
    int countByPhoneExcludeUserId(@Param("phone") String phone, @Param("userId") Long userId);
}