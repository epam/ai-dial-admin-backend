package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ModelEntityMapper;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Model;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class ModelHistoryRepository extends RevisionRepository {
    private final ModelJpaRepository modelJpaRepository;
    private final ModelEntityMapper modelEntityMapper;

    public void rollbackModels(Number revision, AuditReader auditReader) {
        List<ModelEntity> models = getEntitiesAtRevision(revision, auditReader, ModelEntity.class);
        modelJpaRepository.deleteAllExcept(models.stream().map(ModelEntity::getId).collect(Collectors.toList()));
        for (ModelEntity model : models) {
            Model domain = modelEntityMapper.toDomain(model);
            domain.setInterceptors(List.of());
            domain.setAdapter(null);
            ModelEntity entity = modelJpaRepository.findById(domain.getDeployment().getName()).orElseGet(ModelEntity::new);
            ModelEntity modelEntity = modelEntityMapper.toEntity(domain, entity);
            modelJpaRepository.save(modelEntity);
        }
    }
}
