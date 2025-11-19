package com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Key;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@LogExecution
public class KeyToLatestVersionTransformer {

    public void transform(Map<String, Key> keys) {
        MapUtils.emptyIfNull(keys).values()
                .forEach(this::transform);
    }

    private void transform(Key key) {
        if (StringUtils.isBlank(key.getDisplayName())) {
            key.setDisplayName(key.getName());
        }
        if (StringUtils.isBlank(key.getProject())) {
            key.setProject("_UNDEFINED_");
        }
    }
}
