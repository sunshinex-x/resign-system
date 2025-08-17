package com.example.resign.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库降级配置
 * 当数据库连接失败时提供基本功能
 */
@Slf4j
@Configuration
public class DatabaseFallbackConfig {

    /**
     * 数据库连接检查
     */
    @Bean
    public DatabaseHealthChecker databaseHealthChecker(DataSource dataSource) {
        return new DatabaseHealthChecker(dataSource);
    }

    public static class DatabaseHealthChecker {
        private final DataSource dataSource;
        private volatile boolean databaseAvailable = true;
        private long lastCheckTime = 0;
        private static final long CHECK_INTERVAL = 30000; // 30秒检查一次

        public DatabaseHealthChecker(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public boolean isDatabaseAvailable() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCheckTime > CHECK_INTERVAL) {
                checkDatabaseConnection();
                lastCheckTime = currentTime;
            }
            return databaseAvailable;
        }

        private void checkDatabaseConnection() {
            try (Connection connection = dataSource.getConnection()) {
                connection.isValid(5); // 5秒超时
                if (!databaseAvailable) {
                    log.info("数据库连接已恢复");
                    databaseAvailable = true;
                }
            } catch (SQLException e) {
                if (databaseAvailable) {
                    log.warn("数据库连接失败，启用降级模式: {}", e.getMessage());
                    databaseAvailable = false;
                }
            }
        }
    }
}