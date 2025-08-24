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

 Date: 24/08/2025 15:56:32
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
  `expire_date` datetime DEFAULT NULL COMMENT '证书过期时间',
  `description` text COMMENT '证书描述',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, INACTIVE, EXPIRED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_android_cert_name` (`name`),
  KEY `idx_android_cert_status` (`status`),
  KEY `idx_android_cert_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Android签名证书表';

-- ----------------------------
-- Records of android_certificate
-- ----------------------------
BEGIN;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Android重签名任务表';

-- ----------------------------
-- Records of android_resign_task
-- ----------------------------
BEGIN;
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
  `certificate_type` varchar(50) NOT NULL DEFAULT 'DISTRIBUTION' COMMENT '证书类型：DEVELOPMENT, DISTRIBUTION, ADHOC',
  `expire_date` datetime DEFAULT NULL COMMENT '证书过期时间',
  `description` text COMMENT '证书描述',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, INACTIVE, EXPIRED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_ios_cert_name` (`name`),
  KEY `idx_ios_cert_type` (`certificate_type`),
  KEY `idx_ios_cert_status` (`status`),
  KEY `idx_ios_cert_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='iOS签名证书表';

-- ----------------------------
-- Records of ios_certificate
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ios_certificate_profile
-- ----------------------------
DROP TABLE IF EXISTS `ios_certificate_profile`;
CREATE TABLE `ios_certificate_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `certificate_id` bigint NOT NULL COMMENT 'iOS证书ID',
  `profile_id` bigint NOT NULL COMMENT 'iOS Profile ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cert_profile` (`certificate_id`,`profile_id`),
  KEY `idx_cert_profile_cert_id` (`certificate_id`),
  KEY `idx_cert_profile_profile_id` (`profile_id`),
  CONSTRAINT `fk_cert_profile_certificate` FOREIGN KEY (`certificate_id`) REFERENCES `ios_certificate` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cert_profile_profile` FOREIGN KEY (`profile_id`) REFERENCES `ios_profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='证书与Profile关联表';

-- ----------------------------
-- Records of ios_certificate_profile
-- ----------------------------
BEGIN;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='iOS Profile描述文件表';

-- ----------------------------
-- Records of ios_profile
-- ----------------------------
BEGIN;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='iOS重签名任务表';

-- ----------------------------
-- Records of ios_resign_task
-- ----------------------------
BEGIN;
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统日志表';

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (1, 'admin', '管理员', 'admin@example.com', '15201051233', 'e10adc3949ba59abbe56e057f20f883e', '/api/files/avatars/avatar_1_1755190388688.png', 1, '2025-08-23 23:53:00', '127.0.0.1', '2025-08-13 15:51:32', '2025-08-15 00:54:32', 'system', '1');
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (2, 'user001', '普通用户1', 'user001@example.com', '13800138001', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, '2025-08-12 15:51:32', '192.168.1.100', '2025-08-13 15:51:32', '2025-08-13 15:51:32', 'admin', 'admin');
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (3, 'user002', '普通用户2', 'user002@example.com', '13800138002', 'e10adc3949ba59abbe56e057f20f883e', NULL, 0, NULL, NULL, '2025-08-13 15:51:32', '2025-08-13 15:51:32', 'admin', 'admin');
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `avatar`, `status`, `last_login_time`, `last_login_ip`, `create_time`, `update_time`, `create_by`, `update_by`) VALUES (4, 'auditor', '审核员', 'auditor@example.com', '13800138003', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, '2025-08-13 13:51:32', '192.168.1.101', '2025-08-13 15:51:32', '2025-08-13 15:51:32', 'admin', 'admin');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (1, 1, 1, '2025-08-13 15:51:32');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (2, 2, 2, '2025-08-13 15:51:32');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (3, 3, 2, '2025-08-13 15:51:32');
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES (4, 4, 3, '2025-08-13 15:51:32');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
