package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.GlobalInterceptorJpaRepository;
import com.epam.aidial.cfg.dao.mapper.GlobalSettingsMapper;
import com.epam.aidial.cfg.dao.model.GlobalInterceptorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@LogExecution
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalSettingsService {
    private final GlobalInterceptorJpaRepository globalInterceptorJpaRepository;
    private final GlobalSettingsMapper globalSettingsMapper;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<String> getAllGlobalInterceptors() {
        return globalInterceptorJpaRepository.findAll().stream()
                .sorted(Comparator.comparing(GlobalInterceptorEntity::getInterceptorOrder))
                .map(GlobalInterceptorEntity::getId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveGlobalInterceptors(Collection<String> globalInterceptorIds) {
        try {
            if (CollectionUtils.isNotEmpty(globalInterceptorIds)) {
                //todo
                globalInterceptorJpaRepository.deleteAll();
                globalInterceptorJpaRepository.saveAllAndFlush(
                        globalSettingsMapper.toGlobalInterceptorEntity(globalInterceptorIds.stream().toList()));
            }
        } catch (DataIntegrityViolationException ex) {
            log.error("Failed to save global interceptors'{}'", globalInterceptorIds);
            throw new IllegalArgumentException("Some global interceptors(all) don't exist as interceptors", ex);
        }
    }

    @Transactional
    public void deleteAllGlobalInterceptors() {
        globalInterceptorJpaRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public boolean exists(String interceptorName) {
        return globalInterceptorJpaRepository.existsById(interceptorName);
    }

    @Transactional(readOnly = true)
    public Collection<String> getAllAtRevision(Integer revision) {
        var entities = historyService.getEntitiesAtRevision(revision, GlobalInterceptorEntity.class);
        return entities.stream()
                .sorted(Comparator.comparing(GlobalInterceptorEntity::getInterceptorOrder))
                .map(GlobalInterceptorEntity::getId)
                .collect(Collectors.toList());
    }
}