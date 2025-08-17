package com.example.resign.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * iOS签名工具类
 * 处理iOS应用的代码签名
 */
@Slf4j
public class IosSigningTool {

    private static final int TIMEOUT_SECONDS = 300; // 5分钟超时

    /**
     * 使用codesign对应用进行签名
     */
    public static void signApplication(Path appPath, String identity, String password) throws IOException, InterruptedException {
        log.info("开始签名应用: {}", appPath);
        
        // 1. 首先签名所有的框架和库
        signFrameworks(appPath, identity, password);
        
        // 2. 签名插件
        signPlugins(appPath, identity, password);
        
        // 3. 最后签名主应用
        signMainApp(appPath, identity, password);
        
        log.info("应用签名完成: {}", appPath);
    }

    /**
     * 签名框架和库
     */
    private static void signFrameworks(Path appPath, String identity, String password) throws IOException, InterruptedException {
        Path frameworksDir = appPath.resolve("Frameworks");
        if (!Files.exists(frameworksDir)) {
            return;
        }
        
        log.info("签名Frameworks目录: {}", frameworksDir);
        
        Files.walk(frameworksDir)
                .filter(path -> Files.isDirectory(path))
                .filter(path -> path.getFileName().toString().endsWith(".framework") || 
                              path.getFileName().toString().endsWith(".dylib"))
                .forEach(frameworkPath -> {
                    try {
                        executeCodesign(frameworkPath, identity, password, false);
                    } catch (Exception e) {
                        log.warn("签名框架失败: {}", frameworkPath, e);
                    }
                });
    }

    /**
     * 签名插件
     */
    private static void signPlugins(Path appPath, String identity, String password) throws IOException, InterruptedException {
        Path pluginsDir = appPath.resolve("PlugIns");
        if (!Files.exists(pluginsDir)) {
            return;
        }
        
        log.info("签名PlugIns目录: {}", pluginsDir);
        
        Files.walk(pluginsDir, 1)
                .filter(path -> Files.isDirectory(path))
                .filter(path -> path.getFileName().toString().endsWith(".appex"))
                .forEach(pluginPath -> {
                    try {
                        // 先签名插件内的框架
                        signFrameworks(pluginPath, identity, password);
                        // 再签名插件本身
                        executeCodesign(pluginPath, identity, password, true);
                    } catch (Exception e) {
                        log.warn("签名插件失败: {}", pluginPath, e);
                    }
                });
    }

    /**
     * 签名主应用
     */
    private static void signMainApp(Path appPath, String identity, String password) throws IOException, InterruptedException {
        log.info("签名主应用: {}", appPath);
        executeCodesign(appPath, identity, password, true);
    }

    /**
     * 执行codesign命令
     */
    private static void executeCodesign(Path targetPath, String identity, String password, boolean withEntitlements) 
            throws IOException, InterruptedException {
        
        List<String> command = new ArrayList<>();
        command.add("codesign");
        command.add("--force");
        command.add("--sign");
        command.add(identity);
        command.add("--verbose");
        
        // 如果需要权限文件
        if (withEntitlements) {
            Path entitlementsPath = findEntitlementsFile(targetPath);
            if (entitlementsPath != null) {
                command.add("--entitlements");
                command.add(entitlementsPath.toString());
            }
        }
        
        command.add(targetPath.toString());
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("CODESIGN_PASSWORD", password);
        
        log.debug("执行签名命令: {}", String.join(" ", command));
        
        Process process = pb.start();
        
        // 读取输出
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        
        try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            
            String line;
            while ((line = outputReader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }
        }
        
        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("签名超时: " + targetPath);
        }
        
        int exitCode = process.exitValue();
        
        if (exitCode != 0) {
            log.error("签名失败 - 退出码: {}, 错误输出: {}", exitCode, error.toString());
            throw new RuntimeException("签名失败: " + error.toString());
        }
        
        log.debug("签名成功: {}", targetPath);
        if (output.length() > 0) {
            log.debug("签名输出: {}", output.toString());
        }
    }

    /**
     * 查找权限文件
     */
    private static Path findEntitlementsFile(Path appPath) {
        // 首先查找embedded.mobileprovision中的权限
        Path embeddedProfile = appPath.resolve("embedded.mobileprovision");
        if (Files.exists(embeddedProfile)) {
            try {
                Path entitlementsPath = extractEntitlementsFromProfile(embeddedProfile, appPath);
                if (entitlementsPath != null) {
                    return entitlementsPath;
                }
            } catch (Exception e) {
                log.warn("从Profile提取权限失败: {}", embeddedProfile, e);
            }
        }
        
        // 查找现有的权限文件
        Path entitlementsPath = appPath.resolve("entitlements.plist");
        if (Files.exists(entitlementsPath)) {
            return entitlementsPath;
        }
        
        return null;
    }

    /**
     * 从Profile文件中提取权限
     */
    private static Path extractEntitlementsFromProfile(Path profilePath, Path appPath) throws IOException, InterruptedException {
        Path entitlementsPath = appPath.resolve("extracted_entitlements.plist");
        
        ProcessBuilder pb = new ProcessBuilder(
            "security", "cms", "-D", "-i", profilePath.toString()
        );
        
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return null;
        }
        
        if (process.exitValue() != 0) {
            return null;
        }
        
        String profileContent = output.toString();
        
        // 提取Entitlements部分
        int entitlementsStart = profileContent.indexOf("<key>Entitlements</key>");
        if (entitlementsStart == -1) {
            return null;
        }
        
        int dictStart = profileContent.indexOf("<dict>", entitlementsStart);
        if (dictStart == -1) {
            return null;
        }
        
        int dictEnd = findMatchingDictEnd(profileContent, dictStart);
        if (dictEnd == -1) {
            return null;
        }
        
        String entitlementsContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                profileContent.substring(dictStart, dictEnd + 7) + "\n" +
                "</plist>";
        
        Files.write(entitlementsPath, entitlementsContent.getBytes());
        
        log.debug("提取权限文件成功: {}", entitlementsPath);
        return entitlementsPath;
    }

    /**
     * 查找匹配的dict结束标签
     */
    private static int findMatchingDictEnd(String content, int dictStart) {
        int depth = 0;
        int pos = dictStart;
        
        while (pos < content.length()) {
            int dictOpenPos = content.indexOf("<dict>", pos);
            int dictClosePos = content.indexOf("</dict>", pos);
            
            if (dictClosePos == -1) {
                break;
            }
            
            if (dictOpenPos != -1 && dictOpenPos < dictClosePos) {
                depth++;
                pos = dictOpenPos + 6;
            } else {
                if (depth == 0) {
                    return dictClosePos;
                }
                depth--;
                pos = dictClosePos + 7;
            }
        }
        
        return -1;
    }

    /**
     * 验证签名
     */
    public static boolean verifySignature(Path appPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "codesign", "--verify", "--verbose", appPath.toString()
            );
            
            Process process = pb.start();
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            
            return process.exitValue() == 0;
            
        } catch (Exception e) {
            log.error("验证签名失败: {}", appPath, e);
            return false;
        }
    }
}