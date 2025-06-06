package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.validation.annotation.ApplicationTypeSchema;
import com.epam.aidial.core.metaschemas.MetaSchemaHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ConformToCoreMetaSchemaValidator implements ConstraintValidator<ApplicationTypeSchema, ApplicationTypeSchemaDto> {

    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private static final JsonSchema SCHEMA = SCHEMA_FACTORY.getSchema(MetaSchemaHolder.getCustomApplicationMetaSchema());
    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    @SneakyThrows
    @Override
    public boolean isValid(ApplicationTypeSchemaDto dto, ConstraintValidatorContext context) {
        if (!isValidUri(dto.getId())) {
            return false;
        }
        ObjectWriter objectWriter = OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
        String dtoJson = objectWriter.writeValueAsString(dto);
        Set<ValidationMessage> validations = SCHEMA.validate(dtoJson, InputFormat.JSON);
        if (!validations.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addBeanNode()
                    .inContainer(Map.class, 1)
                    .inIterable().atKey(dto.getId())
                    .addConstraintViolation();
            log.warn("ApplicationTypeSchema validation failed: {}", validations.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining(",")));
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
