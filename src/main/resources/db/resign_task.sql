/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80100 (8.1.0)
 Source Host           : localhost:3306
 Source Schema         : resign_db

 Target Server Type    : MySQL
 Target Server Version : 80100 (8.1.0)
 File Encoding         : 65001

 Date: 12/08/2025 23:26:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for resign_task
-- ----------------------------
DROP TABLE IF EXISTS `resign_task`;
CREATE TABLE `resign_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` varchar(64) NOT NULL COMMENT '任务唯一标识',
  `app_type` varchar(20) NOT NULL COMMENT '应用类型：IOS、ANDROID、HARMONY',
  `original_package_url` varchar(255) NOT NULL COMMENT '原始安装包URL',
  `certificate_url` varchar(255) NOT NULL COMMENT '证书URL',
  `certificate_password` varchar(255) DEFAULT NULL COMMENT '证书密码',
  `resigned_package_url` varchar(255) DEFAULT NULL COMMENT '重签名后的安装包URL',
  `status` varchar(20) NOT NULL COMMENT '任务状态：PENDING、PROCESSING、SUCCESS、FAILED',
  `fail_reason` varchar(1000) DEFAULT NULL COMMENT '失败原因',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `callback_url` varchar(255) DEFAULT NULL COMMENT '回调URL',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `provisioning_profile_url` varchar(255) DEFAULT NULL COMMENT 'Provisioning Profile URL',
  `bundle_id` varchar(100) DEFAULT NULL COMMENT '应用包名/Bundle ID',
  `nested_app_profiles` text COMMENT '嵌套应用包名映射 (JSON格式)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='重签名任务表';

-- ----------------------------
-- Records of resign_task
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
