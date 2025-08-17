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
import com.example.resign.model.dto.ChangePasswordDTO;
import com.example.resign.model.dto.SysUserDTO;
import com.example.resign.config.FileUploadConfig;
import com.example.resign.model.dto.UserProfileDTO;
import com.example.resign.model.vo.SysUserVO;
import com.example.resign.service.FileService;
import com.example.resign.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final FileService fileService;
    private final FileUploadConfig fileUploadConfig;

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

    @Override
    public SysUserVO updateProfile(Long userId, Map<String, String> params) {
        // 查询用户是否存在
        SysUser existingUser = getById(userId);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证邮箱格式
        String email = params.get("email");
        if (StrUtil.isNotBlank(email)) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new RuntimeException("邮箱格式不正确");
            }
            
            // 检查邮箱是否已被其他用户使用
            if (sysUserMapper.countByEmailExcludeUserId(email, userId) > 0) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
        }

        // 验证手机号格式
        String phone = params.get("phone");
        if (StrUtil.isNotBlank(phone)) {
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                throw new RuntimeException("手机号格式不正确");
            }
            
            // 检查手机号是否已被其他用户使用
            if (sysUserMapper.countByPhoneExcludeUserId(phone, userId) > 0) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
        }

        // 更新用户信息
        SysUser user = new SysUser();
        user.setId(userId);
        
        // 只更新非空字段
        String nickname = params.get("nickname");
        if (StrUtil.isNotBlank(nickname)) {
            if (nickname.length() > 50) {
                throw new RuntimeException("昵称长度不能超过50个字符");
            }
            user.setNickname(nickname.trim());
        }
        
        if (StrUtil.isNotBlank(email)) {
            user.setEmail(email.trim());
        }
        
        if (StrUtil.isNotBlank(phone)) {
            user.setPhone(phone.trim());
        }
        
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(String.valueOf(userId)); // 使用当前用户ID作为更新人

        boolean updated = updateById(user);
        if (!updated) {
            throw new RuntimeException("更新个人信息失败");
        }

        log.info("用户 {} 更新个人信息成功", userId);
        
        // 返回更新后的用户信息
        return getUserById(userId);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证原密码
        String encryptedOldPassword = DigestUtil.md5Hex(oldPassword);
        if (!encryptedOldPassword.equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 验证新密码强度
        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }
        
        if (newPassword.length() > 20) {
            throw new RuntimeException("新密码长度不能超过20位");
        }
        
        // 检查新密码是否与原密码相同
        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        // 更新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(DigestUtil.md5Hex(newPassword));
        updateUser.setUpdateTime(LocalDateTime.now());
        updateUser.setUpdateBy(String.valueOf(userId));

        boolean updated = updateById(updateUser);
        if (updated) {
            log.info("用户 {} 修改密码成功", userId);
        }
        
        return updated;
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile avatar) {
        try {
            // 验证文件不能为空
            if (avatar.isEmpty()) {
                throw new RuntimeException("请选择要上传的图片文件");
            }

            // 验证文件类型
            String contentType = avatar.getContentType();
            if (!fileUploadConfig.isAllowedImageType(contentType)) {
                throw new RuntimeException("不支持的图片格式，请上传 JPG、PNG、GIF 或 WebP 格式的图片");
            }

            // 验证文件大小
            if (avatar.getSize() > fileUploadConfig.getAvatarMaxSize()) {
                throw new RuntimeException("图片大小不能超过 " + 
                    fileUploadConfig.getFormattedSize(fileUploadConfig.getAvatarMaxSize()) + 
                    "，当前文件大小: " + fileUploadConfig.getFormattedSize(avatar.getSize()));
            }

            // 生成文件名
            String originalFilename = avatar.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String objectName = "avatars/avatar_" + userId + "_" + System.currentTimeMillis() + extension;

            // 删除旧头像
            SysUser existingUser = getById(userId);
            if (existingUser != null && StrUtil.isNotBlank(existingUser.getAvatar())) {
                try {
                    String oldObjectName = fileService.getObjectNameFromUrl(existingUser.getAvatar());
                    if (StrUtil.isNotBlank(oldObjectName)) {
                        fileService.deleteFile(oldObjectName);
                        log.info("删除用户 {} 的旧头像: {}", userId, oldObjectName);
                    }
                } catch (Exception e) {
                    log.warn("删除旧头像失败: {}", e.getMessage());
                }
            }

            // 上传文件到MinIO
            String avatarUrl = fileService.uploadFile(avatar.getInputStream(), objectName, contentType);

            // 验证URL长度，防止数据库字段溢出
            if (avatarUrl.length() > 1000) {
                log.warn("头像URL长度超过限制: {} 字符", avatarUrl.length());
                throw new RuntimeException("头像URL过长，请联系管理员");
            }

            // 更新用户头像
            SysUser user = new SysUser();
            user.setId(userId);
            user.setAvatar(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            user.setUpdateBy(String.valueOf(userId));

            boolean updated = updateById(user);
            if (!updated) {
                throw new RuntimeException("更新头像失败");
            }

            log.info("用户 {} 上传头像成功: {}", userId, avatarUrl);
            return avatarUrl;
        } catch (Exception e) {
            log.error("用户 {} 上传头像失败", userId, e);
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        }
    }

    @Override
    public SysUserVO getCurrentUserProfile(Long userId) {
        SysUserVO userProfile = getUserById(userId);
        if (userProfile == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 清除敏感信息
        // 注意：SysUserVO中不应该包含密码字段，这里只是确保安全
        log.info("获取用户 {} 的个人信息", userId);
        
        return userProfile;
    }

    @Override
    public SysUserVO updateProfile(Long userId, UserProfileDTO profileDTO) {
        // 查询用户是否存在
        SysUser existingUser = getById(userId);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证邮箱唯一性
        if (StrUtil.isNotBlank(profileDTO.getEmail())) {
            if (sysUserMapper.countByEmailExcludeUserId(profileDTO.getEmail(), userId) > 0) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
        }

        // 验证手机号唯一性
        if (StrUtil.isNotBlank(profileDTO.getPhone())) {
            if (sysUserMapper.countByPhoneExcludeUserId(profileDTO.getPhone(), userId) > 0) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
        }

        // 更新用户信息
        SysUser user = new SysUser();
        user.setId(userId);
        
        if (StrUtil.isNotBlank(profileDTO.getNickname())) {
            user.setNickname(profileDTO.getNickname().trim());
        }
        
        if (StrUtil.isNotBlank(profileDTO.getEmail())) {
            user.setEmail(profileDTO.getEmail().trim());
        }
        
        if (StrUtil.isNotBlank(profileDTO.getPhone())) {
            user.setPhone(profileDTO.getPhone().trim());
        }
        
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(String.valueOf(userId));

        boolean updated = updateById(user);
        if (!updated) {
            throw new RuntimeException("更新个人信息失败");
        }

        log.info("用户 {} 更新个人信息成功", userId);
        
        return getUserById(userId);
    }

    @Override
    public boolean changePassword(Long userId, ChangePasswordDTO passwordDTO) {
        return changePassword(userId, passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
    }
}