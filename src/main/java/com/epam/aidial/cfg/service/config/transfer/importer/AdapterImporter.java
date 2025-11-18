package com.epam.aidial.cfg.service.config.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils.ModelEndpointComponents;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.ModelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
    private final ModelEndpointUtils modelEndpointUtils;

    public Collection<ImportComponent<Adapter>> importAdapters(Map<String, CoreModel> coreModels,
                                                               ConfigImportOptions importOptions,
                                                               boolean isPreview) {
        if (MapUtils.isEmpty(coreModels)) {
            return Collections.emptyList();
        }

        Set<String> adapterBaseEndpointsFromCoreModels = getAdapterBaseEndpoints(coreModels);
        if (CollectionUtils.isEmpty(adapterBaseEndpointsFromCoreModels)) {
            return Collections.emptyList();
        }

        Set<String> existingAdapterBaseEndpoints = adapterService.getAll().stream()
                .map(Adapter::getBaseEndpoint)
                .collect(Collectors.toSet());
        boolean createAdapterIfAbsent = importOptions.createAdapterIfAbsent();

        List<ImportComponent<Adapter>> result = new ArrayList<>();

        int i = 0;
        for (String endpoint : adapterBaseEndpointsFromCoreModels) {
            if (!existingAdapterBaseEndpoints.contains(endpoint)) {
                if (!createAdapterIfAbsent) {
                    throw new IllegalArgumentException("Unable to import adapters, adapter with endpoint " + endpoint + " does not exist");
                }

                Adapter adapter = new Adapter();
                adapter.setBaseEndpoint(endpoint);
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

    private Set<String> getAdapterBaseEndpoints(Map<String, CoreModel> coreModels) {
        return coreModels.values()
                .stream()
                .filter(coreModel -> coreModel.getEndpoint() != null)
                .map(this::mapToAdapterBaseEndpoint)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private String mapToAdapterBaseEndpoint(CoreModel coreModel) {
        String modelEndpoint = coreModel.getEndpoint();
        ModelType type = coreModel.getType();
        ModelEndpointComponents endpointComponents = modelEndpointUtils.parseModelEndpoint(modelEndpoint, type);
        return endpointComponents != null ? endpointComponents.adapterEndpoint() : null;
    }

    public List<ImportComponent<Adapter>> importAdminAdapters(Map<String, Adapter> adapters,
                                                              ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(adapters)) {
            return adapters.entrySet()
                    .stream()
                    .map(e -> getImportComponent(importOptions, e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private ImportComponent<Adapter> getImportComponent(ConfigImportOptions importOptions,
                                                        String name,
                                                        Adapter adapter) {
        removeDependency(adapter);
        return processAdapter(name, adapter, importOptions.conflictResolutionPolicy());
    }

    private void removeDependency(Adapter adapter) {
        adapter.setModels(null);
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
