package com.example.resign.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.example.resign.service.ResignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 重签名服务实现类
 */
@Slf4j
@Service
public class ResignServiceImpl implements ResignService {

    @Value("${resign.temp-dir}")
    private String tempDir;

    @Value("${resign.tools.ios.sign-tool}")
    private String iosSignTool;

    @Value("${resign.tools.android.sign-tool}")
    private String androidSignTool;

    @Value("${resign.tools.android.zipalign-tool}")
    private String androidZipalignTool;

    @Value("${resign.tools.harmony.sign-tool}")
    private String harmonySignTool;

    @Override
    public InputStream resignIosApp(InputStream packageInputStream, InputStream certificateInputStream, String certificatePassword) {
        String taskId = IdUtil.fastSimpleUUID();
        String workDir = tempDir + File.separator + taskId;
        String packagePath = workDir + File.separator + "app.ipa";
        String certificatePath = workDir + File.separator + "certificate.p12";
        String outputPath = workDir + File.separator + "resigned.ipa";

        try {
            // 创建工作目录
            FileUtil.mkdir(workDir);

            // 保存安装包和证书到临时文件
            FileUtil.writeFromStream(packageInputStream, packagePath);
            FileUtil.writeFromStream(certificateInputStream, certificatePath);

            // 解压IPA文件
            String unzipDir = workDir + File.separator + "unzip";
            FileUtil.mkdir(unzipDir);
            RuntimeUtil.exec("unzip -q " + packagePath + " -d " + unzipDir);

            // 找到Payload目录和.app文件
            File payloadDir = new File(unzipDir + File.separator + "Payload");
            File appDir = null;
            for (File file : payloadDir.listFiles()) {
                if (file.getName().endsWith(".app")) {
                    appDir = file;
                    break;
                }
            }

            if (appDir == null) {
                throw new RuntimeException("无法找到.app文件");
            }

            // 导入证书到钥匙串
            String keychain = workDir + File.separator + "resign.keychain";
            String keychainPassword = IdUtil.fastSimpleUUID();
            RuntimeUtil.exec("security create-keychain -p " + keychainPassword + " " + keychain);
            RuntimeUtil.exec("security unlock-keychain -p " + keychainPassword + " " + keychain);
            RuntimeUtil.exec("security import " + certificatePath + " -k " + keychain + " -P " + certificatePassword + " -T " + iosSignTool);
            RuntimeUtil.exec("security set-keychain-settings -lut 7200 " + keychain);
            RuntimeUtil.exec("security list-keychains -s " + keychain);

            // 获取证书身份
            String identityOutput = RuntimeUtil.execForStr("security find-identity -v " + keychain);
            String identity = StrUtil.subBetween(identityOutput, "\"", "\"");

            if (StrUtil.isBlank(identity)) {
                throw new RuntimeException("无法获取证书身份");
            }

            // 签名应用
            RuntimeUtil.exec(iosSignTool + " --force -f -s \"" + identity + "\" --entitlements " + appDir.getAbsolutePath() + "/embedded.mobileprovision " + appDir.getAbsolutePath());

            // 重新打包IPA
            RuntimeUtil.exec("cd " + unzipDir + " && zip -qr " + outputPath + " Payload");

            // 清理钥匙串
            RuntimeUtil.exec("security delete-keychain " + keychain);

            // 返回重签名后的安装包输入流
            return new FileInputStream(outputPath);
        } catch (Exception e) {
            log.error("iOS应用重签名失败", e);
            throw new RuntimeException("iOS应用重签名失败: " + e.getMessage());
        } finally {
            // 关闭输入流
            IoUtil.close(packageInputStream);
            IoUtil.close(certificateInputStream);

            // 清理临时文件
            FileUtil.del(workDir);
        }
    }

    @Override
    public InputStream resignAndroidApp(InputStream packageInputStream, InputStream certificateInputStream, String certificatePassword) {
        String taskId = IdUtil.fastSimpleUUID();
        String workDir = tempDir + File.separator + taskId;
        String packagePath = workDir + File.separator + "app.apk";
        String keystorePath = workDir + File.separator + "keystore.jks";
        String alignedPath = workDir + File.separator + "aligned.apk";
        String outputPath = workDir + File.separator + "resigned.apk";

        try {
            // 创建工作目录
            FileUtil.mkdir(workDir);

            // 保存安装包和证书到临时文件
            FileUtil.writeFromStream(packageInputStream, packagePath);
            FileUtil.writeFromStream(certificateInputStream, keystorePath);

            // 使用zipalign优化APK
            RuntimeUtil.exec(androidZipalignTool + " -f -v 4 " + packagePath + " " + alignedPath);

            // 使用apksigner签名APK
            RuntimeUtil.exec(androidSignTool + " sign --ks " + keystorePath + " --ks-pass pass:" + certificatePassword + " --out " + outputPath + " " + alignedPath);

            // 验证签名
            RuntimeUtil.exec(androidSignTool + " verify -v " + outputPath);

            // 返回重签名后的安装包输入流
            return new FileInputStream(outputPath);
        } catch (Exception e) {
            log.error("Android应用重签名失败", e);
            throw new RuntimeException("Android应用重签名失败: " + e.getMessage());
        } finally {
            // 关闭输入流
            IoUtil.close(packageInputStream);
            IoUtil.close(certificateInputStream);

            // 清理临时文件
            FileUtil.del(workDir);
        }
    }

    @Override
    public InputStream resignHarmonyApp(InputStream packageInputStream, InputStream certificateInputStream, String certificatePassword) {
        String taskId = IdUtil.fastSimpleUUID();
        String workDir = tempDir + File.separator + taskId;
        String packagePath = workDir + File.separator + "app.hap";
        String certificatePath = workDir + File.separator + "certificate.p12";
        String profilePath = workDir + File.separator + "profile.p7b";
        String outputPath = workDir + File.separator + "resigned.hap";

        try {
            // 创建工作目录
            FileUtil.mkdir(workDir);

            // 保存安装包和证书到临时文件
            FileUtil.writeFromStream(packageInputStream, packagePath);
            FileUtil.writeFromStream(certificateInputStream, certificatePath);

            // 解压HAP文件
            String unzipDir = workDir + File.separator + "unzip";
            FileUtil.mkdir(unzipDir);
            RuntimeUtil.exec("unzip -q " + packagePath + " -d " + unzipDir);

            // 提取配置文件
            Path configPath = Paths.get(unzipDir, "config.json");
            if (!Files.exists(configPath)) {
                throw new RuntimeException("无法找到config.json文件");
            }

            // 使用鸿蒙签名工具签名
            RuntimeUtil.exec(harmonySignTool + " sign -mode localjks -keyAlias default -keyPwd " + certificatePassword + 
                    " -keystoreFile " + certificatePath + " -inFile " + packagePath + " -outFile " + outputPath + 
                    " -profileFile " + profilePath + " -signAlg SHA256withECDSA");

            // 验证签名
            RuntimeUtil.exec(harmonySignTool + " verify -in " + outputPath);

            // 返回重签名后的安装包输入流
            return new FileInputStream(outputPath);
        } catch (Exception e) {
            log.error("鸿蒙应用重签名失败", e);
            throw new RuntimeException("鸿蒙应用重签名失败: " + e.getMessage());
        } finally {
            // 关闭输入流
            IoUtil.close(packageInputStream);
            IoUtil.close(certificateInputStream);

            // 清理临时文件
            FileUtil.del(workDir);
        }
    }
}