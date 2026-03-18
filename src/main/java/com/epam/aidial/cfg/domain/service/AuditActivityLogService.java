package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.audit.jpa.AuditActivityJpaRepository;
import com.epam.aidial.cfg.dao.audit.listener.AuditParentActivityHolder;
import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.domain.util.AuditMetaBuilder;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import com.epam.aidial.cfg.security.SecurityClaimsExtractor;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.utils.PathUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditActivityLogService {

    private final AuditActivityJpaRepository auditActivityJpaRepository;
    private final TransactionTimestampContext transactionTimestampContext;
    private final AuditParentActivityHolder auditParentActivityHolder;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    @Transactional
    private UUID startParentOperation(ActivityType activityType,
                                      ActivityResourceType resourceType,
                                      String resourceId,
                                      String operationMetadataJson) {
        var entity = createAuditEntity(activityType, resourceType, resourceId, operationMetadataJson);
        resourceId = StringUtils.isNotBlank(resourceId) ? resourceId : entity.getActivityId().toString();
        entity.setResourceId(resourceId);
        auditActivityJpaRepository.save(entity);
        entityManager.flush();
        return entity.getActivityId();
    }

    @Transactional
    public void logAuditOperation(ActivityType activityType,
                                  ActivityResourceType resourceType,
                                  String resourceId,
                                  String operationMetadataJson) {
        try {
            var entity = createAuditEntity(activityType, resourceType, resourceId, operationMetadataJson);
            resourceId = StringUtils.isNotBlank(resourceId) ? resourceId : entity.getActivityId().toString();
            entity.setResourceId(resourceId);
            auditParentActivityHolder.getParentActivityId()
                    .ifPresent(entity::setParentActivityId);
            auditActivityJpaRepository.save(entity);
        } catch (Exception e) {
            log.warn("Failed to record audit for {} {}", activityType, resourceId, e);
        }
    }

    @Transactional
    public void logAssetChange(ActivityType activityType,
                               ActivityResourceType resourceType,
                               String assetId) {
        if (StringUtils.isBlank(assetId)) {
            return;
        }
        var metaJson = AuditMetaBuilder.with(objectMapper)
                .put("assetId", assetId)
                .buildAsJson();
        logAuditOperation(activityType, resourceType, assetId, metaJson);
    }

    @Transactional
    public void logPublication(String publicationPath,
                               ActivityType activityType,
                               String comment) {
        var metaJson = AuditMetaBuilder.with(objectMapper)
                .put("path", publicationPath)
                .putIfNotBlank("comment", comment)
                .buildAsJson();
        logAuditOperation(activityType, ActivityResourceType.Publication, publicationPath, metaJson);
    }

    @Transactional
    public UUID logPublicationUpdate(String path, String fileNames) {
        var metaJson = AuditMetaBuilder.with(objectMapper)
                .put("path", path)
                .putIfNotBlank("fileNames", fileNames)
                .buildAsJson();
        return startParentOperation(ActivityType.PublicationUpdate, ActivityResourceType.Publication,
                null, metaJson);
    }

    @Transactional
    public void logConfigExport(ExportRequest request) {
        var builder = AuditMetaBuilder.with(objectMapper)
                .put("exportFormat", request.getExportFormat().name())
                .put("addSecrets", request.isAddSecrets());

        if (request instanceof FullExportRequest fullExportRequest) {
            builder.put("exportKind", "full")
                    .put("componentTypes", fullExportRequest.getComponentTypes())
                    .put("topicCount", fullExportRequest.getTopics() != null ? fullExportRequest.getTopics().size() : null);
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            builder.put("exportKind", "selected")
                    .put("componentCount", selectedItemsExportRequest.getComponents() != null ? selectedItemsExportRequest.getComponents().size() : 0);
        } else {
            throw new IllegalArgumentException("Unsupported ExportRequest type: " + request.getClass());
        }

        var metaJson = builder.buildAsJson();
        logAuditOperation(ActivityType.Export, ActivityResourceType.ExportConfig, null, metaJson);
    }

    @Transactional
    public void logExportRawConfig(boolean addSecrets) {
        var metaJson = AuditMetaBuilder.with(objectMapper)
                .put("addSecrets", addSecrets).buildAsJson();
        logAuditOperation(ActivityType.ExportRawConfig, ActivityResourceType.ExportConfig, null, metaJson);
    }

    @Transactional
    public void logFileUpload(ActivityType activityType,
                              ImportResources importResources,
                              String zipArchiveName,
                              String fileNames,
                              ImportResourcesFileResult result) {
        if (result == null) {
            return;
        }

        var filesMeta = getFilesMeta(result);
        var metaData = AuditMetaBuilder.with(objectMapper)
                .put("importPath", importResources.getPath())
                .put("conflictResolution", importResources.getConflictResolutionStrategy())
                .put("flatImport", importResources.isFlatImport())
                .putIfNotBlank("zipArchiveName", zipArchiveName)
                .putIfNotBlank("files", filesMeta).buildAsJson();
        String resourceId = StringUtils.firstNonBlank(zipArchiveName, fileNames);
        logAuditOperation(activityType, ActivityResourceType.File, resourceId, metaData);
    }

    private String getFilesMeta(ImportResourcesFileResult result) {
        var builder = AuditMetaBuilder.with(objectMapper);
        if (result == null || result.getImportResults() == null) {
            return builder.buildAsJson();
        }
        for (ImportResourcesResult resourcesResult : result.getImportResults()) {
            var pathForName = StringUtils.firstNonBlank(resourcesResult.getSourcePath(), resourcesResult.getTargetPath());
            if (StringUtils.isNotBlank(pathForName)) {
                try {
                    builder.put("fileName", PathUtils.parsePath(pathForName).getName());
                } catch (IllegalArgumentException e) {
                    builder.put("fileName", pathForName);
                }
            }
            builder.put("result", resourcesResult.getStatus().name());
            builder.putIfNotBlank("error", resourcesResult.getError());
        }
        return builder.buildAsJson();
    }

    @Transactional
    public UUID logImportOperation(String scope, ConfigImportOptions importOptions) {
        var metaJson = AuditMetaBuilder.with(objectMapper)
                .put("scope", scope)
                .put("conflictResolution", importOptions.conflictResolutionPolicy().name())
                .put("createRoleIfAbsent", importOptions.createRoleIfAbsent())
                .put("createRoleIfAbsent", importOptions.createRoleIfAbsent())
                .put("createAdapterIfAbsent", importOptions.createAdapterIfAbsent())
                .buildAsJson();
        return startParentOperation(ActivityType.Import, ActivityResourceType.ImportConfig,
                null, metaJson);
    }

    private AuditActivityEntity createAuditEntity(ActivityType activityType,
                                                  ActivityResourceType resourceType,
                                                  String resourceId,
                                                  String operationMetadataJson) {
        UUID id = UuidCreator.getTimeOrderedEpoch();
        AuditActivityEntity entity = new AuditActivityEntity();
        entity.setActivityId(id);
        entity.setActivityType(activityType);
        entity.setResourceType(resourceType);
        entity.setResourceId(resourceId);
        entity.setEpochTimestampMs(transactionTimestampContext.getTimestamp());
        entity.setInitiatedAuthor(SecurityClaimsExtractor.getAuthor());
        entity.setInitiatedEmail(SecurityClaimsExtractor.getEmail());
        entity.setOperationMetadata(operationMetadataJson);
        return entity;
    }
}