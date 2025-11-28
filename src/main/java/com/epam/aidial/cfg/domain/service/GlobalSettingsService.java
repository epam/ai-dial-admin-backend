package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.GlobalSettingsJpaRepository;
import com.epam.aidial.cfg.dao.mapper.GlobalSettingsEntityMapper;
import com.epam.aidial.cfg.dao.model.GlobalSettingsEntity;
import com.epam.aidial.cfg.domain.model.GlobalSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@LogExecution
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSettingsService {
    private static final Integer GLOBAL_SETTINGS_ID = 1;
    private final GlobalSettingsJpaRepository globalSettingsJpaRepository;
    private final GlobalSettingsEntityMapper globalSettingsMapper;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public GlobalSettings getGlobalSettings() {
        var entity = globalSettingsJpaRepository.findById(GLOBAL_SETTINGS_ID)
                .orElseGet(() -> globalSettingsJpaRepository.save(new GlobalSettingsEntity()));
        return globalSettingsMapper.toDomain(entity);
    }

    @Transactional
    public void saveGlobalSettings(GlobalSettings globalSettings) {
        var entity = globalSettingsJpaRepository.findById(GLOBAL_SETTINGS_ID).orElseGet(GlobalSettingsEntity::new);
        globalSettingsJpaRepository.save(globalSettingsMapper.toGlobalSettingsEntity(globalSettings, entity));
    }

    @Transactional(readOnly = true)
    public GlobalSettings getAtRevision(Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, GLOBAL_SETTINGS_ID, GlobalSettingsEntity.class);
        return globalSettingsMapper.toDomain(entity);
    }
}