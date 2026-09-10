package com.epam.aidial.cfg.service.config.transfer.importer;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.source.AdapterContainerSource;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils.ModelEndpointComponents;
import com.epam.aidial.cfg.exception.DeploymentClientNotExistsException;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.importer.util.ModelSourceRetentionPolicy;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.ModelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class AdapterImporter {
    private final AdapterService adapterService;
    private final ModelService modelService;
    private final ModelEndpointUtils modelEndpointUtils;
    private final ModelSourceRetentionPolicy modelSourceRetentionPolicy;
    private final DeploymentManagerService deploymentManagerService;

    public Collection<ImportComponent<Adapter>> importAdapters(Map<String, CoreModel> coreModels,
                                                               ConfigImportOptions importOptions,
                                                               boolean isPreview) {
        if (MapUtils.isEmpty(coreModels)) {
            return Collections.emptyList();
        }

        Set<Pair<String, String>> adapterEndpointsFromCoreModels = getAdapterEndpoints(coreModels);
        if (CollectionUtils.isEmpty(adapterEndpointsFromCoreModels)) {
            return Collections.emptyList();
        }

        Set<Pair<String, String>> existingAdapterEndpoints = adapterService.getAll().stream()
                .map(adapter -> Pair.of(adapter.getBaseEndpoint(), adapter.getResponsesEndpoint()))
                .collect(Collectors.toSet());
        boolean createAdapterIfAbsent = importOptions.createAdapterIfAbsent();

        List<ImportComponent<Adapter>> result = new ArrayList<>();

        int i = 0;
        for (Pair<String, String> endpoints : adapterEndpointsFromCoreModels) {
            if (!existingAdapterEndpoints.contains(endpoints)) {
                String endpoint = endpoints.getLeft();
                String responsesEndpoint = endpoints.getRight();

                if (!createAdapterIfAbsent) {
                    if (endpoint != null && responsesEndpoint != null) {
                        throw new IllegalArgumentException("Unable to import adapters, adapter with "
                                + "endpoint " + endpoint + " and responses endpoint " + responsesEndpoint + " does not exist");
                    } else if (endpoint != null) {
                        throw new IllegalArgumentException("Unable to import adapters, adapter with "
                                + "endpoint " + endpoint + " does not exist");
                    } else if (responsesEndpoint != null) {
                        throw new IllegalArgumentException("Unable to import adapters, adapter with "
                                + "responses endpoint " + responsesEndpoint + " does not exist");
                    }
                }

                Adapter adapter = new Adapter();
                adapter.setBaseEndpoint(endpoint);
                adapter.setResponsesEndpoint(responsesEndpoint);
                if (!isPreview) {
                    adapter.setName(UUID.randomUUID().toString());
                } else {
                    adapter.setName("<will be defined during import " + i + ">");
                    i++;
                }
                adapter.setDisplayName(adapter.getName());
                adapterService.create(adapter);
                result.add(new ImportComponent<>(CREATE, null, adapter));
            }
        }

        return result;
    }

    private Set<Pair<String, String>> getAdapterEndpoints(Map<String, CoreModel> coreModels) {
        return coreModels.entrySet()
                .stream()
                .filter(entry -> shouldSearchForAdapter(entry.getKey(), entry.getValue()))
                .map(Map.Entry::getValue)
                .map(this::mapToAdapterEndpoints)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private boolean shouldSearchForAdapter(String modelName, CoreModel coreModel) {
        return modelService.tryGetModel(modelName)
                .map(model -> modelSourceRetentionPolicy.shouldChangeSource(coreModel, model))
                .orElseGet(() -> coreModel.getEndpoint() != null || coreModel.getResponsesEndpoint() != null);
    }

    private Pair<String, String> mapToAdapterEndpoints(CoreModel coreModel) {
        String adapterBaseEndpoint = mapToAdapterBaseEndpoint(coreModel);
        String adapterResponsesEndpoint = mapToAdapterResponsesEndpoint(coreModel);
        return (adapterBaseEndpoint != null || adapterResponsesEndpoint != null)
                ? Pair.of(adapterBaseEndpoint, adapterResponsesEndpoint)
                : null;
    }

    private String mapToAdapterBaseEndpoint(CoreModel coreModel) {
        String modelEndpoint = coreModel.getEndpoint();
        ModelType type = coreModel.getType();
        ModelEndpointComponents endpointComponents = modelEndpointUtils.parseModelEndpoint(modelEndpoint, type);
        return endpointComponents != null ? endpointComponents.adapterEndpoint() : null;
    }

    private String mapToAdapterResponsesEndpoint(CoreModel coreModel) {
        String responsesEndpoint = coreModel.getResponsesEndpoint();
        return StringUtils.isNotBlank(responsesEndpoint) ? responsesEndpoint : null;
    }

    public List<ImportComponent<Adapter>> importAdminAdapters(Map<String, Adapter> adapters,
                                                              ConfigImportOptions importOptions) {
        if (MapUtils.isEmpty(adapters)) {
            return Collections.emptyList();
        }
        return adapters.entrySet()
                .stream()
                .map(e -> getImportComponent(importOptions, e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private ImportComponent<Adapter> getImportComponent(ConfigImportOptions importOptions,
                                                        String name,
                                                        Adapter adapter) {
        removeDependency(adapter);
        removeContainerSourceDependencyIfContainerIsAbsent(adapter);
        return processAdapter(name, adapter, importOptions.conflictResolutionPolicy());
    }

    private void removeDependency(Adapter adapter) {
        adapter.setModels(null);
    }

    private void removeContainerSourceDependencyIfContainerIsAbsent(Adapter adapter) {
        if (!(adapter.getSource() instanceof AdapterContainerSource containerSource)) {
            return;
        }

        String containerId = containerSource.getContainerId();
        DeploymentInfoDto deploymentInfo = null;

        try {
            deploymentInfo = deploymentManagerService.getById(containerId);
        } catch (DeploymentClientNotExistsException e) {
            log.warn("Failed to get deployment by ID '{}' on Adapter '{}' import",
                    containerId, adapter.getName(), e);
        }

        if (deploymentInfo == null || StringUtils.isBlank(deploymentInfo.getUrl())) {
            log.debug("Container is missing or not deployed. ContainerId: {}, deploymentInfo: {}", containerId, deploymentInfo);
            adapter.setSource(null);
        }
    }

    private ImportComponent<Adapter> processAdapter(String adapterName,
                                                    Adapter adapter,
                                                    ConflictResolutionPolicy conflictResolutionPolicy) {
        Optional<Adapter> existingAdapter = adapterService.tryGet(adapterName);
        if (existingAdapter.isPresent()) {
            ImportAction importAction = handleExistingAdapter(adapter, conflictResolutionPolicy, adapterName);
            return new ImportComponent<>(importAction, existingAdapter.get(), adapter);
        } else {
            adapterService.create(adapter);
            return new ImportComponent<>(CREATE, null, adapter);
        }
    }

    private ImportAction handleExistingAdapter(Adapter adapter,
                                               ConflictResolutionPolicy resolutionPolicy,
                                               String adapterName) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing adapter will remain unchanged.
            case OVERRIDE -> {
                adapterService.update(adapterName, adapter);
                yield UPDATE;
            }
        };
    }

    public List<ImportComponent<Adapter>> getActualImportedAdapters(Collection<ImportComponent<Adapter>> importComponents) {
        List<String> names = importComponents.stream()
                .map(ImportComponent::getNext)
                .map(Adapter::getName)
                .toList();
        Map<String, Adapter> importedAdaptersByNames = adapterService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(Adapter::getName, Function.identity()));

        return importComponents.stream()
                .map(importComponent -> {
                    var next = importedAdaptersByNames.get(importComponent.getNext().getName());
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(Adapter adapter) {
        if (adapter != null) {
            adapter.setCreatedAt(null);
            adapter.setUpdatedAt(null);
        }
    }
}
