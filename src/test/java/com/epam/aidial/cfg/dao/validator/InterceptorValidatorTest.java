package com.epam.aidial.cfg.dao.validator;

import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.Source;
import com.epam.aidial.cfg.domain.model.SourceType;
import com.epam.aidial.cfg.domain.service.ExternalDeploymentScheduledService;
import com.epam.aidial.cfg.domain.validator.InterceptorValidator;
import com.epam.aidial.cfg.dto.DeploymentInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class InterceptorValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";
    private static final String TEST_ENDPOINT = "https://test.endpoint.com";
    private static final String TEST_CONFIG_ENDPOINT = "https://test.config.endpoint.com";
    private static final String TEST_DEPLOYMENT_URL = "https://test.deployment.com";
    private static final String TEST_CONTAINER_ID = "550e8400-e29b-41d4-a716-446655440000";

    private InterceptorValidator interceptorValidator;

    @Mock
    private ExternalDeploymentScheduledService deploymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        interceptorValidator = new InterceptorValidator(deploymentService);
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
    void validateInterceptorSource_shouldThrowExceptionWhenSourceNameIsNull() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        
        Source source = new Source();
        source.setType(SourceType.TEMPLATE);
        source.setName(null);
        interceptor.setSource(source);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Source name is required when source is specified");
    }

    @Test
    void validateInterceptorSource_shouldThrowExceptionWhenEndpointIsMissingForEndpointsSource() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        
        Source source = new Source();
        source.setType(SourceType.ENDPOINTS);
        source.setName("test-source");
        interceptor.setSource(source);
        interceptor.setEndpoint(null);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Endpoint is required when source type is 'External endpoints'");
    }

    @Test
    void validateInterceptorSource_shouldNotThrowExceptionWhenEndpointIsProvidedForEndpointsSource() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        
        Source source = new Source();
        source.setType(SourceType.ENDPOINTS);
        source.setName("test-source");
        interceptor.setSource(source);
        interceptor.setEndpoint(TEST_ENDPOINT);

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }

    @Test
    void validateInterceptorSource_shouldThrowExceptionWhenEndpointIsProvidedForTemplateSource() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        
        Source source = new Source();
        source.setType(SourceType.TEMPLATE);
        source.setName("test-template");
        interceptor.setSource(source);
        interceptor.setEndpoint(TEST_ENDPOINT);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Both endpoint: 'https://test.endpoint.com' and interceptor runner: 'test-template' are specified. Only one of them should be specified");
    }

    @Test
    void validateInterceptorSource_shouldNotThrowExceptionWhenEndpointIsNullForTemplateSource() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        
        Source source = new Source();
        source.setType(SourceType.TEMPLATE);
        source.setName("test-template");
        interceptor.setSource(source);
        interceptor.setEndpoint(null);

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }

    @Test
    void validateInterceptorSource_shouldThrowExceptionWhenContainerNotFound() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        
        Source source = new Source();
        source.setType(SourceType.CONTAINER);
        source.setName(TEST_CONTAINER_ID);
        interceptor.setSource(source);
        
        when(deploymentService.getById(TEST_CONTAINER_ID)).thenReturn(null);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Container with name '550e8400-e29b-41d4-a716-446655440000' not found");
    }

    @Test
    void validateInterceptorSource_shouldThrowExceptionWhenEndpointDoesNotMatchDeploymentUrl() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        
        Source source = new Source();
        source.setType(SourceType.CONTAINER);
        source.setName(TEST_CONTAINER_ID);
        interceptor.setSource(source);
        interceptor.setEndpoint(TEST_ENDPOINT);
        
        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl(TEST_DEPLOYMENT_URL);
        when(deploymentService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Completion endpoint should start with 'https://test.deployment.com' but was: https://test.endpoint.com");
    }

    @Test
    void validateInterceptorSource_shouldThrowExceptionWhenConfigEndpointDoesNotMatchDeploymentUrl() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        
        Source source = new Source();
        source.setType(SourceType.CONTAINER);
        source.setName(TEST_CONTAINER_ID);
        interceptor.setSource(source);
        interceptor.setEndpoint(TEST_DEPLOYMENT_URL + "/api");
        interceptor.setConfigurationEndpoint(TEST_CONFIG_ENDPOINT);
        
        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl(TEST_DEPLOYMENT_URL);
        when(deploymentService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Configuration endpoint should start with 'https://test.deployment.com' but was: https://test.config.endpoint.com");
    }

    @Test
    void validateInterceptorSource_shouldNotThrowExceptionWhenEndpointsMatchDeploymentUrl() {
        // given
        String validEndpoint = TEST_DEPLOYMENT_URL + "/api";
        String validConfigEndpoint = TEST_DEPLOYMENT_URL + "/config";
        
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        
        Source source = new Source();
        source.setType(SourceType.CONTAINER);
        source.setName(TEST_CONTAINER_ID);
        interceptor.setSource(source);
        interceptor.setEndpoint(validEndpoint);
        interceptor.setConfigurationEndpoint(validConfigEndpoint);
        
        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl(TEST_DEPLOYMENT_URL);
        when(deploymentService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }

    @Test
    void validateInterceptorSource_shouldNotThrowExceptionWhenDeploymentUrlIsNull() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        
        Source source = new Source();
        source.setType(SourceType.CONTAINER);
        source.setName(TEST_CONTAINER_ID);
        interceptor.setSource(source);
        interceptor.setEndpoint(TEST_ENDPOINT);
        
        DeploymentInfoDto deploymentInfo = new DeploymentInfoDto();
        deploymentInfo.setUrl(null);
        when(deploymentService.getById(TEST_CONTAINER_ID)).thenReturn(deploymentInfo);

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }
}