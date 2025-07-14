package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.ModelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        if (MapUtils.isNotEmpty(coreModels)) {
            Map<String, Adapter> adapterByEndpoint = new HashMap<>();
            for (Adapter adapter : adapterService.getAll()) {
                adapterByEndpoint.put(adapter.getBaseEndpoint(), adapter);
            }
            Set<String> coreEndpoints = coreModels.values()
                    .stream()
                    .filter(coreModel -> coreModel.getEndpoint() != null)
                    .map(this::mapToAdapterBaseEndpoint)
                    .collect(Collectors.toSet());
            boolean createAdapterIfAbsent = importOptions.createAdapterIfAbsent();
            List<ImportComponent<Adapter>> result = new ArrayList<>();
            for (String endpoint : coreEndpoints) {
                if (!adapterByEndpoint.containsKey(endpoint)) {
                    if (createAdapterIfAbsent) {
                        Adapter adapter = new Adapter();
                        adapter.setName(UUID.randomUUID().toString());
                        adapter.setBaseEndpoint(endpoint);
                        if (!isPreview) {
                            adapterService.create(adapter);
                        }
                        result.add(new ImportComponent<>(ImportAction.CREATE, adapter));
                    } else {
                        throw new IllegalArgumentException("Unable to import adapters, adapter with endpoint " + endpoint + " does not exist");
                    }
                }
            }

            return result;
        }
        return Collections.emptyList();
    }

    private String mapToAdapterBaseEndpoint(CoreModel coreModel) {
        String modelEndpoint = coreModel.getEndpoint();
        ModelType type = coreModel.getType();
        return modelEndpointUtils.extractAdapterEndpoint(modelEndpoint, type);
    }

    public List<ImportComponent<Adapter>> importAdminAdapters(Map<String, Adapter> adapters,
                                                              ConfigImportOptions importOptions,
                                                              boolean isPreview) {
        if (MapUtils.isNotEmpty(adapters)) {
            return adapters.entrySet()
                    .stream()
                    .map(e -> getImportComponent(importOptions, e.getKey(), e.getValue(), isPreview))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private ImportComponent<Adapter> getImportComponent(ConfigImportOptions importOptions,
                                                        String name,
                                                        Adapter adapter,
                                                        boolean isPreview) {
        ImportAction importAction = processAdapter(name, adapter, importOptions.conflictResolutionPolicy(), isPreview);
        return new ImportComponent<>(importAction, adapter);
    }

    private ImportAction processAdapter(String adapterName,
                                        Adapter adapter,
                                        ConflictResolutionPolicy conflictResolutionPolicy,
                                        boolean isPreview) {
        if (adapterService.exists(adapterName)) {
            return handleExistingAdapter(adapter, conflictResolutionPolicy, adapterName, isPreview);
        } else {
            return createNewAdapter(adapter, isPreview);
        }
    }

    private ImportAction createNewAdapter(Adapter adapter, boolean isPreview) {
        if (!isPreview) {
            adapterService.create(adapter);
        }
        return ImportAction.CREATE;
    }

    private ImportAction handleExistingAdapter(Adapter adapter,
                                               ConflictResolutionPolicy resolutionPolicy,
                                               String adapterName,
                                               boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing adapter will remain unchanged.
                return ImportAction.SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    adapterService.update(adapterName, adapter);
                }
                return ImportAction.UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }
}
