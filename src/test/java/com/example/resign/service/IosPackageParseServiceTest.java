package com.example.resign.service;

import com.example.resign.model.dto.PackageInfoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * iOS包解析服务测试
 */
class IosPackageParseServiceTest {

    private final IosPackageParseService iosPackageParseService = new IosPackageParseService();

    @Test
    void testIsSupported() {
        // 测试IPA文件支持
        MultipartFile ipaFile = new MockMultipartFile("test.ipa", "test.ipa", "application/octet-stream", new byte[0]);
        assertTrue(iosPackageParseService.isSupported(ipaFile));

        // 测试非IPA文件不支持
        MultipartFile apkFile = new MockMultipartFile("test.apk", "test.apk", "application/octet-stream", new byte[0]);
        assertFalse(iosPackageParseService.isSupported(apkFile));
    }

    @Test
    void testExtractPlistValue() {
        String plistContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                "<dict>\n" +
                "    <key>CFBundleDisplayName</key>\n" +
                "    <string>Test App</string>\n" +
                "    <key>CFBundleIdentifier</key>\n" +
                "    <string>com.example.testapp</string>\n" +
                "    <key>CFBundleShortVersionString</key>\n" +
                "    <string>1.0.0</string>\n" +
                "    <key>CFBundleVersion</key>\n" +
                "    <string>1</string>\n" +
                "</dict>\n" +
                "</plist>";

        assertEquals("Test App", iosPackageParseService.extractPlistValue(plistContent, "CFBundleDisplayName"));
        assertEquals("com.example.testapp", iosPackageParseService.extractPlistValue(plistContent, "CFBundleIdentifier"));
        assertEquals("1.0.0", iosPackageParseService.extractPlistValue(plistContent, "CFBundleShortVersionString"));
        assertEquals("1", iosPackageParseService.extractPlistValue(plistContent, "CFBundleVersion"));
        assertNull(iosPackageParseService.extractPlistValue(plistContent, "NonExistentKey"));
    }

    @Test
    void testParsePackageInfoWithMockIPA() throws IOException {
        // 创建一个模拟的IPA文件
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 添加Info.plist文件
            ZipEntry infoPlistEntry = new ZipEntry("Payload/TestApp.app/Info.plist");
            zos.putNextEntry(infoPlistEntry);
            
            String infoPlistContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                    "<plist version=\"1.0\">\n" +
                    "<dict>\n" +
                    "    <key>CFBundleDisplayName</key>\n" +
                    "    <string>Test App</string>\n" +
                    "    <key>CFBundleIdentifier</key>\n" +
                    "    <string>com.example.testapp</string>\n" +
                    "    <key>CFBundleShortVersionString</key>\n" +
                    "    <string>1.0.0</string>\n" +
                    "    <key>CFBundleVersion</key>\n" +
                    "    <string>1</string>\n" +
                    "</dict>\n" +
                    "</plist>";
            
            zos.write(infoPlistContent.getBytes());
            zos.closeEntry();
        }

        MultipartFile mockIpaFile = new MockMultipartFile(
                "test.ipa", 
                "test.ipa", 
                "application/octet-stream", 
                baos.toByteArray()
        );

        PackageInfoDTO result = iosPackageParseService.parsePackageInfo(mockIpaFile);
        
        assertNotNull(result);
        assertEquals("IOS", result.getAppType());
        assertEquals("Test App", result.getAppName());
        assertEquals("com.example.testapp", result.getPackageName());
        assertEquals("1.0.0", result.getVersionName());
        assertEquals("1", result.getVersionCode());
    }
}