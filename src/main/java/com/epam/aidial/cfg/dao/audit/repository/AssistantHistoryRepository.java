package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.AssistantJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AssistantEntityMapper;
import com.epam.aidial.cfg.dao.model.AssistantEntity;
import com.epam.aidial.cfg.domain.model.Assistant;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class AssistantHistoryRepository extends RevisionRepository {

    private final AssistantJpaRepository assistantJpaRepository;
    private final AssistantEntityMapper assistantEntityMapper;

    public void rollbackAssistants(Number revision, AuditReader auditReader) {
        List<AssistantEntity> assistants = getEntitiesAtRevision(revision, auditReader, AssistantEntity.class);
        assistantJpaRepository.deleteAllExcept(assistants.stream().map(AssistantEntity::getId).collect(Collectors.toList()));
        for (AssistantEntity assistant : assistants) {
            Assistant domain = assistantEntityMapper.toDomain(assistant);
            AssistantEntity entity = assistantJpaRepository.findById(domain.getDeployment().getName()).orElseGet(AssistantEntity::new);
            AssistantEntity assistantEntity = assistantEntityMapper.toEntity(domain, entity);
            assistantJpaRepository.save(assistantEntity);
        }
    }
}
