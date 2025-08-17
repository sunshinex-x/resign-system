-- 更新resign_task表结构，添加缺失的字段

-- 添加profile_file_urls字段
ALTER TABLE resign_task ADD COLUMN IF NOT EXISTS profile_file_urls TEXT COMMENT 'Profile文件URL列表，多个文件用逗号分隔';

-- 添加bundle_ids字段  
ALTER TABLE resign_task ADD COLUMN IF NOT EXISTS bundle_ids TEXT COMMENT 'Bundle ID列表，多个ID用逗号分隔，与profile_file_urls对应';

-- 添加description字段
ALTER TABLE resign_task ADD COLUMN IF NOT EXISTS description VARCHAR(500) COMMENT '任务描述';

-- 添加permissions字段
ALTER TABLE resign_task ADD COLUMN IF NOT EXISTS permissions TEXT COMMENT '应用权限信息，JSON格式';

-- 添加sign_type字段
ALTER TABLE resign_task ADD COLUMN IF NOT EXISTS sign_type VARCHAR(50) DEFAULT 'DEVELOPMENT' COMMENT '签名类型：DEVELOPMENT, DISTRIBUTION, ADHOC';

-- 添加processing_time字段
ALTER TABLE resign_task ADD COLUMN IF NOT EXISTS processing_time BIGINT COMMENT '处理耗时（毫秒）';

-- 添加original_file_size字段
ALTER TABLE resign_task ADD COLUMN IF NOT EXISTS original_file_size BIGINT COMMENT '原始文件大小（字节）';

-- 添加resigned_file_size字段
ALTER TABLE resign_task ADD COLUMN IF NOT EXISTS resigned_file_size BIGINT COMMENT '重签名后文件大小（字节）';

-- 更新app_type字段注释（移除HARMONY）
ALTER TABLE resign_task MODIFY COLUMN app_type varchar(20) NOT NULL COMMENT '应用类型：IOS、ANDROID';

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_resign_task_app_type ON resign_task(app_type);
CREATE INDEX IF NOT EXISTS idx_resign_task_status_new ON resign_task(status);
CREATE INDEX IF NOT EXISTS idx_resign_task_create_time_new ON resign_task(create_time);