package com.epam.aidial.cfg.service.config.transfer.importer.util;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
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

        return endpoint == null
                || endpoint.equals(model.getEndpoint())
                || endpointMatchesAdapterSource(endpoint, model);
    }

    private boolean endpointMatchesAdapterSource(String endpoint, Model model) {
        if (model.getSource() instanceof AdapterSource adapterSource) {
            Adapter adapter = adapterService.get(adapterSource.getAdapterName());
            return endpoint.equals(ModelEndpointUtils.concatEndpointAndPath(adapter.getBaseEndpoint(), adapterSource.getCompletionEndpointPath()));
        }
        return false;
    }
}
