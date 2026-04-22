package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Mcp;
import com.epam.aidial.cfg.domain.model.source.ApplicationEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSchemaSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
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
    @Mock
    private DeploymentManagerService deploymentManagerService;
    @Mock
    private DeploymentInfoValidator deploymentInfoValidator;

    private ApplicationValidator applicationValidator;

    @BeforeEach
    void setUp() {
        applicationValidator = new ApplicationValidator(displayFieldsValidator, deploymentValidator, featuresValidator,
                deploymentManagerService, deploymentInfoValidator, null);
    }

    @Test
    void validateCreation_shouldDelegateToDisplayFieldsValidator() {
        // given
        Application application = new Application();
        application.setDisplayName("display name");
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setSource(new ApplicationEndpointsSource());
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
        application.setSource(new ApplicationEndpointsSource());
        Deployment deployment = new Deployment("text");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid endpoint: '" + endpoint + "'. Application: text");

        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("display name", "1.0", "Application", "text");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenEndpointsSourceWithNoEndpoints() {
        // given
        Application application = new Application();
        application.setDisplayName("display name");
        application.setDisplayVersion("1.0");
        application.setSource(new ApplicationEndpointsSource());

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least application endpoint or MCP endpoint must be provided. Application: deploymentName");

        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("display name", "1.0", "Application", "deploymentName");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenSchemaSourceWithNullSchemaId() {
        Application application = new Application();
        application.setDisplayName("text");
        application.setDisplayVersion("1.0");
        application.setSource(new ApplicationSchemaSource(null));
        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Application type schema id must be provided for schema source. Application: deploymentName");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenSchemaSourceWithEndpointSet() {
        // given
        Application application = new Application();
        application.setDisplayName("text");
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setSource(new ApplicationSchemaSource(URI.create("https://test.com")));
        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Neither application endpoint nor MCP must be set for schema based application. Application: deploymentName");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenSchemaSourceWithMcpProvided() {
        Application application = new Application();
        application.setDisplayName("text");
        application.setDisplayVersion("1.0");
        application.setSource(new ApplicationSchemaSource(URI.create("https://test.com")));

        Mcp mcp = new Mcp();
        mcp.setEndpoint("http://mcp");
        application.setMcp(mcp);

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Neither application endpoint nor MCP must be set for schema based application. Application: deploymentName");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenSourceIsNull() {
        Application application = new Application();
        application.setDisplayName("text");
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Application source must be provided. Application: deploymentName");
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
        application.setSource(new ApplicationEndpointsSource());

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
        application.setSource(new ApplicationEndpointsSource());

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateUpdate(deployment.getName(), application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid endpoint: '" + endpoint + "'. Application: deploymentName");

        verify(displayFieldsValidator).validateDisplayNameDisplayVersion("display name", "1.0", "Application", "deploymentName");
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenEndpointsSourceWithNoEndpoints() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Application application = new Application();
        application.setDeployment(deployment);
        application.setDisplayName("text");
        application.setDisplayVersion("1.0");
        application.setSource(new ApplicationEndpointsSource());

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateUpdate(deploymentName, application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least application endpoint or MCP endpoint must be provided. Application: deploymentName");
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenSchemaSourceWithEndpointSet() {
        // given
        Application application = new Application();
        application.setDisplayName("text");
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setSource(new ApplicationSchemaSource(URI.create("https://test.com")));

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateUpdate(deployment.getName(), application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Neither application endpoint nor MCP must be set for schema based application. Application: deploymentName");
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid-name", "valid_name", "ValidName123", "name-123_456", "name.with.dots"})
    void validateCreation_shouldNotThrowExceptionForValidName(String name) {
        // given
        ReflectionTestUtils.setField(applicationValidator, "applicationNameValidationPattern", NAME_VALIDATION_PATTERN);

        Deployment deployment = new Deployment(name);
        Application application = new Application();
        application.setDeployment(deployment);
        application.setEndpoint("test");
        application.setSource(new ApplicationEndpointsSource());

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
        application.setEndpoint("test");
        application.setSource(new ApplicationEndpointsSource());

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