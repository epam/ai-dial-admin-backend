package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.AdminSettingsJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AdminSettingsEntityMapper;
import com.epam.aidial.cfg.dao.model.AdminSettingsEntity;
import com.epam.aidial.cfg.domain.model.AdminSettings;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@LogExecution
@Slf4j
public class AdminSettingsService {

    private static final Integer ADMIN_SETTINGS_ID = 1;

    private final AdminSettingsJpaRepository adminSettingsJpaRepository;
    private final AdminSettingsEntityMapper adminSettingsEntityMapper;
    private final HistoryService historyService;
    private final HashCalculator hashCalculator;

    @Transactional(readOnly = true)
    public AdminSettings getAdminSettings() {
        AdminSettingsEntity adminSettingsEntity = getOrThrow();
        return adminSettingsEntityMapper.toDomain(adminSettingsEntity);
    }

    @Transactional(readOnly = true)
    public DomainObjectWithHash<AdminSettings> getAdminSettingsWithHash() {
        AdminSettings adminSettings = getAdminSettings();
        return new DomainObjectWithHash<>(adminSettings, hashCalculator.calculateHash(adminSettings));
    }

    @Transactional
    public void updateCoreConfigVersion(String coreConfigVersion) {
        AdminSettingsEntity adminSettingsEntity = getOrThrow();
        adminSettingsEntity.setCoreConfigVersion(coreConfigVersion);
        adminSettingsJpaRepository.save(adminSettingsEntity);
    }

    private AdminSettingsEntity getOrThrow() {
        return adminSettingsJpaRepository.findById(ADMIN_SETTINGS_ID)
                .orElseThrow(() -> new IllegalStateException("Admin settings does not exist"));
    }

    @Transactional(readOnly = true)
    public AdminSettings getAtRevision(Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, ADMIN_SETTINGS_ID, AdminSettingsEntity.class);
        return adminSettingsEntityMapper.toDomain(entity);
    }

    @Transactional
    public void rollbackAdminSettings(Number revision) {
        AdminSettings adminSettingsAtRevision = getAtRevision((Integer) revision);
        AdminSettingsEntity currentAdminSettingsEntity = getOrThrow();
        AdminSettingsEntity adminSettingsEntityToSave = adminSettingsEntityMapper.toEntity(adminSettingsAtRevision, currentAdminSettingsEntity);
        adminSettingsJpaRepository.save(adminSettingsEntityToSave);
    }
}
