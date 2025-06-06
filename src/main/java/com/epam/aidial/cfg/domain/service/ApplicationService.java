package com.epam.aidial.cfg.domain.service;


import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ApplicationEntityMapper;
import com.epam.aidial.cfg.dao.model.AddonEntity;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.normalizer.ApplicationNormalizer;
import com.epam.aidial.cfg.domain.validator.ApplicationValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("coreApplicationService")
@RequiredArgsConstructor
public class ApplicationService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Application with name %s does not exist";

    private final ApplicationJpaRepository applicationJpaRepository;
    private final ApplicationEntityMapper mapper;
    private final DeploymentService deploymentService;
    private final ApplicationNormalizer applicationNormalizer;
    private final ApplicationValidator applicationValidator;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<Application> getAllApplications() {
        return StreamSupport.stream(applicationJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Application getApplication(String applicationName) {
        return Optional.ofNullable(applicationName)
                .flatMap(applicationJpaRepository::findById)
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(applicationName)));
    }

    @Transactional
    public void createApplication(Application application) {
        applicationNormalizer.normalize(application);
        applicationValidator.validateCreation(application);
        deploymentService.assertDeploymentNotExists(application.getDeployment().getName());
        assertNotExists(application.getDisplayName(), application.getDisplayVersion());
        Optional.of(application)
                .map(domainModel -> mapper.toEntity(domainModel, new ApplicationEntity()))
                .map(applicationJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to create application " + application.getDeployment().getName()));
    }

    @Transactional
    public void updateApplication(String applicationName, Application value) {
        applicationNormalizer.normalize(value);
        applicationValidator.validateUpdate(applicationName, value);
        ApplicationEntity applicationEntity = applicationJpaRepository.findById(applicationName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(applicationName)));
        assertNewApplicationDisplayNameAndDisplayVersion(applicationEntity, value);
        Optional.of(value)
                .map(domainModel -> mapper.toEntity(domainModel, applicationEntity))
                .map(applicationJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to update application " + value.getDeployment().getName()));
    }

    @Transactional
    public void deleteApplication(String applicationName) {
        assertExists(applicationName);
        applicationJpaRepository.deleteById(applicationName);
    }

    @Transactional(readOnly = true)
    public boolean exists(String applicationName) {
        return applicationJpaRepository.existsById(applicationName);
    }

    private void assertExists(String name) {
        boolean exists = applicationJpaRepository.existsById(name);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(name));
        }
    }

    private void assertNotExists(String displayName, String displayVersion) {
        if ((displayName != null || displayVersion != null) && applicationJpaRepository.existsByDisplayNameAndDisplayVersion(displayName, displayVersion)) {
            throw new EntityAlreadyExistsException("Application with display name: '" + displayName + "' and display version: '" + displayVersion + "' already exists");
        }
    }

    private void assertNewApplicationDisplayNameAndDisplayVersion(ApplicationEntity entity, Application domain) {
        String displayName = entity.getDisplayName();
        String displayVersion = entity.getDisplayVersion();
        String newDisplayName = domain.getDisplayName();
        String newDisplayVersion = domain.getDisplayVersion();

        if (!Objects.equals(displayName, newDisplayName) || !Objects.equals(displayVersion, newDisplayVersion)) {
            assertNotExists(newDisplayName, newDisplayVersion);
        }
    }

    @Transactional(readOnly = true)
    public Application getSnapshot(String applicationName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, applicationName, ApplicationEntity.class);
        return mapper.toDomain(entity);
    }
}
