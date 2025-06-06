package com.epam.aidial.cfg.functional.config.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

public class H2TestPersistenceService implements TestPersistenceService {

    private static final String DUMP_FILE = "test_dump.sql";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dumpDb() {
        entityManager.createNativeQuery("SCRIPT TO '%s';".formatted(DUMP_FILE)).getResultList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void restoreDb() {
        entityManager.createNativeQuery("DROP ALL OBJECTS;").executeUpdate();
        entityManager.createNativeQuery("RUNSCRIPT FROM '%s';".formatted(DUMP_FILE)).executeUpdate();
    }

    @Override
    @SneakyThrows
    public void cleanupResources() {
        Files.delete(Path.of(DUMP_FILE));
    }
}
