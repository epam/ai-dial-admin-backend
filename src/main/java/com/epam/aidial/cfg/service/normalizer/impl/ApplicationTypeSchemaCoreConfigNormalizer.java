package com.epam.aidial.cfg.service.normalizer.impl;

import com.epam.aidial.cfg.service.normalizer.CoreConfigNormalizer;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.validation.SchemaConformToMetaSchemaValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

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

        for (Map.Entry<String, String> entry : applicationTypeSchemas.entrySet()) {
            String schemaId = entry.getKey();
            if (!SchemaConformToMetaSchemaValidator.isValid(entry.getValue())) {
                log.debug("normalizeConfig. remove invalid application type schema which doesn't conform to meta schema: {}", schemaId);
                applicationTypeSchemas.remove(schemaId);
            }
        }
    }
}