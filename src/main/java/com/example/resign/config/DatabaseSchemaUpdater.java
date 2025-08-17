package com.example.resign.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 数据库表结构更新器
 * 在应用启动时自动更新数据库表结构
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSchemaUpdater implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // log.info("开始检查和更新数据库表结构...");
            // updateResignTaskTable();
            // log.info("数据库表结构更新完成");
        } catch (Exception e) {
            // log.error("数据库表结构更新失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }

    private void updateResignTaskTable() {
        try {
            // 检查表是否存在
            String checkTableSql = "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = DATABASE() AND table_name = 'resign_task'";

            Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class);
            if (tableCount == null || tableCount == 0) {
                log.warn("resign_task表不存在，跳过表结构更新");
                return;
            }

            // 添加缺失的字段
            addColumnIfNotExists("profile_file_urls", "TEXT", "Profile文件URL列表，多个文件用逗号分隔");
            addColumnIfNotExists("bundle_ids", "TEXT", "Bundle ID列表，多个ID用逗号分隔，与profile_file_urls对应");
            addColumnIfNotExists("description", "VARCHAR(500)", "任务描述");
            addColumnIfNotExists("permissions", "TEXT", "应用权限信息，JSON格式");
            addColumnIfNotExists("sign_type", "VARCHAR(50) DEFAULT 'DEVELOPMENT'", "签名类型：DEVELOPMENT, DISTRIBUTION, ADHOC");
            addColumnIfNotExists("processing_time", "BIGINT", "处理耗时（毫秒）");
            addColumnIfNotExists("original_file_size", "BIGINT", "原始文件大小（字节）");
            addColumnIfNotExists("resigned_file_size", "BIGINT", "重签名后文件大小（字节）");

            log.info("resign_task表结构更新完成");

        } catch (Exception e) {
            log.error("更新resign_task表结构失败", e);
        }
    }

    private void addColumnIfNotExists(String columnName, String columnType, String comment) {
        try {
            // 检查字段是否存在
            String checkColumnSql = "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() AND table_name = 'resign_task' AND column_name = ?";

            Integer columnCount = jdbcTemplate.queryForObject(checkColumnSql, Integer.class, columnName);

            if (columnCount == null || columnCount == 0) {
                // 字段不存在，添加字段
                String addColumnSql = String.format(
                    "ALTER TABLE resign_task ADD COLUMN %s %s COMMENT '%s'",
                    columnName, columnType, comment
                );

                jdbcTemplate.execute(addColumnSql);
                log.info("已添加字段: {}", columnName);
            } else {
                log.debug("字段已存在: {}", columnName);
            }

        } catch (Exception e) {
            log.warn("添加字段 {} 失败: {}", columnName, e.getMessage());
        }
    }
}