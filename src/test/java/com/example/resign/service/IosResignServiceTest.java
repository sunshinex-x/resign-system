package com.example.resign.service;

import com.example.resign.service.IosResignService;
import com.example.resign.util.IosPermissionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * iOS签名服务测试
 */
@SpringBootTest
public class IosResignServiceTest {

    @Test
    public void testIsSupportedFile() {
        IosResignService service = new IosResignService(null);
        
        // 测试IPA文件支持
        MockMultipartFile ipaFile = new MockMultipartFile(
            "file", "test.ipa", "application/octet-stream", "test content".getBytes()
        );
        assertTrue(service.isSupported(ipaFile));
        
        // 测试非IPA文件不支持
        MockMultipartFile apkFile = new MockMultipartFile(
            "file", "test.apk", "application/octet-stream", "test content".getBytes()
        );
        assertFalse(service.isSupported(apkFile));
    }

    @Test
    public void testPermissionExtraction() throws Exception {
        // 创建测试Info.plist文件
        String plistContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <key>CFBundleIdentifier</key>
                <string>com.example.testapp</string>
                <key>NSCameraUsageDescription</key>
                <string>需要访问相机</string>
                <key>NSLocationWhenInUseUsageDescription</key>
                <string>需要访问位置</string>
                <key>CFBundleURLTypes</key>
                <array>
                    <dict>
                        <key>CFBundleURLSchemes</key>
                        <array>
                            <string>testapp</string>
                        </array>
                    </dict>
                </array>
            </dict>
            </plist>
            """;
        
        Path tempPlist = Files.createTempFile("test_info", ".plist");
        Files.write(tempPlist, plistContent.getBytes());
        
        try {
            var permissions = IosPermissionHandler.extractPermissions(tempPlist);
            
            assertFalse(permissions.isEmpty());
            assertTrue(permissions.contains("相机"));
            assertTrue(permissions.contains("位置(使用时)"));
            assertTrue(permissions.stream().anyMatch(p -> p.contains("testapp")));
            
        } finally {
            Files.deleteIfExists(tempPlist);
        }
    }

    @Test
    public void testProfileBundleIdExtraction() throws Exception {
        // 创建测试Profile文件内容（简化版）
        String profileContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <key>Entitlements</key>
                <dict>
                    <key>application-identifier</key>
                    <string>TEAM123.com.example.testapp</string>
                </dict>
            </dict>
            </plist>
            """;
        
        Path tempProfile = Files.createTempFile("test_profile", ".mobileprovision");
        Files.write(tempProfile, profileContent.getBytes());
        
        try {
            String bundleId = IosPermissionHandler.extractBundleIdFromProfile(tempProfile);
            assertEquals("com.example.testapp", bundleId);
            
        } finally {
            Files.deleteIfExists(tempProfile);
        }
    }

    @Test
    public void testWildcardProfileMatching() throws Exception {
        // 创建通配符Profile
        String wildcardProfileContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <key>Entitlements</key>
                <dict>
                    <key>application-identifier</key>
                    <string>TEAM123.com.example.*</string>
                </dict>
            </dict>
            </plist>
            """;
        
        Path wildcardProfile = Files.createTempFile("wildcard_profile", ".mobileprovision");
        Files.write(wildcardProfile, wildcardProfileContent.getBytes());
        
        try {
            assertTrue(IosPermissionHandler.isWildcardProfile(wildcardProfile));
            assertTrue(IosPermissionHandler.matchesBundleId("com.example.testapp", wildcardProfile));
            assertTrue(IosPermissionHandler.matchesBundleId("com.example.plugin", wildcardProfile));
            assertFalse(IosPermissionHandler.matchesBundleId("com.other.app", wildcardProfile));
            
        } finally {
            Files.deleteIfExists(wildcardProfile);
        }
    }
}