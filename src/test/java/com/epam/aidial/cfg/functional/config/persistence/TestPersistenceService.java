package com.epam.aidial.cfg.functional.config.persistence;

public interface TestPersistenceService {

    void dumpDb();

    void restoreDb();

    void cleanupResources();
}
