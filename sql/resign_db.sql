/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : resign_db

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 24/08/2025 20:58:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for android_certificate
-- ----------------------------
DROP TABLE IF EXISTS `android_certificate`;
CREATE TABLE `android_certificate` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '证书ID',
  `name` varchar(255) NOT NULL COMMENT '证书名称',
  `file_url` varchar(500) NOT NULL COMMENT '证书文件URL（jks格式）',
  `password` varchar(255) NOT NULL COMMENT '证书密码',
  `key_alias` varchar(255) NOT NULL COMMENT '密钥别名',
  `key_password` varchar(255) NOT NULL COMMENT '密钥密码',
  `bundle_id` varchar(255) DEFAULT NULL COMMENT '主应用包名',
  `expire_date` datetime DEFAULT NULL COMMENT '证书过期时间',
  `description` text COMMENT '证书描述',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, INACTIVE, EXPIRED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `subject` varchar(500) DEFAULT NULL COMMENT '证书主题名称',
  `issuer` varchar(500) DEFAULT NULL COMMENT '证书签发者',
  `serial_number` varchar(100) DEFAULT NULL COMMENT '证书序列号',
  PRIMARY KEY (`id`),
  KEY `idx_android_cert_name` (`name`),
  KEY `idx_android_cert_status` (`status`),
  KEY `idx_android_cert_create_time` (`create_time`),
  KEY `idx_android_cert_bundle_id` (`bundle_id`),
  KEY `idx_android_cert_serial` (`serial_number`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Android签名证书表';

-- ----------------------------
-- Records of android_certificate
-- ----------------------------
BEGIN;
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (1, 'Android发布证书-主应用', '/certificates/android/main_release.jks', 'release123', 'main_key', 'key123456', 'com.company.mainapp', '2026-01-15 23:59:59', '主应用的Android发布证书', 'ACTIVE', '2025-01-10 09:00:00', '2025-08-24 10:54:43', 'admin', 'admin', 'CN=Android Debug, O=Android, C=US', 'CN=Android Debug, O=Android, C=US', '1a2b3c4d5e6f7890');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (2, 'Android调试证书-开发', '/certificates/android/debug.jks', 'android', 'androiddebugkey', 'android', 'com.debug.testapp', '2026-12-31 23:59:59', 'Android开发调试证书', 'ACTIVE', '2025-01-10 09:30:00', '2025-08-24 10:54:43', 'admin', 'admin', 'CN=MyApp Release, O=MyCompany, L=Beijing, C=CN', 'CN=MyCompany CA, O=MyCompany, C=CN', '9f8e7d6c5b4a3210');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (3, 'Android企业证书-内部', '/certificates/android/enterprise.jks', 'ent456789', 'enterprise_key', 'ent789012', 'com.enterprise.internal', '2025-12-31 23:59:59', '企业内部应用签名证书', 'ACTIVE', '2025-01-11 10:15:00', '2025-08-24 10:54:43', 'admin', 'admin', 'CN=TestApp, O=TestOrg, L=Shanghai, ST=Shanghai, C=CN', 'CN=TestOrg Root CA, O=TestOrg, C=CN', 'abcdef1234567890');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (4, 'Android测试证书-Beta', '/certificates/android/beta_test.jks', 'beta123456', 'beta_key', 'beta789012', 'com.beta.testapp', '2025-11-30 23:59:59', 'Beta测试版本签名证书', 'ACTIVE', '2025-01-12 11:20:00', '2025-08-24 10:54:43', 'user001', 'user001', 'CN=Android Certificate 4, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000004');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (5, 'Android游戏证书-Game', '/certificates/android/game_release.jks', 'game999888', 'game_key', 'game888999', 'com.game.adventure', '2025-10-31 23:59:59', '游戏应用专用签名证书', 'ACTIVE', '2025-01-13 14:45:00', '2025-08-24 10:54:43', 'user001', 'user001', 'CN=Android Certificate 5, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000005');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (6, 'Android电商应用证书', '/certificates/android/ecommerce_release.jks', 'ecom123456', 'ecommerce_key', 'ecom789012', 'com.shop.ecommerce', '2026-08-31 23:59:59', '电商应用的Android发布证书', 'ACTIVE', '2025-01-22 10:30:00', '2025-08-24 10:54:43', 'admin', 'admin', 'CN=Android Certificate 6, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000006');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (7, 'Android社交应用证书', '/certificates/android/social_release.jks', 'social789012', 'social_key', 'social345678', 'com.social.connect', '2026-07-15 23:59:59', '社交应用的Android发布证书', 'ACTIVE', '2025-01-22 11:30:00', '2025-08-24 10:54:43', 'developer01', 'developer01', 'CN=Android Certificate 7, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000007');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (8, 'Android教育应用证书', '/certificates/android/education_release.jks', 'edu456789012', 'education_key', 'edu123456789', 'com.education.learn', '2026-06-30 23:59:59', '教育应用的Android发布证书', 'ACTIVE', '2025-01-22 12:30:00', '2025-08-24 10:54:43', 'developer02', 'developer02', 'CN=Android Certificate 8, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000008');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (9, 'Android金融应用证书', '/certificates/android/finance_release.jks', 'fin999888777', 'finance_key', 'fin666555444', 'com.finance.bank', '2026-05-31 23:59:59', '金融应用的Android发布证书', 'ACTIVE', '2025-01-22 13:30:00', '2025-08-24 10:54:43', 'admin', 'admin', 'CN=Android Certificate 9, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000009');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (10, 'Android工具应用证书', '/certificates/android/tools_release.jks', 'tools123456789', 'tools_key', 'tools987654321', 'com.tools.utility', '2026-04-30 23:59:59', '工具应用的Android发布证书', 'ACTIVE', '2025-01-22 14:30:00', '2025-08-24 10:54:43', 'developer01', 'developer01', 'CN=Android Certificate 10, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000010');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (11, 'Android音乐应用证书', '/certificates/android/music_release.jks', 'music111222', 'music_key', 'music333444', 'com.music.player', '2026-03-31 23:59:59', '音乐应用的Android发布证书', 'ACTIVE', '2025-01-22 15:00:00', '2025-08-24 10:54:43', 'developer02', 'developer02', 'CN=Android Certificate 11, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000011');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (12, 'Android视频应用证书', '/certificates/android/video_release.jks', 'video555666', 'video_key', 'video777888', 'com.video.player', '2026-02-28 23:59:59', '视频应用的Android发布证书', 'ACTIVE', '2025-01-22 15:30:00', '2025-08-24 10:54:43', 'developer01', 'developer01', 'CN=Android Certificate 12, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000012');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (13, 'Android新闻应用证书', '/certificates/android/news_release.jks', 'news999000', 'news_key', 'news111222', 'com.news.reader', '2026-01-31 23:59:59', '新闻应用的Android发布证书', 'ACTIVE', '2025-01-22 16:00:00', '2025-08-24 10:54:43', 'admin', 'admin', 'CN=Android Certificate 13, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000013');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (14, 'Android天气应用证书', '/certificates/android/weather_release.jks', 'weather333444', 'weather_key', 'weather555666', 'com.weather.forecast', '2025-12-31 23:59:59', '天气应用的Android发布证书', 'ACTIVE', '2025-01-22 16:30:00', '2025-08-24 10:54:43', 'developer02', 'developer02', 'CN=Android Certificate 14, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000014');
INSERT INTO `android_certificate` (`id`, `name`, `file_url`, `password`, `key_alias`, `key_password`, `bundle_id`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (15, 'Android健康应用证书', '/certificates/android/health_release.jks', 'health777888', 'health_key', 'health999000', 'com.health.tracker', '2025-11-30 23:59:59', '健康应用的Android发布证书', 'ACTIVE', '2025-01-22 17:00:00', '2025-08-24 10:54:43', 'developer01', 'developer01', 'CN=Android Certificate 15, O=Default Org, C=US', 'CN=Default Android CA, O=Default Org, C=US', 'default000000000015');
COMMIT;

-- ----------------------------
-- Table structure for android_resign_task
-- ----------------------------
DROP TABLE IF EXISTS `android_resign_task`;
CREATE TABLE `android_resign_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_id` varchar(64) NOT NULL COMMENT '任务唯一标识',
  `original_apk_url` varchar(500) NOT NULL COMMENT '原始APK文件URL',
  `certificate_id` bigint NOT NULL COMMENT '使用的Android证书ID',
  `app_name` varchar(255) DEFAULT NULL COMMENT '应用名称（从APK解析）',
  `app_version` varchar(50) DEFAULT NULL COMMENT '应用版本（从APK解析）',
  `package_name` varchar(255) DEFAULT NULL COMMENT '包名（从APK解析）',
  `permissions` text COMMENT '应用权限信息（JSON格式）',
  `resigned_apk_url` varchar(500) DEFAULT NULL COMMENT '重签名后的APK文件URL',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING, PROCESSING, SUCCESS, FAILED',
  `fail_reason` text COMMENT '失败原因',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `processing_time` bigint DEFAULT NULL COMMENT '处理耗时（毫秒）',
  `original_file_size` bigint DEFAULT NULL COMMENT '原始文件大小（字节）',
  `resigned_file_size` bigint DEFAULT NULL COMMENT '重签名后文件大小（字节）',
  `callback_url` varchar(500) DEFAULT NULL COMMENT '回调URL',
  `description` varchar(500) DEFAULT NULL COMMENT '任务描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_android_task_id` (`task_id`),
  KEY `idx_android_task_status` (`status`),
  KEY `idx_android_task_cert_id` (`certificate_id`),
  KEY `idx_android_task_create_time` (`create_time`),
  CONSTRAINT `fk_android_task_certificate` FOREIGN KEY (`certificate_id`) REFERENCES `android_certificate` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Android重签名任务表';

-- ----------------------------
-- Records of android_resign_task
-- ----------------------------
BEGIN;
INSERT INTO `android_resign_task` (`id`, `task_id`, `original_apk_url`, `certificate_id`, `app_name`, `app_version`, `package_name`, `permissions`, `resigned_apk_url`, `status`, `fail_reason`, `retry_count`, `processing_time`, `original_file_size`, `resigned_file_size`, `callback_url`, `description`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (1, 'android_task_20250118_001', '/uploads/android/mainapp_v1.apk', 1, 'MainApp', '1.5.2', 'com.company.mainapp', '{\"CAMERA\": true, \"LOCATION\": true, \"WRITE_EXTERNAL_STORAGE\": true}', '/downloads/android/mainapp_signed.apk', 'SUCCESS', NULL, 0, 8750, 25165824, 25167000, 'https://api.example.com/callback/android/001', '主应用生产版本重签名', '2025-01-18 09:30:00', '2025-01-18 09:30:09', 'admin', 'admin');
INSERT INTO `android_resign_task` (`id`, `task_id`, `original_apk_url`, `certificate_id`, `app_name`, `app_version`, `package_name`, `permissions`, `resigned_apk_url`, `status`, `fail_reason`, `retry_count`, `processing_time`, `original_file_size`, `resigned_file_size`, `callback_url`, `description`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (2, 'android_task_20250118_002', '/uploads/android/gameapp_android.apk', 5, 'GameApp', '1.8.0', 'com.game.adventure', '{\"INTERNET\": true, \"ACCESS_NETWORK_STATE\": true, \"VIBRATE\": true}', '/downloads/android/gameapp_signed.apk', 'SUCCESS', NULL, 0, 12340, 67108864, 67110000, NULL, '游戏应用Android版重签名', '2025-01-18 15:20:00', '2025-01-18 15:20:12', 'user001', 'user001');
INSERT INTO `android_resign_task` (`id`, `task_id`, `original_apk_url`, `certificate_id`, `app_name`, `app_version`, `package_name`, `permissions`, `resigned_apk_url`, `status`, `fail_reason`, `retry_count`, `processing_time`, `original_file_size`, `resigned_file_size`, `callback_url`, `description`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (3, 'android_task_20250119_003', '/uploads/android/enterprise_tool.apk', 3, 'EnterpriseTool', '2.0.1', 'com.enterprise.tool', '{\"READ_PHONE_STATE\": true, \"ACCESS_WIFI_STATE\": true}', NULL, 'PROCESSING', NULL, 0, NULL, 18874368, NULL, 'https://api.example.com/callback/android/003', '企业工具应用重签名', '2025-01-19 10:15:00', '2025-01-19 10:15:00', 'admin', 'admin');
INSERT INTO `android_resign_task` (`id`, `task_id`, `original_apk_url`, `certificate_id`, `app_name`, `app_version`, `package_name`, `permissions`, `resigned_apk_url`, `status`, `fail_reason`, `retry_count`, `processing_time`, `original_file_size`, `resigned_file_size`, `callback_url`, `description`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (4, 'android_task_20250119_004', '/uploads/android/beta_test.apk', 4, 'BetaApp', '0.5.0', 'com.beta.testapp', '{\"CAMERA\": true, \"RECORD_AUDIO\": true}', NULL, 'FAILED', 'APK文件损坏，无法解析', 1, NULL, 12582912, NULL, NULL, 'Beta测试应用重签名', '2025-01-19 14:45:00', '2025-01-19 14:46:30', 'user001', 'user001');
INSERT INTO `android_resign_task` (`id`, `task_id`, `original_apk_url`, `certificate_id`, `app_name`, `app_version`, `package_name`, `permissions`, `resigned_apk_url`, `status`, `fail_reason`, `retry_count`, `processing_time`, `original_file_size`, `resigned_file_size`, `callback_url`, `description`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (5, 'android_task_20250120_005', '/uploads/android/utility_app.apk', 2, 'UtilityApp', '1.1.0', 'com.utility.helper', '{\"WRITE_EXTERNAL_STORAGE\": true, \"READ_EXTERNAL_STORAGE\": true}', NULL, 'PENDING', NULL, 0, NULL, 8388608, NULL, 'https://api.example.com/callback/android/005', '实用工具应用重签名', '2025-01-20 16:30:00', '2025-01-20 16:30:00', 'admin', 'admin');
INSERT INTO `android_resign_task` (`id`, `task_id`, `original_apk_url`, `certificate_id`, `app_name`, `app_version`, `package_name`, `permissions`, `resigned_apk_url`, `status`, `fail_reason`, `retry_count`, `processing_time`, `original_file_size`, `resigned_file_size`, `callback_url`, `description`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (6, 'android_task_20250121_006', '/uploads/android/social_app.apk', 1, 'SocialApp', '2.3.1', 'com.social.connect', '{\"CAMERA\": true, \"LOCATION\": true, \"CONTACTS\": true, \"MICROPHONE\": true}', '/downloads/android/social_signed.apk', 'SUCCESS', NULL, 0, 19850, 45097156, 45099000, 'https://api.example.com/callback/android/006', '社交应用重签名完成', '2025-01-21 08:20:00', '2025-01-21 08:20:20', 'user001', 'user001');
COMMIT;

-- ----------------------------
-- Table structure for ios_certificate
-- ----------------------------
DROP TABLE IF EXISTS `ios_certificate`;
CREATE TABLE `ios_certificate` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '证书ID',
  `name` varchar(255) NOT NULL COMMENT '证书名称',
  `file_url` varchar(500) NOT NULL COMMENT '证书文件URL（p12格式）',
  `password` varchar(255) NOT NULL COMMENT '证书密码',
  `team_id` varchar(50) DEFAULT NULL COMMENT '开发者团队ID',
  `bundle_id` varchar(255) DEFAULT NULL COMMENT '主应用Bundle ID',
  `certificate_type` varchar(50) NOT NULL DEFAULT 'DISTRIBUTION' COMMENT '证书类型：DEVELOPMENT, DISTRIBUTION, ADHOC',
  `expire_date` datetime DEFAULT NULL COMMENT '证书过期时间',
  `description` text COMMENT '证书描述',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, INACTIVE, EXPIRED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `subject` varchar(500) DEFAULT NULL COMMENT '证书主题名称',
  `issuer` varchar(500) DEFAULT NULL COMMENT '证书签发者',
  `serial_number` varchar(100) DEFAULT NULL COMMENT '证书序列号',
  PRIMARY KEY (`id`),
  KEY `idx_ios_cert_name` (`name`),
  KEY `idx_ios_cert_type` (`certificate_type`),
  KEY `idx_ios_cert_status` (`status`),
  KEY `idx_ios_cert_create_time` (`create_time`),
  KEY `idx_ios_cert_bundle_id` (`bundle_id`),
  KEY `idx_ios_cert_serial` (`serial_number`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='iOS签名证书表';

-- ----------------------------
-- Records of ios_certificate
-- ----------------------------
BEGIN;
INSERT INTO `ios_certificate` (`id`, `name`, `file_url`, `password`, `team_id`, `bundle_id`, `certificate_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (1, 'iOS开发证书-TeamA', '/certificates/ios/dev_team_a.p12', 'dev123456', 'ABCD123456', 'com.teama.myapp', 'DEVELOPMENT', '2025-12-31 23:59:59', '团队A的iOS开发证书，用于开发环境签名', 'ACTIVE', '2025-01-15 10:30:00', '2025-08-24 10:54:43', 'admin', 'admin', 'CN=iPhone Developer: John Doe (ABCD123456), OU=ABCD123456, O=John Doe, C=US', 'CN=Apple Worldwide Developer Relations Certification Authority, OU=Apple Worldwide Developer Relations, O=Apple Inc., C=US', '1234567890abcdef');
INSERT INTO `ios_certificate` (`id`, `name`, `file_url`, `password`, `team_id`, `bundle_id`, `certificate_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (2, 'iOS发布证书-TeamA', '/certificates/ios/dist_team_a.p12', 'dist123456', 'ABCD123456', 'com.teama.myapp', 'DISTRIBUTION', '2025-11-30 23:59:59', '团队A的iOS发布证书，用于App Store发布', 'ACTIVE', '2025-01-15 11:00:00', '2025-08-24 10:54:43', 'admin', 'admin', 'CN=iPhone Distribution: MyCompany Ltd (XYZ9876543), OU=XYZ9876543, O=MyCompany Ltd, C=CN', 'CN=Apple Worldwide Developer Relations Certification Authority, OU=Apple Worldwide Developer Relations, O=Apple Inc., C=US', 'fedcba0987654321');
INSERT INTO `ios_certificate` (`id`, `name`, `file_url`, `password`, `team_id`, `bundle_id`, `certificate_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (3, 'iOS企业证书-CompanyB', '/certificates/ios/enterprise_b.p12', 'ent789012', 'EFGH789012', 'com.companyb.gameapp', 'DISTRIBUTION', '2025-10-15 23:59:59', '公司B的企业证书，用于内部分发', 'ACTIVE', '2025-01-16 09:15:00', '2025-08-24 10:54:43', 'admin', 'admin', 'CN=Apple Development: Test User (TEST123456), OU=TEST123456, O=Test Organization, C=US', 'CN=Apple Worldwide Developer Relations Certification Authority, OU=Apple Worldwide Developer Relations, O=Apple Inc., C=US', '1122334455667788');
INSERT INTO `ios_certificate` (`id`, `name`, `file_url`, `password`, `team_id`, `bundle_id`, `certificate_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (4, 'iOS测试证书-TeamC', '/certificates/ios/adhoc_team_c.p12', 'test345678', 'IJKL345678', 'com.teamc.testapp', 'ADHOC', '2025-09-30 23:59:59', '团队C的Ad Hoc证书，用于测试分发', 'ACTIVE', '2025-01-17 14:20:00', '2025-08-24 10:54:43', 'user001', 'user001', 'CN=iOS Certificate 4, O=Default Org, C=US', 'CN=Apple Worldwide Developer Relations Certification Authority, OU=Apple Worldwide Developer Relations, O=Apple Inc., C=US', 'ios0000000000004');
INSERT INTO `ios_certificate` (`id`, `name`, `file_url`, `password`, `team_id`, `bundle_id`, `certificate_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (5, 'iOS过期证书-OldTeam', '/certificates/ios/expired_old.p12', 'old999999', 'MNOP999999', 'com.oldteam.legacy', 'DEVELOPMENT', '2024-12-31 23:59:59', '已过期的开发证书', 'EXPIRED', '2024-06-01 08:00:00', '2025-08-24 10:54:43', 'admin', 'system', 'CN=iOS Certificate 5, O=Default Org, C=US', 'CN=Apple Worldwide Developer Relations Certification Authority, OU=Apple Worldwide Developer Relations, O=Apple Inc., C=US', 'ios0000000000005');
INSERT INTO `ios_certificate` (`id`, `name`, `file_url`, `password`, `team_id`, `bundle_id`, `certificate_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (6, 'iOS电商应用证书', '/certificates/ios/ecommerce_cert.p12', 'ecom123456', 'SHOP123456', 'com.shop.ecommerce', 'DISTRIBUTION', '2025-08-31 23:59:59', '电商应用的iOS发布证书', 'ACTIVE', '2025-01-22 10:00:00', '2025-08-24 10:54:43', 'admin', 'admin', 'CN=iOS Certificate 6, O=Default Org, C=US', 'CN=Apple Worldwide Developer Relations Certification Authority, OU=Apple Worldwide Developer Relations, O=Apple Inc., C=US', 'ios0000000000006');
INSERT INTO `ios_certificate` (`id`, `name`, `file_url`, `password`, `team_id`, `bundle_id`, `certificate_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (7, 'iOS社交应用证书', '/certificates/ios/social_cert.p12', 'social789', 'SOCIAL7890', 'com.social.connect', 'DEVELOPMENT', '2025-07-15 23:59:59', '社交应用的iOS开发证书', 'ACTIVE', '2025-01-22 11:00:00', '2025-08-24 10:54:43', 'developer01', 'developer01', 'CN=iOS Certificate 7, O=Default Org, C=US', 'CN=Apple Worldwide Developer Relations Certification Authority, OU=Apple Worldwide Developer Relations, O=Apple Inc., C=US', 'ios0000000000007');
INSERT INTO `ios_certificate` (`id`, `name`, `file_url`, `password`, `team_id`, `bundle_id`, `certificate_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (8, 'iOS教育应用证书', '/certificates/ios/education_cert.p12', 'edu456789', 'EDU4567890', 'com.education.learn', 'DISTRIBUTION', '2025-06-30 23:59:59', '教育应用的iOS发布证书', 'ACTIVE', '2025-01-22 12:00:00', '2025-08-24 10:54:43', 'developer02', 'developer02', 'CN=iOS Certificate 8, O=Default Org, C=US', 'CN=Apple Worldwide Developer Relations Certification Authority, OU=Apple Worldwide Developer Relations, O=Apple Inc., C=US', 'ios0000000000008');
INSERT INTO `ios_certificate` (`id`, `name`, `file_url`, `password`, `team_id`, `bundle_id`, `certificate_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (9, 'iOS金融应用证书', '/certificates/ios/finance_cert.p12', 'fin999888', 'FIN9998880', 'com.finance.bank', 'DISTRIBUTION', '2025-05-31 23:59:59', '金融应用的iOS发布证书', 'ACTIVE', '2025-01-22 13:00:00', '2025-08-24 10:54:43', 'admin', 'admin', 'CN=iOS Certificate 9, O=Default Org, C=US', 'CN=Apple Worldwide Developer Relations Certification Authority, OU=Apple Worldwide Developer Relations, O=Apple Inc., C=US', 'ios0000000000009');
INSERT INTO `ios_certificate` (`id`, `name`, `file_url`, `password`, `team_id`, `bundle_id`, `certificate_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`, `subject`, `issuer`, `serial_number`) VALUES (10, 'iOS工具应用证书', '/certificates/ios/tools_cert.p12', 'tools12345', 'TOOLS12345', 'com.tools.utility', 'DEVELOPMENT', '2025-04-30 23:59:59', '工具应用的iOS开发证书', 'ACTIVE', '2025-01-22 14:00:00', '2025-08-24 10:54:43', 'developer01', 'developer01', 'CN=iOS Certificate 10, O=Default Org, C=US', 'CN=Apple Worldwide Developer Relations Certification Authority, OU=Apple Worldwide Developer Relations, O=Apple Inc., C=US', 'ios0000000000010');
COMMIT;

-- ----------------------------
-- Table structure for ios_profile
-- ----------------------------
DROP TABLE IF EXISTS `ios_profile`;
CREATE TABLE `ios_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Profile ID',
  `name` varchar(255) NOT NULL COMMENT 'Profile名称',
  `certificate_id` bigint NOT NULL COMMENT '关联的iOS证书ID',
  `bundle_id` varchar(255) NOT NULL COMMENT 'Bundle ID',
  `file_url` varchar(500) NOT NULL COMMENT 'Profile文件URL（mobileprovision格式）',
  `team_id` varchar(50) DEFAULT NULL COMMENT '开发者团队ID',
  `profile_type` varchar(50) NOT NULL DEFAULT 'DISTRIBUTION' COMMENT 'Profile类型：DEVELOPMENT, DISTRIBUTION, ADHOC',
  `expire_date` datetime DEFAULT NULL COMMENT 'Profile过期时间',
  `description` text COMMENT 'Profile描述',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, INACTIVE, EXPIRED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_ios_profile_name` (`name`),
  KEY `idx_ios_profile_bundle_id` (`bundle_id`),
  KEY `idx_ios_profile_cert_id` (`certificate_id`),
  KEY `idx_ios_profile_type` (`profile_type`),
  KEY `idx_ios_profile_status` (`status`),
  KEY `idx_ios_profile_create_time` (`create_time`),
  CONSTRAINT `fk_ios_profile_certificate` FOREIGN KEY (`certificate_id`) REFERENCES `ios_certificate` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='iOS Profile描述文件表';

-- ----------------------------
-- Records of ios_profile
-- ----------------------------
BEGIN;
INSERT INTO `ios_profile` (`id`, `name`, `certificate_id`, `bundle_id`, `file_url`, `team_id`, `profile_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (1, 'MyApp开发Profile', 1, 'com.teama.myapp', '/profiles/ios/myapp_dev.mobileprovision', 'ABCD123456', 'DEVELOPMENT', '2025-12-31 23:59:59', 'MyApp应用的开发Profile', 'ACTIVE', '2025-01-15 10:35:00', '2025-01-15 10:35:00', 'admin', 'admin');
INSERT INTO `ios_profile` (`id`, `name`, `certificate_id`, `bundle_id`, `file_url`, `team_id`, `profile_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (2, 'MyApp发布Profile', 2, 'com.teama.myapp', '/profiles/ios/myapp_dist.mobileprovision', 'ABCD123456', 'DISTRIBUTION', '2025-11-30 23:59:59', 'MyApp应用的发布Profile', 'ACTIVE', '2025-01-15 11:05:00', '2025-01-15 11:05:00', 'admin', 'admin');
INSERT INTO `ios_profile` (`id`, `name`, `certificate_id`, `bundle_id`, `file_url`, `team_id`, `profile_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (3, 'GameApp企业Profile', 3, 'com.companyb.gameapp', '/profiles/ios/gameapp_ent.mobileprovision', 'EFGH789012', 'DISTRIBUTION', '2025-10-15 23:59:59', 'GameApp的企业分发Profile', 'ACTIVE', '2025-01-16 09:20:00', '2025-01-16 09:20:00', 'admin', 'admin');
INSERT INTO `ios_profile` (`id`, `name`, `certificate_id`, `bundle_id`, `file_url`, `team_id`, `profile_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (4, 'TestApp测试Profile', 4, 'com.teamc.testapp', '/profiles/ios/testapp_adhoc.mobileprovision', 'IJKL345678', 'ADHOC', '2025-09-30 23:59:59', 'TestApp的Ad Hoc测试Profile', 'ACTIVE', '2025-01-17 14:25:00', '2025-01-17 14:25:00', 'user001', 'user001');
INSERT INTO `ios_profile` (`id`, `name`, `certificate_id`, `bundle_id`, `file_url`, `team_id`, `profile_type`, `expire_date`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (5, 'UtilsApp开发Profile', 1, 'com.teama.utils', '/profiles/ios/utils_dev.mobileprovision', 'ABCD123456', 'DEVELOPMENT', '2025-12-31 23:59:59', 'UtilsApp工具应用的开发Profile', 'ACTIVE', '2025-01-18 16:10:00', '2025-01-18 16:10:00', 'user001', 'user001');
COMMIT;

-- ----------------------------
-- Table structure for ios_resign_task
-- ----------------------------
DROP TABLE IF EXISTS `ios_resign_task`;
CREATE TABLE `ios_resign_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_id` varchar(64) NOT NULL COMMENT '任务唯一标识',
  `original_ipa_url` varchar(500) NOT NULL COMMENT '原始IPA文件URL',
  `certificate_id` bigint NOT NULL COMMENT '使用的iOS证书ID',
  `bundle_config` text NOT NULL COMMENT 'Bundle ID配置信息（JSON格式，key是bundleId，value是profileId）',
  `app_name` varchar(255) DEFAULT NULL COMMENT '应用名称（从IPA解析）',
  `app_version` varchar(50) DEFAULT NULL COMMENT '应用版本（从IPA解析）',
  `app_build` varchar(50) DEFAULT NULL COMMENT '构建版本（从IPA解析）',
  `bundle_ids` text COMMENT '所有Bundle ID列表（JSON数组格式）',
  `permissions` text COMMENT '应用权限信息（JSON格式）',
  `resigned_ipa_url` varchar(500) DEFAULT NULL COMMENT '重签名后的IPA文件URL',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING, PROCESSING, SUCCESS, FAILED',
  `fail_reason` text COMMENT '失败原因',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `processing_time` bigint DEFAULT NULL COMMENT '处理耗时（毫秒）',
  `original_file_size` bigint DEFAULT NULL COMMENT '原始文件大小（字节）',
  `resigned_file_size` bigint DEFAULT NULL COMMENT '重签名后文件大小（字节）',
  `callback_url` varchar(500) DEFAULT NULL COMMENT '回调URL',
  `description` varchar(500) DEFAULT NULL COMMENT '任务描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ios_task_id` (`task_id`),
  KEY `idx_ios_task_status` (`status`),
  KEY `idx_ios_task_cert_id` (`certificate_id`),
  KEY `idx_ios_task_create_time` (`create_time`),
  CONSTRAINT `fk_ios_task_certificate` FOREIGN KEY (`certificate_id`) REFERENCES `ios_certificate` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='iOS重签名任务表';

-- ----------------------------
-- Records of ios_resign_task
-- ----------------------------
BEGIN;
INSERT INTO `ios_resign_task` (`id`, `task_id`, `original_ipa_url`, `certificate_id`, `bundle_config`, `app_name`, `app_version`, `app_build`, `bundle_ids`, `permissions`, `resigned_ipa_url`, `status`, `fail_reason`, `retry_count`, `processing_time`, `original_file_size`, `resigned_file_size`, `callback_url`, `description`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (1, 'ios_task_20250120_001', '/uploads/ios/myapp_original.ipa', 2, '{\"com.teama.myapp\": 2}', 'MyApp', '1.2.0', '100', '[\"com.teama.myapp\"]', '{\"camera\": true, \"location\": true, \"push\": true}', '/downloads/ios/myapp_resigned.ipa', 'SUCCESS', NULL, 0, 15420, 52428800, 52430000, 'https://api.example.com/callback/ios/001', 'MyApp生产版本重签名', '2025-01-20 10:30:00', '2025-01-20 10:30:25', 'admin', 'admin');
INSERT INTO `ios_resign_task` (`id`, `task_id`, `original_ipa_url`, `certificate_id`, `bundle_config`, `app_name`, `app_version`, `app_build`, `bundle_ids`, `permissions`, `resigned_ipa_url`, `status`, `fail_reason`, `retry_count`, `processing_time`, `original_file_size`, `resigned_file_size`, `callback_url`, `description`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (2, 'ios_task_20250120_002', '/uploads/ios/gameapp_v2.ipa', 3, '{\"com.companyb.gameapp\": 3}', 'GameApp', '2.1.5', '205', '[\"com.companyb.gameapp\"]', '{\"camera\": false, \"microphone\": true, \"storage\": true}', '/downloads/ios/gameapp_resigned.ipa', 'SUCCESS', NULL, 0, 28750, 125829120, 125831000, NULL, '游戏应用企业版重签名', '2025-01-20 14:15:00', '2025-01-20 14:15:29', 'user001', 'user001');
INSERT INTO `ios_resign_task` (`id`, `task_id`, `original_ipa_url`, `certificate_id`, `bundle_config`, `app_name`, `app_version`, `app_build`, `bundle_ids`, `permissions`, `resigned_ipa_url`, `status`, `fail_reason`, `retry_count`, `processing_time`, `original_file_size`, `resigned_file_size`, `callback_url`, `description`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (3, 'ios_task_20250121_003', '/uploads/ios/testapp_beta.ipa', 4, '{\"com.teamc.testapp\": 4}', 'TestApp', '0.9.0', '90', '[\"com.teamc.testapp\"]', '{\"location\": true, \"contacts\": true}', NULL, 'PROCESSING', NULL, 0, NULL, 31457280, NULL, 'https://api.example.com/callback/ios/003', 'TestApp Beta版本重签名', '2025-01-21 09:45:00', '2025-01-21 09:45:00', 'user001', 'user001');
INSERT INTO `ios_resign_task` (`id`, `task_id`, `original_ipa_url`, `certificate_id`, `bundle_config`, `app_name`, `app_version`, `app_build`, `bundle_ids`, `permissions`, `resigned_ipa_url`, `status`, `fail_reason`, `retry_count`, `processing_time`, `original_file_size`, `resigned_file_size`, `callback_url`, `description`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (4, 'ios_task_20250121_004', '/uploads/ios/utils_v1.ipa', 1, '{\"com.teama.utils\": 5}', 'UtilsApp', '1.0.0', '10', '[\"com.teama.utils\"]', '{\"storage\": true}', NULL, 'FAILED', '证书已过期，无法完成签名', 2, NULL, 15728640, NULL, NULL, '工具应用开发版重签名', '2025-01-21 16:20:00', '2025-01-21 16:22:15', 'admin', 'admin');
INSERT INTO `ios_resign_task` (`id`, `task_id`, `original_ipa_url`, `certificate_id`, `bundle_config`, `app_name`, `app_version`, `app_build`, `bundle_ids`, `permissions`, `resigned_ipa_url`, `status`, `fail_reason`, `retry_count`, `processing_time`, `original_file_size`, `resigned_file_size`, `callback_url`, `description`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (5, 'ios_task_20250122_005', '/uploads/ios/multiapp_complex.ipa', 2, '{\"com.teama.main\": 2, \"com.teama.extension\": 2}', 'MultiApp', '3.0.0', '300', '[\"com.teama.main\", \"com.teama.extension\"]', '{\"camera\": true, \"location\": true, \"push\": true, \"health\": true}', NULL, 'PENDING', NULL, 0, NULL, 89478485, NULL, 'https://api.example.com/callback/ios/005', '多Bundle应用重签名任务', '2025-01-22 11:00:00', '2025-01-22 11:00:00', 'admin', 'admin');
COMMIT;

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `level` varchar(10) NOT NULL COMMENT '日志级别：DEBUG, INFO, WARN, ERROR',
  `message` text NOT NULL COMMENT '日志内容',
  `logger` varchar(255) DEFAULT NULL COMMENT '日志来源（类名）',
  `thread` varchar(100) DEFAULT NULL COMMENT '线程名',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '操作用户名',
  `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `action` varchar(50) DEFAULT NULL COMMENT '操作类型',
  `request_uri` varchar(500) DEFAULT NULL COMMENT '请求URI',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `request_params` text COMMENT '请求参数',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
  `user_agent` varchar(1000) DEFAULT NULL COMMENT '用户代理',
  `exception` text COMMENT '异常信息',
  `execution_time` bigint DEFAULT NULL COMMENT '执行时间（毫秒）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_log_level` (`level`),
  KEY `idx_sys_log_user_id` (`user_id`),
  KEY `idx_sys_log_module` (`module`),
  KEY `idx_sys_log_action` (`action`),
  KEY `idx_sys_log_create_time` (`create_time`),
  KEY `idx_sys_log_logger` (`logger`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统日志表';

-- ----------------------------
-- Records of sys_log
-- ----------------------------
BEGIN;
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (1, 'INFO', '用户登录成功', 'com.example.resign.controller.AuthController', 'http-nio-8080-exec-1', 1, 'admin', '用户管理', '登录', '/api/auth/login', 'POST', NULL, '127.0.0.1', NULL, NULL, NULL, '2025-08-23 17:03:49');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (2, 'INFO', '创建iOS重签名任务', 'com.example.resign.controller.IosResignTaskController', 'http-nio-8080-exec-2', 1, 'admin', '任务管理', '创建任务', '/api/ios/resign-task', 'POST', NULL, '127.0.0.1', NULL, NULL, NULL, '2025-08-23 16:03:49');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (3, 'WARN', '证书即将过期', 'com.example.resign.service.CertificateService', 'scheduler-1', NULL, 'system', '证书管理', '检查过期', NULL, NULL, NULL, '127.0.0.1', NULL, NULL, NULL, '2025-08-23 15:03:49');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (4, 'ERROR', '任务执行失败：签名证书无效', 'com.example.resign.service.IosResignTaskServiceImpl', 'task-executor-1', 2, 'user001', '任务管理', '执行任务', NULL, NULL, NULL, '192.168.1.100', NULL, NULL, NULL, '2025-08-23 14:03:49');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (5, 'INFO', '系统启动完成', 'com.example.resign.ResignSystemApplication', 'main', NULL, 'system', '系统管理', '启动', NULL, NULL, NULL, '127.0.0.1', NULL, NULL, NULL, '2025-08-22 17:03:49');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (6, 'INFO', '测试日志', 'test.logger', 'main', NULL, 'testuser', '测试模块', '测试操作', NULL, NULL, NULL, '127.0.0.1', NULL, NULL, NULL, '2025-08-24 01:33:51');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (7, 'INFO', '用户登录成功', 'com.example.resign.controller.AuthController', 'http-nio-8080-exec-3', 5, 'developer01', '用户管理', '登录', '/api/auth/login', 'POST', '{\"username\":\"developer01\"}', '192.168.1.105', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', NULL, 245, '2025-01-22 09:30:00');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (8, 'INFO', '创建iOS证书', 'com.example.resign.controller.IosController', 'http-nio-8080-exec-4', 1, 'admin', '证书管理', '创建证书', '/api/ios/certificate', 'POST', '{\"name\":\"新iOS证书\"}', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', NULL, 1250, '2025-01-22 10:15:00');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (9, 'WARN', 'Android证书即将过期', 'com.example.resign.service.AndroidCertificateService', 'scheduler-2', NULL, 'system', '证书管理', '检查过期', NULL, NULL, NULL, '127.0.0.1', NULL, NULL, NULL, '2025-01-22 06:00:00');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (10, 'ERROR', 'iOS重签名任务失败', 'com.example.resign.service.IosResignTaskServiceImpl', 'task-executor-2', 1, 'admin', '任务管理', '执行任务', NULL, NULL, '{\"taskId\":\"ios_task_20250121_004\"}', '127.0.0.1', NULL, 'java.security.cert.CertificateExpiredException: 证书已过期', 5420, '2025-01-21 16:22:15');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (11, 'INFO', '批量导入Android证书', 'com.example.resign.controller.AndroidController', 'http-nio-8080-exec-5', 6, 'developer02', '证书管理', '批量导入', '/api/android/certificate/batch', 'POST', '{\"count\":3}', '192.168.1.106', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NULL, 3250, '2025-01-21 15:30:00');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (12, 'DEBUG', '任务状态检查', 'com.example.resign.service.TaskMonitorService', 'monitor-thread-1', NULL, 'system', '任务管理', '状态检查', NULL, NULL, NULL, '127.0.0.1', NULL, NULL, 150, '2025-01-22 11:00:00');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (13, 'INFO', '用户权限更新', 'com.example.resign.controller.UserController', 'http-nio-8080-exec-6', 1, 'admin', '用户管理', '权限更新', '/api/user/5/permissions', 'PUT', '{\"permissions\":[\"TASK:CREATE\",\"TASK:VIEW\"]}', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', NULL, 680, '2025-01-22 14:20:00');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (14, 'WARN', '磁盘空间不足', 'com.example.resign.service.SystemMonitorService', 'monitor-thread-2', NULL, 'system', '系统管理', '磁盘检查', NULL, NULL, NULL, '127.0.0.1', NULL, NULL, NULL, '2025-01-22 12:00:00');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (15, 'ERROR', 'Android APK解析失败', 'com.example.resign.service.AndroidResignTaskServiceImpl', 'task-executor-3', 4, 'user001', '任务管理', '执行任务', NULL, NULL, '{\"taskId\":\"android_task_20250119_004\"}', '192.168.1.100', NULL, 'java.util.zip.ZipException: APK文件格式错误', 2100, '2025-01-19 14:46:30');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (16, 'INFO', '系统配置更新', 'com.example.resign.controller.SystemController', 'http-nio-8080-exec-7', 1, 'admin', '系统管理', '配置更新', '/api/system/config', 'PUT', '{\"maxTaskCount\":100}', '127.0.0.1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', NULL, 420, '2025-01-22 16:45:00');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (17, 'INFO', '定时任务执行', 'com.example.resign.task.CertificateExpiryTask', 'scheduler-3', NULL, 'system', '证书管理', '过期检查', NULL, NULL, NULL, '127.0.0.1', NULL, NULL, 850, '2025-01-22 18:00:00');
INSERT INTO `sys_log` (`id`, `level`, `message`, `logger`, `thread`, `user_id`, `username`, `module`, `action`, `request_uri`, `request_method`, `request_params`, `client_ip`, `user_agent`, `exception`, `execution_time`, `create_time`) VALUES (18, 'DEBUG', '缓存清理完成', 'com.example.resign.service.CacheService', 'cache-cleaner-1', NULL, 'system', '系统管理', '缓存清理', NULL, NULL, NULL, '127.0.0.1', NULL, NULL, 320, '2025-01-22 20:00:00');
COMMIT;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父权限ID',
  `name` varchar(50) NOT NULL COMMENT '权限名称',
  `code` varchar(100) NOT NULL COMMENT '权限编码',
  `type` varchar(20) NOT NULL COMMENT '权限类型：menu-菜单，button-按钮，api-接口',
  `path` varchar(255) DEFAULT NULL COMMENT '路径/接口地址',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `description` varchar(255) DEFAULT NULL COMMENT '权限描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统权限表';

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
BEGIN;
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (1, NULL, '控制台', 'DASHBOARD', 'menu', '/dashboard', 'Odometer', 1, 1, '系统控制台', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (2, NULL, '任务管理', 'TASK', 'menu', '/task', 'Document', 2, 1, '任务管理模块', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (3, NULL, '用户管理', 'USER', 'menu', '/user', 'User', 3, 1, '用户管理模块', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (4, NULL, '系统管理', 'SYSTEM', 'menu', '/system', 'Setting', 4, 1, '系统管理模块', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (11, 1, '查看控制台', 'DASHBOARD:VIEW', 'button', '', '', 1, 1, '查看控制台权限', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (21, 2, '任务列表', 'TASK:LIST', 'menu', '/task/list', 'List', 1, 1, '任务列表页面', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (22, 2, '创建任务', 'TASK:CREATE', 'menu', '/task/create', 'Plus', 2, 1, '创建任务页面', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (23, 2, '查看任务', 'TASK:VIEW', 'button', '', '', 3, 1, '查看任务详情权限', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (24, 2, '编辑任务', 'TASK:EDIT', 'button', '', '', 4, 1, '编辑任务权限', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (25, 2, '删除任务', 'TASK:DELETE', 'button', '', '', 5, 1, '删除任务权限', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (26, 2, '重试任务', 'TASK:RETRY', 'button', '', '', 6, 1, '重试任务权限', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (31, 3, '用户列表', 'USER:LIST', 'menu', '/user/list', 'UserFilled', 1, 1, '用户列表页面', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (32, 3, '角色管理', 'USER:ROLE', 'menu', '/user/role', 'Avatar', 2, 1, '角色管理页面', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (33, 3, '权限管理', 'USER:PERMISSION', 'menu', '/user/permission', 'Key', 3, 1, '权限管理页面', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (34, 3, '创建用户', 'USER:CREATE', 'button', '', '', 4, 1, '创建用户权限', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (35, 3, '编辑用户', 'USER:EDIT', 'button', '', '', 5, 1, '编辑用户权限', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (36, 3, '删除用户', 'USER:DELETE', 'button', '', '', 6, 1, '删除用户权限', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (37, 3, '重置密码', 'USER:RESET_PASSWORD', 'button', '', '', 7, 1, '重置用户密码权限', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (41, 4, '日志管理', 'SYSTEM:LOG', 'menu', '/system/log', 'Document', 1, 1, '日志管理页面', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `type`, `path`, `icon`, `sort`, `status`, `description`, `create_time`, `update_time`) VALUES (42, 4, '系统配置', 'SYSTEM:CONFIG', 'menu', '/system/config', 'Tools', 2, 1, '系统配置页面', '2025-08-13 15:51:32', '2025-08-13 15:51:32');
COMMIT;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色表';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1, '2025-08-13 15:51:32', '2025-08-13 15:51:32', 'system', 'system');
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (2, '普通用户', 'USER', '普通用户，只能查看和操作自己的数据', 1, '2025-08-13 15:51:32', '2025-08-13 15:51:32', 'system', 'system');
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (3, '审核员', 'AUDITOR', '负责审核任务和数据', 1, '2025-08-13 15:51:32', '2025-08-13 15:51:32', 'system', 'system');
COMMIT;

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (1, 1, 1, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (2, 1, 2, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (3, 1, 3, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (4, 1, 4, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (5, 1, 11, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (6, 1, 21, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (7, 1, 22, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (8, 1, 23, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (9, 1, 24, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (10, 1, 25, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (11, 1, 26, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (12, 1, 31, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (13, 1, 32, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (14, 1, 33, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (15, 1, 34, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (16, 1, 35, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (17, 1, 36, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (18, 1, 37, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (19, 1, 41, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (20, 1, 42, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (32, 2, 1, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (33, 2, 11, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (34, 2, 2, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (35, 2, 21, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (36, 2, 23, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (37, 3, 1, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (38, 3, 11, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (39, 3, 2, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (40, 3, 21, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (41, 3, 22, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (42, 3, 23, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (43, 3, 26, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (44, 3, 4, '2025-08-13 15:51:32');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`) VALUES (45, 3, 41, '2025-08-13 15:51:32');
COMMIT;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `nickname` varchar(50) NOT NULL COMMENT '昵称',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `avatar` varchar(1000) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (1, 'admin', '管理员', 'admin@example.com', '15201051233', 'e10adc3949ba59abbe56e057f20f883e', '/api/files/avatars/avatar_1_1755190388688.png', 1, '2025-08-24 16:41:34', '127.0.0.1', '2025-08-13 15:51:32', '2025-08-15 00:54:32', 'system', '1');
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (2, 'user001', '普通用户1', 'user001@example.com', '13800138001', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, '2025-08-12 15:51:32', '192.168.1.100', '2025-08-13 15:51:32', '2025-08-13 15:51:32', 'admin', 'admin');
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (3, 'user002', '普通用户2', 'user002@example.com', '13800138002', 'e10adc3949ba59abbe56e057f20f883e', NULL, 0, NULL, NULL, '2025-08-13 15:51:32', '2025-08-13 15:51:32', 'admin', 'admin');
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (4, 'auditor', '审核员', 'auditor@example.com', '13800138003', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, '2025-08-13 13:51:32', '192.168.1.101', '2025-08-13 15:51:32', '2025-08-13 15:51:32', 'admin', 'admin');
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (5, 'developer01', '开发者张三', 'zhangsan@company.com', '13912345678', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, '2025-01-22 09:30:00', '192.168.1.105', '2025-01-15 10:00:00', '2025-01-22 09:30:00', 'admin', 'admin');
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (6, 'developer02', '开发者李四', 'lisi@company.com', '13987654321', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, '2025-01-21 16:45:00', '192.168.1.106', '2025-01-15 10:30:00', '2025-01-21 16:45:00', 'admin', 'admin');
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (7, 'tester01', '测试员王五', 'wangwu@company.com', '13800001111', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, '2025-01-22 08:15:00', '192.168.1.107', '2025-01-16 09:00:00', '2025-01-22 08:15:00', 'admin', 'admin');
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (8, 'manager01', '项目经理赵六', 'zhaoliu@company.com', '13800002222', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, '2025-01-21 18:20:00', '192.168.1.108', '2025-01-16 14:00:00', '2025-01-21 18:20:00', 'admin', 'admin');
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (9, 'guest01', '访客用户', 'guest@example.com', '13800003333', 'e10adc3949ba59abbe56e057f20f883e', NULL, 0, NULL, NULL, '2025-01-17 11:00:00', '2025-01-20 10:00:00', 'admin', 'admin');
COMMIT;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (1, 1, 1, '2025-08-13 15:51:32');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (2, 2, 2, '2025-08-13 15:51:32');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (3, 3, 2, '2025-08-13 15:51:32');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (4, 4, 3, '2025-08-13 15:51:32');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (5, 5, 2, '2025-01-15 10:00:00');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (6, 6, 2, '2025-01-15 10:30:00');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (7, 7, 3, '2025-01-16 09:00:00');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (8, 8, 3, '2025-01-16 14:00:00');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (9, 9, 2, '2025-01-17 11:00:00');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
