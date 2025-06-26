package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorRunnerJpaRepository;
import com.epam.aidial.cfg.dao.mapper.InterceptorRunnerEntityMapper;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.InterceptorRunnerEntity;
import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class InterceptorRunnerHistoryRepository extends RevisionRepository {

    private final InterceptorRunnerJpaRepository interceptorRunnerJpaRepository;
    private final InterceptorRunnerEntityMapper interceptorRunnerEntityMapper;
    private final InterceptorJpaRepository interceptorJpaRepository;

    public void rollbackInterceptorRunners(Number revision, AuditReader auditReader) {
        Iterable<InterceptorEntity> interceptors = interceptorJpaRepository.findAll();
        interceptors.forEach(entity -> entity.setInterceptorRunner(null));
        interceptorJpaRepository.saveAllAndFlush(interceptors);

        List<InterceptorRunnerEntity> interceptorRunners = getEntitiesAtRevision(revision, auditReader, InterceptorRunnerEntity.class);
        interceptorRunnerJpaRepository.deleteAllExcept(interceptorRunners.stream().map(InterceptorRunnerEntity::getId).collect(Collectors.toList()));
        for (InterceptorRunnerEntity interceptorRunner : interceptorRunners) {
            InterceptorRunner domain = interceptorRunnerEntityMapper.toDomain(interceptorRunner);
            InterceptorRunnerEntity entity = interceptorRunnerJpaRepository.findById(domain.getName()).orElseGet(InterceptorRunnerEntity::new);
            InterceptorRunnerEntity interceptorRunnerEntity = interceptorRunnerEntityMapper.toEntity(domain, entity);
            interceptorRunnerJpaRepository.save(interceptorRunnerEntity);
        }
    }
}
