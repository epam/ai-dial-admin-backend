package com.epam.aidial.cfg.domain.service;


import com.epam.aidial.cfg.dao.jpa.RoleJpaRepository;
import com.epam.aidial.cfg.dao.mapper.RoleEntityMapper;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.validator.RoleValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@Service("coreRoleService")
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Role with name %s does not exist";

    private final RoleJpaRepository roleJpaRepository;
    private final RoleEntityMapper mapper;
    private final RoleValidator roleValidator;
    private final HistoryService historyService;
    private final HashCalculator calculator;

    @Transactional(readOnly = true)
    public Collection<Role> getAllRoles() {
        return StreamSupport.stream(roleJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<Role> getAllByNames(List<String> names) {
        return StreamSupport.stream(roleJpaRepository.findAllById(names).spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Role getRole(String roleName) {
        return tryGetRole(roleName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(roleName)));
    }

    @Transactional(readOnly = true)
    public DomainObjectWithHash<Role> getRoleWithHash(String roleName) {
        var role = getRole(roleName);
        return new DomainObjectWithHash<>(role, calculator.calculateHash(role));
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
                .map(domainModel -> mapper.toEntity(domainModel, new RoleEntity()))
                .ifPresent(roleJpaRepository::save);
    }

    @Transactional
    public void updateRole(String roleName, Role role) {
        performUpdate(roleName, role, ANY_HASH);
    }

    @Transactional
    public String updateRole(String roleName, Role role, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. Role:%s.", roleName));
        }
        var savedRole = performUpdate(roleName, role, hash);
        return calculator.calculateHash(mapper.toDomain(savedRole));
    }

    private RoleEntity performUpdate(String roleName, Role role, String hash) {
        roleValidator.validateRoleUpdate(roleName, role);
        RoleEntity roleEntity = roleJpaRepository.findById(roleName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(roleName)));
        assertNotConcurrencyOverwrite(roleEntity, hash);
        return roleJpaRepository.save(mapper.toEntity(role, roleEntity));
    }

    private void assertNotConcurrencyOverwrite(RoleEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }
        var currentHash = calculator.calculateHash(mapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: roleName={}, expectedHash={}, currentHash={}",
                    entity.getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Optimistic lock conflict on update: roleName:'"
                    + "%s'. Reload the data.", entity.getName()));
        }
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

    @Transactional(readOnly = true)
    public void assertExists(String name) {
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
    public Collection<Role> getAllAtRevision(Integer revision) {
        return historyService.getEntitiesAtRevision(revision, RoleEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private void assertNotExists(String name) {
        if (roleJpaRepository.existsById(name)) {
            throw new EntityAlreadyExistsException("Role with name " + name + " already exists");
        }
    }
}
