package com.epam.aidial.cfg.dao.audit.event;

import com.epam.aidial.cfg.dao.audit.jpa.AuditActivityJpaRepository;
import com.epam.aidial.cfg.dao.audit.listener.AuditParentActivityHolder;
import com.epam.aidial.cfg.dao.audit.model.AuditActivityEntity;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.security.SecurityClaimsExtractor;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Creates parent audit records for compound operations (import, publication update)
 * and manages the ThreadLocal scope for parent-child linking.
 *
 * <p>Parent records are created in a {@code REQUIRES_NEW} transaction so they
 * commit independently of the main business transaction. This ensures:
 * <ul>
 *   <li>The FK constraint is satisfied before Envers creates child records</li>
 *   <li>The parent record survives even if the main transaction rolls back
 *       (desirable for audit — you want to know an attempt was made)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditOperationScope {

    private final AuditActivityJpaRepository auditActivityJpaRepository;
    private final TransactionTimestampContext transactionTimestampContext;
    private final AuditParentActivityHolder auditParentActivityHolder;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID startImportOperation(String scope, ConfigImportOptions importOptions) {
        var meta = new LinkedHashMap<String, Object>();
        meta.put("scope", scope);
        meta.put("conflictResolution", importOptions.conflictResolutionPolicy().name());
        meta.put("createRoleIfAbsent", importOptions.createRoleIfAbsent());
        meta.put("createAdapterIfAbsent", importOptions.createAdapterIfAbsent());
        return createParentRecord(ActivityType.Import, ActivityResourceType.ImportConfig, serializeJson(meta));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID startPublicationUpdateOperation(String path, String fileNames) {
        var meta = new LinkedHashMap<String, Object>();
        meta.put("path", path);
        if (StringUtils.isNotBlank(fileNames)) {
            meta.put("fileNames", fileNames);
        }
        return createParentRecord(ActivityType.PublicationUpdate, ActivityResourceType.Publication, serializeJson(meta));
    }

    public AuditParentActivityHolder.Scope openScope(UUID parentId) {
        return auditParentActivityHolder.openScope(parentId);
    }

    private UUID createParentRecord(ActivityType activityType,
                                    ActivityResourceType resourceType,
                                    String metadataJson) {
        UUID id = UuidCreator.getTimeOrderedEpoch();
        AuditActivityEntity entity = new AuditActivityEntity();
        entity.setActivityId(id);
        entity.setActivityType(activityType);
        entity.setResourceType(resourceType);
        entity.setResourceId(id.toString());
        entity.setEpochTimestampMs(transactionTimestampContext.getTimestamp());
        entity.setInitiatedAuthor(SecurityClaimsExtractor.getAuthor());
        entity.setInitiatedEmail(SecurityClaimsExtractor.getEmail());
        entity.setOperationMetadata(metadataJson);
        auditActivityJpaRepository.save(entity);
        entityManager.flush();
        return id;
    }

    private String serializeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit metadata", e);
            return null;
        }
    }
}
