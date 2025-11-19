package com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@LogExecution
public class ApplicationTypeSchemaToLatestVersionTransformer {

    public void transform(Map<String, ApplicationTypeSchema> applicationTypeSchemas) {
        MapUtils.emptyIfNull(applicationTypeSchemas).values()
                .forEach(this::transform);
    }

    private void transform(ApplicationTypeSchema applicationTypeSchema) {
        if (StringUtils.isBlank(applicationTypeSchema.getApplicationTypeDisplayName())) {
            applicationTypeSchema.setApplicationTypeDisplayName(applicationTypeSchema.getSchemaId());
        }
    }
}
