package com.epam.aidial.cfg.domain.service;


import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ApplicationEntityMapper;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.normalizer.ApplicationNormalizer;
import com.epam.aidial.cfg.domain.validator.ApplicationValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@Service("coreApplicationService")
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Application with name %s does not exist";

    private final ApplicationJpaRepository applicationJpaRepository;
    private final ApplicationEntityMapper mapper;
    private final DeploymentService deploymentService;
    private final ApplicationNormalizer applicationNormalizer;
    private final ApplicationValidator applicationValidator;
    private final HistoryService historyService;
    private final HashCalculator calculator;

    @Transactional(readOnly = true)
    public Collection<Application> getAllApplications() {
        return StreamSupport.stream(applicationJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Application getApplication(String applicationName) {
        return tryGetApplication(applicationName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(applicationName)));
    }

    @Transactional(readOnly = true)
    public DomainObjectWithHash<Application> getApplicationWithHash(String applicationName) {
        var application = getApplication(applicationName);
        return new DomainObjectWithHash<>(application, calculator.calculateHash(application));
    }


    @Transactional(readOnly = true)
    public Optional<Application> tryGetApplication(String applicationName) {
        return Optional.ofNullable(applicationName)
                .flatMap(applicationJpaRepository::findById)
                .map(mapper::toDomain);
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
    public void updateApplication(String applicationName, Application application) {
        performUpdate(applicationName, application, ANY_HASH);
    }

    @Transactional
    public String updateApplication(String applicationName, Application application, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    "Hash must not be null. Use \"*\" to skip optimistic check.");
        }
        var savedModel = performUpdate(applicationName, application, hash);
        return calculator.calculateHash(mapper.toDomain(savedModel));
    }

    private ApplicationEntity performUpdate(String applicationName, Application application, String hash) {
        applicationNormalizer.normalize(application);
        applicationValidator.validateUpdate(applicationName, application);
        var applicationEntity = applicationJpaRepository.findById(applicationName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(applicationName)));

        assertNewApplicationDisplayNameAndDisplayVersion(applicationEntity, application);
        assertNotConcurrencyOverwrite(applicationEntity, hash);
        return applicationJpaRepository.save(mapper.toEntity(application, applicationEntity));
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

    @Transactional(readOnly = true)
    public Collection<Application> getAllAtRevision(Integer revision) {
        return historyService.getEntitiesAtRevision(revision, ApplicationEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private void assertNotConcurrencyOverwrite(ApplicationEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }
        var currentHash = calculator.calculateHash(mapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: applicationName={}, expectedHash={}, currentHash={}",
                    entity.getDeploymentName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException("Optimistic lock conflict on update: applicationName:'"
                    + entity.getDeploymentName() + "'. Reload the data.");
        }
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
