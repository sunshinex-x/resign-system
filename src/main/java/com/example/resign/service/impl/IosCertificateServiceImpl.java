package com.example.resign.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.IosCertificate;
import com.example.resign.entity.IosProfile;
import com.example.resign.mapper.IosCertificateMapper;
import com.example.resign.mapper.IosProfileMapper;
import com.example.resign.service.IosCertificateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.stream.Collectors;

/**
 * iOS证书管理Service实现类
 */
@Slf4j
@Service
public class IosCertificateServiceImpl implements IosCertificateService {

    @Autowired
    private IosCertificateMapper certificateMapper;
    
    @Autowired
    private IosProfileMapper profileMapper;

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Override
    public IosCertificate uploadCertificate(MultipartFile file, String name, String password,
                                          String teamId, String bundleId, String certificateType, String description) {
        try {
            // 验证文件格式
            if (!file.getOriginalFilename().endsWith(".p12")) {
                throw new RuntimeException("只支持p12格式的证书文件");
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + ".p12";
            String filePath = uploadPath + "/certificates/ios/" + fileName;

            // 确保目录存在
            File dir = new File(uploadPath + "/certificates/ios/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File destFile = new File(filePath);
            file.transferTo(destFile);

            // 解析证书信息
            CertificateInfo certInfo = parseCertificateInfo(filePath, password);

            // 创建证书记录
            IosCertificate certificate = new IosCertificate();
            certificate.setName(name);
            certificate.setFileUrl(filePath);
            certificate.setPassword(password);
            certificate.setTeamId(teamId);
            certificate.setBundleId(bundleId);
            certificate.setCertificateType(certificateType);
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
            
            log.info("iOS证书上传成功: {}", certificate.getName());
            return certificate;
            
        } catch (IOException e) {
            log.error("证书文件保存失败", e);
            throw new RuntimeException("证书文件保存失败: " + e.getMessage());
        }
    }

    @Override
    public IosCertificate getCertificateById(Long id) {
        return certificateMapper.selectById(id);
    }

    @Override
    public Page<IosCertificate> getCertificateList(int page, int size, String status, String teamId, String certificateType) {
        Page<IosCertificate> pageParam = new Page<>(page, size);
        QueryWrapper<IosCertificate> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (StringUtils.hasText(teamId)) {
            queryWrapper.eq("team_id", teamId);
        }
        if (StringUtils.hasText(certificateType)) {
            queryWrapper.eq("certificate_type", certificateType);
        }
        
        queryWrapper.orderByDesc("create_time");
        return certificateMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public List<IosCertificate> getCertificatesByStatus(String status) {
        return certificateMapper.selectByStatus(status);
    }

    @Override
    public List<IosCertificate> getCertificatesByTeamId(String teamId) {
        return certificateMapper.selectByTeamId(teamId);
    }

    @Override
    public boolean updateCertificate(IosCertificate certificate) {
        certificate.setUpdateTime(LocalDateTime.now());
        // TODO: 从当前登录用户获取
        certificate.setUpdateBy("system");
        return certificateMapper.updateById(certificate) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCertificate(Long id) {
        IosCertificate certificate = certificateMapper.selectById(id);
        if (certificate != null) {
            try {
                // 1. 查询关联的Profile ID列表
                List<IosProfile> iosProfiles = profileMapper.selectByCertificateId(id);

                // 2. 删除关联的Profile文件
                for (IosProfile profile : iosProfiles) {
                    if (profile != null && profile.getFileUrl() != null) {
                        File profileFile = new File(profile.getFileUrl());
                        if (profileFile.exists()) {
                            profileFile.delete();
                            log.info("删除Profile文件: {}", profile.getFileUrl());
                        }
                    }
                }
                
                // 3. 删除Profile数据库记录
                List<Long> profileIds = iosProfiles.stream().map(IosProfile::getId).collect(Collectors.toList());
                if (!profileIds.isEmpty()) {
                    profileMapper.deleteBatchIds(profileIds);
                    log.info("删除Profile记录数量: {}", profileIds.size());
                }
                
                // 4. 删除证书文件
                File certificateFile = new File(certificate.getFileUrl());
                if (certificateFile.exists()) {
                    certificateFile.delete();
                    log.info("删除证书文件: {}", certificate.getFileUrl());
                }
                
                // 6. 删除证书数据库记录
                boolean result = certificateMapper.deleteById(id) > 0;
                log.info("删除证书记录: {}, 结果: {}", certificate.getName(), result);
                
                return result;
            } catch (Exception e) {
                log.error("删除证书失败: {}", e.getMessage(), e);
                throw e; // 抛出异常触发事务回滚
            }
        }
        return false;
    }

    @Override
    public boolean enableCertificate(Long id) {
        IosCertificate certificate = new IosCertificate();
        certificate.setId(id);
        certificate.setStatus("ACTIVE");
        certificate.setUpdateTime(LocalDateTime.now());
        return certificateMapper.updateById(certificate) > 0;
    }

    @Override
    public boolean disableCertificate(Long id) {
        IosCertificate certificate = new IosCertificate();
        certificate.setId(id);
        certificate.setStatus("INACTIVE");
        certificate.setUpdateTime(LocalDateTime.now());
        return certificateMapper.updateById(certificate) > 0;
    }

    @Override
    public boolean validateCertificate(Long id) {
        // TODO: 实现证书验证逻辑
        // 可以通过调用系统命令验证p12证书的有效性
        return true;
    }

    @Override
    public boolean isCertificateExpired(Long id) {
        IosCertificate certificate = certificateMapper.selectById(id);
        if (certificate != null && certificate.getExpireDate() != null) {
            return certificate.getExpireDate().isBefore(LocalDateTime.now());
        }
        return false;
    }

    @Override
    public List<IosCertificate> getExpiringCertificates() {
        QueryWrapper<IosCertificate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE")
                   .isNotNull("expire_date")
                   .le("expire_date", LocalDateTime.now().plusDays(30))
                   .ge("expire_date", LocalDateTime.now());
        return certificateMapper.selectList(queryWrapper);
    }

    /**
     * 解析iOS证书信息
     */
    private CertificateInfo parseCertificateInfo(String p12Path, String password) {
        try {
            // 使用openssl命令解析p12证书
            ProcessBuilder pb = new ProcessBuilder(
                "openssl", "pkcs12", "-in", p12Path, "-passin", "pass:" + password,
                "-nokeys", "-clcerts", "-noout", "-text"
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
                log.error("执行openssl命令失败，退出码: {}", exitCode);
                return null;
            }
            
            String certOutput = output.toString();
            log.debug("iOS证书信息输出: {}", certOutput);
            
            return parseCertificateOutput(certOutput);
            
        } catch (Exception e) {
            log.error("解析iOS证书信息失败", e);
            return null;
        }
    }

    /**
     * 解析openssl输出的证书信息
     */
    private CertificateInfo parseCertificateOutput(String output) {
        CertificateInfo info = new CertificateInfo();
        
        try {
            // 解析序列号
            Pattern serialPattern = Pattern.compile("Serial Number:\\s*([a-fA-F0-9:]+)");
            Matcher serialMatcher = serialPattern.matcher(output);
            if (serialMatcher.find()) {
                info.setSerialNumber(serialMatcher.group(1).replaceAll(":", ""));
            }
            
            // 解析主题
            Pattern subjectPattern = Pattern.compile("Subject: (.+)");
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
            Pattern validPattern = Pattern.compile("Not After : (.+)");
            Matcher validMatcher = validPattern.matcher(output);
            if (validMatcher.find()) {
                String notAfterStr = validMatcher.group(1).trim();
                try {
                    // openssl输出格式通常为: "Jan  1 00:00:00 2024 GMT"
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d HH:mm:ss yyyy zzz");
                    info.setExpireDate(LocalDateTime.parse(notAfterStr, formatter));
                } catch (Exception e) {
                    log.warn("解析iOS证书过期时间失败: {}", notAfterStr, e);
                }
            }
            
        } catch (Exception e) {
            log.error("解析iOS证书输出信息失败", e);
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