package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.AdapterJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AdapterEntityMapper;
import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.domain.model.Adapter;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class AdapterHistoryRepository extends RevisionRepository {

    private final AdapterJpaRepository adapterJpaRepository;
    private final AdapterEntityMapper adapterEntityMapper;


    public void rollbackAdapters(Number revision, AuditReader auditReader) {
        List<AdapterEntity> adapters = getEntitiesAtRevision(revision, auditReader, AdapterEntity.class);
        adapterJpaRepository.deleteAllExcept(adapters.stream().map(AdapterEntity::getId).collect(Collectors.toList()));
        for (AdapterEntity adapter : adapters) {
            Adapter domain = adapterEntityMapper.toDomain(adapter);
            AdapterEntity entity = adapterJpaRepository.findById(domain.getName()).orElseGet(AdapterEntity::new);
            AdapterEntity keyEntity = adapterEntityMapper.toEntity(domain, entity);
            adapterJpaRepository.save(keyEntity);
        }
    }
}
