package com.example.resign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.AndroidCertificate;
import com.example.resign.mapper.AndroidCertificateMapper;
import com.example.resign.service.AndroidCertificateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Android证书管理Service实现类
 */
@Slf4j
@Service
public class AndroidCertificateServiceImpl implements AndroidCertificateService {

    @Autowired
    private AndroidCertificateMapper certificateMapper;

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Override
    public AndroidCertificate uploadCertificate(MultipartFile file, String name, String password,
                                              String keyAlias, String keyPassword, String bundleId, String description) {
        try {
            // 验证文件格式
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || (!originalFilename.endsWith(".jks") && !originalFilename.endsWith(".keystore"))) {
                throw new RuntimeException("只支持jks或keystore格式的证书文件");
            }

            // 生成唯一文件名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            String filePath = uploadPath + "/certificates/android/" + fileName;

            // 确保目录存在
            File dir = new File(uploadPath + "/certificates/android/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File destFile = new File(filePath);
            file.transferTo(destFile);

            // 验证证书文件
            if (!validateKeystoreFile(filePath, password, keyAlias, keyPassword)) {
                // 如果验证失败，删除已保存的文件
                destFile.delete();
                throw new RuntimeException("证书文件验证失败，请检查密码和密钥别名");
            }

            // 解析证书信息
            CertificateInfo certInfo = parseCertificateInfo(filePath, password, keyAlias);

            // 创建证书记录
            AndroidCertificate certificate = new AndroidCertificate();
            certificate.setName(name);
            certificate.setFileUrl(filePath);
            certificate.setPassword(password);
            certificate.setKeyAlias(keyAlias);
            certificate.setKeyPassword(keyPassword);
            certificate.setBundleId(bundleId);
            certificate.setDescription(description);
            certificate.setStatus("ACTIVE");
            certificate.setCreateTime(LocalDateTime.now());
            certificate.setUpdateTime(LocalDateTime.now());
            
            // 设置解析出的证书信息
            if (certInfo != null) {
                certificate.setSubject(certInfo.getSubject());
                certificate.setIssuer(certInfo.getIssuer());
                certificate.setSerialNumber(certInfo.getSerialNumber());
                if (certInfo.getExpireDate() != null) {
                    certificate.setExpireDate(certInfo.getExpireDate());
                }
            }
            // TODO: 从当前登录用户获取
            certificate.setCreateBy("system");
            certificate.setUpdateBy("system");

            // 保存到数据库
            certificateMapper.insert(certificate);
            
            log.info("Android证书上传成功: {}", certificate.getName());
            return certificate;
            
        } catch (IOException e) {
            log.error("证书文件保存失败", e);
            throw new RuntimeException("证书文件保存失败: " + e.getMessage());
        }
    }

    @Override
    public AndroidCertificate getCertificateById(Long id) {
        return certificateMapper.selectById(id);
    }

    @Override
    public Page<AndroidCertificate> getCertificateList(int page, int size, String status, String keyAlias) {
        Page<AndroidCertificate> pageParam = new Page<>(page, size);
        QueryWrapper<AndroidCertificate> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (StringUtils.hasText(keyAlias)) {
            queryWrapper.like("key_alias", keyAlias);
        }
        
        queryWrapper.orderByDesc("create_time");
        return certificateMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public List<AndroidCertificate> getCertificatesByStatus(String status) {
        return certificateMapper.selectByStatus(status);
    }

    @Override
    public List<AndroidCertificate> getCertificatesByKeyAlias(String keyAlias) {
        return certificateMapper.selectByKeyAlias(keyAlias);
    }

    @Override
    public List<AndroidCertificate> getActiveCertificates() {
        return certificateMapper.selectActiveCertificates();
    }

    @Override
    public boolean updateCertificate(AndroidCertificate certificate) {
        certificate.setUpdateTime(LocalDateTime.now());
        // TODO: 从当前登录用户获取
        certificate.setUpdateBy("system");
        return certificateMapper.updateById(certificate) > 0;
    }

    @Override
    public boolean deleteCertificate(Long id) {
        AndroidCertificate certificate = certificateMapper.selectById(id);
        if (certificate != null) {
            // 删除文件
            File file = new File(certificate.getFileUrl());
            if (file.exists()) {
                file.delete();
            }
            // 删除数据库记录
            return certificateMapper.deleteById(id) > 0;
        }
        return false;
    }

    @Override
    public boolean enableCertificate(Long id) {
        AndroidCertificate certificate = new AndroidCertificate();
        certificate.setId(id);
        certificate.setStatus("ACTIVE");
        certificate.setUpdateTime(LocalDateTime.now());
        return certificateMapper.updateById(certificate) > 0;
    }

    @Override
    public boolean disableCertificate(Long id) {
        AndroidCertificate certificate = new AndroidCertificate();
        certificate.setId(id);
        certificate.setStatus("INACTIVE");
        certificate.setUpdateTime(LocalDateTime.now());
        return certificateMapper.updateById(certificate) > 0;
    }

    @Override
    public boolean validateCertificate(Long id) {
        AndroidCertificate certificate = certificateMapper.selectById(id);
        if (certificate != null) {
            return validateKeystoreFile(certificate.getFileUrl(), certificate.getPassword(), 
                                      certificate.getKeyAlias(), certificate.getKeyPassword());
        }
        return false;
    }

    @Override
    public boolean isCertificateExpired(Long id) {
        AndroidCertificate certificate = certificateMapper.selectById(id);
        if (certificate != null && certificate.getExpireDate() != null) {
            return certificate.getExpireDate().isBefore(LocalDateTime.now());
        }
        return false;
    }

    @Override
    public List<AndroidCertificate> getExpiringCertificates() {
        QueryWrapper<AndroidCertificate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE")
                   .isNotNull("expire_date")
                   .le("expire_date", LocalDateTime.now().plusDays(30))
                   .ge("expire_date", LocalDateTime.now());
        return certificateMapper.selectList(queryWrapper);
    }

    @Override
    public boolean validateCertificatePassword(Long id, String password, String keyPassword) {
        AndroidCertificate certificate = certificateMapper.selectById(id);
        if (certificate != null) {
            return validateKeystoreFile(certificate.getFileUrl(), password, 
                                      certificate.getKeyAlias(), keyPassword);
        }
        return false;
    }

    /**
     * 验证Keystore文件
     */
    private boolean validateKeystoreFile(String keystorePath, String password, String keyAlias, String keyPassword) {
        try {
            // 使用keytool命令验证keystore文件
            ProcessBuilder pb = new ProcessBuilder(
                "keytool", "-list", "-keystore", keystorePath, 
                "-storepass", password, "-alias", keyAlias
            );
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // 进一步验证密钥密码
                ProcessBuilder pb2 = new ProcessBuilder(
                    "keytool", "-list", "-keystore", keystorePath,
                    "-storepass", password, "-alias", keyAlias, "-keypass", keyPassword
                );
                
                Process process2 = pb2.start();
                int exitCode2 = process2.waitFor();
                
                return exitCode2 == 0;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("验证Keystore文件失败", e);
            return false;
        }
    }

    /**
     * 解析证书信息
     */
    private CertificateInfo parseCertificateInfo(String keystorePath, String password, String keyAlias) {
        try {
            // 使用keytool命令获取证书详细信息
            ProcessBuilder pb = new ProcessBuilder(
                "keytool", "-list", "-v", "-keystore", keystorePath,
                "-storepass", password, "-alias", keyAlias
            );
            
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("执行keytool命令失败，退出码: {}", exitCode);
                return null;
            }
            
            String certOutput = output.toString();
            log.debug("证书信息输出: {}", certOutput);
            
            return parseCertificateOutput(certOutput);
            
        } catch (Exception e) {
            log.error("解析证书信息失败", e);
            return null;
        }
    }

    /**
     * 解析keytool输出的证书信息
     */
    private CertificateInfo parseCertificateOutput(String output) {
        CertificateInfo info = new CertificateInfo();
        
        try {
            // 解析序列号
            Pattern serialPattern = Pattern.compile("Serial number: ([a-fA-F0-9]+)");
            Matcher serialMatcher = serialPattern.matcher(output);
            if (serialMatcher.find()) {
                info.setSerialNumber(serialMatcher.group(1));
            }
            
            // 解析主题
            Pattern subjectPattern = Pattern.compile("Owner: (.+)");
            Matcher subjectMatcher = subjectPattern.matcher(output);
            if (subjectMatcher.find()) {
                info.setSubject(subjectMatcher.group(1).trim());
            }
            
            // 解析签发者
            Pattern issuerPattern = Pattern.compile("Issuer: (.+)");
            Matcher issuerMatcher = issuerPattern.matcher(output);
            if (issuerMatcher.find()) {
                info.setIssuer(issuerMatcher.group(1).trim());
            }
            
            // 解析有效期
            Pattern validPattern = Pattern.compile("Valid from: (.+) until: (.+)");
            Matcher validMatcher = validPattern.matcher(output);
            if (validMatcher.find()) {
                String untilStr = validMatcher.group(2).trim();
                try {
                    // 尝试解析日期格式，keytool通常输出格式为: "Mon Jan 01 00:00:00 UTC 2024"
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");
                    info.setExpireDate(LocalDateTime.parse(untilStr, formatter));
                } catch (Exception e) {
                    log.warn("解析证书过期时间失败: {}", untilStr, e);
                }
            }
            
        } catch (Exception e) {
            log.error("解析证书输出信息失败", e);
        }
        
        return info;
    }

    /**
     * 证书信息内部类
     */
    private static class CertificateInfo {
        private String subject;
        private String issuer;
        private String serialNumber;
        private LocalDateTime expireDate;
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        
        public LocalDateTime getExpireDate() { return expireDate; }
        public void setExpireDate(LocalDateTime expireDate) { this.expireDate = expireDate; }
    }
}