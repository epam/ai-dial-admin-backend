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
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

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

//    @Transactional
//    public void rollbackGlobalSettings(Number revision) {
//
//        Iterable<InterceptorEntity> interceptors = interceptorJpaRepository.findAll();
//        interceptors.forEach(entity -> entity.setInterceptorRunner(null));
//        interceptorJpaRepository.saveAllAndFlush(interceptors);
//
//        Collection<InterceptorRunner> interceptorRunners = getAllAtRevision(revision);
//        interceptorRunnerJpaRepository.deleteAllExcept(interceptorRunners.stream().map(InterceptorRunner::getName).toList());
//
//        for (InterceptorRunner interceptorRunner : interceptorRunners) {
//            InterceptorRunnerEntity entity = interceptorRunnerJpaRepository.findById(interceptorRunner.getName()).orElseGet(InterceptorRunnerEntity::new);
//            InterceptorRunnerEntity interceptorRunnerEntity = toEntity(interceptorRunner, entity);
//            interceptorRunnerJpaRepository.save(interceptorRunnerEntity);
//        }
//    }

    @Transactional(readOnly = true)
    public List<GlobalSettings> getAllAtRevision(Number revision) {
        return historyService.getEntitiesAtRevision(revision, GlobalSettingsEntity.class)
                .stream()
                .map(globalSettingsMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rollbackGlobalSettings(Number revision) {
        var history = getAllAtRevision(revision);
        var currentEntity = globalSettingsJpaRepository.findById(GLOBAL_SETTINGS_ID).orElseGet(GlobalSettingsEntity::new);
        GlobalSettingsEntity entityToSave;
        if (CollectionUtils.isEmpty(history)) {
            var defaultDomain = new GlobalSettings();
            entityToSave = globalSettingsMapper.toGlobalSettingsEntity(defaultDomain, currentEntity);
        } else {
            var domain = history.get(0);
            entityToSave = globalSettingsMapper.toGlobalSettingsEntity(domain, currentEntity);
        }
        globalSettingsJpaRepository.save(entityToSave);
    }
}