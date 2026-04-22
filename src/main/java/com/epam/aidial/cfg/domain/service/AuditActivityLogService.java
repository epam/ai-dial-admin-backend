package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.audit.jpa.AuditActivityJpaRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditActivityLogService {

    private final AuditActivityJpaRepository auditActivityJpaRepository;
    private final TransactionTimestampContext transactionTimestampContext;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public UUID logImportOperation(String format, ConfigImportOptions importOptions) {
        var metaJson = AuditMetaBuilder.with(objectMapper)
                .put("format", format)
                .put("importOptions", importOptions)
                .buildAsJson();
        return startParentOperation(ActivityType.Import, ActivityResourceType.Config, metaJson);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public UUID logRollbackOperation(Number revision) {
        var metaJson = AuditMetaBuilder.with(objectMapper)
                .put("revision", revision)
                .buildAsJson();
        return startParentOperation(ActivityType.Rollback, ActivityResourceType.System, metaJson);
    }

    private UUID startParentOperation(ActivityType activityType,
                                      ActivityResourceType resourceType,
                                      String operationMetadataJson) {
        var entity = createAuditEntity(activityType, resourceType, operationMetadataJson);
        auditActivityJpaRepository.save(entity);
        return entity.getActivityId();
    }

    private AuditActivityEntity createAuditEntity(ActivityType activityType,
                                                  ActivityResourceType resourceType,
                                                  String operationMetadataJson) {
        UUID id = UuidCreator.getTimeOrderedEpoch();
        AuditActivityEntity entity = new AuditActivityEntity();
        entity.setActivityId(id);
        entity.setResourceId(entity.getActivityId().toString());
        entity.setActivityType(activityType);
        entity.setResourceType(resourceType);
        entity.setEpochTimestampMs(transactionTimestampContext.getTimestamp());
        entity.setInitiatedAuthor(SecurityClaimsExtractor.getAuthor());
        entity.setInitiatedEmail(SecurityClaimsExtractor.getEmail());
        entity.setOperationMetadata(operationMetadataJson);
        return entity;
    }
}