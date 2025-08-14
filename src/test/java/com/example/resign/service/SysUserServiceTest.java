package com.example.resign.service;

import com.example.resign.model.dto.ChangePasswordDTO;
import com.example.resign.model.dto.UserProfileDTO;
import com.example.resign.model.vo.SysUserVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试类
 */
@SpringBootTest
@ActiveProfiles("test")
public class SysUserServiceTest {

    // 注意：这里只是示例测试结构，实际测试需要配置测试数据库和Mock数据
    
    @Test
    public void testUpdateProfileDTO() {
        // 测试个人信息更新DTO的验证
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setNickname("测试用户");
        profileDTO.setEmail("test@example.com");
        profileDTO.setPhone("13800138000");
        
        // 验证DTO字段设置
        assertEquals("测试用户", profileDTO.getNickname());
        assertEquals("test@example.com", profileDTO.getEmail());
        assertEquals("13800138000", profileDTO.getPhone());
    }
    
    @Test
    public void testChangePasswordDTO() {
        // 测试密码修改DTO的验证
        ChangePasswordDTO passwordDTO = new ChangePasswordDTO();
        passwordDTO.setOldPassword("oldpass123");
        passwordDTO.setNewPassword("newpass123");
        
        // 验证DTO字段设置
        assertEquals("oldpass123", passwordDTO.getOldPassword());
        assertEquals("newpass123", passwordDTO.getNewPassword());
    }
}