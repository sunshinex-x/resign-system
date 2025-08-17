-- 更新resign_task表结构以支持iOS复杂签名需求

-- 检查并添加缺失的字段
-- 添加profile_file_urls字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'resign_task' 
     AND COLUMN_NAME = 'profile_file_urls') = 0,
    'ALTER TABLE resign_task ADD COLUMN profile_file_urls TEXT COMMENT ''Profile文件URL列表，多个文件用逗号分隔''',
    'SELECT ''Column profile_file_urls already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加bundle_ids字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'resign_task' 
     AND COLUMN_NAME = 'bundle_ids') = 0,
    'ALTER TABLE resign_task ADD COLUMN bundle_ids TEXT COMMENT ''Bundle ID列表，多个ID用逗号分隔，与profile_file_urls对应''',
    'SELECT ''Column bundle_ids already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加description字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'resign_task' 
     AND COLUMN_NAME = 'description') = 0,
    'ALTER TABLE resign_task ADD COLUMN description VARCHAR(500) COMMENT ''任务描述''',
    'SELECT ''Column description already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加permissions字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'resign_task' 
     AND COLUMN_NAME = 'permissions') = 0,
    'ALTER TABLE resign_task ADD COLUMN permissions TEXT COMMENT ''应用权限信息，JSON格式''',
    'SELECT ''Column permissions already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加sign_type字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'resign_task' 
     AND COLUMN_NAME = 'sign_type') = 0,
    'ALTER TABLE resign_task ADD COLUMN sign_type VARCHAR(50) DEFAULT ''DEVELOPMENT'' COMMENT ''签名类型：DEVELOPMENT, DISTRIBUTION, ADHOC''',
    'SELECT ''Column sign_type already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加processing_time字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'resign_task' 
     AND COLUMN_NAME = 'processing_time') = 0,
    'ALTER TABLE resign_task ADD COLUMN processing_time BIGINT COMMENT ''处理耗时（毫秒）''',
    'SELECT ''Column processing_time already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加original_file_size字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'resign_task' 
     AND COLUMN_NAME = 'original_file_size') = 0,
    'ALTER TABLE resign_task ADD COLUMN original_file_size BIGINT COMMENT ''原始文件大小（字节）''',
    'SELECT ''Column original_file_size already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加resigned_file_size字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'resign_task' 
     AND COLUMN_NAME = 'resigned_file_size') = 0,
    'ALTER TABLE resign_task ADD COLUMN resigned_file_size BIGINT COMMENT ''重签名后文件大小（字节）''',
    'SELECT ''Column resigned_file_size already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加索引（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'resign_task' 
     AND INDEX_NAME = 'idx_resign_task_app_type') = 0,
    'CREATE INDEX idx_resign_task_app_type ON resign_task(app_type)',
    'SELECT ''Index idx_resign_task_app_type already exists'' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;