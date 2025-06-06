package com.epam.aidial.cfg.dao.audit.repository;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ApplicationTypeSchemaJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ApplicationTypeSchemaEntityMapper;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class ApplicationTypeSchemaHistoryRepository extends RevisionRepository {

    private final ApplicationJpaRepository applicationJpaRepository;
    private final ApplicationTypeSchemaJpaRepository applicationTypeSchemaJpaRepository;
    private final ApplicationTypeSchemaEntityMapper applicationTypeSchemaEntityMapper;

    public void rollbackApplicationTypeSchemas(Number revision, AuditReader auditReader) {
        Iterable<ApplicationEntity> applications = applicationJpaRepository.findAll();
        applications.forEach(applicationEntity -> {
            applicationEntity.setApplicationTypeSchema(null);
            applicationEntity.setEndpoint("endpoint");
        });
        applicationJpaRepository.saveAllAndFlush(applications);
        List<ApplicationTypeSchemaEntity> applicationTypeSchemas = getEntitiesAtRevision(revision, auditReader, ApplicationTypeSchemaEntity.class);
        applicationTypeSchemaJpaRepository.deleteAllExcept(applicationTypeSchemas.stream().map(ApplicationTypeSchemaEntity::getId).collect(Collectors.toList()));
        for (ApplicationTypeSchemaEntity applicationTypeSchema : applicationTypeSchemas) {
            ApplicationTypeSchema domain = applicationTypeSchemaEntityMapper.toDomain(applicationTypeSchema);
            ApplicationTypeSchemaEntity entity = applicationTypeSchemaJpaRepository.findById(domain.getSchemaId()).orElseGet(ApplicationTypeSchemaEntity::new);
            ApplicationTypeSchemaEntity applicationTypeSchemaEntity = applicationTypeSchemaEntityMapper.toEntity(domain, entity);
            applicationTypeSchemaJpaRepository.save(applicationTypeSchemaEntity);
        }
    }
}
