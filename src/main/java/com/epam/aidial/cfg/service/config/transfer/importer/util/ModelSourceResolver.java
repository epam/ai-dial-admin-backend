package com.epam.aidial.cfg.service.config.transfer.importer.util;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.source.ModelAdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.core.config.CoreModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        String endpoint = coreModel.getEndpoint();
        String responsesEndpoint = coreModel.getResponsesEndpoint();

        boolean hasEndpoint = StringUtils.isNotBlank(endpoint);
        boolean hasResponsesEndpoint = StringUtils.isNotBlank(responsesEndpoint);

        if (!hasEndpoint && hasResponsesEndpoint) {
            Adapter adapter = adapterService.getByResponsesEndpointAndNullEndpoint(responsesEndpoint);
            return new ModelAdapterSource(adapter.getName(), null);
        }

        if (hasEndpoint) {
            ModelEndpointComponents components = getModelEndpointComponents(coreModel);
            if (components != null) {
                Adapter adapter = hasResponsesEndpoint
                        ? adapterService.getByEndpointAndResponsesEndpoint(components.adapterEndpoint(), responsesEndpoint)
                        : adapterService.getByEndpointAndNullResponsesEndpoint(components.adapterEndpoint());
                return new ModelAdapterSource(adapter.getName(), components.completionEndpointPath());
            }
        }

        return new ModelEndpointsSource();
    }

    private ModelEndpointComponents getModelEndpointComponents(CoreModel coreModel) {
        return modelEndpointUtils.parseModelEndpoint(coreModel.getEndpoint(), coreModel.getType());
    }
}
