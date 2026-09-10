package com.epam.aidial.cfg.dto.validation.annotation;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.CreateApplicationResourceDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.dto.UpstreamDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

class EndpointValidationTest {

    private Validator validator;

    @BeforeEach
    void init() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @ParameterizedTest
    @MethodSource("validEndpointsIncludingEmpty")
    void testApplicationDto_ValidEndpoint(String endpoint) {
        ApplicationDto dto = new ApplicationDto();
        dto.setName("test-app");
        dto.setDisplayName("Test App");
        dto.setEndpoint(endpoint);

        Set<ConstraintViolation<ApplicationDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEndpoints")
    void testApplicationDto_InvalidEndpoint(String endpoint) {
        ApplicationDto dto = new ApplicationDto();
        dto.setName("test-app");
        dto.setDisplayName("Test App");
        dto.setEndpoint(endpoint);

        Set<ConstraintViolation<ApplicationDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("endpoint")))
                .isTrue();
    }

    @ParameterizedTest
    @MethodSource("validEndpointsIncludingEmpty")
    void testCreateApplicationResourceDto_ValidEndpoint(String endpoint) {
        CreateApplicationResourceDto dto = new CreateApplicationResourceDto();
        dto.setName("test-app");
        dto.setVersion("1.0.0");
        dto.setFolderId("folder-1");
        dto.setEndpoint(endpoint);

        Set<ConstraintViolation<CreateApplicationResourceDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEndpoints")
    void testCreateApplicationResourceDto_InvalidEndpoint(String endpoint) {
        CreateApplicationResourceDto dto = new CreateApplicationResourceDto();
        dto.setName("test-app");
        dto.setVersion("1.0.0");
        dto.setFolderId("folder-1");
        dto.setEndpoint(endpoint);

        Set<ConstraintViolation<CreateApplicationResourceDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("endpoint")))
                .isTrue();
    }

    @ParameterizedTest
    @MethodSource("validEndpointsIncludingEmpty")
    void testModelDto_ValidEndpoint(String endpoint) {
        ModelDto dto = new ModelDto();
        dto.setName("test-model");
        dto.setDisplayName("Test Model");
        dto.setEndpoint(endpoint);

        Set<ConstraintViolation<ModelDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEndpoints")
    void testModelDto_InvalidEndpoint(String endpoint) {
        ModelDto dto = new ModelDto();
        dto.setName("test-model");
        dto.setDisplayName("Test Model");
        dto.setEndpoint(endpoint);

        Set<ConstraintViolation<ModelDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("endpoint")))
                .isTrue();
    }

    @ParameterizedTest
    @MethodSource("validEndpointsIncludingEmpty")
    void testInterceptorDto_ValidEndpoint(String endpoint) {
        InterceptorDto dto = new InterceptorDto();
        dto.setName("test-interceptor");
        dto.setDisplayName("Test Interceptor");
        dto.setEndpoint(endpoint);

        Set<ConstraintViolation<InterceptorDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEndpoints")
    void testInterceptorDto_InvalidEndpoint(String endpoint) {
        InterceptorDto dto = new InterceptorDto();
        dto.setName("test-interceptor");
        dto.setDisplayName("Test Interceptor");
        dto.setEndpoint(endpoint);

        Set<ConstraintViolation<InterceptorDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("endpoint")))
                .isTrue();
    }

    @ParameterizedTest
    @MethodSource("validEndpointsIncludingEmpty")
    void testToolSetDto_ValidEndpoint(String endpoint) {
        ToolSetDto dto = new ToolSetDto();
        dto.setName("test-toolset");
        dto.setDisplayName("Test ToolSet");
        dto.setTransport(ToolSetDto.TransportDto.HTTP);
        dto.setEndpoint(endpoint);

        Set<ConstraintViolation<ToolSetDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEndpoints")
    void testToolSetDto_InvalidEndpoint(String endpoint) {
        ToolSetDto dto = new ToolSetDto();
        dto.setName("test-toolset");
        dto.setDisplayName("Test ToolSet");
        dto.setTransport(ToolSetDto.TransportDto.HTTP);
        dto.setEndpoint(endpoint);

        Set<ConstraintViolation<ToolSetDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("endpoint")))
                .isTrue();
    }

    @ParameterizedTest
    @MethodSource("validEndpointsIncludingEmpty")
    void testAdapterDto_ValidBaseEndpoint(String endpoint) {
        AdapterDto dto = new AdapterDto();
        dto.setName("test-adapter");
        dto.setDisplayName("Test Adapter");
        dto.setBaseEndpoint(endpoint);

        Set<ConstraintViolation<AdapterDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEndpoints")
    void testAdapterDto_InvalidBaseEndpoint(String endpoint) {
        AdapterDto dto = new AdapterDto();
        dto.setName("test-adapter");
        dto.setDisplayName("Test Adapter");
        dto.setBaseEndpoint(endpoint);

        Set<ConstraintViolation<AdapterDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("baseEndpoint")))
                .isTrue();
    }

    @ParameterizedTest
    @MethodSource("validEndpointsIncludingEmpty")
    void testInterceptorRunnerDto_ValidEndpoints(String endpoint) {
        InterceptorRunnerDto dto = new InterceptorRunnerDto();
        dto.setName("test-runner");
        dto.setDisplayName("Test Runner");
        dto.setCompletionEndpoint(endpoint);
        dto.setConfigurationEndpoint(endpoint);

        Set<ConstraintViolation<InterceptorRunnerDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    void testInterceptorRunnerDto_InvalidCompletionEndpoint() {
        InterceptorRunnerDto dto = new InterceptorRunnerDto();
        dto.setName("test-runner");
        dto.setDisplayName("Test Runner");
        dto.setCompletionEndpoint("test-invalid-input");
        dto.setConfigurationEndpoint("http://example.com");

        Set<ConstraintViolation<InterceptorRunnerDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("completionEndpoint")))
                .isTrue();
    }

    @Test
    void testInterceptorRunnerDto_InvalidConfigurationEndpoint() {
        InterceptorRunnerDto dto = new InterceptorRunnerDto();
        dto.setName("test-runner");
        dto.setDisplayName("Test Runner");
        dto.setCompletionEndpoint("http://example.com");
        dto.setConfigurationEndpoint("http:/invalid-url");

        Set<ConstraintViolation<InterceptorRunnerDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("configurationEndpoint")))
                .isTrue();
    }

    @ParameterizedTest
    @MethodSource("validEndpointsIncludingEmpty")
    void testApplicationDto_ValidResponsesEndpoint(String responsesEndpoint) {
        ApplicationDto dto = new ApplicationDto();
        dto.setName("test-app");
        dto.setDisplayName("Test App");
        dto.setResponsesEndpoint(responsesEndpoint);

        Set<ConstraintViolation<ApplicationDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEndpoints")
    void testApplicationDto_InvalidResponsesEndpoint(String responsesEndpoint) {
        ApplicationDto dto = new ApplicationDto();
        dto.setName("test-app");
        dto.setDisplayName("Test App");
        dto.setResponsesEndpoint(responsesEndpoint);

        Set<ConstraintViolation<ApplicationDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("responsesEndpoint")))
                .isTrue();
    }

    @Test
    void testApplicationDto_BothEndpointsNull() {
        ApplicationDto dto = new ApplicationDto();
        dto.setName("test-app");
        dto.setDisplayName("Test App");

        Set<ConstraintViolation<ApplicationDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("validEndpointsIncludingEmpty")
    void testModelDto_ValidResponsesEndpoint(String responsesEndpoint) {
        ModelDto dto = new ModelDto();
        dto.setName("test-model");
        dto.setDisplayName("Test Model");
        dto.setResponsesEndpoint(responsesEndpoint);

        Set<ConstraintViolation<ModelDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEndpoints")
    void testModelDto_InvalidResponsesEndpoint(String responsesEndpoint) {
        ModelDto dto = new ModelDto();
        dto.setName("test-model");
        dto.setDisplayName("Test Model");
        dto.setResponsesEndpoint(responsesEndpoint);

        Set<ConstraintViolation<ModelDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("responsesEndpoint")))
                .isTrue();
    }

    @Test
    void testModelDto_BothEndpointsNull() {
        ModelDto dto = new ModelDto();
        dto.setName("test-model");
        dto.setDisplayName("Test Model");

        Set<ConstraintViolation<ModelDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    // Upstream endpoint is validated by @UpstreamEndpoint, whose @Pattern rejects an empty string,
    // so only non-empty endpoints are valid here.
    @ParameterizedTest
    @MethodSource("validEndpoints")
    void testUpstreamDto_ValidEndpoint(String endpoint) {
        UpstreamDto dto = new UpstreamDto();
        dto.setEndpoint(endpoint);

        Set<ConstraintViolation<UpstreamDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("validEndpointsIncludingEmpty")
    void testUpstreamDto_ValidResponsesEndpoint(String responsesEndpoint) {
        UpstreamDto dto = new UpstreamDto();
        dto.setEndpoint("http://example.com");
        dto.setResponsesEndpoint(responsesEndpoint);

        Set<ConstraintViolation<UpstreamDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEndpoints")
    void testUpstreamDto_InvalidResponsesEndpoint(String responsesEndpoint) {
        UpstreamDto dto = new UpstreamDto();
        dto.setEndpoint("http://example.com");
        dto.setResponsesEndpoint(responsesEndpoint);

        Set<ConstraintViolation<UpstreamDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("responsesEndpoint")))
                .isTrue();
    }

    @Test
    void testUpstreamDto_BothEndpointsNull() {
        UpstreamDto dto = new UpstreamDto();

        Set<ConstraintViolation<UpstreamDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations).isEmpty();
    }

    // Common valid endpoints (without empty string)
    private static Stream<Arguments> validEndpoints() {
        return Stream.of(
                Arguments.of("http://example.com"),
                Arguments.of("https://example.com"),
                Arguments.of("http://localhost:8080"),
                Arguments.of("http://127.0.0.1:8080"),
                Arguments.of("http://ai-test:50/"),
                Arguments.of("http://ai-test:50/api"),
                Arguments.of("http://ai-test/api"),
                Arguments.of("http://sub.example.local"),
                Arguments.of("http://example.dial-dev")
        );
    }

    // empty value validation added to tests by current state. it makes sense to add empty value validation for other entities too in separate PR
    // Valid endpoints including empty string (for all entities except adapter)
    private static Stream<Arguments> validEndpointsIncludingEmpty() {
        return Stream.concat(
                validEndpoints(),
                Stream.of(Arguments.of(""))
        );
    }

    // Common invalid endpoints (without empty string)
    private static Stream<Arguments> invalidEndpoints() {
        return Stream.of(
                Arguments.of("test-invalid-input"),
                Arguments.of("http:/invalid-url"),
                Arguments.of("ftp://example.com"),
                Arguments.of("example.com"),
                Arguments.of("http://"),
                Arguments.of("http://example.com:invalidport"),
                Arguments.of("http://example.local:999999")
        );
    }
}

