package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.AdminSettingsJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AdminSettingsEntityMapper;
import com.epam.aidial.cfg.dao.model.AdminSettingsEntity;
import com.epam.aidial.cfg.domain.model.AdminSettings;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.validator.AdminSettingsValidator;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

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
    private final AdminSettingsValidator adminSettingsValidator;

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
    public String updateCoreConfigVersion(String coreConfigVersion, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException("Hash must not be null. Use \"*\" to skip optimistic check. AdminSettings");
        }

        adminSettingsValidator.validateCoreConfigVersionUpdate(coreConfigVersion);

        AdminSettingsEntity adminSettingsEntity = getOrThrow();
        assertNotConcurrencyOverwrite(adminSettingsEntity, hash);
        adminSettingsEntity.setCoreConfigVersion(coreConfigVersion);
        AdminSettingsEntity savedAdminSettings = adminSettingsJpaRepository.save(adminSettingsEntity);

        return hashCalculator.calculateHash(adminSettingsEntityMapper.toDomain(savedAdminSettings));
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

    private void assertNotConcurrencyOverwrite(AdminSettingsEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }

        var currentHash = hashCalculator.calculateHash(adminSettingsEntityMapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: adminSettings, expectedHash={}, currentHash={}",
                    expectedHash, currentHash);
            throw new OptimisticLockConflictException("Unable to update AdminSettings. The data may have been modified by another user. "
                    + "Please reload the data and try again.");
        }
    }
}
