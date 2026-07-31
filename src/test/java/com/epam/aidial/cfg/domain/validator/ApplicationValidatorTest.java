package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.AuthenticationType;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.DeploymentInterface;
import com.epam.aidial.cfg.domain.model.ExternalService;
import com.epam.aidial.cfg.domain.model.Mcp;
import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import com.epam.aidial.cfg.domain.model.source.ApplicationEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ApplicationSchemaSource;
import com.epam.aidial.cfg.domain.value.LocalizedValue;
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
import java.util.Map;

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
        applicationValidator = new ApplicationValidator(displayFieldsValidator, deploymentValidator, featuresValidator,
                new DeploymentInterfacesValidator(), new ResourceAuthSettingsValidator(), null);
    }

    @Test
    void validateCreation_shouldDelegateToDisplayFieldsValidator() {
        // given
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setSource(new ApplicationEndpointsSource());
        Deployment deployment = new Deployment("text");
        application.setDeployment(deployment);

        // when
        applicationValidator.validateCreation(application);

        // then
        verify(displayFieldsValidator).validateDisplayNameDisplayVersion(LocalizedValue.of("display name"), "1.0", "Application", "text");
    }

    @ParameterizedTest
    @CsvSource({"''", "' '"})
    void validateCreation_shouldThrowExceptionWhenEndpointIsNotNullButBlank(String endpoint) {
        // given
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setEndpoint(endpoint);
        application.setSource(new ApplicationEndpointsSource());
        Deployment deployment = new Deployment("text");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid endpoint: '" + endpoint + "'. Application: text");

        verify(displayFieldsValidator).validateDisplayNameDisplayVersion(
                application.getDisplayName(), application.getDisplayVersion(), "Application", "text");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenEndpointsSourceWithNoEndpoints() {
        // given
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setSource(new ApplicationEndpointsSource());

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least application endpoint, MCP endpoint or interfaces must be provided. Application: deploymentName");

        verify(displayFieldsValidator).validateDisplayNameDisplayVersion(
                application.getDisplayName(), application.getDisplayVersion(), "Application", "deploymentName");
    }

    @Test
    void validateCreation_shouldNotThrowWhenEndpointsSourceWithOnlyInterfaces() {
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setSource(new ApplicationEndpointsSource());
        application.setInterfaces(interfaces("openaiChatCompletions", "http://app.adapter.test.com"));

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        Assertions.assertThatNoException().isThrownBy(() -> applicationValidator.validateCreation(application));
    }

    @Test
    void validateCreation_shouldThrowWhenApplicationHasUnsupportedInterfaceType() {
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setSource(new ApplicationEndpointsSource());
        application.setInterfaces(interfaces("anthropicMessages", "http://app.adapter.test.com"));

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported interface type 'anthropicMessages'");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenSchemaSourceWithInterfacesSet() {
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("text"));
        application.setDisplayVersion("1.0");
        application.setInterfaces(interfaces("openaiChatCompletions", "http://app.adapter.test.com"));
        application.setSource(new ApplicationSchemaSource(URI.create("https://test.com")));
        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Neither application endpoint, MCP nor interfaces must be set for schema based application. Application: deploymentName");
    }

    private static Map<String, DeploymentInterface> interfaces(String type, String baseUrl) {
        DeploymentInterface deploymentInterface = new DeploymentInterface();
        deploymentInterface.setBaseUrl(baseUrl);
        return Map.of(type, deploymentInterface);
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenSchemaSourceWithNullSchemaId() {
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("text"));
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
        application.setDisplayName(LocalizedValue.of("text"));
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setSource(new ApplicationSchemaSource(URI.create("https://test.com")));
        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Neither application endpoint, MCP nor interfaces must be set for schema based application. Application: deploymentName");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenSchemaSourceWithMcpProvided() {
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("text"));
        application.setDisplayVersion("1.0");
        application.setSource(new ApplicationSchemaSource(URI.create("https://test.com")));

        Mcp mcp = new Mcp();
        mcp.setEndpoint("http://mcp");
        application.setMcp(mcp);

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Neither application endpoint, MCP nor interfaces must be set for schema based application. Application: deploymentName");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenSourceIsNull() {
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("text"));
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
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setDeployment(deployment);
        application.setEndpoint("test");
        application.setSource(new ApplicationEndpointsSource());

        // when
        applicationValidator.validateUpdate(deploymentName, application);

        // then
        verify(deploymentValidator).validateUpdate(deploymentName, deployment, "Application");
        verify(displayFieldsValidator).validateDisplayNameDisplayVersion(
                application.getDisplayName(), application.getDisplayVersion(), "Application", deploymentName);
    }

    @ParameterizedTest
    @CsvSource({"''", "' '"})
    void validateUpdate_shouldThrowExceptionWhenEndpointIsNotNullButBlank(String endpoint) {
        // given
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setEndpoint(endpoint);
        application.setSource(new ApplicationEndpointsSource());

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateUpdate(deployment.getName(), application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid endpoint: '" + endpoint + "'. Application: deploymentName");

        verify(displayFieldsValidator).validateDisplayNameDisplayVersion(
                application.getDisplayName(), application.getDisplayVersion(), "Application", "deploymentName");
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenEndpointsSourceWithNoEndpoints() {
        // given
        String deploymentName = "deploymentName";

        Deployment deployment = new Deployment(deploymentName);

        Application application = new Application();
        application.setDeployment(deployment);
        application.setDisplayName(LocalizedValue.of("text"));
        application.setDisplayVersion("1.0");
        application.setSource(new ApplicationEndpointsSource());

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateUpdate(deploymentName, application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least application endpoint, MCP endpoint or interfaces must be provided. Application: deploymentName");
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenSchemaSourceWithEndpointSet() {
        // given
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("text"));
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setSource(new ApplicationSchemaSource(URI.create("https://test.com")));

        Deployment deployment = new Deployment("deploymentName");
        application.setDeployment(deployment);

        // then
        Assertions.assertThatThrownBy(() -> applicationValidator.validateUpdate(deployment.getName(), application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Neither application endpoint, MCP nor interfaces must be set for schema based application. Application: deploymentName");
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
    void validateCreation_shouldThrowExceptionWhenExternalServiceHasIncompleteOauthAuthSettings() {
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setSource(new ApplicationEndpointsSource());
        application.setDeployment(new Deployment("deploymentName"));
        application.setExternalServices(Map.of("svc", externalService(AuthenticationType.OAUTH)));

        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank for Application external service with id:'deploymentName/svc'");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenExternalServiceHasIncompleteApiKeyAuthSettings() {
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setSource(new ApplicationEndpointsSource());
        application.setDeployment(new Deployment("deploymentName"));
        application.setExternalServices(Map.of("svc", externalService(AuthenticationType.API_KEY)));

        Assertions.assertThatThrownBy(() -> applicationValidator.validateCreation(application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API Key header")
                .hasMessageContaining("Application external service with id:'deploymentName/svc'");
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenExternalServiceHasIncompleteOauthAuthSettings() {
        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setSource(new ApplicationEndpointsSource());
        application.setDeployment(new Deployment("deploymentName"));
        application.setExternalServices(Map.of("svc", externalService(AuthenticationType.OAUTH)));

        Assertions.assertThatThrownBy(() -> applicationValidator.validateUpdate("deploymentName", application))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank for Application external service with id:'deploymentName/svc'");
    }

    @Test
    void validateCreation_shouldNotThrowWhenExternalServiceAuthSettingsAreValid() {
        ResourceAuthSettings authSettings = new ResourceAuthSettings();
        authSettings.setAuthenticationType(AuthenticationType.OAUTH);
        authSettings.setClientId("client-id");
        authSettings.setClientSecret("client-secret");
        authSettings.setAuthorizationEndpoint("https://auth");
        authSettings.setTokenEndpoint("https://token");
        ExternalService externalService = new ExternalService();
        externalService.setDisplayName("Service display name");
        externalService.setAuthSettings(authSettings);

        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setSource(new ApplicationEndpointsSource());
        application.setDeployment(new Deployment("deploymentName"));
        application.setExternalServices(Map.of("svc", externalService));

        Assertions.assertThatNoException().isThrownBy(() -> applicationValidator.validateCreation(application));
    }

    @Test
    void validateCreation_shouldValidateExternalServiceDisplayName() {
        ExternalService externalService = externalService(AuthenticationType.NONE);

        Application application = new Application();
        application.setDisplayName(LocalizedValue.of("display name"));
        application.setDisplayVersion("1.0");
        application.setEndpoint("test");
        application.setSource(new ApplicationEndpointsSource());
        application.setDeployment(new Deployment("deploymentName"));
        application.setExternalServices(Map.of("svc", externalService));

        applicationValidator.validateCreation(application);

        verify(displayFieldsValidator)
                .validateDisplayName("Service display name", "Application external service", "deploymentName/svc");
    }

    private static ExternalService externalService(AuthenticationType authenticationType) {
        ResourceAuthSettings authSettings = new ResourceAuthSettings();
        authSettings.setAuthenticationType(authenticationType);
        ExternalService externalService = new ExternalService();
        externalService.setDisplayName("Service display name");
        externalService.setAuthSettings(authSettings);
        return externalService;
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