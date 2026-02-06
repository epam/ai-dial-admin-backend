package com.epam.aidial.cfg.service.config.transfer.importer.util;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.core.config.CoreModel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static com.epam.aidial.cfg.domain.utils.ModelEndpointUtils.ModelEndpointComponents;

@Component
@RequiredArgsConstructor
@LogExecution
public class ModelSourceResolver {

    private final AdapterService adapterService;
    private final ModelEndpointUtils modelEndpointUtils;
    private final ModelSourceRetentionPolicy modelSourceRetentionPolicy;

    public ModelSource resolveSourceForExistingModel(@NotNull CoreModel coreModel, @NotNull Model model) {
        return modelSourceRetentionPolicy.shouldRetainSource(coreModel, model)
                ? model.getSource()
                : resolveSourceForNewModel(coreModel);
    }

    public ModelSource resolveSourceForNewModel(@NotNull CoreModel coreModel) {
        ModelEndpointComponents modelEndpointComponents = getModelEndpointComponents(coreModel);
        if (modelEndpointComponents == null) {
            return new ModelEndpointsSource();
        }

        Adapter adapter = resolveAdapter(modelEndpointComponents);
        return new AdapterSource(adapter.getName(), modelEndpointComponents.completionEndpointPath());
    }

    private ModelEndpointComponents getModelEndpointComponents(CoreModel coreModel) {
        if (coreModel.getEndpoint() == null) {
            return null;
        }
        return modelEndpointUtils.parseModelEndpoint(coreModel.getEndpoint(), coreModel.getType());
    }

    private Adapter resolveAdapter(ModelEndpointComponents modelEndpointComponents) {
        String adapterEndpoint = modelEndpointComponents.adapterEndpoint();
        return adapterService.getByEndpoint(adapterEndpoint);
    }
}
