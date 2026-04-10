package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Features;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorRunnerSource;
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

@ExtendWith(MockitoExtension.class)
class InterceptorValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";
    private static final String TEST_ENDPOINT = "https://test.endpoint.com";
    private static final String TEST_CONTAINER_NAME = "test-container";
    private static final String TEST_CONTAINER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String COMPLETION_PATH = "/api/completion";
    private static final String CONFIG_PATH = "/api/config";

    private InterceptorValidator interceptorValidator;

    @Mock
    private IdFieldValidator idFieldValidator;
    @Mock
    private DisplayFieldsValidator displayFieldsValidator;
    @Mock
    private FeaturesValidator featuresValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        interceptorValidator = new InterceptorValidator(
                idFieldValidator,
                displayFieldsValidator,
                featuresValidator,
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
        Features features = new Features();
        features.setConfigurationEndpoint("invalid-url");

        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setFeatures(features);

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid configuration endpoint: 'invalid-url'. Interceptor: test-interceptor");
    }

    @Test
    void validateInterceptorSource_shouldNotThrowExceptionForNullConfigurationEndpoint() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
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
                .hasMessage("Completion endpoint is required when source type is 'Interceptor endpoints'. Interceptor: test-interceptor");
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
                .hasMessage("Invalid completion endpoint: 'invalid-url'. Interceptor: test-interceptor");
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
                .hasMessage("Runner name is required when source type is 'Interceptor runner'. Interceptor: test-interceptor");
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
    void validateContainerSource_shouldValidateEndpointPaths() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorContainerSource(TEST_CONTAINER_ID, TEST_CONTAINER_NAME, "invalid path with spaces", CONFIG_PATH));

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid completion endpoint path: 'invalid path with spaces'. Interceptor: test-interceptor");
    }

    @Test
    void validateContainerSource_shouldValidateConfigurationEndpointPath() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorContainerSource(TEST_CONTAINER_ID, TEST_CONTAINER_NAME, COMPLETION_PATH, "invalid path with spaces"));

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid configuration endpoint path: 'invalid path with spaces'. Interceptor: test-interceptor");
    }

    @Test
    void validateContainerSource_shouldAcceptValidEndpointPaths() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorContainerSource(TEST_CONTAINER_ID, TEST_CONTAINER_NAME, COMPLETION_PATH, CONFIG_PATH));

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }

    @Test
    void validateContainerSource_shouldAcceptPathsWithoutLeadingSlash() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");
        interceptor.setSource(new InterceptorContainerSource(TEST_CONTAINER_ID, TEST_CONTAINER_NAME, "api/completion", "api/config"));

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenIdFieldValidatorThrows() {
        // given
        Interceptor interceptor = new Interceptor();
        interceptor.setName("test-interceptor");

        doThrow(IllegalArgumentException.class).when(idFieldValidator)
                .validateName("Interceptor", "test-interceptor");

        // when/then
        Assertions.assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class);
    }
}