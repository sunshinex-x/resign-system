/*
 扩展avatar字段长度以支持MinIO预签名URL
 MinIO预签名URL通常很长，需要更大的字段长度
*/

-- 修改avatar字段长度从varchar(255)扩展到varchar(1000)
ALTER TABLE `sys_user` MODIFY COLUMN `avatar` varchar(1000) DEFAULT NULL COMMENT '头像URL';

-- 添加索引以提高查询性能（可选）
-- CREATE INDEX idx_avatar ON sys_user(avatar(100));

-- 验证修改结果
-- DESCRIBE sys_user;