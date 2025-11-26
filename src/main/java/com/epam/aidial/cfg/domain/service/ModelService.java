package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.AdapterJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ModelContainerEntityMapper;
import com.epam.aidial.cfg.dao.mapper.ModelEntityMapper;
import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.ModelContainerEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.RoleBased;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.domain.normalizer.ModelNormalizer;
import com.epam.aidial.cfg.domain.util.ContainerEndpointResolver;
import com.epam.aidial.cfg.domain.validator.ModelValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import com.google.api.client.util.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@Slf4j
@LogExecution
@Service
@RequiredArgsConstructor
public class ModelService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Model with name %s does not exist";

    private final ModelJpaRepository modelJpaRepository;
    private final InterceptorJpaRepository interceptorJpaRepository;
    private final AdapterJpaRepository adapterJpaRepository;
    private final ModelEntityMapper mapper;
    private final ModelContainerEntityMapper modelContainerEntityMapper;
    private final DeploymentService deploymentService;
    private final ModelNormalizer modelNormalizer;
    private final ModelValidator modelValidator;
    private final HistoryService historyService;
    private final ModelRefreshService refreshService;
    private final ContainerEndpointResolver endpointResolver;
    private final HashCalculator calculator;

    @Transactional(readOnly = true)
    public Collection<Model> getAll() {
        return modelJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<Model> getAllByNames(List<String> names) {
        return modelJpaRepository.findAllById(names).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Model getModel(String modelName) {
        return getModelOrThrow(modelName);
    }

    @Transactional(readOnly = true)
    public DomainObjectWithHash<Model> getModelWithHash(String modelName) {
        var model = getModelOrThrow(modelName);
        return new DomainObjectWithHash<>(model, calculator.calculateHash(model));
    }

    private Model getModelOrThrow(String modelName) {
        return tryGetModel(modelName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(modelName)));
    }

    @Transactional(readOnly = true)
    public Optional<Model> tryGetModel(String modelName) {
        return Optional.ofNullable(modelName)
                .flatMap(modelJpaRepository::findById)
                .map(mapper::toDomain);
    }

    @Transactional
    public void createModel(Model model) {
        modelNormalizer.normalize(model);
        modelValidator.validateCreation(model);
        deploymentService.assertDeploymentNotExists(model.getDeployment().getName());
        assertNotExists(model.getDisplayName(), model.getDisplayVersion());
        resolveEndpointsIfContainerSource(model);
        Optional.of(model)
                .map(domainModel -> toEntity(domainModel, new ModelEntity()))
                .map(this::save)
                .orElseThrow(() -> new RuntimeException("Unable to create model " + model.getDeployment().getName()));
    }

    @Transactional
    public void updateModel(String modelName, Model model) {
        performUpdate(modelName, model, ANY_HASH);
    }

    @Transactional
    public String updateModel(String modelName, Model model, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    "Hash must not be null. Use \"*\" to skip optimistic check.");
        }
        var savedModel = performUpdate(modelName, model, hash);
        return calculator.calculateHash(mapper.toDomain(savedModel));
    }

    private ModelEntity performUpdate(String modelName, Model model, String hash) {
        modelNormalizer.normalize(model);
        modelValidator.validateUpdate(modelName, model);
        ModelEntity modelEntity = modelJpaRepository.findById(modelName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(modelName)));

        assertNewModelDisplayNameAndDisplayVersion(modelEntity, model);
        assertNotConcurrencyOverwrite(modelEntity, hash);
        resolveEndpointsIfContainerSource(model);
        return save(toEntity(model, modelEntity));
    }

    private ModelEntity save(ModelEntity modelEntity) {
        ModelEntity savedModelEntity = modelJpaRepository.save(modelEntity);
        deploymentService.addDeploymentRoleLimitToRoleIfAbsent(savedModelEntity.getDeployment());
        return savedModelEntity;
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

    @Transactional(readOnly = true)
    public Collection<Model> getAllAtRevision(Number revision) {
        return historyService.getEntitiesAtRevision(revision, ModelEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rollbackModels(Number revision) {
        Collection<Model> models = getAllAtRevision(revision);
        List<String> ids = models.stream().map(RoleBased::getDeployment).map(Deployment::getName).toList();
        modelJpaRepository.deleteAllExcept(ids);

        for (Model model : models) {
            model.setInterceptors(List.of());
            ModelEntity entity = modelJpaRepository.findById(model.getDeployment().getName()).orElseGet(ModelEntity::new);
            ModelEntity modelEntity = toEntity(model, entity);
            modelJpaRepository.save(modelEntity);
        }
    }

    @Transactional(readOnly = true)
    public void refreshEndpoints() {
        var modelEntities = modelJpaRepository.findByContainerIdIsNotNull();
        List<String> successfulModels = new ArrayList<>();
        List<String> failedModels = new ArrayList<>();

        for (var entity : modelEntities) {
            String modelName = entity.getDeploymentName();
            log.debug("Refreshing endpoints for model '%s'".formatted(modelName));
            try {
                refreshService.refreshEndpoints(entity);
                successfulModels.add(modelName);
            } catch (Exception e) {
                log.error("Failed to refresh endpoints for model '{}'", modelName, e);
                failedModels.add(modelName);
            }
        }

        if (!failedModels.isEmpty()) {
            log.warn("Failed to refresh endpoints for {} models: {}",
                    failedModels.size(), String.join(", ", failedModels));
        }

        if (!successfulModels.isEmpty()) {
            log.debug("Successfully refreshed endpoints for {} models: {}",
                    successfulModels.size(), String.join(", ", successfulModels));
        }
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

    private void assertNotConcurrencyOverwrite(ModelEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }
        var currentHash = calculator.calculateHash(mapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: modelName={}, expectedHash={}, currentHash={}",
                    entity.getDeployment().getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException("Optimistic lock conflict on update: modelName:'"
                    + entity.getDeployment().getName() + "'. Reload the data.");

        }
    }

    private void resolveEndpointsIfContainerSource(Model model) {
        if (!(model.getSource() instanceof ModelContainerSource)) {
            return;
        }
        endpointResolver.processContainerEndpoints(model);
    }

    private ModelEntity toEntity(Model domain, ModelEntity entity) {
        List<InterceptorEntity> interceptors = findInterceptorsByNames(domain.getInterceptors());

        AdapterEntity adapterEntity = null;
        String completionEndpointPath = null;
        ModelContainerEntity modelContainer = null;

        ModelSource source = domain.getSource();
        if (source != null) {
            if (source instanceof AdapterSource adapterSource) {
                adapterEntity = findAdapter(adapterSource.getAdapterName());
                completionEndpointPath = adapterSource.getCompletionEndpointPath();
            } else if (source instanceof ModelContainerSource containerSource) {
                modelContainer = modelContainerEntityMapper.toEntity(containerSource);
            }
        }

        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> rolesForLimits = deploymentService.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        return mapper.toEntity(domain, entity, interceptors, adapterEntity, completionEndpointPath, modelContainer,
                roleLimits, rolesForLimits);
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

    private AdapterEntity findAdapter(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return adapterJpaRepository.findById(name)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find Adapter with name: '%s'".formatted(name)));
    }
}
