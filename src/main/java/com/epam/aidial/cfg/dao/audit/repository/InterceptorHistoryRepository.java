package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.mapper.InterceptorEntityMapper;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.model.Interceptor;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class InterceptorHistoryRepository extends RevisionRepository {

    private final InterceptorJpaRepository interceptorJpaRepository;
    private final InterceptorEntityMapper interceptorEntityMapper;

    public void rollbackInterceptors(Number revision, AuditReader auditReader) {
        List<InterceptorEntity> interceptors = getEntitiesAtRevision(revision, auditReader, InterceptorEntity.class);
        interceptorJpaRepository.deleteAllExcept(interceptors.stream().map(InterceptorEntity::getId).collect(Collectors.toList()));
        for (InterceptorEntity interceptor : interceptors) {
            Interceptor domain = interceptorEntityMapper.toDomain(interceptor);
            InterceptorEntity entity = interceptorJpaRepository.findById(domain.getName()).orElseGet(InterceptorEntity::new);
            InterceptorEntity interceptorEntity = interceptorEntityMapper.toEntity(domain, entity);
            interceptorJpaRepository.save(interceptorEntity);
        }
    }
}
