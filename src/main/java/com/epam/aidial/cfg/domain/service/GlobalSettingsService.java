package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.GlobalSettingsJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.mapper.GlobalSettingsEntityMapper;
import com.epam.aidial.cfg.dao.model.GlobalSettingsEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.GlobalSettings;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import com.google.api.client.util.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSettingsService {
    private static final Integer GLOBAL_SETTINGS_ID = 1;
    private final GlobalSettingsJpaRepository globalSettingsJpaRepository;
    private final InterceptorJpaRepository interceptorJpaRepository;
    private final GlobalSettingsEntityMapper globalSettingsMapper;
    private final HistoryService historyService;
    private final HashCalculator calculator;

    @Transactional(readOnly = true)
    public GlobalSettings getGlobalSettings() {
        var entity = globalSettingsJpaRepository.findById(GLOBAL_SETTINGS_ID)
                .orElseThrow(() -> new EntityNotFoundException(("Global settings does not exist")));
        return globalSettingsMapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public DomainObjectWithHash<GlobalSettings> getGlobalSettingsWithHash() {
        var globalSettings = getGlobalSettings();
        return new DomainObjectWithHash<>(globalSettings, calculator.calculateHash(globalSettings));
    }

    @Transactional
    public void update(GlobalSettings globalSettings) {
        performUpdate(globalSettings, ANY_HASH);
    }

    @Transactional
    public String update(GlobalSettings globalSettings, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. GlobalSettings."));
        }
        var savedGlobalSettings = performUpdate(globalSettings, hash);
        return calculator.calculateHash(globalSettingsMapper.toDomain(savedGlobalSettings));
    }

    private GlobalSettingsEntity performUpdate(GlobalSettings globalSettings, String hash) {
        validateGlobalInterceptorsByNames(globalSettings.getGlobalInterceptors());
        var entity = globalSettingsJpaRepository.findById(GLOBAL_SETTINGS_ID)
                .orElseThrow(() -> new IllegalStateException("Global settings does not exist"));
        assertNotConcurrencyOverwrite(entity, hash);
        return globalSettingsJpaRepository.save(globalSettingsMapper.toGlobalSettingsEntity(globalSettings, entity));
    }

    @Transactional(readOnly = true)
    public GlobalSettings getAtRevision(Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, GLOBAL_SETTINGS_ID, GlobalSettingsEntity.class);
        return globalSettingsMapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public List<GlobalSettings> getAllAtRevision(Number revision) {
        return historyService.getEntitiesAtRevision(revision, GlobalSettingsEntity.class)
                .stream()
                .map(globalSettingsMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rollbackGlobalSettings(Number revision) {
        var history = getAtRevision((Integer) revision);
        var currentEntity = globalSettingsJpaRepository.findById(GLOBAL_SETTINGS_ID).orElseGet(GlobalSettingsEntity::new);
        GlobalSettingsEntity entityToSave;
        validateGlobalInterceptorsByNames(history.getGlobalInterceptors());
        entityToSave = globalSettingsMapper.toGlobalSettingsEntity(history, currentEntity);
        globalSettingsJpaRepository.save(entityToSave);
    }

    private void validateGlobalInterceptorsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return;
        }

        List<InterceptorEntity> interceptors = Lists.newArrayList(interceptorJpaRepository.findAllById(names));
        Set<String> existingInterceptors = interceptors.stream().map(InterceptorEntity::getName).collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingInterceptors);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("One or more global interceptor IDs do not exist as interceptors: " + namesDiff);
        }
    }

    private void assertNotConcurrencyOverwrite(GlobalSettingsEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }
        var currentHash = calculator.calculateHash(globalSettingsMapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: globalSettings, expectedHash={}, currentHash={}",
                    expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Unable to update GlobalSettings. The data may have been modified by another user, "
                    + "Please reload the data and try again."));
        }
    }
}