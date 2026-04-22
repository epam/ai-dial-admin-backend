package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.audit.jpa.AuditActivityJpaRepository;
import com.epam.aidial.cfg.dao.audit.listener.AuditParentActivityHolder;
import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.domain.model.ImportFormat;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditActivityLogServiceTest {
    @Mock
    private AuditActivityJpaRepository auditActivityJpaRepository;

    @Mock
    private TransactionTimestampContext transactionTimestampContext;

    @Mock
    private AuditParentActivityHolder auditParentActivityHolder;

    private ObjectMapper objectMapper;
    private AuditActivityLogService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new AuditActivityLogService(
                auditActivityJpaRepository,
                transactionTimestampContext,
                objectMapper
        );
    }

    @Test
    void logImportOperation_returnsIdAndSave() {
        var options = new ConfigImportOptions(ConflictResolutionPolicy.SKIP, false, true);

        UUID id = service.logImportOperation(ImportFormat.ADMIN, options);

        AuditActivityEntity expectedEntity = new AuditActivityEntity();
        expectedEntity.setActivityId(id);
        expectedEntity.setResourceType(ActivityResourceType.Config);
        expectedEntity.setActivityType(ActivityType.Import);
        expectedEntity.setEpochTimestampMs(0L);
        expectedEntity.setOperationMetadata(
                "{\"format\":\"ADMIN\",\"importOptions\":{" +
                        "\"conflictResolutionPolicy\":\"SKIP\"," +
                        "\"createRoleIfAbsent\":false," +
                        "\"createAdapterIfAbsent\":true" +
                        "}}"
        );

        verify(auditActivityJpaRepository).save(expectedEntity);
    }

    @Test
    void logRollbackOperation_returnsIdAndSave() {
        UUID id = service.logRollbackOperation(1);

        AuditActivityEntity expectedEntity = new AuditActivityEntity();
        expectedEntity.setActivityId(id);
        expectedEntity.setResourceType(ActivityResourceType.System);
        expectedEntity.setActivityType(ActivityType.Rollback);
        expectedEntity.setEpochTimestampMs(0L);
        expectedEntity.setOperationMetadata("{\"revision\":1}");

        verify(auditActivityJpaRepository).save(expectedEntity);
    }
}