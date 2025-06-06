package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.AssistantsPropertyJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AssistantsPropertyEntityMapper;
import com.epam.aidial.cfg.dao.model.AssistantsPropertyEntity;
import com.epam.aidial.cfg.domain.model.AssistantsProperty;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@LogExecution
public class AssistantPropertiesHistoryRepository extends RevisionRepository {

    private final AssistantsPropertyJpaRepository assistantsPropertyJpaRepository;
    private final AssistantsPropertyEntityMapper assistantsPropertyEntityMapper;

    public void rollbackAssistantsProperties(Number revision, AuditReader auditReader) {
        List<AssistantsPropertyEntity> assistantsPropertyEntities = getEntitiesAtRevision(revision, auditReader, AssistantsPropertyEntity.class);
        if (assistantsPropertyEntities.size() > 1) {
            throw new IllegalStateException("Expected to be only one assistants property record " + assistantsPropertyEntities);
        }
        assistantsPropertyEntities
                .stream()
                .findFirst()
                .ifPresent(historyEntity -> {
                    AssistantsProperty domain = assistantsPropertyEntityMapper.toDomain(historyEntity);
                    AssistantsPropertyEntity targetEntity = assistantsPropertyJpaRepository.findById(1L).orElseGet(AssistantsPropertyEntity::new);
                    AssistantsPropertyEntity entity = assistantsPropertyEntityMapper.toEntity(domain, targetEntity);
                    assistantsPropertyJpaRepository.save(entity);
                });
    }
}
