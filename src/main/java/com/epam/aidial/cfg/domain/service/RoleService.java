package com.epam.aidial.cfg.domain.service;


import com.epam.aidial.cfg.dao.jpa.RoleJpaRepository;
import com.epam.aidial.cfg.dao.mapper.RoleEntityMapper;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.validator.RoleValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("coreRoleService")
@RequiredArgsConstructor
public class RoleService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Role with name %s does not exist";

    private final RoleJpaRepository roleJpaRepository;
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
        return Optional.ofNullable(roleName)
                .flatMap(roleJpaRepository::findById)
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(roleName)));
    }

    @Transactional
    public void createRole(Role role) {
        assertNotExists(role.getName());
        Optional.of(role)
                .map(domainModel -> mapper.toEntity(domainModel, new RoleEntity()))
                .ifPresent(roleJpaRepository::save);
    }

    @Transactional
    public void updateRole(String roleName, Role role) {
        roleValidator.validateRoleUpdate(roleName, role);
        RoleEntity roleEntity = roleJpaRepository.findById(roleName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(roleName)));
        Optional.of(role)
                .map(domainModel -> mapper.toEntity(domainModel, roleEntity))
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

    private void assertNotExists(String name) {
        if (roleJpaRepository.existsById(name)) {
            throw new EntityAlreadyExistsException("Role with name " + name + " already exists");
        }
    }
}
