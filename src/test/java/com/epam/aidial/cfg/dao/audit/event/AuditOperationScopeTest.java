package com.epam.aidial.cfg.dao.audit.event;

import com.epam.aidial.cfg.dao.audit.jpa.AuditActivityJpaRepository;
import com.epam.aidial.cfg.dao.audit.listener.AuditParentActivityHolder;
import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditOperationScopeTest {

    @Mock
    private AuditActivityJpaRepository repository;
    @Mock
    private TransactionTimestampContext timestampContext;
    @Mock
    private AuditParentActivityHolder parentHolder;
    @Mock
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AuditOperationScope scope;

    @BeforeEach
    void setUp() {
        scope = new AuditOperationScope(repository, timestampContext, parentHolder, objectMapper, entityManager);
    }

    @Test
    void startImportOperation_createsParentRecord() {
        when(timestampContext.getTimestamp()).thenReturn(1000L);

        var options = new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE, true, false);
        UUID parentId = scope.startImportOperation("core", options);

        assertThat(parentId).isNotNull();

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(repository).save(captor.capture());
        verify(entityManager).flush();

        var entity = captor.getValue();
        assertThat(entity.getActivityType()).isEqualTo(ActivityType.Import);
        assertThat(entity.getResourceType()).isEqualTo(ActivityResourceType.ImportConfig);
        assertThat(entity.getResourceId()).isEqualTo(entity.getActivityId().toString());
        assertThat(entity.getOperationMetadata()).contains("\"scope\":\"core\"");
        assertThat(entity.getOperationMetadata()).contains("\"conflictResolution\":\"OVERRIDE\"");
        assertThat(entity.getOperationMetadata()).contains("\"createRoleIfAbsent\":true");
        assertThat(entity.getOperationMetadata()).contains("\"createAdapterIfAbsent\":false");
    }

    @Test
    void startPublicationUpdateOperation_createsParentRecord() {
        when(timestampContext.getTimestamp()).thenReturn(2000L);

        UUID parentId = scope.startPublicationUpdateOperation("publications/test", "file1.txt,file2.txt");

        assertThat(parentId).isNotNull();

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(repository).save(captor.capture());
        verify(entityManager).flush();

        var entity = captor.getValue();
        assertThat(entity.getActivityType()).isEqualTo(ActivityType.PublicationUpdate);
        assertThat(entity.getResourceType()).isEqualTo(ActivityResourceType.Publication);
        assertThat(entity.getOperationMetadata()).contains("\"path\":\"publications/test\"");
        assertThat(entity.getOperationMetadata()).contains("\"fileNames\":\"file1.txt,file2.txt\"");
    }

    @Test
    void openScope_delegatesToParentHolder() {
        AuditParentActivityHolder.Scope mockScope = () -> {};
        UUID parentId = UUID.randomUUID();
        when(parentHolder.openScope(parentId)).thenReturn(mockScope);

        var result = scope.openScope(parentId);

        assertThat(result).isSameAs(mockScope);
        verify(parentHolder).openScope(parentId);
    }
}
