package com.epam.aidial.cfg.domain.service;


import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ModelEntityMapper;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.normalizer.ModelNormalizer;
import com.epam.aidial.cfg.domain.validator.ModelValidator;
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

@Service("coreModelService")
@RequiredArgsConstructor
public class ModelService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Model with name %s does not exist";

    private final ModelJpaRepository modelJpaRepository;
    private final ModelEntityMapper mapper;
    private final DeploymentService deploymentService;
    private final ModelNormalizer modelNormalizer;
    private final ModelValidator modelValidator;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<Model> getAllModels() {
        return StreamSupport.stream(modelJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Model getModel(String modelName) {
        return Optional.ofNullable(modelName)
                .flatMap(modelJpaRepository::findById)
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(modelName)));
    }

    @Transactional
    public void createModel(Model model) {
        modelNormalizer.normalize(model);
        modelValidator.validateCreation(model);
        deploymentService.assertDeploymentNotExists(model.getDeployment().getName());
        assertNotExists(model.getDisplayName(), model.getDisplayVersion());
        Optional.of(model)
                .map(domainModel -> mapper.toEntity(domainModel, new ModelEntity()))
                .map(modelJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to create model " + model.getDeployment().getName()));
    }

    @Transactional
    public void updateModel(String modelName, Model model) {
        modelNormalizer.normalize(model);
        modelValidator.validateUpdate(modelName, model);
        ModelEntity modelEntity = modelJpaRepository.findById(modelName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(modelName)));
        assertNewModelDisplayNameAndDisplayVersion(modelEntity, model);
        Optional.of(model)
                .map(domainModel -> mapper.toEntity(domainModel, modelEntity))
                .map(modelJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to update model " + model.getDeployment().getName()));
    }

    @Transactional
    public void deleteModel(String modelName) {
        assertExists(modelName);
        modelJpaRepository.deleteById(modelName);
    }

    @Transactional(readOnly = true)
    public boolean exists(String modelName) {
        return modelJpaRepository.existsById(modelName);
    }

    @Transactional(readOnly = true)
    public Model getSnapshot(String modelName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, modelName, ModelEntity.class);
        return mapper.toDomain(entity);
    }

    private void assertExists(String name) {
        boolean exists = modelJpaRepository.existsById(name);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(name));
        }
    }

    private void assertNotExists(String displayName, String displayVersion) {
        if ((displayName != null || displayVersion != null) && modelJpaRepository.existsByDisplayNameAndDisplayVersion(displayName, displayVersion)) {
            throw new EntityAlreadyExistsException("Model with display name: '" + displayName + "' and display version: '" + displayVersion + "' already exists");
        }
    }

    private void assertNewModelDisplayNameAndDisplayVersion(ModelEntity entity, Model domain) {
        String displayName = entity.getDisplayName();
        String displayVersion = entity.getDisplayVersion();
        String newDisplayName = domain.getDisplayName();
        String newDisplayVersion = domain.getDisplayVersion();

        if (!Objects.equals(displayName, newDisplayName) || !Objects.equals(displayVersion, newDisplayVersion)) {
            assertNotExists(newDisplayName, newDisplayVersion);
        }
    }
}
