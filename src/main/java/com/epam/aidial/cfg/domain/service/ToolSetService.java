package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.ToolSetJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ToolSetEntityMapper;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.normalizer.ToolSetNormalizer;
import com.epam.aidial.cfg.domain.validator.ToolSetValidator;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ToolSetService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "ToolSet with name %s does not exist";

    private final ToolSetJpaRepository toolSetJpaRepository;
    private final ToolSetEntityMapper mapper;
    private final DeploymentService deploymentService;
    private final ToolSetValidator toolSetValidator;
    private final ToolSetNormalizer toolSetNormalizer;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<ToolSet> getAll() {
        return StreamSupport.stream(toolSetJpaRepository.findAll().spliterator(), false)
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
        Optional.of(toolSet)
                    .map(domainModel -> mapper.toEntity(domainModel, new ToolSetEntity()))
                    .map(toolSetJpaRepository::save)
                    .orElseThrow(() -> new RuntimeException("Unable to create ToolSet " + toolSet.getDeployment().getName()));
    }

    @Transactional
    public void update(String toolSetName, ToolSet value) {
        toolSetNormalizer.normalize(value);
        toolSetValidator.validateUpdate(toolSetName, value);
        ToolSetEntity toolSetEntity = toolSetJpaRepository.findById(toolSetName)
                    .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(toolSetName)));
        Optional.of(value)
                    .map(domainModel -> mapper.toEntity(domainModel, toolSetEntity))
                    .map(toolSetJpaRepository::save)
                    .orElseThrow(() -> new RuntimeException("Unable to update ToolSet " + value.getDeployment().getName()));
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
    public Collection<ToolSet> getAllAtRevision(Integer revision) {
        return historyService.getEntitiesAtRevision(revision, ToolSetEntity.class)
                    .stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
    }

    private void assertExists(String name) {
        boolean exists = toolSetJpaRepository.existsById(name);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(name));
        }
    }

}
