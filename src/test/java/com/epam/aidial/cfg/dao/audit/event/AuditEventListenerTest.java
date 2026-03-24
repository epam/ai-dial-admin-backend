package com.epam.aidial.cfg.dao.audit.event;

import com.epam.aidial.cfg.dao.audit.jpa.AuditActivityJpaRepository;
import com.epam.aidial.cfg.dao.audit.listener.AuditParentActivityHolder;
import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEventListenerTest {

    @Mock
    private AuditActivityJpaRepository repository;
    @Mock
    private TransactionTimestampContext timestampContext;
    @Mock
    private AuditParentActivityHolder parentHolder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AuditEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new AuditEventListener(repository, timestampContext, parentHolder, objectMapper);
    }

    @Test
    void handle_AssetChanged_savesEntityWithCorrectFields() {
        when(timestampContext.getTimestamp()).thenReturn(1000L);
        when(parentHolder.getParentActivityId()).thenReturn(Optional.empty());

        listener.handle(new AuditEvent.AssetChanged(
                ActivityType.Delete, ActivityResourceType.File, "public/test.txt"));

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(repository).save(captor.capture());

        var entity = captor.getValue();
        assertThat(entity.getActivityType()).isEqualTo(ActivityType.Delete);
        assertThat(entity.getResourceType()).isEqualTo(ActivityResourceType.File);
        assertThat(entity.getResourceId()).isEqualTo("public/test.txt");
        assertThat(entity.getEpochTimestampMs()).isEqualTo(1000L);
        assertThat(entity.getParentActivityId()).isNull();
        assertThat(entity.getOperationMetadata()).contains("\"assetId\":\"public/test.txt\"");
    }

    @Test
    void handle_AssetChanged_setsParentId() {
        UUID parentId = UUID.randomUUID();
        when(timestampContext.getTimestamp()).thenReturn(1000L);
        when(parentHolder.getParentActivityId()).thenReturn(Optional.of(parentId));

        listener.handle(new AuditEvent.AssetChanged(
                ActivityType.Create, ActivityResourceType.Prompt, "public/prompt1"));

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getParentActivityId()).isEqualTo(parentId);
    }

    @Test
    void handle_ConfigExported_serializesMetadata() {
        when(timestampContext.getTimestamp()).thenReturn(2000L);
        when(parentHolder.getParentActivityId()).thenReturn(Optional.empty());

        var request = new FullExportRequest();
        request.setExportFormat(ExportFormat.ADMIN);
        request.setAddSecrets(true);

        listener.handle(new AuditEvent.ConfigExported(request));

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(repository).save(captor.capture());

        var entity = captor.getValue();
        assertThat(entity.getActivityType()).isEqualTo(ActivityType.Export);
        assertThat(entity.getResourceType()).isEqualTo(ActivityResourceType.ExportConfig);
        assertThat(entity.getOperationMetadata()).contains("\"exportFormat\":\"ADMIN\"");
        assertThat(entity.getOperationMetadata()).contains("\"addSecrets\":true");
        assertThat(entity.getOperationMetadata()).contains("\"exportKind\":\"full\"");
    }

    @Test
    void handle_RawConfigExported_serializesMetadata() {
        when(timestampContext.getTimestamp()).thenReturn(3000L);
        when(parentHolder.getParentActivityId()).thenReturn(Optional.empty());

        listener.handle(new AuditEvent.RawConfigExported(false));

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(repository).save(captor.capture());

        var entity = captor.getValue();
        assertThat(entity.getActivityType()).isEqualTo(ActivityType.ExportRawConfig);
        assertThat(entity.getOperationMetadata()).contains("\"addSecrets\":false");
    }

    @Test
    void handle_PublicationChanged_serializesComment() {
        when(timestampContext.getTimestamp()).thenReturn(4000L);
        when(parentHolder.getParentActivityId()).thenReturn(Optional.empty());

        listener.handle(new AuditEvent.PublicationChanged(
                "publications/test", ActivityType.PublicationReject, "not ready"));

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(repository).save(captor.capture());

        var entity = captor.getValue();
        assertThat(entity.getActivityType()).isEqualTo(ActivityType.PublicationReject);
        assertThat(entity.getResourceType()).isEqualTo(ActivityResourceType.Publication);
        assertThat(entity.getResourceId()).isEqualTo("publications/test");
        assertThat(entity.getOperationMetadata()).contains("\"comment\":\"not ready\"");
    }

    @Test
    void handle_FileUploaded_buildsFilesAsArray() throws Exception {
        when(timestampContext.getTimestamp()).thenReturn(5000L);
        when(parentHolder.getParentActivityId()).thenReturn(Optional.empty());

        var importResources = ImportResources.builder()
                .path("public/")
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .flatImport(false)
                .build();

        var results = List.of(
                ImportResourcesResult.createSuccess("source1.txt", "public/source1.txt"),
                ImportResourcesResult.createFailure("source2.txt", "public/source2.txt", "error msg")
        );
        var fileResult = ImportResourcesFileResult.builder().importResults(results).build();

        listener.handle(new AuditEvent.FileUploaded(
                ActivityType.FileUpload, importResources, null, "source1.txt,source2.txt", fileResult));

        var captor = ArgumentCaptor.forClass(AuditActivityEntity.class);
        verify(repository).save(captor.capture());

        var entity = captor.getValue();
        assertThat(entity.getActivityType()).isEqualTo(ActivityType.FileUpload);
        assertThat(entity.getResourceType()).isEqualTo(ActivityResourceType.File);
        assertThat(entity.getResourceId()).isEqualTo("source1.txt,source2.txt");
        assertThat(entity.getOperationMetadata()).contains("\"importPath\":\"public/\"");
        // files metadata must be an actual JSON array, not a double-encoded string
        var metaNode = objectMapper.readTree(entity.getOperationMetadata());
        assertThat(metaNode.get("files").isArray()).isTrue();
        assertThat(metaNode.get("files")).hasSize(2);
        assertThat(metaNode.get("files").get(0).get("result").asText()).isEqualTo("SUCCESS");
        assertThat(metaNode.get("files").get(1).get("result").asText()).isEqualTo("FAILURE");
        assertThat(metaNode.get("files").get(1).get("error").asText()).isEqualTo("error msg");
    }
}
