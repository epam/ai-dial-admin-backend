package com.epam.aidial.cfg.domain.service;


import com.epam.aidial.cfg.dao.jpa.DeploymentJpaRepository;
import com.epam.aidial.cfg.dao.jpa.KeyJpaRepository;
import com.epam.aidial.cfg.dao.jpa.RoleJpaRepository;
import com.epam.aidial.cfg.dao.mapper.RoleEntityMapper;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.validator.RoleValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
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

@Service("coreRoleService")
@RequiredArgsConstructor
public class RoleService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Role with name %s does not exist";

    private final RoleJpaRepository roleJpaRepository;
    private final DeploymentJpaRepository deploymentJpaRepository;
    private final KeyJpaRepository keyJpaRepository;
    private final RoleEntityMapper mapper;
    private final RoleValidator roleValidator;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<Role> getAllRoles() {
        return StreamSupport.stream(roleJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Role getRole(String roleName) {
        return tryGetRole(roleName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(roleName)));
    }

    @Transactional(readOnly = true)
    public Optional<Role> tryGetRole(String roleName) {
        return Optional.ofNullable(roleName)
                .flatMap(roleJpaRepository::findById)
                .map(mapper::toDomain);
    }

    @Transactional
    public void createRole(Role role) {
        roleValidator.validateRoleCreation(role);
        assertNotExists(role.getName());
        Optional.of(role)
                .map(domainModel -> toEntity(domainModel, new RoleEntity()))
                .ifPresent(roleJpaRepository::save);
    }

    @Transactional
    public void updateRole(String roleName, Role role) {
        roleValidator.validateRoleUpdate(roleName, role);
        RoleEntity roleEntity = roleJpaRepository.findById(roleName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(roleName)));
        Optional.of(role)
                .map(domainModel -> toEntity(domainModel, roleEntity))
                .ifPresent(roleJpaRepository::save);
    }

    @Transactional
    public void deleteRole(String roleName) {
        roleValidator.validateRoleDeletion(roleName);
        assertExists(roleName);
        roleJpaRepository.deleteById(roleName);
    }

    @Transactional(readOnly = true)
    public boolean exists(String roleName) {
        return roleJpaRepository.existsById(roleName);
    }

    private void assertExists(String name) {
        boolean exists = roleJpaRepository.existsById(name);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(name));
        }
    }

    @Transactional(readOnly = true)
    public Role getSnapshot(String roleName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, roleName, RoleEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<Role> getAllAtRevision(Number revision) {
        return historyService.getEntitiesAtRevision(revision, RoleEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rollbackRoles(Number revision) {
        Collection<Role> roles = getAllAtRevision(revision);
        List<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toList());
        roleJpaRepository.deleteAllExcept(roleNames);
        Set<String> allDeploymentNames = deploymentJpaRepository.findAllNames();
        Set<String> allKeys = keyJpaRepository.findAllKeys();
        for (Role role : roles) {
            RoleEntity entity = roleJpaRepository.findById(role.getName()).orElseGet(RoleEntity::new);
            role.getLimits().removeIf(roleLimit -> !allDeploymentNames.contains(roleLimit.getDeploymentName()));
            role.getShare().removeIf(roleShare -> !allDeploymentNames.contains(roleShare.getDeploymentName()));
            role.getKeys().removeIf(key -> !allKeys.contains(key));
            RoleEntity roleEntity = toEntity(role, entity);
            roleJpaRepository.save(roleEntity);
        }
    }

    private void assertNotExists(String name) {
        if (roleJpaRepository.existsById(name)) {
            throw new EntityAlreadyExistsException("Role with name " + name + " already exists");
        }
    }

    private RoleEntity toEntity(Role domain, RoleEntity entity) {
        List<KeyEntity> keyEntities = findKeyEntitiesByKeys(domain.getKeys());

        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getLimits());
        List<DeploymentEntity> limitDeployments = findDeploymentsByNames(roleLimits.stream().map(RoleLimit::getDeploymentName).toList());

        List<RoleShareResourceLimit> roleShareResourceLimits = ListUtils.emptyIfNull(domain.getShare());
        List<DeploymentEntity> roleShareDeployments = findDeploymentsByNames(roleShareResourceLimits.stream().map(RoleShareResourceLimit::getDeploymentName).toList());

        return mapper.toEntity(domain, entity, keyEntities, roleLimits, limitDeployments, roleShareResourceLimits, roleShareDeployments);
    }

    private List<KeyEntity> findKeyEntitiesByKeys(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return List.of();
        }

        List<KeyEntity> existingKeysEntities = Lists.newArrayList(keyJpaRepository.findAllById(keys));
        Set<String> existingKeys = existingKeysEntities.stream().map(KeyEntity::getName).collect(Collectors.toSet());

        Set<String> keysDiff = SetUtils.difference(new HashSet<>(keys), existingKeys);
        if (!keysDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find keys: " + keysDiff);
        }

        return existingKeysEntities;
    }

    private List<DeploymentEntity> findDeploymentsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<DeploymentEntity> existingDeployments = Lists.newArrayList(deploymentJpaRepository.findAllById(names));
        Set<String> existingDeploymentNames = existingDeployments.stream()
                .map(DeploymentEntity::getName)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingDeploymentNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find deployments: " + namesDiff);
        }

        return existingDeployments;
    }
}
