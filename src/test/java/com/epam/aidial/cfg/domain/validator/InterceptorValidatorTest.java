package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorRunnerSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterceptorValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";
    private static final String TEST_ENDPOINT = "https://test.endpoint.com";
    private static final String TEST_CONTAINER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String COMPLETION_PATH = "/api/completion";
    private static final String CONFIG_PATH = "/api/config";

    private InterceptorValidator interceptorValidator;

    @Mock
    private DeploymentManagerService deploymentManagerService;
    @Mock
    private IdFieldValidator idFieldValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        interceptorValidator = new InterceptorValidator(
                deploymentManagerService,
                new DeploymentInfoValidator(),
                idFieldValidator,
                null
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid-name", "valid_name", "ValidName123", "name-123_456", "name.with.dots"})
    void validateCreation_shouldNotThrowExceptionForValidName(String name) {
        // given
        ReflectionTestUtils.setField(interceptorValidator, "interceptorNameValidationPattern", NAME_VALIDATION_PATTERN);

        Interceptor interceptor = new Interceptor();
        interceptor.setName(name);
        interceptor.setEndpoint(TEST_ENDPOINT);

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid name with spaces", "invalid@name", "invalid#name", "invalid$name",
            "name-that-is-way-too-long-for-validation-pattern"})
    void validateCreation_shouldThrowExceptionForInvalidName(String name) {
        // given
        ReflectionTestUtils.setField(interceptorValidator, "interceptorNameValidationPattern", NAME_VALIDATION_PATTERN);

        Interceptor interceptor = new Interceptor();
        interceptor.setName(name);
        interceptor.setEndpoint(TEST_ENDPOINT);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern");
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenInterceptorNameIsUpdated() {
        Interceptor interceptor = new Interceptor();
        interceptor.setName("new_interceptor_name");
        interceptor.setEndpoint(TEST_ENDPOINT);

        assertThatThrownBy(() -> interceptorValidator.validateUpdate("interceptor_name", interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Interceptor with name: 'interceptor_name' can not be renamed. New interceptor name: 'new_interceptor_name'");
    }

    @Test
    void validateInterceptorSource_shouldThrowExceptionForInvalidConfigurationEndpoint() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setConfigurationEndpoint("invalid-url");

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid configuration endpoint: 'invalid-url'");
    }

    @Test
    void validateInterceptorSource_shouldNotThrowExceptionForNullConfigurationEndpoint() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setConfigurationEndpoint(null);
        interceptor.setEndpoint(TEST_ENDPOINT);

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }

    @Test
    void validateEndpointsSource_shouldThrowExceptionWhenCompletionEndpointIsMissing() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorEndpointsSource());
        interceptor.setEndpoint(null);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Completion endpoint is required when source type is 'Interceptor endpoints'");
    }

    @Test
    void validateEndpointsSource_shouldValidateEndpoints() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorEndpointsSource());
        interceptor.setEndpoint("invalid-url");

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid completion endpoint: 'invalid-url'");
    }

    @Test
    void validateRunnerSource_shouldThrowExceptionWhenRunnerNameIsBlank() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorRunnerSource(""));

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Runner name is required when source type is 'Interceptor runner'");
    }

    @Test
    void validateRunnerSource_shouldNotThrowExceptionWhenRunnerNameIsProvided() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorRunnerSource("runner-name"));

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }

    @Test
    void validateContainerSource_shouldThrowExceptionWhenContainerNotFound() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorContainerSource(TEST_CONTAINER_ID, COMPLETION_PATH, CONFIG_PATH));

        when(deploymentManagerService.getById(TEST_CONTAINER_ID)).thenReturn(null);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Container with ID '550e8400-e29b-41d4-a716-446655440000' not found");
    }

    @Test
    void validateContainerSource_shouldThrowExceptionWhenDeploymentUrlIsBlank() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorContainerSource(TEST_CONTAINER_ID, COMPLETION_PATH, CONFIG_PATH));

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl("");
        when(deploymentManagerService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Container URL is not present, please check if it is deployed. Container ID: 550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void validateContainerSource_shouldValidateEndpointPaths() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorContainerSource(TEST_CONTAINER_ID, "invalid path with spaces", CONFIG_PATH));

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl("https://deployment.url");
        when(deploymentManagerService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid completion endpoint path: 'invalid path with spaces'");
    }

    @Test
    void validateContainerSource_shouldValidateConfigurationEndpointPath() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorContainerSource(TEST_CONTAINER_ID, COMPLETION_PATH, "invalid path with spaces"));

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl("https://deployment.url");
        when(deploymentManagerService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid configuration endpoint path: 'invalid path with spaces'");
    }

    @Test
    void validateContainerSource_shouldAcceptValidEndpointPaths() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorContainerSource(TEST_CONTAINER_ID, COMPLETION_PATH, CONFIG_PATH));

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl("https://deployment.url");
        when(deploymentManagerService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }

    @Test
    void validateContainerSource_shouldAcceptPathsWithoutLeadingSlash() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorContainerSource(TEST_CONTAINER_ID, "api/completion", "api/config"));

        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl("https://deployment.url");
        when(deploymentManagerService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenIdFieldValidatorThrows() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");

        doThrow(IllegalArgumentException.class).when(idFieldValidator).validateName("test-interceptor");

        // when/then
        Assertions.assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class);
    }
}