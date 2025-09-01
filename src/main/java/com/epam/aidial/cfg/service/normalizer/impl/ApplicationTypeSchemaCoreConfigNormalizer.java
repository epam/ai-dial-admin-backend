package com.epam.aidial.cfg.service.normalizer.impl;

import com.epam.aidial.cfg.service.normalizer.CoreConfigNormalizer;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplication;
import com.epam.aidial.core.config.validation.SchemaConformToMetaSchemaValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ApplicationTypeSchemaCoreConfigNormalizer implements CoreConfigNormalizer {

    @Override
    public void normalize(Config config) {
        Map<String, String> applicationTypeSchemas = config.getApplicationTypeSchemas();
        if (MapUtils.isEmpty(applicationTypeSchemas)) {
            return;
        }

        Map<String, CoreApplication> applications = config.getApplications();

        for (Map.Entry<String, String> entry : new HashMap<>(applicationTypeSchemas).entrySet()) {
            String schemaId = entry.getKey();
            if (!SchemaConformToMetaSchemaValidator.isValid(entry.getValue())) {
                log.warn("normalizeConfig. remove invalid application type schema which doesn't conform to meta schema: {}", schemaId);
                applicationTypeSchemas.remove(schemaId);

                List<String> applicationsWithInvalidAppTypeSchema = applications.entrySet().stream()
                        .filter(appEntry -> appEntry.getValue().getApplicationTypeSchemaId() != null)
                        .filter(appEntry -> appEntry.getValue().getApplicationTypeSchemaId().toString().equals(schemaId))
                        .map(Map.Entry::getKey)
                        .toList();
                if (CollectionUtils.isNotEmpty(applicationsWithInvalidAppTypeSchema)) {
                    log.warn("normalizeConfig. remove applications: {} with invalid application type schema: {}", applicationsWithInvalidAppTypeSchema, schemaId);
                    applicationsWithInvalidAppTypeSchema.forEach(applications::remove);
                }
            }
        }
    }
}