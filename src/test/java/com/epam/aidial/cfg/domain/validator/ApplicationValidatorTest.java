package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.Deployment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicationValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";

    @Mock
    private DisplayFieldsValidator displayFieldsValidator;
    @Mock
    private DeploymentValidator deploymentValidator;
    @Mock
    private FeaturesValidator featuresValidator;

    private ApplicationValidator applicationValidator;

    @BeforeEach
    void setUp() {
        applicationValidator = new ApplicationValidator(displayFieldsValidator, deploymentValidator, featuresValidator, null);
    }

    @Test
    void validateCreation_shouldDelegateToDisplayFieldsValidator() {
        // given
        Application application = new Application();
        application.setDisplayName("display name");
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        Deployment deployment = new Deployment("text");
        application.setDeployment(deployment);

        // when
        applicationValidator.validateCreation(application);

        // then
        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("display name", "1.0", "Application", "text");
    }

    @ParameterizedTest
    @CsvSource({"''", "' '"})
    void validateCreation_shouldThrowExceptionWhenEndpointIsNotNullButBlank(String endpoint) {
        // given
        Application application = new Application();
        application.setDisplayName("display name");
        application.setDisplayVersion("1.0");
        application.setEndpoint(endpoint);
        Deployment deployment = new Deployment("text");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid endpoint: '" + endpoint + "'. Application: text");

        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("display name", "1.0", "Application", "text");
    }

    @ParameterizedTest
    @CsvSource(value = {"null, null", "null, ''"}, nullValues = "null")
    void validateCreation_shouldThrowExceptionWhenEndpointAndApplicationTypeSchemaIdAreBlank(String endpoint, String applicationTypeSchemaIdAsString) {
        // given
        URI applicationTypeSchemaId = applicationTypeSchemaIdAsString != null ? URI.create(applicationTypeSchemaIdAsString) : null;

        Application application = new Application();
        application.setDisplayName("display name");
        application.setDisplayVersion("1.0");
        application.setEndpoint(endpoint);
        application.setApplicationTypeSchemaId(applicationTypeSchemaId);

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing endpoint and application type schema id. Only one of them should be specified. Application: deploymentName");

        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("display name", "1.0", "Application", "deploymentName");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenEndpointAndApplicationTypeSchemaIdAreNotBlank() {
        // given
        Application application = new Application();
        application.setDisplayName("text");
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setApplicationTypeSchemaId(URI.create("https://test.com"));
        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Both endpoint: 'test' and application type schema id: 'https://test.com' are specified. "
                        + "Only one of them should be specified. Application: deploymentName");
    }

    @Test
    void validateUpdate_shouldDelegateToDisplayFieldsAndDeploymentValidators() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Application application = new Application();
        application.setDisplayName("display name");
        application.setDisplayVersion("1.0");
        application.setDeployment(deployment);
        application.setEndpoint("test");

        // when
        applicationValidator.validateUpdate(deploymentName, application);

        // then
        verify(deploymentValidator).validateUpdate(deploymentName, deployment, "Application");
        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("display name", "1.0", "Application", deploymentName);
    }

    @ParameterizedTest
    @CsvSource({"''", "' '"})
    void validateUpdate_shouldThrowExceptionWhenEndpointIsNotNullButBlank(String endpoint) {
        // given
        Application application = new Application();
        application.setDisplayName("display name");
        application.setDisplayVersion("1.0");
        application.setEndpoint(endpoint);

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateUpdate(deployment.getName(), application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid endpoint: '" + endpoint + "'. Application: deploymentName");

        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("display name", "1.0", "Application", "deploymentName");
    }

    @ParameterizedTest
    @CsvSource(value = {"null, null", "null, ''"}, nullValues = "null")
    void validateUpdate_shouldThrowExceptionWhenEndpointAndApplicationTypeSchemaIdAreBlank(String endpoint, String applicationTypeSchemaIdAsString) {
        // given
        URI applicationTypeSchemaId = applicationTypeSchemaIdAsString != null ? URI.create(applicationTypeSchemaIdAsString) : null;
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Application application = new Application();
        application.setDeployment(deployment);
        application.setDisplayName("text");
        application.setDisplayVersion("1.0");
        application.setEndpoint(endpoint);
        application.setApplicationTypeSchemaId(applicationTypeSchemaId);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateUpdate(deploymentName, application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing endpoint and application type schema id. Only one of them should be specified. Application: deploymentName");
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenEndpointAndApplicationTypeSchemaIdAreNotBlank() {
        // given
        Application application = new Application();
        application.setDisplayName("text");
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setApplicationTypeSchemaId(URI.create("https://test.com"));

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateUpdate(deployment.getName(), application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Both endpoint: 'test' and application type schema id: 'https://test.com' are specified. "
                        + "Only one of them should be specified. Application: deploymentName");
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenMcpNotNullAndMcpEndpointIsNull() {
        // given
        Application application = new Application();
        application.setDisplayName("text");
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setMcp(new Application.Mcp());

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateUpdate(deployment.getName(), application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("MCP endpoint must be provided");
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid-name", "valid_name", "ValidName123", "name-123_456", "name.with.dots"})
    void validateCreation_shouldNotThrowExceptionForValidName(String name) {
        // given
        ReflectionTestUtils.setField(applicationValidator, "applicationNameValidationPattern", NAME_VALIDATION_PATTERN);

        Deployment deployment = new Deployment(name);
        Application application = new Application();
        application.setDeployment(deployment);
        application.setEndpoint("test"); // To avoid other validation errors

        // when/then
        Assertions.assertThatNoException().isThrownBy(() -> applicationValidator.validateCreation(application));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid name with spaces", "invalid@name", "invalid#name", "invalid$name",
            "name-that-is-way-too-long-for-validation-pattern"})
    void validateCreation_shouldThrowExceptionForInvalidName(String name) {
        // given
        ReflectionTestUtils.setField(applicationValidator, "applicationNameValidationPattern", NAME_VALIDATION_PATTERN);

        Deployment deployment = new Deployment(name);
        Application application = new Application();
        application.setDeployment(deployment);
        application.setEndpoint("test"); // To avoid other validation errors

        // when/then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenDeploymentValidatorThrows() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Application application = new Application();
        application.setDeployment(deployment);

        doThrow(IllegalArgumentException.class).when(deploymentValidator)
                .validateCreation("Application", deploymentName);

        // when/then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class);
    }
}