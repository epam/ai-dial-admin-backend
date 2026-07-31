package com.epam.aidial.cfg.service.config.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.value.LocalizedValue;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@LogExecution
public class ModelToLatestVersionTransformer {

    public void transform(Map<String, Model> models) {
        MapUtils.emptyIfNull(models).values()
                .forEach(this::transform);
    }

    private void transform(Model model) {
        if (model.getDisplayName() == null) {
            model.setDisplayName(LocalizedValue.of(model.getDeployment().getName()));
        }
    }
}
