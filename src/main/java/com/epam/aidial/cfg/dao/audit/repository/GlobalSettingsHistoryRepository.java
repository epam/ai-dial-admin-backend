package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.GlobalSettingsJpaRepository;
import com.epam.aidial.cfg.dao.mapper.GlobalSettingsEntityMapper;
import com.epam.aidial.cfg.dao.model.GlobalSettingsEntity;
import com.epam.aidial.cfg.domain.model.GlobalSettings;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@RequiredArgsConstructor
@LogExecution
public class GlobalSettingsHistoryRepository extends RevisionRepository {
    private static final Integer GLOBAL_SETTINGS_ID = 1;
    private final GlobalSettingsJpaRepository globalSettingsJpaRepository;
    private final GlobalSettingsEntityMapper globalSettingsMapper;

    public void rollbackGlobalSettings(Number revision, AuditReader auditReader) {
        var history = getEntitiesAtRevision(revision, auditReader, GlobalSettingsEntity.class);
        var currentEntity = globalSettingsJpaRepository.findById(GLOBAL_SETTINGS_ID).orElseGet(GlobalSettingsEntity::new);
        GlobalSettingsEntity entityToSave;
        if (CollectionUtils.isEmpty(history)) {
            var defaultDomain = new GlobalSettings();
            entityToSave = globalSettingsMapper.toGlobalSettingsEntity(defaultDomain, currentEntity);
        } else {
            var historicalEntity = history.get(0);
            var domain = globalSettingsMapper.toDomain(historicalEntity);
            entityToSave = globalSettingsMapper.toGlobalSettingsEntity(domain, currentEntity);
        }
        globalSettingsJpaRepository.save(entityToSave);
    }
}