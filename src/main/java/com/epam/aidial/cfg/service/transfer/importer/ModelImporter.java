package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ModelCoreMapper;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
public class ModelImporter extends RoleBasedImporter {

    private final ModelService modelService;
    private final AdapterService adapterService;
    private final ModelCoreMapper modelMapper;
    private final ModelEndpointUtils modelEndpointUtils;

    public ModelImporter(RoleService roleService, ModelService modelService, AdapterService adapterService, ModelCoreMapper modelMapper, ModelEndpointUtils modelEndpointUtils) {
        super(roleService);
        this.modelService = modelService;
        this.adapterService = adapterService;
        this.modelMapper = modelMapper;
        this.modelEndpointUtils = modelEndpointUtils;
    }

    public Collection<ImportComponent<Model>> importModels(Map<String, CoreModel> coreModels,
                                                           Map<String, CoreRole> roles,
                                                           ConfigImportOptions importOptions,
                                                           boolean isPreview) {
        if (MapUtils.isNotEmpty(coreModels)) {
            Map<String, Model> models = coreModels.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue(), roles)));
            return importAdminModels(models, importOptions, isPreview);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Model>> importAdminModels(Map<String, Model> models,
                                                                ConfigImportOptions importOptions,
                                                                boolean isPreview) {
        if (MapUtils.isNotEmpty(models)) {
            return models.entrySet().stream()
                    .map(modelEntry -> {
                                var model = modelEntry.getValue();
                                createRoleIfAbsent(importOptions, model.getDeployment().getRoleLimits());
                                var importAction = processModel(modelEntry.getKey(), model, importOptions.conflictResolutionPolicy(), isPreview);
                                return new ImportComponent<>(importAction, model);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processModel(String modelName, Model newModel,
                                      ConflictResolutionPolicy resolutionPolicy,
                                      boolean isPreview) {
        if (modelService.exists(modelName)) {
            return handleExistingModel(newModel, resolutionPolicy, modelName, isPreview);
        } else {
            return createNewModel(newModel, isPreview);
        }
    }

    private ImportAction handleExistingModel(
            Model newModel,
            ConflictResolutionPolicy resolutionPolicy,
            String modelName,
            boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing model will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    modelService.updateModel(modelName, newModel);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }

    private ImportAction createNewModel(Model model, boolean isPreview) {
        if (!isPreview) {
            modelService.createModel(model);
        }
        return CREATE;
    }

    private Model map(String modelName, CoreModel model, Map<String, CoreRole> roles) {
        model.setName(modelName);
        Adapter adapter = getAdapterByEndpoint(modelName, model);
        return modelMapper.mapModel(model, roles, adapter);
    }

    private Adapter getAdapterByEndpoint(String name, CoreModel coreModel) {
        if (coreModel == null || coreModel.getEndpoint() == null) {
            return null;
        }
        String adapterEndpoint = modelEndpointUtils.extractAdapterEndpoint(coreModel.getEndpoint(), name, coreModel.getType());
        return adapterService.getByEndpoint(adapterEndpoint);
    }

}
