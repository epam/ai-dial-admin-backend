package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.validation.annotation.ApplicationTypeSchema;
import com.epam.aidial.core.config.validation.SchemaConformToMetaSchemaValidator;
import com.epam.aidial.core.config.validation.ValidationErrors;
import com.networknt.schema.Error;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractConformToCoreMetaSchemaValidator<V> implements ConstraintValidator<ApplicationTypeSchema, V> {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    @SneakyThrows
    protected boolean isValid(V dto, String id, ConstraintValidatorContext context) {
        if (!isValidUri(id)) {
            context.disableDefaultConstraintViolation();
            String message = "The ID field contains invalid characters or formatting and does not meet validation criteria. "
                    + "ID must be a valid URI with scheme and host. Please adjust the ID before saving.";
            context.buildConstraintViolationWithTemplate(message)
                    .addBeanNode()
                    .inContainer(Map.class, 1)
                    .inIterable().atKey(id)
                    .addConstraintViolation();
            log.warn("Invalid ID format: {}", id);
            return false;
        }
        ObjectWriter objectWriter = OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
        String dtoJson = objectWriter.writeValueAsString(dto);
        List<Error> validations = SchemaConformToMetaSchemaValidator.getValidationMessages(dtoJson);
        if (!validations.isEmpty()) {
            context.disableDefaultConstraintViolation();
            String validationsMessage = validations.stream()
                    .map(ValidationErrors::format)
                    .collect(Collectors.joining(","));
            String message = "ApplicationTypeSchema validation failed: %s".formatted(validationsMessage);
            context.buildConstraintViolationWithTemplate(message)
                    .addBeanNode()
                    .inContainer(Map.class, 1)
                    .inIterable().atKey(id)
                    .addConstraintViolation();
            log.warn(message);
            return false;
        }
        return true;
    }


    private boolean isValidUri(String uriString) {
        try {
            URI uri = new URI(uriString);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (URISyntaxException e) {
            log.warn("invalid id: {}", uriString);
            return false;
        }
    }
}
