-- 创建证书管理表
CREATE TABLE IF NOT EXISTS certificate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '证书名称',
    platform VARCHAR(50) NOT NULL COMMENT '平台类型：IOS, ANDROID',
    file_url VARCHAR(500) NOT NULL COMMENT '证书文件URL',
    password VARCHAR(255) NOT NULL COMMENT '证书密码',
    description TEXT COMMENT '描述信息',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, INACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_platform (platform),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书管理表';

-- 创建Profile描述文件管理表（仅iOS使用）
CREATE TABLE IF NOT EXISTS profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Profile名称',
    certificate_id BIGINT NOT NULL COMMENT '关联的证书ID',
    bundle_id VARCHAR(255) NOT NULL COMMENT 'Bundle ID',
    file_url VARCHAR(500) NOT NULL COMMENT 'Profile文件URL',
    description TEXT COMMENT '描述信息',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, INACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_certificate_id (certificate_id),
    INDEX idx_bundle_id (bundle_id),
    INDEX idx_status (status),
    FOREIGN KEY (certificate_id) REFERENCES certificate(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Profile描述文件管理表';

-- 创建任务证书关联表
CREATE TABLE IF NOT EXISTS task_certificate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    certificate_id BIGINT NOT NULL COMMENT '证书ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (task_id),
    INDEX idx_certificate_id (certificate_id),
    UNIQUE KEY uk_task_certificate (task_id, certificate_id),
    FOREIGN KEY (certificate_id) REFERENCES certificate(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务证书关联表';

-- 创建任务Profile关联表（仅iOS使用）
CREATE TABLE IF NOT EXISTS task_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    profile_id BIGINT NOT NULL COMMENT 'Profile ID',
    bundle_id VARCHAR(255) NOT NULL COMMENT 'Bundle ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_id (task_id),
    INDEX idx_profile_id (profile_id),
    INDEX idx_bundle_id (bundle_id),
    UNIQUE KEY uk_task_profile_bundle (task_id, profile_id, bundle_id),
    FOREIGN KEY (profile_id) REFERENCES profile(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务Profile关联表';