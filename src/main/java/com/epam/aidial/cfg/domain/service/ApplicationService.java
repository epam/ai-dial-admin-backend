package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.client.ToolsClient;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ApplicationTypeSchemaJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ApplicationContainerEntityMapper;
import com.epam.aidial.cfg.dao.mapper.ApplicationEntityMapper;
import com.epam.aidial.cfg.dao.model.ApplicationContainerEntity;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.RoleBased;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.source.ApplicationContainerSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSchemaSource;
import com.epam.aidial.cfg.domain.normalizer.ApplicationNormalizer;
import com.epam.aidial.cfg.domain.util.ContainerEndpointResolver;
import com.epam.aidial.cfg.domain.util.ContainerSourceChangeDetector;
import com.epam.aidial.cfg.domain.utils.CoreClientUrlUtils;
import com.epam.aidial.cfg.domain.validator.ApplicationValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import com.epam.aidial.cfg.utils.AuthHeaderUtils;
import com.google.api.client.util.Lists;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Application with name %s does not exist";

    private final ApplicationJpaRepository applicationJpaRepository;
    private final ApplicationTypeSchemaJpaRepository applicationTypeSchemaJpaRepository;
    private final InterceptorJpaRepository interceptorJpaRepository;
    private final ApplicationEntityMapper mapper;
    private final ApplicationContainerEntityMapper applicationContainerEntityMapper;
    private final DeploymentService deploymentService;
    private final ApplicationNormalizer applicationNormalizer;
    private final ApplicationValidator applicationValidator;
    private final HistoryService historyService;
    private final HashCalculator calculator;
    private final CoreClientUrlUtils coreClientUrlUtils;
    private final ToolCallService toolCallService;
    private final ContainerEndpointResolver endpointResolver;
    private final ApplicationRefreshService applicationRefreshService;
    private final ToolsClient toolsClient;

    @Transactional(readOnly = true)
    public Collection<Application> getAllApplications() {
        return applicationJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<Application> getAllByNames(List<String> names) {
        return applicationJpaRepository.findAllById(names).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Application> getAllApplicationsOrderedByDisplayNameAscDisplayVersionAscNameAsc() {
        return applicationJpaRepository.findAllByOrderByDisplayNameAscDisplayVersionAscIdAsc().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Application> getAllValidApplicationsOrderedByDisplayNameAscDisplayVersionAscNameAsc() {
        return applicationJpaRepository.findByValidityStateIsValidTrueOrderByDisplayNameAscDisplayVersionAscIdAsc().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Application> getAllByNamesOrderedByDisplayNameAscDisplayVersionAscNameAsc(Collection<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Collections.emptyList();
        }
        return applicationJpaRepository.findByIdInOrderByDisplayNameAscDisplayVersionAscIdAsc(names).stream()
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
        deploymentService.assertInterceptorNotExists(application.getDeployment().getName());
        resolveEndpointsIfContainerSource(application);
        Optional.of(application)
                .map(domainModel -> toEntity(domainModel, new ApplicationEntity()))
                .map(this::save)
                .orElseThrow(() -> new RuntimeException("Unable to create application " + application.getDeployment().getName()));
    }

    @Transactional
    public void updateApplication(String applicationName, Application application) {
        performUpdate(applicationName, application, ANY_HASH);
    }

    @Transactional
    public String updateApplication(String applicationName, Application application, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. Application:%s.", applicationName));
        }
        var savedApplication = performUpdate(applicationName, application, hash);
        return calculator.calculateHash(mapper.toDomain(savedApplication));
    }

    private ApplicationEntity performUpdate(String applicationName, Application application, String hash) {
        applicationNormalizer.normalize(application);
        applicationValidator.validateUpdate(applicationName, application);
        var applicationEntity = applicationJpaRepository.findById(applicationName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(applicationName)));

        assertNewApplicationDisplayNameAndDisplayVersion(applicationEntity, application);
        assertNotConcurrencyOverwrite(applicationEntity, hash);
        resolveEndpointsIfContainerSource(application, applicationEntity);
        return save(toEntity(application, applicationEntity));
    }

    private ApplicationEntity save(ApplicationEntity applicationEntity) {
        ApplicationEntity savedApplicationEntity = applicationJpaRepository.save(applicationEntity);
        deploymentService.addDeploymentRoleLimitToRoleIfAbsent(savedApplicationEntity.getDeployment());
        return savedApplicationEntity;
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
    public Application getSnapshot(String applicationName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, applicationName, ApplicationEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<Application> getAllAtRevision(Number revision) {
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
            throw OptimisticLockConflictException.onUpdate("Application", entity.getDeploymentName(), expectedHash, currentHash);
        }
    }

    @Transactional
    public void rollbackApplications(Number revision) {
        Collection<Application> applications = getAllAtRevision(revision);
        List<String> ids = applications.stream().map(RoleBased::getDeployment).map(Deployment::getName).toList();
        if (CollectionUtils.isEmpty(ids)) {
            applicationJpaRepository.deleteAll();
        } else {
            Iterable<ApplicationEntity> applicationsToDelete = applicationJpaRepository.findByIdNotIn(ids);
            applicationJpaRepository.deleteAll(applicationsToDelete);
        }

        Set<String> allInterceptorNames = interceptorJpaRepository.findAllNames();
        Set<String> allSchemaIds = applicationTypeSchemaJpaRepository.findAllIds();
        for (Application application : applications) {
            application.getInterceptors().removeIf(interceptor -> !allInterceptorNames.contains(interceptor));
            if (application.getSource() instanceof ApplicationSchemaSource schemaSource
                    && !allSchemaIds.contains(schemaSource.getApplicationTypeSchemaId().toString())) {
                application.setSource(new ApplicationEndpointsSource());
                application.setEndpoint("endpoint");
            }
            ApplicationEntity entity = applicationJpaRepository.findById(application.getDeployment().getName()).orElseGet(ApplicationEntity::new);
            ApplicationEntity applicationEntity = toEntity(application, entity);
            applicationJpaRepository.save(applicationEntity);
        }
    }

    @Transactional(readOnly = true)
    public McpSchema.CallToolResult callTool(String applicationName, McpSchema.CallToolRequest callToolRequest) {
        var application = getApplication(applicationName);

        var transport = resolveTransport(application, applicationName);

        var url = String.format(
                "%s/v1/deployments/%s/mcp",
                coreClientUrlUtils.getNormalizedCoreClientUrl(),
                application.getDeployment().getName()
        );
        return toolCallService.callTool(
                url,
                transport,
                AuthHeaderUtils.getAuthHeaders(),
                callToolRequest);
    }

    @Transactional(readOnly = true)
    public McpSchema.ListToolsResult getDiscoveredTools(String applicationName, String nextCursor) {
        assertExists(applicationName);
        return toolsClient.getTools(applicationName, nextCursor);
    }

    @Transactional(readOnly = true)
    public void refreshEndpoints() {
        var applicationEntities = applicationJpaRepository.findByApplicationContainerIsNotNull();
        List<String> successfulApplications = new ArrayList<>();
        List<String> failedApplications = new ArrayList<>();

        for (var entity : applicationEntities) {
            String applicationName = entity.getDeploymentName();
            try {
                applicationRefreshService.refreshEndpoints(entity);
                successfulApplications.add(applicationName);
            } catch (Exception e) {
                log.debug("Failed to refresh endpoints for application '{}'", applicationName, e);
                failedApplications.add(applicationName);
            }
        }

        if (!failedApplications.isEmpty()) {
            log.warn("Failed to refresh endpoints for {} applications: {}. Use DEBUG log level for details",
                    failedApplications.size(), String.join(", ", failedApplications));
        }

        if (!successfulApplications.isEmpty()) {
            log.debug("Successfully refreshed endpoints for {} applications: {}",
                    successfulApplications.size(), String.join(", ", successfulApplications));
        }
    }

    private ToolSet.Transport resolveTransport(Application application, String applicationName) {
        if (application.getMcp() != null) {
            return ToolSet.Transport.valueOf(application.getMcp().getTransport().name());
        }

        if (application.getSource() instanceof ApplicationSchemaSource schemaSource) {
            var schema = findApplicationTypeSchemaById(schemaSource.getApplicationTypeSchemaId());

            if (schema != null && schema.getApplicationTypeMcp() != null) {
                return ToolSet.Transport.valueOf(
                        schema.getApplicationTypeMcp().getTransport().name()
                );
            }
        }

        throw new UnsupportedOperationException(
                "Application '%s' does not support MCP tool discovery".formatted(applicationName)
        );
    }

    private void resolveEndpointsIfContainerSource(Application application) {
        if (!(application.getSource() instanceof ApplicationContainerSource)) {
            return;
        }
        endpointResolver.processContainerEndpoints(application);
    }

    private void resolveEndpointsIfContainerSource(Application application, ApplicationEntity existingEntity) {
        if (!(application.getSource() instanceof ApplicationContainerSource incomingContainer)) {
            return;
        }

        ApplicationContainerEntity existingContainer = existingEntity.getApplicationContainer();
        if (existingContainer == null
                || ContainerSourceChangeDetector.hasSourceChanged(incomingContainer, existingContainer)) {
            endpointResolver.processContainerEndpoints(application);
            return;
        }

        endpointResolver.tryProcessContainerEndpoints(application, existingEntity);
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

    private ApplicationEntity toEntity(Application domain, ApplicationEntity entity) {
        List<InterceptorEntity> interceptors = findInterceptorsByNames(domain.getInterceptors());

        URI schemaId = domain.getSource() instanceof ApplicationSchemaSource schemaSource
                ? schemaSource.getApplicationTypeSchemaId() : null;
        ApplicationTypeSchemaEntity applicationTypeSchema = findApplicationTypeSchemaById(schemaId);

        ApplicationContainerEntity applicationContainer = null;
        if (domain.getSource() instanceof ApplicationContainerSource containerSource) {
            applicationContainer = applicationContainerEntityMapper.toEntity(containerSource);
        }

        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> rolesForLimits = deploymentService.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        return mapper.toEntity(domain, entity, interceptors, applicationTypeSchema, applicationContainer, roleLimits, rolesForLimits);
    }

    private List<InterceptorEntity> findInterceptorsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<InterceptorEntity> interceptors = Lists.newArrayList(interceptorJpaRepository.findAllById(names));
        Set<String> existingInterceptors = interceptors.stream().map(InterceptorEntity::getName).collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingInterceptors);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find interceptors: " + namesDiff);
        }

        return interceptors;
    }

    private ApplicationTypeSchemaEntity findApplicationTypeSchemaById(URI applicationTypeSchemaId) {
        String schemaId = applicationTypeSchemaId != null ? applicationTypeSchemaId.toString() : null;

        if (StringUtils.isBlank(schemaId)) {
            return null;
        }

        return applicationTypeSchemaJpaRepository.findById(schemaId)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find application type schema with schema id: " + schemaId));
    }
}