package com.epam.aidial.cfg.service.config.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@LogExecution
public class AdapterToLatestVersionTransformer {

    public void transform(Map<String, Adapter> adapters) {
        MapUtils.emptyIfNull(adapters).values()
                .forEach(this::transform);
    }

    private void transform(Adapter adapter) {
        if (StringUtils.isBlank(adapter.getDisplayName())) {
            adapter.setDisplayName(adapter.getName());
        }
    }
}
