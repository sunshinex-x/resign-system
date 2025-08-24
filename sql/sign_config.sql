-- 签名配置表
CREATE TABLE `sign_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `app_name` varchar(255) NOT NULL COMMENT '应用名称',
  `package_name` varchar(255) NOT NULL COMMENT '包名/Bundle ID',
  `app_type` varchar(20) NOT NULL COMMENT '应用类型：iOS、Android',
  `config_json` text NOT NULL COMMENT '配置JSON字符串',
  `description` varchar(500) DEFAULT NULL COMMENT '配置描述',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(100) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_package_name_type` (`package_name`, `app_type`) COMMENT '包名和类型联合唯一索引',
  KEY `idx_app_name` (`app_name`) COMMENT '应用名称索引',
  KEY `idx_app_type` (`app_type`) COMMENT '应用类型索引',
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签名配置表';

-- 插入示例数据
INSERT INTO `sign_config` (`app_name`, `package_name`, `app_type`, `config_json`, `description`, `status`, `create_by`) VALUES
('微信', 'com.tencent.mm', 'Android', '{"keystore_path":"/path/to/wechat.keystore","keystore_password":"password123","key_alias":"wechat","key_password":"keypass123","sign_version":"v2","zipalign":true}', '微信Android应用签名配置', 1, 'admin'),
('支付宝', 'com.eg.android.AlipayGphone', 'Android', '{"keystore_path":"/path/to/alipay.keystore","keystore_password":"alipay2023","key_alias":"alipay","key_password":"alikey123","sign_version":"v2","zipalign":true}', '支付宝Android应用签名配置', 1, 'admin'),
('抖音', 'com.ss.android.ugc.aweme', 'Android', '{"keystore_path":"/path/to/douyin.keystore","keystore_password":"douyin123","key_alias":"douyin","key_password":"dykey123","sign_version":"v2","zipalign":true,"additional_params":{"channel":"official","env":"prod"}}', '抖音Android应用签名配置', 1, 'admin'),
('微信', 'com.tencent.xin', 'iOS', '{"certificate_path":"/path/to/wechat.p12","certificate_password":"wechat123","provisioning_profile":"/path/to/wechat.mobileprovision","team_id":"ABCD123456","bundle_id":"com.tencent.xin"}', '微信iOS应用签名配置', 1, 'admin'),
('QQ', 'com.tencent.qq', 'iOS', '{"certificate_path":"/path/to/qq.p12","certificate_password":"qq123456","provisioning_profile":"/path/to/qq.mobileprovision","team_id":"ABCD123456","bundle_id":"com.tencent.qq"}', 'QQ iOS应用签名配置', 1, 'admin');