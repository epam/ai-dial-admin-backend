package com.epam.aidial.cfg.service.config.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.LocalizedValue;
import com.epam.aidial.cfg.domain.model.source.ApplicationEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSchemaSource;
import org.apache.commons.collections4.MapUtils;
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
        if (application.getDisplayName() == null) {
            application.setDisplayName(LocalizedValue.of(application.getDeployment().getName()));
        }
        if (application.getSource() == null) {
            if (application.getApplicationTypeSchemaId() != null) {
                application.setSource(new ApplicationSchemaSource(application.getApplicationTypeSchemaId()));
                application.setApplicationTypeSchemaId(null);
            } else {
                application.setSource(new ApplicationEndpointsSource());
            }
        }
    }
}
