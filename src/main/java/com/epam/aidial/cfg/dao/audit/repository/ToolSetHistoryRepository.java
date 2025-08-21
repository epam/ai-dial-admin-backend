package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ToolSetJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ToolSetEntityMapper;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.ToolSet;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@LogExecution
@RequiredArgsConstructor
public class ToolSetHistoryRepository extends RevisionRepository {

    private final ToolSetJpaRepository toolSetJpaRepository;
    private final ToolSetEntityMapper toolSetEntityMapper;

    public void rollbackToolSets(Number revision, AuditReader auditReader) {
        List<ToolSetEntity> toolSets = getEntitiesAtRevision(revision, auditReader, ToolSetEntity.class);
        toolSetJpaRepository.deleteAllExcept(toolSets.stream().map(ToolSetEntity::getId).collect(Collectors.toList()));
        for (ToolSetEntity toolSet : toolSets) {
            ToolSet domain = toolSetEntityMapper.toDomain(toolSet);
            ToolSetEntity entity = toolSetJpaRepository.findById(domain.getDeployment().getName()).orElseGet(ToolSetEntity::new);
            ToolSetEntity toolSetEntity = toolSetEntityMapper.toEntity(domain, entity);
            toolSetJpaRepository.save(toolSetEntity);
        }
    }
}
