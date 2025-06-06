package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.AddonJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AddonEntityMapper;
import com.epam.aidial.cfg.dao.model.AddonEntity;
import com.epam.aidial.cfg.domain.model.Addon;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class AddonHistoryRepository extends RevisionRepository {

    private final AddonJpaRepository addonJpaRepository;
    private final AddonEntityMapper addonEntityMapper;

    public void rollbackAddons(Number revision, AuditReader auditReader) {
        List<AddonEntity> addons = getEntitiesAtRevision(revision, auditReader, AddonEntity.class);
        addonJpaRepository.deleteAllExcept(addons.stream().map(AddonEntity::getId).collect(Collectors.toList()));
        for (AddonEntity addon : addons) {
            Addon domain = addonEntityMapper.toDomain(addon);
            AddonEntity entity = addonJpaRepository.findById(domain.getDeployment().getName()).orElseGet(AddonEntity::new);
            AddonEntity addonEntity = addonEntityMapper.toEntity(domain, entity);
            addonJpaRepository.save(addonEntity);
        }
    }
}
