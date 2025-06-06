package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ApplicationEntityMapper;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.domain.model.Application;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class ApplicationHistoryRepository extends RevisionRepository {

    private final ApplicationJpaRepository applicationJpaRepository;
    private final ApplicationEntityMapper applicationEntityMapper;

    public void rollbackApplications(Number revision, AuditReader auditReader) {
        List<ApplicationEntity> applications = getEntitiesAtRevision(revision, auditReader, ApplicationEntity.class);
        applicationJpaRepository.deleteAllExcept(applications.stream().map(ApplicationEntity::getId).collect(Collectors.toList()));
        for (ApplicationEntity application : applications) {
            Application domain = applicationEntityMapper.toDomain(application);
            domain.setInterceptors(List.of());
            ApplicationEntity entity = applicationJpaRepository.findById(domain.getDeployment().getName()).orElseGet(ApplicationEntity::new);
            ApplicationEntity applicationEntity = applicationEntityMapper.toEntity(domain, entity);
            applicationJpaRepository.save(applicationEntity);
        }
    }
}
