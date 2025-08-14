/*
 用户管理模块数据库表结构
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统权限表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';

-- ----------------------------
-- 插入初始数据
-- ----------------------------

-- 插入权限数据
INSERT INTO `sys_permission` VALUES 
(1, NULL, '控制台', 'DASHBOARD', 'menu', '/dashboard', 'Odometer', 1, 1, '系统控制台', NOW(), NOW()),
(2, NULL, '任务管理', 'TASK', 'menu', '/task', 'Document', 2, 1, '任务管理模块', NOW(), NOW()),
(3, NULL, '用户管理', 'USER', 'menu', '/user', 'User', 3, 1, '用户管理模块', NOW(), NOW()),
(4, NULL, '系统管理', 'SYSTEM', 'menu', '/system', 'Setting', 4, 1, '系统管理模块', NOW(), NOW()),

-- 控制台子权限
(11, 1, '查看控制台', 'DASHBOARD:VIEW', 'button', '', '', 1, 1, '查看控制台权限', NOW(), NOW()),

-- 任务管理子权限
(21, 2, '任务列表', 'TASK:LIST', 'menu', '/task/list', 'List', 1, 1, '任务列表页面', NOW(), NOW()),
(22, 2, '创建任务', 'TASK:CREATE', 'menu', '/task/create', 'Plus', 2, 1, '创建任务页面', NOW(), NOW()),
(23, 2, '查看任务', 'TASK:VIEW', 'button', '', '', 3, 1, '查看任务详情权限', NOW(), NOW()),
(24, 2, '编辑任务', 'TASK:EDIT', 'button', '', '', 4, 1, '编辑任务权限', NOW(), NOW()),
(25, 2, '删除任务', 'TASK:DELETE', 'button', '', '', 5, 1, '删除任务权限', NOW(), NOW()),
(26, 2, '重试任务', 'TASK:RETRY', 'button', '', '', 6, 1, '重试任务权限', NOW(), NOW()),

-- 用户管理子权限
(31, 3, '用户列表', 'USER:LIST', 'menu', '/user/list', 'UserFilled', 1, 1, '用户列表页面', NOW(), NOW()),
(32, 3, '角色管理', 'USER:ROLE', 'menu', '/user/role', 'Avatar', 2, 1, '角色管理页面', NOW(), NOW()),
(33, 3, '权限管理', 'USER:PERMISSION', 'menu', '/user/permission', 'Key', 3, 1, '权限管理页面', NOW(), NOW()),
(34, 3, '创建用户', 'USER:CREATE', 'button', '', '', 4, 1, '创建用户权限', NOW(), NOW()),
(35, 3, '编辑用户', 'USER:EDIT', 'button', '', '', 5, 1, '编辑用户权限', NOW(), NOW()),
(36, 3, '删除用户', 'USER:DELETE', 'button', '', '', 6, 1, '删除用户权限', NOW(), NOW()),
(37, 3, '重置密码', 'USER:RESET_PASSWORD', 'button', '', '', 7, 1, '重置用户密码权限', NOW(), NOW()),

-- 系统管理子权限
(41, 4, '日志管理', 'SYSTEM:LOG', 'menu', '/system/log', 'Document', 1, 1, '日志管理页面', NOW(), NOW()),
(42, 4, '系统配置', 'SYSTEM:CONFIG', 'menu', '/system/config', 'Tools', 2, 1, '系统配置页面', NOW(), NOW());

-- 插入角色数据
INSERT INTO `sys_role` VALUES 
(1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1, NOW(), NOW(), 'system', 'system'),
(2, '普通用户', 'USER', '普通用户，只能查看和操作自己的数据', 1, NOW(), NOW(), 'system', 'system'),
(3, '审核员', 'AUDITOR', '负责审核任务和数据', 1, NOW(), NOW(), 'system', 'system');

-- 插入用户数据（密码为123456的MD5加密）
INSERT INTO `sys_user` VALUES 
(1, 'admin', '管理员', 'admin@example.com', '13800138000', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, NOW(), '127.0.0.1', NOW(), NOW(), 'system', 'system'),
(2, 'user001', '普通用户1', 'user001@example.com', '13800138001', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), '192.168.1.100', NOW(), NOW(), 'admin', 'admin'),
(3, 'user002', '普通用户2', 'user002@example.com', '13800138002', 'e10adc3949ba59abbe56e057f20f883e', NULL, 0, NULL, NULL, NOW(), NOW(), 'admin', 'admin'),
(4, 'auditor', '审核员', 'auditor@example.com', '13800138003', 'e10adc3949ba59abbe56e057f20f883e', NULL, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), '192.168.1.101', NOW(), NOW(), 'admin', 'admin');

-- 插入用户角色关联数据
INSERT INTO `sys_user_role` VALUES 
(1, 1, 1, NOW()), -- admin -> 超级管理员
(2, 2, 2, NOW()), -- user001 -> 普通用户
(3, 3, 2, NOW()), -- user002 -> 普通用户
(4, 4, 3, NOW()); -- auditor -> 审核员

-- 插入角色权限关联数据
-- 超级管理员拥有所有权限
INSERT INTO `sys_role_permission` (role_id, permission_id, create_time) 
SELECT 1, id, NOW() FROM sys_permission WHERE status = 1;

-- 普通用户只有查看权限
INSERT INTO `sys_role_permission` (role_id, permission_id, create_time) VALUES 
(2, 1, NOW()),   -- 控制台
(2, 11, NOW()),  -- 查看控制台
(2, 2, NOW()),   -- 任务管理
(2, 21, NOW()),  -- 任务列表
(2, 23, NOW());  -- 查看任务

-- 审核员有部分权限
INSERT INTO `sys_role_permission` (role_id, permission_id, create_time) VALUES 
(3, 1, NOW()),   -- 控制台
(3, 11, NOW()),  -- 查看控制台
(3, 2, NOW()),   -- 任务管理
(3, 21, NOW()),  -- 任务列表
(3, 22, NOW()),  -- 创建任务
(3, 23, NOW()),  -- 查看任务
(3, 26, NOW()),  -- 重试任务
(3, 4, NOW()),   -- 系统管理
(3, 41, NOW());  -- 日志管理

SET FOREIGN_KEY_CHECKS = 1;