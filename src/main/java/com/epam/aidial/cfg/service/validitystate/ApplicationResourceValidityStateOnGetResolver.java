package com.epam.aidial.cfg.service.validitystate;

import com.epam.aidial.cfg.client.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapper;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.model.ValidityStateResource;
import com.epam.aidial.core.config.validation.CustomApplicationConformToTypeSchemaValidationContext;
import com.epam.aidial.core.config.validation.CustomApplicationConformToTypeSchemaValidator;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class ApplicationResourceValidityStateOnGetResolver {

    private final ApplicationTypeSchemaService applicationTypeSchemaService;
    private final ApplicationTypeSchemaCoreMapper applicationTypeSchemaCoreMapper;

    public ValidityStateResource resolveValidityState(ApplicationResourceDto applicationResourceDto) {
        if (applicationResourceDto == null) {
            return null;
        }

        String applicationTypeSchemaId = applicationResourceDto.getApplicationTypeSchemaId();
        if (applicationTypeSchemaId == null) {
            return ValidityStateResource.builder().isValid(true).build();
        }

        var applicationTypeSchema = applicationTypeSchemaService.tryGet(applicationTypeSchemaId);
        if (applicationTypeSchema.isEmpty()) {
            return ValidityStateResource.builder()
                    .message("Schema not found")
                    .isValid(false)
                    .build();
        }

        String coreApplicationTypeSchema = mapToCoreApplicationTypeSchema(applicationTypeSchema.get());
        var validationContext = new CustomApplicationConformToTypeSchemaValidationContext(
                Map.of(applicationTypeSchema.get().getSchemaId(), coreApplicationTypeSchema)
        );
        Set<ValidationMessage> validationMessages = CustomApplicationConformToTypeSchemaValidator.validate(applicationResourceDto, validationContext);

        if (!validationMessages.isEmpty()) {
            String validityStateMessage = validationMessages.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining(", "));
            return ValidityStateResource.builder()
                    .message(validityStateMessage)
                    .isValid(false)
                    .build();
        } else {
            return ValidityStateResource.builder().isValid(true).build();
        }
    }

    private String mapToCoreApplicationTypeSchema(ApplicationTypeSchema applicationTypeSchema) {
        return applicationTypeSchemaCoreMapper.mapToCoreString(applicationTypeSchema);
    }
}
