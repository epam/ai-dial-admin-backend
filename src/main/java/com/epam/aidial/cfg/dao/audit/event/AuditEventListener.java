package com.epam.aidial.cfg.dao.audit.event;

import com.epam.aidial.cfg.dao.audit.jpa.AuditActivityJpaRepository;
import com.epam.aidial.cfg.dao.audit.listener.AuditParentActivityHolder;
import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import com.epam.aidial.cfg.security.SecurityClaimsExtractor;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.utils.PathUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditActivityJpaRepository auditActivityJpaRepository;
    private final TransactionTimestampContext transactionTimestampContext;
    private final AuditParentActivityHolder auditParentActivityHolder;
    private final ObjectMapper objectMapper;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(AuditEvent.AssetChanged event) {
        if (StringUtils.isBlank(event.resourceId())) {
            return;
        }
        var meta = Map.of("assetId", (Object) event.resourceId());
        saveAuditRecord(event.action(), event.resourceType(), event.resourceId(), serializeJson(meta));
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(AuditEvent.FileUploaded event) {
        if (event.result() == null) {
            return;
        }
        var meta = new LinkedHashMap<String, Object>();
        meta.put("importPath", event.importResources().getPath());
        meta.put("conflictResolution", event.importResources().getConflictResolutionStrategy());
        meta.put("flatImport", event.importResources().isFlatImport());
        putIfNotBlank(meta, "zipArchiveName", event.zipArchiveName());
        String filesMeta = buildFilesMeta(event.result());
        putIfNotBlank(meta, "files", filesMeta);

        String resourceId = StringUtils.firstNonBlank(event.zipArchiveName(), event.fileNames());
        saveAuditRecord(event.action(), ActivityResourceType.File, resourceId, serializeJson(meta));
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(AuditEvent.ConfigExported event) {
        ExportRequest request = event.request();
        var meta = new LinkedHashMap<String, Object>();
        meta.put("exportFormat", request.getExportFormat().name());
        meta.put("addSecrets", request.isAddSecrets());

        if (request instanceof FullExportRequest full) {
            meta.put("exportKind", "full");
            meta.put("componentTypes", full.getComponentTypes());
            meta.put("topicCount", full.getTopics() != null ? full.getTopics().size() : null);
        } else if (request instanceof SelectedItemsExportRequest selected) {
            meta.put("exportKind", "selected");
            meta.put("componentCount", selected.getComponents() != null ? selected.getComponents().size() : 0);
        }

        saveAuditRecord(ActivityType.Export, ActivityResourceType.ExportConfig, null, serializeJson(meta));
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(AuditEvent.RawConfigExported event) {
        var meta = Map.of("addSecrets", (Object) event.addSecrets());
        saveAuditRecord(ActivityType.ExportRawConfig, ActivityResourceType.ExportConfig, null, serializeJson(meta));
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(AuditEvent.PublicationChanged event) {
        var meta = new LinkedHashMap<String, Object>();
        meta.put("path", event.publicationPath());
        putIfNotBlank(meta, "comment", event.comment());
        saveAuditRecord(event.action(), ActivityResourceType.Publication, event.publicationPath(), serializeJson(meta));
    }

    private void saveAuditRecord(ActivityType activityType,
                                 ActivityResourceType resourceType,
                                 String resourceId,
                                 String metadataJson) {
        try {
            UUID id = UuidCreator.getTimeOrderedEpoch();
            AuditActivityEntity entity = new AuditActivityEntity();
            entity.setActivityId(id);
            entity.setActivityType(activityType);
            entity.setResourceType(resourceType);
            entity.setResourceId(StringUtils.isNotBlank(resourceId) ? resourceId : id.toString());
            entity.setEpochTimestampMs(transactionTimestampContext.getTimestamp());
            entity.setInitiatedAuthor(SecurityClaimsExtractor.getAuthor());
            entity.setInitiatedEmail(SecurityClaimsExtractor.getEmail());
            entity.setOperationMetadata(metadataJson);
            auditParentActivityHolder.getParentActivityId().ifPresent(entity::setParentActivityId);
            auditActivityJpaRepository.save(entity);
        } catch (Exception e) {
            log.warn("Failed to record audit for {} {}", activityType, resourceId, e);
        }
    }

    private String buildFilesMeta(ImportResourcesFileResult result) {
        if (result.getImportResults() == null) {
            return null;
        }
        List<Map<String, Object>> files = new ArrayList<>();
        for (ImportResourcesResult r : result.getImportResults()) {
            var entry = new LinkedHashMap<String, Object>();
            var pathForName = StringUtils.firstNonBlank(r.getSourcePath(), r.getTargetPath());
            if (StringUtils.isNotBlank(pathForName)) {
                try {
                    entry.put("fileName", PathUtils.parsePath(pathForName).getName());
                } catch (IllegalArgumentException e) {
                    entry.put("fileName", pathForName);
                }
            }
            entry.put("result", r.getStatus().name());
            if (StringUtils.isNotBlank(r.getError())) {
                entry.put("error", r.getError());
            }
            files.add(entry);
        }
        return serializeJson(files);
    }

    private String serializeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit metadata", e);
            return null;
        }
    }

    private static void putIfNotBlank(Map<String, Object> map, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            map.put(key, value);
        }
    }
}
