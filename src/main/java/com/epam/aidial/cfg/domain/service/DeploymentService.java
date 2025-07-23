package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.DeploymentJpaRepository;
import com.epam.aidial.cfg.dao.mapper.DeploymentEntityMapper;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class DeploymentService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Deployment with name %s does not exist";

    private final DeploymentJpaRepository deploymentJpaRepository;
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
    public Set<String> findAllByNames(Set<String> deploymentNames) {
        return deploymentJpaRepository.findAllByNames(deploymentNames);
    }

    public void assertDeploymentNotExists(String name) {
        boolean exists = deploymentJpaRepository.existsById(name);
        if (exists) {
            throw new EntityAlreadyExistsException("Deployment with name " + name + " already exists");
        }
    }
}
