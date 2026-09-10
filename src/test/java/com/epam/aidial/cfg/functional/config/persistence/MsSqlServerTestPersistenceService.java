package com.epam.aidial.cfg.functional.config.persistence;

import com.epam.aidial.cfg.functional.MsSqlServerFunctionalTests;
import com.epam.aidial.cfg.functional.config.persistence.utils.PersistenceServiceUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.MSSQLServerContainer;

public class MsSqlServerTestPersistenceService implements TestPersistenceService {

    private final HikariDataSource hikariDataSource;

    private final String adminJdbcUrl;
    private final String username;
    private final String password;
    private final String dbName;
    private final String snapshotDbName;
    private final String snapshotFile;

    public MsSqlServerTestPersistenceService(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;

        MSSQLServerContainer<?> c = MsSqlServerFunctionalTests.getContainer();
        // Build url to maintenance DB, not test DB.
        this.adminJdbcUrl = c.getJdbcUrl().replaceAll("database=[^;]+", "database=master");
        this.username = c.getUsername();
        this.password = c.getPassword();
        this.dbName = MsSqlServerFunctionalTests.TEST_DB_NAME;
        this.snapshotDbName = "snapshot_" + dbName;
        this.snapshotFile = "/var/opt/mssql/data/" + snapshotDbName + ".ss";
    }

    @Override
    public void dumpDb() {
        hikariDataSource.getHikariPoolMXBean().suspendPool();
        hikariDataSource.getHikariPoolMXBean().softEvictConnections();
        try {
            PersistenceServiceUtils.waitForActiveConnectionsToDrain(hikariDataSource, 30000);

            PersistenceServiceUtils.executeWithinRawConnection(adminJdbcUrl, username, password,
                    String.format(
                            "CREATE DATABASE [%s] ON (NAME = %s, FILENAME = '%s') AS SNAPSHOT OF [%s];",
                            snapshotDbName, dbName, snapshotFile, dbName)
            );
        } finally {
            hikariDataSource.getHikariPoolMXBean().resumePool();
        }
    }

    @Override
    public void restoreDb() {
        RuntimeException primaryFailure = null;

        hikariDataSource.getHikariPoolMXBean().suspendPool();
        hikariDataSource.getHikariPoolMXBean().softEvictConnections();

        try {
            PersistenceServiceUtils.waitForActiveConnectionsToDrain(hikariDataSource, 30000);

            PersistenceServiceUtils.executeWithinRawConnection(adminJdbcUrl, username, password,
                    String.format("""
                            ALTER DATABASE [%s] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
                            RESTORE DATABASE [%s] FROM DATABASE_SNAPSHOT = '%s';
                            """, dbName, dbName, snapshotDbName));
        } catch (RuntimeException e) {
            primaryFailure = e;
            throw e;
        } finally {
            try {
                PersistenceServiceUtils.executeWithinRawConnection(adminJdbcUrl, username, password,
                        String.format("ALTER DATABASE [%s] SET MULTI_USER;", dbName));
            } catch (RuntimeException cleanupFailure) {
                if (primaryFailure != null) {
                    primaryFailure.addSuppressed(cleanupFailure);
                } else {
                    throw cleanupFailure;
                }
            } finally {
                hikariDataSource.getHikariPoolMXBean().resumePool();
            }
        }
    }

    @Override
    public void cleanupResources() {
        PersistenceServiceUtils.executeWithinRawConnection(adminJdbcUrl, username, password,
                String.format("DROP DATABASE [%s];", snapshotDbName)
        );
    }
}
