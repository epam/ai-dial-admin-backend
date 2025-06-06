package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ApplicationTypeSchemaJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ApplicationTypeSchemaEntityMapper;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.validator.ApplicationTypeSchemaValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("coreApplicationTypeSchemaService")
@RequiredArgsConstructor
public class ApplicationTypeSchemaService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Application type schema with schema id %s does not exist";

    private final ApplicationTypeSchemaJpaRepository jpaRepository;
    private final ApplicationTypeSchemaEntityMapper mapper;
    private final ApplicationJpaRepository applicationJpaRepository;
    private final ApplicationTypeSchemaValidator applicationTypeSchemaValidator;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<ApplicationTypeSchema> getAll() {
        return StreamSupport.stream(jpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationTypeSchema get(String id) {
        return Optional.ofNullable(id)
                .flatMap(jpaRepository::findById)
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(id)));
    }

    @Transactional
    public void create(ApplicationTypeSchema applicationTypeSchema) {
        assertNotExists(applicationTypeSchema.getSchemaId());
        Optional.of(applicationTypeSchema)
                .map(domainModel -> mapper.toEntity(domainModel, new ApplicationTypeSchemaEntity()))
                .ifPresent(jpaRepository::save);
    }

    @Transactional
    public void update(String schemaId, ApplicationTypeSchema value) {
        applicationTypeSchemaValidator.validateUpdate(schemaId, value);
        ApplicationTypeSchemaEntity applicationTypeSchemaEntity = findBySchemaId(schemaId);
        Optional.of(value)
                .map(domainModel -> mapper.toEntity(domainModel, applicationTypeSchemaEntity))
                .ifPresent(jpaRepository::save);
    }

    @Transactional
    public void delete(String id, boolean removeApplication) {
        ApplicationTypeSchemaEntity applicationTypeSchema = findBySchemaId(id);
        List<ApplicationEntity> applications = applicationTypeSchema.getApplications();

        if (CollectionUtils.isEmpty(applications)) {
            jpaRepository.delete(applicationTypeSchema);
            return;
        }

        if (removeApplication) {
            removeApplicationsFromSchema(applicationTypeSchema, applications);
        } else {
            detachApplicationsFromSchema(applicationTypeSchema, applications);
        }
    }

    @Transactional(readOnly = true)
    public boolean exists(String schemaId) {
        return jpaRepository.existsById(schemaId);
    }

    @Transactional(readOnly = true)
    public ApplicationTypeSchema getSnapshot(String id, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, id, ApplicationTypeSchemaEntity.class);
        return mapper.toDomain(entity);
    }

    private ApplicationTypeSchemaEntity findBySchemaId(String schemaId) {
        return jpaRepository.findById(schemaId)
                .orElseThrow(() -> new EntityNotFoundException("Application type schema with schema id " + schemaId + " does not exist"));
    }

    private void removeApplicationsFromSchema(ApplicationTypeSchemaEntity schemaEntity, List<ApplicationEntity> applications) {
        applicationJpaRepository.deleteAll(applications);
        jpaRepository.delete(schemaEntity);
    }

    private void detachApplicationsFromSchema(ApplicationTypeSchemaEntity schemaEntity, List<ApplicationEntity> applications) {
        applications.forEach(app -> {
            app.setApplicationTypeSchema(null);
            app.setEndpoint(schemaEntity.getSchemaId());
        });
        jpaRepository.delete(schemaEntity);
    }

    private void assertNotExists(String schemaId) {
        if (jpaRepository.existsById(schemaId)) {
            throw new EntityAlreadyExistsException("Application type schema with schema id " + schemaId + " already exists");
        }
    }
}
