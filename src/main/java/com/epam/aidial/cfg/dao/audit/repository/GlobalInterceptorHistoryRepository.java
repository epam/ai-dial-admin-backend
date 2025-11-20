package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.GlobalInterceptorJpaRepository;
import com.epam.aidial.cfg.dao.model.GlobalInterceptorEntity;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class GlobalInterceptorHistoryRepository extends RevisionRepository {
    private final GlobalInterceptorJpaRepository globalInterceptorJpaRepository;

    public void rollbackGlobalInterceptors(Number revision, AuditReader auditReader) {
        var globalInterceptors = getEntitiesAtRevision(revision, auditReader, GlobalInterceptorEntity.class);
        globalInterceptorJpaRepository.deleteAllExcept(globalInterceptors.stream().map(GlobalInterceptorEntity::getId).collect(Collectors.toList()));
        globalInterceptorJpaRepository.saveAll(globalInterceptors);
    }
}