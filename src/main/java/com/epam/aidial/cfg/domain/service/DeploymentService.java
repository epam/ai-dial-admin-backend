package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.DeploymentJpaRepository;
import com.epam.aidial.cfg.dao.jpa.RoleJpaRepository;
import com.epam.aidial.cfg.dao.mapper.DeploymentEntityMapper;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class DeploymentService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Deployment with name %s does not exist";

    private final DeploymentJpaRepository deploymentJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final DeploymentEntityMapper mapper;

    @Transactional(readOnly = true)
    public Collection<Deployment> getAll() {
        return StreamSupport.stream(deploymentJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void ensureExists(String deploymentName) {
        if (!deploymentJpaRepository.existsById(deploymentName)) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(deploymentName));
        }
    }

    @Transactional(readOnly = true)
    public void assertDeploymentNotExists(String name) {
        boolean exists = deploymentJpaRepository.existsById(name);
        if (exists) {
            throw new EntityAlreadyExistsException("Deployment with name " + name + " already exists");
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void addDeploymentRoleLimitToRoleIfAbsent(DeploymentEntity deploymentEntity) {
        deploymentEntity.getRoleLimits().forEach(roleLimit -> {
            var roleLimits = roleLimit.getRole().getLimits();
            if (!roleLimits.contains(roleLimit)) {
                roleLimits.add(roleLimit);
            }
        });
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public List<RoleEntity> findRolesByNames(List<String> names) {
        if (names.isEmpty()) {
            return List.of();
        }

        List<RoleEntity> existingRoles = Lists.newArrayList(roleJpaRepository.findAllById(names));
        Set<String> existingRoleNames = existingRoles.stream().map(RoleEntity::getName).collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingRoleNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find roles: " + namesDiff);
        }

        return existingRoles;
    }
}
