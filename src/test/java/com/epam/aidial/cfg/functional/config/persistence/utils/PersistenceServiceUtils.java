package com.epam.aidial.cfg.functional.config.persistence.utils;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class PersistenceServiceUtils {

    public static void executeWithinRawConnection(String jdbcUrl, String username, String password, String sql) {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                Statement statement = connection.createStatement()
        ) {
            connection.setAutoCommit(true);
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL", e);
        }
    }

    public static void executeWithinRawConnection(String jdbcUrl, String username, String password, List<String> sqlList) {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                Statement statement = connection.createStatement()
        ) {

            connection.setAutoCommit(true);
            for (var sql : sqlList) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL", e);
        }
    }

    public static void waitForActiveConnectionsToDrain(HikariDataSource hikariDataSource) {
        int maxWaitMs = 15000;
        int elapsed = 0;

        while (hikariDataSource.getHikariPoolMXBean().getActiveConnections() > 0 && elapsed < maxWaitMs) {
            try {
                Thread.sleep(100);
                elapsed += 100;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}