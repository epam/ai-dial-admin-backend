package com.epam.aidial.core.config.validation;

import com.epam.aidial.core.config.CoreApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

@Component
public class CustomApplicationConformToTypeSchemaValidator {

    public boolean isValid(CoreApplication application,
                           CustomApplicationConformToTypeSchemaValidationContext validationContext) {
        return CollectionUtils.isEmpty(validate(application, validationContext));
    }

    public static Set<ValidationMessage> validate(CoreApplication application,
                                                  CustomApplicationConformToTypeSchemaValidationContext validationContext) {
        URI schemaId = application.getApplicationTypeSchemaId();
        if (schemaId == null) {
            return Set.of();
        }

        if (application.getApplicationProperties() == null) {
            return Set.of();
        }

        JsonSchemaFactory schemaFactory = validationContext.getSchemaFactory();
        ObjectMapper mapper = validationContext.getMapper();

        JsonNode applicationNode = mapper.valueToTree(application.getApplicationProperties());
        JsonSchema schema = schemaFactory.getSchema(schemaId);

        return schema.validate(applicationNode);
    }
}
