package com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Model;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.isBlank(model.getDisplayName())) {
            model.setDisplayName(model.getDeployment().getName());
        }
    }
}
