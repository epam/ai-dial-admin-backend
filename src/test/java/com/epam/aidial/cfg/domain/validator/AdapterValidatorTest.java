package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AdapterValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";

    @Mock
    private IdFieldValidator idFieldValidator;
    @Mock
    private DisplayFieldsValidator displayFieldsValidator;
    @Mock
    private DeploymentManagerService deploymentManagerService;

    private AdapterValidator adapterValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapterValidator = new AdapterValidator(
                idFieldValidator,
                displayFieldsValidator,
                deploymentManagerService,
                new DeploymentInfoValidator(),
                null
        );
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenIdFieldValidatorThrows() {
        // given
        Adapter adapter = new Adapter();
        adapter.setName("adapter_name");

        doThrow(IllegalArgumentException.class).when(idFieldValidator)
                .validateName("Adapter", "adapter_name");

        // when/then
        Assertions.assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid-name", "valid_name", "ValidName123", "name-123_456", "name.with.dots"})
    void validateCreation_shouldNotThrowExceptionForValidName(String name) {
        // given
        ReflectionTestUtils.setField(adapterValidator, "adapterNameValidationPattern", NAME_VALIDATION_PATTERN);

        Adapter adapter = new Adapter();
        adapter.setName(name);
        adapter.setBaseEndpoint("https://base-endpoint.com");

        // when/then
        assertThatNoException().isThrownBy(() -> adapterValidator.validateCreation(adapter));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid name with spaces", "invalid@name", "invalid#name", "invalid$name",
            "name-that-is-way-too-long-for-validation-pattern"})
    void validateCreation_shouldThrowExceptionForInvalidName(String name) {
        // given
        ReflectionTestUtils.setField(adapterValidator, "adapterNameValidationPattern", NAME_VALIDATION_PATTERN);

        Adapter adapter = new Adapter();
        adapter.setName(name);

        // when/then
        assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenDisplayFieldsValidatorThrows() {
        // given
        Adapter adapter = new Adapter();
        adapter.setName("adapter_name");
        adapter.setDisplayName("adapter_display_name");

        doThrow(IllegalArgumentException.class).when(displayFieldsValidator)
                .validateDisplayName("adapter_display_name", "Adapter", "adapter_name");

        // when/then
        Assertions.assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void validateCreation_shouldThrowExceptionWhenBaseEndpointIsBlank(String baseEndpoint) {
        // given
        Adapter adapter = new Adapter();
        adapter.setName("adapter_name");
        adapter.setDisplayName("adapter_display_name");
        adapter.setBaseEndpoint(baseEndpoint);

        // when/then
        Assertions.assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Blank adapter base endpoint. Adapter: adapter_name");
    }

    @ParameterizedTest
    @CsvSource({
            "http://invalid-url=$",
            "http://invalid-url/==",
            "adapter123"
    })
    void validateCreation_shouldThrowExceptionWhenBaseEndpointIsInvalid(String baseEndpoint) {
        // given
        Adapter adapter = new Adapter();
        adapter.setName("adapter_name");
        adapter.setDisplayName("adapter_display_name");
        adapter.setBaseEndpoint(baseEndpoint);

        // when/then
        Assertions.assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid adapter base endpoint: '%s'. Adapter: adapter_name".formatted(baseEndpoint));
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenAdapterNameIsUpdated() {
        Adapter adapter = new Adapter();
        adapter.setName("new_adapter_name");

        assertThatThrownBy(() -> adapterValidator.validateUpdate("adapter_name", adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Adapter with name: 'adapter_name' can not be renamed. New adapter name: 'new_adapter_name'");
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenDisplayFieldsValidatorThrows() {
        // given
        Adapter adapter = new Adapter();
        adapter.setName("adapter_name");
        adapter.setDisplayName("adapter_display_name");

        doThrow(IllegalArgumentException.class).when(displayFieldsValidator)
                .validateDisplayName("adapter_display_name", "Adapter", "adapter_name");

        // when/then
        Assertions.assertThatThrownBy(() -> adapterValidator.validateUpdate("adapter_name", adapter))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void validateUpdate_shouldThrowExceptionWhenBaseEndpointIsBlank(String baseEndpoint) {
        // given
        Adapter adapter = new Adapter();
        adapter.setName("adapter_name");
        adapter.setDisplayName("adapter_display_name");
        adapter.setBaseEndpoint(baseEndpoint);

        // when/then
        Assertions.assertThatThrownBy(() -> adapterValidator.validateUpdate("adapter_name", adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Blank adapter base endpoint. Adapter: adapter_name");
    }

    @ParameterizedTest
    @CsvSource({
            "http://invalid-url=$",
            "http://invalid-url/==",
            "adapter123"
    })
    void validateUpdate_shouldThrowExceptionWhenBaseEndpointIsInvalid(String baseEndpoint) {
        // given
        Adapter adapter = new Adapter();
        adapter.setName("adapter_name");
        adapter.setDisplayName("adapter_display_name");
        adapter.setBaseEndpoint(baseEndpoint);

        // when/then
        Assertions.assertThatThrownBy(() -> adapterValidator.validateUpdate("adapter_name", adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid adapter base endpoint: '%s'. Adapter: adapter_name".formatted(baseEndpoint));
    }

}