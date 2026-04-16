package com.epam.aidial.cfg.functional.config.persistence;

import com.epam.aidial.cfg.functional.PostgresFunctionalTests;
import com.epam.aidial.cfg.functional.config.persistence.utils.PersistenceServiceUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

public class PostgresTestPersistenceService implements TestPersistenceService {

    private final HikariDataSource hikariDataSource;

    private final String adminJdbcUrl;
    private final String username;
    private final String password;
    private final String dbName;
    private final String templateDbName;

    public PostgresTestPersistenceService(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;

        PostgreSQLContainer<?> c = PostgresFunctionalTests.getContainer();
        // Build url to maintenance DB, not test DB.
        this.adminJdbcUrl = c.getJdbcUrl().replace("/" + c.getDatabaseName(), "/postgres");
        this.username = c.getUsername();
        this.password = c.getPassword();
        this.dbName = c.getDatabaseName();
        this.templateDbName = "template_" + dbName;
    }

    @Override
    public void dumpDb() {
        hikariDataSource.getHikariPoolMXBean().suspendPool();
        hikariDataSource.getHikariPoolMXBean().softEvictConnections();
        try {
            PersistenceServiceUtils.waitForActiveConnectionsToDrain(hikariDataSource, 30000);

            PersistenceServiceUtils.executeWithinRawConnection(adminJdbcUrl, username, password, List.of(
                    String.format("ALTER DATABASE %s CONNECTION LIMIT 0;", dbName),
                    String.format("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '%s' AND pid <> pg_backend_pid();", dbName),
                    String.format("CREATE DATABASE %s TEMPLATE %s;", templateDbName, dbName),
                    String.format("ALTER DATABASE %s CONNECTION LIMIT -1;", dbName)
            ));
        } finally {
            hikariDataSource.getHikariPoolMXBean().resumePool();
        }
    }

    @Override
    public void restoreDb() {
        hikariDataSource.getHikariPoolMXBean().suspendPool();
        hikariDataSource.getHikariPoolMXBean().softEvictConnections();

        try {
            PersistenceServiceUtils.waitForActiveConnectionsToDrain(hikariDataSource, 30000);

            PersistenceServiceUtils.executeWithinRawConnection(adminJdbcUrl, username, password, List.of(
                    String.format("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '%s' AND pid <> pg_backend_pid();", dbName),
                    String.format("DROP DATABASE IF EXISTS %s;", dbName),
                    String.format("CREATE DATABASE %s TEMPLATE %s;", dbName, templateDbName),
                    String.format("ALTER DATABASE %s CONNECTION LIMIT -1;", dbName)
            ));
        } finally {
            hikariDataSource.getHikariPoolMXBean().resumePool();
        }
    }

    @Override
    public void cleanupResources() {
        PersistenceServiceUtils.executeWithinRawConnection(adminJdbcUrl, username, password,
                String.format("DROP DATABASE IF EXISTS %s;", templateDbName)
        );
    }
}
