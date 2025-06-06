package com.epam.aidial.cfg.functional.config.persistence;

import com.epam.aidial.cfg.functional.PostgresFunctionalTests;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;

public class PostgresTestPersistenceService implements TestPersistenceService {

    private static final String DUMP_FILE = "/tmp/test_dump.tar";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    @Lazy
    private PostgresTestPersistenceService self;

    @Override
    public void dumpDb() {
        PostgreSQLContainer<?> postgres = PostgresFunctionalTests.getContainer();
        runContainerCommand(
                String.format(
                        "pg_dump -Ft -U %s -f %s %s",
                        postgres.getUsername(), DUMP_FILE, postgres.getDatabaseName()
                ),
                String.format("take a snapshot '%s'", DUMP_FILE)
        );
    }

    @Override
    public void restoreDb() {
        self.dropAndCreatePublicSchema();
        PostgreSQLContainer<?> postgres = PostgresFunctionalTests.getContainer();
        runContainerCommand(
                String.format(
                        "pg_restore -Ft -U %s -d %s %s",
                        postgres.getUsername(), postgres.getDatabaseName(), DUMP_FILE
                ),
                String.format("restore from a snapshot '%s'", DUMP_FILE)
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dropAndCreatePublicSchema() {
        entityManager.createNativeQuery("DROP SCHEMA public cascade;").executeUpdate();
        entityManager.createNativeQuery("CREATE SCHEMA public;").executeUpdate();
    }

    @Override
    public void cleanupResources() {
        runContainerCommand(String.format("rm %s", DUMP_FILE), String.format("remove snapshot '%s'", DUMP_FILE));
    }

    private void runContainerCommand(String command, String description) {
        PostgreSQLContainer<?> postgres = PostgresFunctionalTests.getContainer();
        try {
            var result = postgres.execInContainer("sh", "-c", command);

            if (result.getExitCode() != 0) {
                throw new RuntimeException("couldn't " + description + ": " + result.getStderr());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("couldn't " + description, e);
        }
    }
}
