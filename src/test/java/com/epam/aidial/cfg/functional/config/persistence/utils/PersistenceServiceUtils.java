package com.epam.aidial.cfg.functional.config.persistence.utils;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Slf4j
public class PersistenceServiceUtils {

    public static void executeWithinRawConnection(String jdbcUrl, String username, String password, String sql) {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                Statement statement = connection.createStatement()
        ) {
            log.debug("jdbc url: {}, username: {}, sql: {}", jdbcUrl, sql, username);
            connection.setAutoCommit(true);
            statement.execute(sql);
        } catch (SQLException e) {
            log.warn("Failed to execute SQL: {}. jdbc url: {}, username: {}", sql, jdbcUrl, username);
            throw new RuntimeException("Failed to execute SQL", e);
        }
    }

    public static void executeWithinRawConnection(String jdbcUrl, String username, String password, List<String> sqlList) {
        log.debug("jdbc url: {}", jdbcUrl);
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                Statement statement = connection.createStatement()
        ) {
            log.debug("jdbc url: {}, username: {}", jdbcUrl, username);
            connection.setAutoCommit(true);
            for (var sql : sqlList) {
                log.debug("sql: {}", sql);
                statement.execute(sql);
            }
        } catch (SQLException e) {
            log.warn("Failed to execute SQLs: {}. jdbc url: {}, username: {}", sqlList, jdbcUrl, username);
            throw new RuntimeException("Failed to execute SQLs", e);
        }
    }

    public static void waitForActiveConnectionsToDrain(HikariDataSource hikariDataSource, long maxWaitMs) {
        long waited = 0;
        while (hikariDataSource.getHikariPoolMXBean().getActiveConnections() > 0 && waited < maxWaitMs) {
            try {
                Thread.sleep(100);
                waited += 100;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        int active = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
        if (active > 0) {
            throw new IllegalStateException("Timed out waiting for active DB connections to drain. active=" + active);
        }
    }
}