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
import com.google.api.client.util.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        applicationTypeSchemaValidator.validateCreation(applicationTypeSchema);
        assertNotExists(applicationTypeSchema.getSchemaId());
        Optional.of(applicationTypeSchema)
                .map(domainModel -> toEntity(domainModel, new ApplicationTypeSchemaEntity()))
                .ifPresent(jpaRepository::save);
    }

    @Transactional
    public void update(String schemaId, ApplicationTypeSchema value) {
        applicationTypeSchemaValidator.validateUpdate(schemaId, value);
        ApplicationTypeSchemaEntity applicationTypeSchemaEntity = findBySchemaId(schemaId);
        Optional.of(value)
                .map(domainModel -> toEntity(domainModel, applicationTypeSchemaEntity))
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

    @Transactional(readOnly = true)
    public Collection<ApplicationTypeSchema> getAllAtRevision(Number revision) {
        return historyService.getEntitiesAtRevision(revision, ApplicationTypeSchemaEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rollbackApplicationTypeSchemas(Number revision) {
        Iterable<ApplicationEntity> applications = applicationJpaRepository.findAll();
        applications.forEach(applicationEntity -> {
            applicationEntity.setApplicationTypeSchema(null);
            applicationEntity.setEndpoint("endpoint");
        });
        applicationJpaRepository.saveAllAndFlush(applications);
        Collection<ApplicationTypeSchema> applicationTypeSchemas = getAllAtRevision(revision);
        jpaRepository.deleteAllExcept(applicationTypeSchemas.stream().map(ApplicationTypeSchema::getSchemaId).collect(Collectors.toList()));
        for (ApplicationTypeSchema domain : applicationTypeSchemas) {
            ApplicationTypeSchemaEntity entity = jpaRepository.findById(domain.getSchemaId()).orElseGet(ApplicationTypeSchemaEntity::new);
            ApplicationTypeSchemaEntity applicationTypeSchemaEntity = toEntity(domain, entity);
            jpaRepository.save(applicationTypeSchemaEntity);
        }
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

    private ApplicationTypeSchemaEntity toEntity(ApplicationTypeSchema domain, ApplicationTypeSchemaEntity entity) {
        List<ApplicationEntity> applications = findApplicationsByNames(domain.getApplications());
        return mapper.toEntity(domain, entity, applications);
    }

    private List<ApplicationEntity> findApplicationsByNames(List<String> names) {
        if (names == null) {
            return null;
        }

        if (names.isEmpty()) {
            return List.of();
        }

        List<ApplicationEntity> existingApplications = Lists.newArrayList(applicationJpaRepository.findAllById(names));
        Set<String> existingApplicationsNames = existingApplications.stream()
                .map(ApplicationEntity::getDeploymentName)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingApplicationsNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find applications: " + namesDiff);
        }

        return existingApplications;
    }
}
