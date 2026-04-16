package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.audit.jpa.AuditActivityJpaRepository;
import com.epam.aidial.cfg.dao.audit.listener.AuditParentActivityHolder;
import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.domain.util.AuditMetaBuilder;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.security.SecurityClaimsExtractor;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
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

    @Transactional
    public UUID startParentOperation(ActivityType activityType,
                                     ActivityResourceType resourceType,
                                     String operationMetadataJson) {
        var entity = createAuditEntity(activityType, resourceType, null, operationMetadataJson);
        entity.setResourceId(entity.getActivityId().toString());
        auditActivityJpaRepository.save(entity);
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
    public UUID logImportOperation(String format, ConfigImportOptions importOptions) {
        var metaJson = AuditMetaBuilder.with(objectMapper)
                .put("format", format)
                .put("conflictResolution", importOptions.conflictResolutionPolicy().name())
                .put("createRoleIfAbsent", importOptions.createRoleIfAbsent())
                .put("createAdapterIfAbsent", importOptions.createAdapterIfAbsent())
                .buildAsJson();
        return startParentOperation(ActivityType.Import, ActivityResourceType.ImportConfig, metaJson);
    }

    @Transactional
    public UUID logRollbackOperation(Number revision) {
        var metaJson = AuditMetaBuilder.with(objectMapper)
                .put("revision", revision)
                .buildAsJson();
        return startParentOperation(ActivityType.Rollback, ActivityResourceType.Rollback, metaJson);
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