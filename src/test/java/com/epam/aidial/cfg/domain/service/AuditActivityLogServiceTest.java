package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.audit.jpa.AuditActivityJpaRepository;
import com.epam.aidial.cfg.dao.audit.listener.AuditParentActivityHolder;
import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.security.SecurityClaimsExtractor;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private MockedStatic<SecurityClaimsExtractor> securityClaimsExtractor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new AuditActivityLogService(
                auditActivityJpaRepository,
                transactionTimestampContext,
                auditParentActivityHolder,
                objectMapper
        );

        securityClaimsExtractor = mockStatic(SecurityClaimsExtractor.class);
        securityClaimsExtractor.when(SecurityClaimsExtractor::getAuthor).thenReturn("testAuthor");
        securityClaimsExtractor.when(SecurityClaimsExtractor::getEmail).thenReturn("test@example.com");
    }

    @AfterEach
    void tearDown() {
        if (securityClaimsExtractor != null) {
            securityClaimsExtractor.close();
        }
    }

    @Test
    void logImportOperation_returnsIdAndSave() throws Exception {
        var options = new ConfigImportOptions(ConflictResolutionPolicy.SKIP, false, true);

        UUID id = service.logImportOperation("admin", options);

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(auditActivityJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getActivityId()).isEqualTo(id);
        assertThat(captor.getValue().getResourceType()).isEqualTo(ActivityResourceType.ImportConfig);
        assertThat(captor.getValue().getActivityType()).isEqualTo(ActivityType.Import);
        var meta = objectMapper.readTree(captor.getValue().getOperationMetadata());
        assertThat(meta.get("format").asText()).isEqualTo("admin");
        assertThat(meta.get("conflictResolution").asText()).isEqualTo("SKIP");
        assertThat(meta.get("createRoleIfAbsent").asBoolean()).isFalse();
        assertThat(meta.get("createAdapterIfAbsent").asBoolean()).isTrue();
    }

    @Test
    void logRollbackOperation_returnsIdAndSave() throws Exception {
        UUID id = service.logRollbackOperation(1);

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(auditActivityJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getActivityId()).isEqualTo(id);
        assertThat(captor.getValue().getResourceType()).isEqualTo(ActivityResourceType.Rollback);
        assertThat(captor.getValue().getActivityType()).isEqualTo(ActivityType.Rollback);
        var meta = objectMapper.readTree(captor.getValue().getOperationMetadata());
        assertThat(meta.get("revision").asText()).isEqualTo("1");
    }

    @Test
    void logAuditOperation_linksParentActivityWhenPresent() {
        UUID parentId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        when(auditParentActivityHolder.getParentActivityId()).thenReturn(Optional.of(parentId));

        service.logAuditOperation(ActivityType.Delete, ActivityResourceType.Model, "model1", "{}");

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(auditActivityJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getResourceType()).isEqualTo(ActivityResourceType.Model);
        assertThat(captor.getValue().getActivityType()).isEqualTo(ActivityType.Delete);
        assertThat(captor.getValue().getParentActivityId()).isEqualTo(parentId);
    }
}