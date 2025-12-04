package com.epam.aidial.core.config.validation;

import com.epam.aidial.cfg.client.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.core.config.CoreApplication;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CustomApplicationConformToTypeSchemaValidatorTest {

    private static CustomApplicationConformToTypeSchemaValidationContext validationContext;

    @BeforeAll
    static void beforeAll() {
        String schema = """
                {
                    "$id": "https://test-schema.example",
                    "$schema": "https://dial.epam.com/application_type_schemas/schema#",
                    "required": [
                       "requiredProp"
                    ]
                 }
                 """;
        Map<String, String> applicationTypeSchemas = Map.of("https://test-schema.example", schema);
        validationContext = new CustomApplicationConformToTypeSchemaValidationContext(applicationTypeSchemas);
    }

    @Test
    void validate_emptyValidationMessagesForCoreApplicationWhenSchemaIdIsNull() {
        // given
        CoreApplication application = new CoreApplication();
        application.setApplicationTypeSchemaId(null);

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertThat(actualValidationMessages).isEmpty();
    }

    @Test
    void validate_emptyValidationMessagesForCoreApplicationWhenAppPropertiesIsNull() {
        // given
        CoreApplication application = new CoreApplication();
        application.setApplicationTypeSchemaId(URI.create("https://test-schema.example"));
        application.setApplicationProperties(null);

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertThat(actualValidationMessages).isEmpty();
    }

    @Test
    void validate_validationMessagesWithErrorForCoreApplicationWhenAppPropertiesFailValidation() {
        // given
        CoreApplication application = new CoreApplication();
        application.setApplicationTypeSchemaId(URI.create("https://test-schema.example"));
        application.setApplicationProperties(Map.of());

        Set<String> expectedValidationMessages = Set.of("$: required property 'requiredProp' not found");

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertValidationMessages(actualValidationMessages, expectedValidationMessages);
    }

    @Test
    void validate_emptyValidationMessagesForCoreApplicationWhenAppPropertiesPassValidation() {
        // given
        CoreApplication application = new CoreApplication();
        application.setApplicationTypeSchemaId(URI.create("https://test-schema.example"));
        application.setApplicationProperties(Map.of("requiredProp", "some-value"));

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertThat(actualValidationMessages).isEmpty();
    }

    @Test
    void validate_emptyValidationMessagesForApplicationWhenSchemaIdIsNull() {
        // given
        Application application = new Application();
        application.setApplicationTypeSchemaId(null);

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertThat(actualValidationMessages).isEmpty();
    }

    @Test
    void validate_emptyValidationMessagesForApplicationWhenAppPropertiesIsNull() {
        // given
        Application application = new Application();
        application.setApplicationTypeSchemaId(URI.create("https://test-schema.example"));
        application.setApplicationProperties(null);

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertThat(actualValidationMessages).isEmpty();
    }

    @Test
    void validate_validationMessagesWithErrorForApplicationWhenAppPropertiesFailValidation() {
        // given
        Application application = new Application();
        application.setApplicationTypeSchemaId(URI.create("https://test-schema.example"));
        application.setApplicationProperties(Map.of());

        Set<String> expectedValidationMessages = Set.of("$: required property 'requiredProp' not found");

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertValidationMessages(actualValidationMessages, expectedValidationMessages);
    }

    @Test
    void validate_emptyValidationMessagesForApplicationWhenAppPropertiesPassValidation() {
        // given
        Application application = new Application();
        application.setApplicationTypeSchemaId(URI.create("https://test-schema.example"));
        application.setApplicationProperties(Map.of("requiredProp", "some-value"));

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertThat(actualValidationMessages).isEmpty();
    }

    @Test
    void validate_emptyValidationMessagesForApplicationResourceDtoWhenSchemaIdIsNull() {
        // given
        ApplicationResourceDto application = new ApplicationResourceDto();
        application.setApplicationTypeSchemaId(null);

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertThat(actualValidationMessages).isEmpty();
    }

    @Test
    void validate_validationMessagesWithErrorForApplicationResourceDtoWhenSchemaIdIsNotValidUri() {
        // given
        ApplicationResourceDto application = new ApplicationResourceDto();
        application.setApplicationTypeSchemaId("invalid uri");

        Set<String> expectedValidationMessages = Set.of("Illegal character in path at index 7: invalid uri");

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertValidationMessages(actualValidationMessages, expectedValidationMessages);
    }

    @Test
    void validate_emptyValidationMessagesForApplicationResourceDtoWhenAppPropertiesIsNull() {
        // given
        ApplicationResourceDto application = new ApplicationResourceDto();
        application.setApplicationTypeSchemaId("https://test-schema.example");
        application.setApplicationProperties(null);

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertThat(actualValidationMessages).isEmpty();
    }

    @Test
    void validate_validationMessagesWithErrorForApplicationResourceDtoWhenAppPropertiesFailValidation() {
        // given
        ApplicationResourceDto application = new ApplicationResourceDto();
        application.setApplicationTypeSchemaId("https://test-schema.example");
        application.setApplicationProperties(Map.of());

        Set<String> expectedValidationMessages = Set.of("$: required property 'requiredProp' not found");

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertValidationMessages(actualValidationMessages, expectedValidationMessages);
    }

    @Test
    void validate_emptyValidationMessagesForApplicationResourceDtoWhenAppPropertiesPassValidation() {
        // given
        ApplicationResourceDto application = new ApplicationResourceDto();
        application.setApplicationTypeSchemaId("https://test-schema.example");
        application.setApplicationProperties(Map.of("requiredProp", "some-value"));

        // when
        Set<ValidationMessage> actualValidationMessages = CustomApplicationConformToTypeSchemaValidator.validate(application, validationContext);

        // then
        assertThat(actualValidationMessages).isEmpty();
    }

    private void assertValidationMessages(Set<ValidationMessage> actualValidationMessages, Set<String> expectedValidationMessages) {
        Set<String> actualValidationMessageStrings = actualValidationMessages.stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.toSet());
        assertThat(actualValidationMessageStrings).containsExactlyInAnyOrderElementsOf(expectedValidationMessages);
    }
}