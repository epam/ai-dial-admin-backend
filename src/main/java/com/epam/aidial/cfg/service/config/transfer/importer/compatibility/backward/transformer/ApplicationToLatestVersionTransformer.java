package com.epam.aidial.cfg.service.config.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Application;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@LogExecution
public class ApplicationToLatestVersionTransformer {

    public void transform(Map<String, Application> applications) {
        MapUtils.emptyIfNull(applications).values()
                .forEach(this::transform);
    }

    private void transform(Application application) {
        if (StringUtils.isBlank(application.getDisplayName())) {
            application.setDisplayName(application.getDeployment().getName());
        }
    }
}
