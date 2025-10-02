package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.AssistantJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AssistantEntityMapper;
import com.epam.aidial.cfg.dao.model.AssistantEntity;
import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.domain.validator.AssistantValidator;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.features.flag.annotation.FeatureFlagGate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("coreAssistantService")
@RequiredArgsConstructor
public class AssistantService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Assistant with name %s does not exist";

    private final AssistantJpaRepository assistantJpaRepository;
    private final AssistantEntityMapper mapper;
    private final DeploymentService deploymentService;
    private final AssistantValidator assistantValidator;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<Assistant> getAllAssistants() {
        return StreamSupport.stream(assistantJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<Assistant> getAllByNames(List<String> names) {
        return StreamSupport.stream(assistantJpaRepository.findAllById(names).spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Assistant getAssistant(String assistantName) {
        return tryGetAssistant(assistantName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(assistantName)));
    }

    @Transactional(readOnly = true)
    public Optional<Assistant> tryGetAssistant(String assistantName) {
        return Optional.ofNullable(assistantName)
                .flatMap(assistantJpaRepository::findById)
                .map(mapper::toDomain);
    }

    @FeatureFlagGate(featureFlag = "assistantsSupported")
    @Transactional
    public void createAssistant(Assistant assistant) {
        assistantValidator.validateAssistantCreation(assistant);
        deploymentService.assertDeploymentNotExists(assistant.getDeployment().getName());
        Optional.of(assistant)
                .map(domainModel -> mapper.toEntity(domainModel, new AssistantEntity()))
                .map(assistantJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("unable to create assistant " + assistant.getDeployment().getName()));
    }

    @FeatureFlagGate(featureFlag = "assistantsSupported")
    @Transactional
    public void updateAssistant(String assistantName, Assistant assistant) {
        assistantValidator.validateUpdate(assistantName, assistant);
        AssistantEntity assistantEntity = assistantJpaRepository.findById(assistantName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(assistantName)));
        Optional.of(assistant)
                .map(domainModel -> mapper.toEntity(domainModel, assistantEntity))
                .map(assistantJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("unable to update assistant " + assistant.getDeployment().getName()));
    }

    @FeatureFlagGate(featureFlag = "assistantsSupported")
    @Transactional
    public void deleteAssistant(String assistantName) {
        assertExists(assistantName);
        assistantJpaRepository.deleteById(assistantName);
    }

    @Transactional(readOnly = true)
    public boolean exists(String assistantName) {
        return assistantJpaRepository.existsById(assistantName);
    }

    @Transactional(readOnly = true)
    public Assistant getSnapshot(String assistantName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, assistantName, AssistantEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<Assistant> getAllAtRevision(Integer revision) {
        return historyService.getEntitiesAtRevision(revision, AssistantEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private void assertExists(String name) {
        boolean exists = assistantJpaRepository.existsById(name);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(name));
        }
    }
}
