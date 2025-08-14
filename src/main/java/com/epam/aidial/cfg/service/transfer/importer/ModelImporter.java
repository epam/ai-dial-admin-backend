package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ModelCoreMapper;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils.ModelEndpointComponents;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class ModelImporter extends RoleBasedImporter {

    private final ModelService modelService;
    private final AdapterService adapterService;
    private final ModelCoreMapper modelMapper;
    private final ModelEndpointUtils modelEndpointUtils;

    public Collection<ImportComponent<Model>> importModels(Map<String, CoreModel> coreModels,
                                                           Map<String, Role> roles,
                                                           ConfigImportOptions importOptions,
                                                           Collection<ImportComponent<Adapter>> adaptersForPreview,
                                                           boolean isPreview) {
        if (MapUtils.isNotEmpty(coreModels)) {
            Map<String, Model> models = coreModels.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> map(entry.getKey(), entry.getValue(), adaptersForPreview, isPreview)
                            )
                    );
            return importAdminModels(models, roles, importOptions, isPreview);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Model>> importAdminModels(Map<String, Model> models,
                                                                Map<String, Role> roles,
                                                                ConfigImportOptions importOptions,
                                                                boolean isPreview) {
        if (MapUtils.isNotEmpty(models)) {
            return models.entrySet().stream()
                    .map(modelEntry -> {
                                var model = modelEntry.getValue();
                                var importAction = processModel(modelEntry.getKey(), model, roles, importOptions.conflictResolutionPolicy(), isPreview);
                                return new ImportComponent<>(importAction, model);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processModel(String modelName,
                                      Model newModel,
                                      Map<String, Role> roles,
                                      ConflictResolutionPolicy resolutionPolicy,
                                      boolean isPreview) {
        Optional<Model> model = modelService.tryGetModel(modelName);
        if (model.isPresent()) {
            Model existingModel = model.get();
            setRoleLimits(modelName, existingModel.getDeployment().getRoleLimits(), roles, newModel.getDeployment(), isPreview);
            return handleExistingModel(newModel, resolutionPolicy, modelName, isPreview);
        } else {
            setRoleLimits(modelName, List.of(), roles, newModel.getDeployment(), isPreview);
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

    private Model map(String modelName,
                      CoreModel model,
                      Collection<ImportComponent<Adapter>> adaptersForPreview,
                      boolean isPreview) {
        model.setName(modelName);
        ModelEndpointComponents modelEndpointComponents = getModelEndpointComponents(model);
        Adapter adapter = resolveAdapter(adaptersForPreview, isPreview, modelEndpointComponents);
        String endpointDeploymentName = resolveEndpointDeploymentName(modelEndpointComponents);
        return modelMapper.mapModel(model, adapter, endpointDeploymentName);
    }

    private ModelEndpointComponents getModelEndpointComponents(CoreModel coreModel) {
        if (coreModel == null || coreModel.getEndpoint() == null) {
            return null;
        }
        return modelEndpointUtils.parseModelEndpoint(coreModel.getEndpoint(), coreModel.getType());
    }

    private Adapter resolveAdapter(Collection<ImportComponent<Adapter>> adaptersForPreview,
                                   boolean isPreview,
                                   ModelEndpointComponents modelEndpointComponents) {
        if (modelEndpointComponents == null) {
            return null;
        }

        String adapterEndpoint = modelEndpointComponents.adapterEndpoint();

        if (!isPreview) {
            return adapterService.getByEndpoint(adapterEndpoint);
        }

        return adaptersForPreview.stream()
                .filter(importComponent -> importComponent.getImportAction() == CREATE)
                .map(ImportComponent::getValue)
                .filter(adapter -> Strings.CS.equals(adapter.getBaseEndpoint(), adapterEndpoint))
                .findFirst()
                .orElseGet(() -> adapterService.getByEndpoint(adapterEndpoint));
    }

    private String resolveEndpointDeploymentName(ModelEndpointComponents modelEndpointComponents) {
        return modelEndpointComponents != null ? modelEndpointComponents.endpointDeploymentName() : null;
    }

}
