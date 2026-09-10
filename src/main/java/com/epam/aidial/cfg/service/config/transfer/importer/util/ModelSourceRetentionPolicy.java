package com.epam.aidial.cfg.service.config.transfer.importer.util;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.source.ModelAdapterSource;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.core.config.CoreModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@LogExecution
public class ModelSourceRetentionPolicy {

    private final AdapterService adapterService;

    public boolean shouldChangeSource(CoreModel coreModel, Model model) {
        return !shouldRetainSource(coreModel, model);
    }

    public boolean shouldRetainSource(CoreModel coreModel, Model model) {
        String endpoint = coreModel.getEndpoint();
        String responsesEndpoint = coreModel.getResponsesEndpoint();

        return (endpoint == null || endpoint.equals(getCurrentEndpoint(model)))
                && (responsesEndpoint == null || responsesEndpoint.equals(getCurrentResponsesEndpoint(model)));
    }

    private String getCurrentEndpoint(Model model) {
        if (model.getSource() instanceof ModelAdapterSource adapterSource) {
            Adapter adapter = adapterService.get(adapterSource.getAdapterName());
            return ModelEndpointUtils.concatEndpointAndPath(adapter.getBaseEndpoint(), adapterSource.getCompletionEndpointPath());
        }
        return model.getEndpoint();
    }

    private String getCurrentResponsesEndpoint(Model model) {
        if (model.getSource() instanceof ModelAdapterSource adapterSource) {
            Adapter adapter = adapterService.get(adapterSource.getAdapterName());
            return adapter.getResponsesEndpoint();
        }
        return model.getResponsesEndpoint();
    }
}
