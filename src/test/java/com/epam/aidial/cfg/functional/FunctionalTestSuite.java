package com.epam.aidial.cfg.functional;

import com.epam.aidial.cfg.client.mcp.McpClientFactory;
import com.epam.aidial.cfg.features.flag.aspect.FeatureFlagGateEvaluationAspect;
import com.epam.aidial.cfg.functional.config.persistence.TestPersistenceService;
import com.epam.aidial.cfg.service.config.reload.CoreConfigReloadCache;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class FunctionalTestSuite {
    @Autowired
    private McpClientFactory mcpClientFactory;
    @Autowired
    private TestPersistenceService persistenceService;
    @Autowired
    private FeatureFlagGateEvaluationAspect featureFlagAspect;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;
    @Autowired
    private CoreConfigReloadCache coreConfigReloadCache;

    @BeforeAll
    void beforeAllTests() {
        persistenceService.dumpDb();
    }

    @AfterEach
    void afterEachTest() {
        persistenceService.restoreDb();
        doNothing().when(featureFlagAspect).evaluate(any(), any());
        reset(transactionTimestampContext, coreConfigReloadCache, mcpClientFactory);
    }

    @AfterAll
    void afterAllTests() {
        persistenceService.cleanupResources();
    }
}