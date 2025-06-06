package com.epam.aidial.cfg.functional;

import com.epam.aidial.cfg.features.flag.aspect.FeatureFlagGateEvaluationAspect;
import com.epam.aidial.cfg.functional.config.persistence.TestPersistenceService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class FunctionalTestSuite {

    @Autowired
    private TestPersistenceService persistenceService;
    @Autowired
    private FeatureFlagGateEvaluationAspect featureFlagAspect;

    @BeforeAll
    void beforeAllTests() {
        persistenceService.dumpDb();
    }

    @AfterEach
    void afterEachTest() {
        persistenceService.restoreDb();
        doNothing().when(featureFlagAspect).evaluate(any(), any());
    }

    @AfterAll
    void afterAllTests() {
        persistenceService.cleanupResources();
    }
}
