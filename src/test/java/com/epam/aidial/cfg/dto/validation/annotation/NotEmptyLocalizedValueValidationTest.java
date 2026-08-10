package com.epam.aidial.cfg.dto.validation.annotation;

import com.epam.aidial.cfg.domain.value.LocalizedValue;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

class NotEmptyLocalizedValueValidationTest {

    private Validator validator;

    @BeforeEach
    void init() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @ParameterizedTest
    @MethodSource("validDisplayNames")
    void testApplicationDto_ValidDisplayName(LocalizedValue displayName) {
        ApplicationDto dto = new ApplicationDto();
        dto.setName("test-app");
        dto.setDisplayName(displayName);

        Set<ConstraintViolation<ApplicationDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("displayName")))
                .isFalse();
    }

    @ParameterizedTest
    @MethodSource("invalidDisplayNames")
    void testApplicationDto_InvalidDisplayName(LocalizedValue displayName) {
        ApplicationDto dto = new ApplicationDto();
        dto.setName("test-app");
        dto.setDisplayName(displayName);

        Set<ConstraintViolation<ApplicationDto>> violations = validator.validate(dto);
        assertDisplayNameViolation(violations);
    }

    @ParameterizedTest
    @MethodSource("validDisplayNames")
    void testModelDto_ValidDisplayName(LocalizedValue displayName) {
        ModelDto dto = new ModelDto();
        dto.setName("test-model");
        dto.setDisplayName(displayName);

        Set<ConstraintViolation<ModelDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("displayName")))
                .isFalse();
    }

    @ParameterizedTest
    @MethodSource("invalidDisplayNames")
    void testModelDto_InvalidDisplayName(LocalizedValue displayName) {
        ModelDto dto = new ModelDto();
        dto.setName("test-model");
        dto.setDisplayName(displayName);

        Set<ConstraintViolation<ModelDto>> violations = validator.validate(dto);
        assertDisplayNameViolation(violations);
    }

    @ParameterizedTest
    @MethodSource("validDisplayNames")
    void testToolSetDto_ValidDisplayName(LocalizedValue displayName) {
        ToolSetDto dto = new ToolSetDto();
        dto.setName("test-toolset");
        dto.setTransport(ToolSetDto.TransportDto.HTTP);
        dto.setDisplayName(displayName);

        Set<ConstraintViolation<ToolSetDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("displayName")))
                .isFalse();
    }

    @ParameterizedTest
    @MethodSource("invalidDisplayNames")
    void testToolSetDto_InvalidDisplayName(LocalizedValue displayName) {
        ToolSetDto dto = new ToolSetDto();
        dto.setName("test-toolset");
        dto.setTransport(ToolSetDto.TransportDto.HTTP);
        dto.setDisplayName(displayName);

        Set<ConstraintViolation<ToolSetDto>> violations = validator.validate(dto);
        assertDisplayNameViolation(violations);
    }

    @ParameterizedTest
    @MethodSource("validDisplayNames")
    void testInterceptorDto_ValidDisplayName(LocalizedValue displayName) {
        InterceptorDto dto = new InterceptorDto();
        dto.setName("test-interceptor");
        dto.setDisplayName(displayName);

        Set<ConstraintViolation<InterceptorDto>> violations = validator.validate(dto);
        Assertions.assertThat(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("displayName")))
                .isFalse();
    }

    @ParameterizedTest
    @MethodSource("invalidDisplayNames")
    void testInterceptorDto_InvalidDisplayName(LocalizedValue displayName) {
        InterceptorDto dto = new InterceptorDto();
        dto.setName("test-interceptor");
        dto.setDisplayName(displayName);

        Set<ConstraintViolation<InterceptorDto>> violations = validator.validate(dto);
        assertDisplayNameViolation(violations);
    }

    private static <T> void assertDisplayNameViolation(Set<ConstraintViolation<T>> violations) {
        Assertions.assertThat(violations).isNotEmpty();
        Assertions.assertThat(violations.stream()
                        .filter(v -> v.getPropertyPath().toString().equals("displayName"))
                        .findFirst())
                .isPresent()
                .get()
                .extracting(ConstraintViolation::getMessage)
                .isEqualTo("DisplayName is required");
    }

    private static Stream<Arguments> validDisplayNames() {
        return Stream.of(
                Arguments.of(LocalizedValue.of("Test Display Name")),
                Arguments.of(LocalizedValue.of(Map.of("en", "Test Display Name"))),
                Arguments.of(LocalizedValue.of(Map.of("en", " ", "fr", "Nom de test")))
        );
    }

    private static Stream<Arguments> invalidDisplayNames() {
        return Stream.of(
                Arguments.of((LocalizedValue) null),
                Arguments.of(LocalizedValue.of("")),
                Arguments.of(LocalizedValue.of(" ")),
                Arguments.of(LocalizedValue.of(Map.of())),
                Arguments.of(LocalizedValue.of(Map.of("en", ""))),
                Arguments.of(LocalizedValue.of(Map.of("en", " ", "fr", "")))
        );
    }
}
