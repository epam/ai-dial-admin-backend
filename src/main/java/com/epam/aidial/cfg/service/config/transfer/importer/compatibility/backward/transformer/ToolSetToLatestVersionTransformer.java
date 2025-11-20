package com.epam.aidial.cfg.service.config.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@LogExecution
public class ToolSetToLatestVersionTransformer {

    public void transform(Map<String, ToolSet> toolSets) {
        MapUtils.emptyIfNull(toolSets).values()
                .forEach(this::transform);
    }

    private void transform(ToolSet toolSet) {
        if (StringUtils.isBlank(toolSet.getDisplayName())) {
            toolSet.setDisplayName(toolSet.getDeployment().getName());
        }
    }
}
