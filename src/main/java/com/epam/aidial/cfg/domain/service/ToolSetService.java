package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ToolSetJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ToolSetContainerEntityMapper;
import com.epam.aidial.cfg.dao.mapper.ToolSetEntityMapper;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.ToolSetContainerEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.SecuredResource;
import com.epam.aidial.cfg.domain.model.SecuredRoleBased;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import com.epam.aidial.cfg.domain.model.source.ToolSetSource;
import com.epam.aidial.cfg.domain.normalizer.ToolSetNormalizer;
import com.epam.aidial.cfg.domain.util.ContainerEndpointResolver;
import com.epam.aidial.cfg.domain.validator.ToolSetValidator;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class ToolSetService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "ToolSet with name %s does not exist";

    private final ToolSetJpaRepository toolSetJpaRepository;
    private final ToolSetNormalizer toolSetNormalizer;
    private final ToolSetValidator toolSetValidator;
    private final ToolSetEntityMapper mapper;
    private final ToolSetContainerEntityMapper toolSetContainerEntityMapper;
    private final DeploymentService deploymentService;
    private final HistoryService historyService;
    private final ToolDiscoveryService toolDiscoveryService;
    private final ToolSetRefreshService toolSetRefreshService;
    private final ContainerEndpointResolver endpointResolver;

    @Transactional(readOnly = true)
    public Collection<ToolSet> getAll() {
        return toolSetJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ToolSet get(String toolSetName) {
        return tryGetToolSet(toolSetName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(toolSetName)));
    }

    @Transactional(readOnly = true)
    public Optional<ToolSet> tryGetToolSet(String toolSetName) {
        return Optional.ofNullable(toolSetName)
                .flatMap(toolSetJpaRepository::findById)
                .map(mapper::toDomain);
    }

    @Transactional
    public void create(ToolSet toolSet) {
        toolSetNormalizer.normalize(toolSet);
        toolSetValidator.validateCreation(toolSet);
        deploymentService.assertDeploymentNotExists(toolSet.getDeployment().getName());
        resolveEndpointsIfContainerSource(toolSet);
        Optional.of(toolSet)
                .map(domainModel -> toEntity(domainModel, new ToolSetEntity()))
                .map(toolSetJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to create ToolSet " + toolSet.getDeployment().getName()));
    }

    @Transactional
    public void update(String toolSetName, ToolSet toolSet) {
        toolSetNormalizer.normalize(toolSet);
        toolSetValidator.validateUpdate(toolSetName, toolSet);
        ToolSetEntity toolSetEntity = toolSetJpaRepository.findById(toolSetName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(toolSetName)));
        resolveEndpointsIfContainerSource(toolSet);
        Optional.of(toolSet)
                .map(domainModel -> toEntity(domainModel, toolSetEntity))
                .map(toolSetJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to update ToolSet " + toolSet.getDeployment().getName()));
    }

    @Transactional
    public void delete(String toolSetName) {
        assertExists(toolSetName);
        toolSetJpaRepository.deleteById(toolSetName);
    }

    @Transactional(readOnly = true)
    public boolean exists(String toolSetName) {
        return toolSetJpaRepository.existsById(toolSetName);
    }

    @Transactional(readOnly = true)
    public ToolSet getSnapshot(String toolSetName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, toolSetName, ToolSetEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<ToolSet> getAllAtRevision(Number revision) {
        return historyService.getEntitiesAtRevision(revision, ToolSetEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rollbackToolSets(Number revision) {
        Collection<ToolSet> toolSets = getAllAtRevision(revision);
        List<String> ids = toolSets.stream().map(SecuredRoleBased::getDeployment).map(SecuredResource::getName).toList();
        toolSetJpaRepository.deleteAllExcept(ids);

        for (ToolSet toolSet : toolSets) {
            ToolSetEntity entity = toolSetJpaRepository.findById(toolSet.getDeployment().getName()).orElseGet(ToolSetEntity::new);
            ToolSetEntity toolSetEntity = toEntity(toolSet, entity);
            toolSetJpaRepository.save(toolSetEntity);
        }
    }

    @Transactional(readOnly = true)
    public McpSchema.ListToolsResult getDiscoveredTools(String toolSetName, String nextCursor) {
        var toolSet = get(toolSetName);
        return toolDiscoveryService.discoverTools(toolSet.getEndpoint(), toolSet.getTransport(), nextCursor);
    }

    @Transactional(readOnly = true)
    public void refreshEndpoints() {
        var entities = toolSetJpaRepository.findByContainerIdIsNotNull();
        List<String> successfulToolSets = new ArrayList<>();
        List<String> failedToolSets = new ArrayList<>();

        for (var entity : entities) {
            String name = entity.getDeploymentName();
            log.debug("Refreshing endpoints for toolset '%s'".formatted(name));
            try {
                toolSetRefreshService.refreshEndpoints(entity);
                successfulToolSets.add(name);
            } catch (Exception e) {
                log.error("Failed to refresh endpoints for toolset '{}'", name, e);
                failedToolSets.add(name);
            }
        }

        if (!failedToolSets.isEmpty()) {
            log.warn("Failed to refresh endpoints for {} toolsets: {}",
                    failedToolSets.size(), String.join(", ", failedToolSets));
        }

        if (!successfulToolSets.isEmpty()) {
            log.debug("Successfully refreshed endpoints for {} toolsets: {}",
                    successfulToolSets.size(), String.join(", ", successfulToolSets));
        }
    }

    private void resolveEndpointsIfContainerSource(ToolSet toolSet) {
        if (!(toolSet.getSource() instanceof ToolSetContainerSource)) {
            return;
        }
        endpointResolver.processContainerEndpoints(toolSet);
    }

    private void assertExists(String name) {
        boolean exists = toolSetJpaRepository.existsById(name);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(name));
        }
    }

    private ToolSetEntity toEntity(ToolSet domain, ToolSetEntity entity) {
        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> rolesForLimits = deploymentService.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        List<RoleShareResourceLimit> roleShareResourceLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleShareResourceLimits());
        List<RoleEntity> rolesForResourceShareLimits = deploymentService.findRolesByNames(roleShareResourceLimits.stream().map(RoleShareResourceLimit::getRole).toList());

        ToolSetContainerEntity toolSetContainer = null;

        ToolSetSource source = domain.getSource();
        if (source instanceof ToolSetContainerSource containerSource) {
            toolSetContainer = toolSetContainerEntityMapper.toEntity(containerSource);
        }

        return mapper.toEntity(domain, entity, toolSetContainer, roleLimits, rolesForLimits, roleShareResourceLimits, rolesForResourceShareLimits);
    }

}
