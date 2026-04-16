package com.epam.aidial.cfg.dao.listener.validitystate.resolver;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.mapper.ApplicationEntityMapper;
import com.epam.aidial.cfg.dao.mapper.ApplicationTypeSchemaEntityMapper;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.ValidityStateEntity;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapper;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.core.config.validation.CustomApplicationConformToTypeSchemaValidationContext;
import com.epam.aidial.core.config.validation.CustomApplicationConformToTypeSchemaValidator;
import com.epam.aidial.core.config.validation.SchemaConformToMetaSchemaValidator;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@LogExecution
public class ApplicationValidityStateResolver {

    private final ApplicationEntityMapper applicationEntityMapper;

    private final ApplicationTypeSchemaEntityMapper applicationTypeSchemaEntityMapper;
    private final ApplicationTypeSchemaCoreMapper applicationTypeSchemaCoreMapper;

    public ValidityStateEntity resolveValidityState(ApplicationEntity applicationEntity) {
        ValidityStateEntity validityStateEntity = new ValidityStateEntity();

        ApplicationTypeSchemaEntity applicationTypeSchemaEntity = applicationEntity.getApplicationTypeSchema();
        if (applicationTypeSchemaEntity == null) {
            validityStateEntity.setValid(true);
            return validityStateEntity;
        }

        Application application = mapToApplication(applicationEntity);
        String coreApplicationTypeSchema = mapToCoreApplicationTypeSchema(applicationTypeSchemaEntity);
        var validationContext = new CustomApplicationConformToTypeSchemaValidationContext(
                Map.of(applicationTypeSchemaEntity.getSchemaId(), coreApplicationTypeSchema)
        );
        Set<ValidationMessage> appValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);
        Set<ValidationMessage> schemaValidationMessages = SchemaConformToMetaSchemaValidator.getValidationMessages(coreApplicationTypeSchema);

        if (appValidationMessages.isEmpty() && schemaValidationMessages.isEmpty()) {
            validityStateEntity.setValid(true);
            return validityStateEntity;
        }

        String validityStateMessage = resolveValidityStateMessage(appValidationMessages, schemaValidationMessages);
        validityStateEntity.setMessage(validityStateMessage);
        validityStateEntity.setValid(false);

        return validityStateEntity;
    }

    private Application mapToApplication(ApplicationEntity applicationEntity) {
        return applicationEntityMapper.toDomainWithoutRoleLimits(applicationEntity);
    }

    private String mapToCoreApplicationTypeSchema(ApplicationTypeSchemaEntity entity) {
        var applicationTypeSchema = applicationTypeSchemaEntityMapper.toDomain(entity);
        return applicationTypeSchemaCoreMapper.mapToCoreString(applicationTypeSchema);
    }

    private String resolveValidityStateMessage(Set<ValidationMessage> appValidationMessages,
                                               Set<ValidationMessage> schemaValidationMessages) {
        if (!appValidationMessages.isEmpty() && !schemaValidationMessages.isEmpty()) {
            String appValidityStateMessage = "App: " + buildValidationMessage(appValidationMessages);
            String schemaValidityStateMessage = "Schema: " + buildValidationMessage(schemaValidationMessages);
            return appValidityStateMessage + "; " + schemaValidityStateMessage;
        } else if (!appValidationMessages.isEmpty()) {
            return "App: " + buildValidationMessage(appValidationMessages);
        } else if (!schemaValidationMessages.isEmpty()) {
            return "Schema: " + buildValidationMessage(schemaValidationMessages);
        } else {
            throw new IllegalArgumentException("Validation messages are empty");
        }
    }

    private String buildValidationMessage(Set<ValidationMessage> validationMessages) {
        return validationMessages.stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.joining(", "));
    }
}
