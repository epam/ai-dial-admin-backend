package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.audit.jpa.AuditActivityJpaRepository;
import com.epam.aidial.cfg.dao.audit.listener.AuditParentActivityHolder;
import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.domain.model.ImportFormat;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.security.SecurityClaimsExtractor;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
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
    private MockedStatic<SecurityClaimsExtractor> securityClaimsExtractor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new AuditActivityLogService(
                auditActivityJpaRepository,
                transactionTimestampContext,
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

        UUID id = service.logImportOperation(ImportFormat.ADMIN, options);

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(auditActivityJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getActivityId()).isEqualTo(id);
        assertThat(captor.getValue().getResourceType()).isEqualTo(ActivityResourceType.Config);
        assertThat(captor.getValue().getActivityType()).isEqualTo(ActivityType.Import);
        var meta = objectMapper.readTree(captor.getValue().getOperationMetadata());
        assertThat(meta.get("format").asText()).isEqualTo("ADMIN");
        var importOptions = (JsonNode) meta.get("importOptions");
        assertThat(importOptions.path("conflictResolutionPolicy").asText()).isEqualTo("SKIP");
        assertThat(importOptions.path("createRoleIfAbsent").asBoolean()).isEqualTo(false);
        assertThat(importOptions.path("createAdapterIfAbsent").asBoolean()).isEqualTo(true);
    }

    @Test
    void logRollbackOperation_returnsIdAndSave() throws Exception {
        UUID id = service.logRollbackOperation(1);

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(auditActivityJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getActivityId()).isEqualTo(id);
        assertThat(captor.getValue().getResourceType()).isEqualTo(ActivityResourceType.System);
        assertThat(captor.getValue().getActivityType()).isEqualTo(ActivityType.Rollback);
        var meta = objectMapper.readTree(captor.getValue().getOperationMetadata());
        assertThat(meta.get("revision").asText()).isEqualTo("1");
    }
}