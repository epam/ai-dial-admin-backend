package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.source.AdapterContainerSource;
import com.epam.aidial.cfg.domain.model.source.AdapterEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AdapterValidatorTest {

    private static final String ADAPTER_NAME = "adapter_name";
    private static final String DISPLAY_NAME = "adapter_display_name";
    private static final String VALID_BASE_ENDPOINT = "https://base-endpoint.com";
    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";
    private static final String CONTAINER_ID = "container-1";

    @Mock
    private IdFieldValidator idFieldValidator;
    @Mock
    private DisplayFieldsValidator displayFieldsValidator;

    private AdapterValidator adapterValidator;

    @BeforeEach
    void setUp() {
        adapterValidator = new AdapterValidator(
                idFieldValidator,
                displayFieldsValidator,
                null
        );
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenIdFieldValidatorThrows() {
        Adapter adapter = createMinimalAdapter();
        adapter.setName("adapter_name");
        doThrow(IllegalArgumentException.class).when(idFieldValidator)
                .validateName("Adapter", "adapter_name");

        assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid-name", "valid_name", "ValidName123", "name-123_456", "name.with.dots"})
    void validateCreation_shouldNotThrowExceptionForValidName(String name) {
        ReflectionTestUtils.setField(adapterValidator, "adapterNameValidationPattern", NAME_VALIDATION_PATTERN);
        Adapter adapter = createMinimalAdapter();
        adapter.setName(name);
        adapter.setBaseEndpoint(VALID_BASE_ENDPOINT);

        assertThatNoException().isThrownBy(() -> adapterValidator.validateCreation(adapter));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid name with spaces", "invalid@name", "invalid#name", "invalid$name",
            "name-that-is-way-too-long-for-validation-pattern"})
    void validateCreation_shouldThrowExceptionForInvalidName(String name) {
        ReflectionTestUtils.setField(adapterValidator, "adapterNameValidationPattern", NAME_VALIDATION_PATTERN);
        Adapter adapter = createMinimalAdapter();
        adapter.setName(name);

        assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenDisplayFieldsValidatorThrows() {
        Adapter adapter = createMinimalAdapter();
        adapter.setDisplayName(DISPLAY_NAME);
        doThrow(IllegalArgumentException.class).when(displayFieldsValidator)
                .validateDisplayName(DISPLAY_NAME, "Adapter", ADAPTER_NAME);

        assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenSourceIsNullAndBaseEndpointIsInvalid() {
        Adapter adapter = createMinimalAdapter();
        adapter.setBaseEndpoint("http://invalid-url=$");

        assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid base endpoint: 'http://invalid-url=$'. Adapter: " + ADAPTER_NAME);
    }

    @Test
    void validateCreation_shouldNotThrowWhenSourceIsNullAndBaseEndpointIsValid() {
        Adapter adapter = createMinimalAdapter();
        adapter.setBaseEndpoint(VALID_BASE_ENDPOINT);

        assertThatNoException().isThrownBy(() -> adapterValidator.validateCreation(adapter));
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenEndpointsSourceAndBaseEndpointIsNull() {
        Adapter adapter = createMinimalAdapter();
        adapter.setSource(new AdapterEndpointsSource());
        adapter.setBaseEndpoint(null);

        assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Base endpoint is required when source type is 'Adapter endpoints'. Adapter: " + ADAPTER_NAME);
    }

    @ParameterizedTest
    @CsvSource({
            "http://invalid-url=$",
            "http://invalid-url/==",
            "adapter123"
    })
    void validateCreation_shouldThrowExceptionWhenEndpointsSourceAndBaseEndpointIsInvalid(String baseEndpoint) {
        Adapter adapter = createMinimalAdapter();
        adapter.setSource(new AdapterEndpointsSource());
        adapter.setBaseEndpoint(baseEndpoint);

        assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid base endpoint: '%s'. Adapter: %s".formatted(baseEndpoint, ADAPTER_NAME));
    }

    @Test
    void validateCreation_shouldNotThrowWhenEndpointsSourceAndBaseEndpointIsValid() {
        Adapter adapter = createMinimalAdapter();
        adapter.setSource(new AdapterEndpointsSource());
        adapter.setBaseEndpoint(VALID_BASE_ENDPOINT);

        assertThatNoException().isThrownBy(() -> adapterValidator.validateCreation(adapter));
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenContainerSourceAndCompletionPathIsInvalid() {
        Adapter adapter = createMinimalAdapter();
        adapter.setSource(new AdapterContainerSource(CONTAINER_ID, null, "invalid path with spaces"));

        assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid endpoint path: 'invalid path with spaces'. Adapter: " + ADAPTER_NAME);
    }

    @Test
    void validateCreation_shouldNotThrowWhenContainerSourceIsValid() {
        Adapter adapter = createMinimalAdapter();
        adapter.setSource(new AdapterContainerSource(CONTAINER_ID, "Container", "/completions"));

        assertThatNoException().isThrownBy(() -> adapterValidator.validateCreation(adapter));
    }

    @Test
    void validateCreation_shouldNotThrowWhenContainerSourceAndCompletionPathIsNull() {
        Adapter adapter = createMinimalAdapter();
        adapter.setSource(new AdapterContainerSource(CONTAINER_ID, null, null));

        assertThatNoException().isThrownBy(() -> adapterValidator.validateCreation(adapter));
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenSourceTypeIsUnsupported() {
        Adapter adapter = createMinimalAdapter();
        adapter.setSource(new UnsupportedAdapterSource());

        assertThatThrownBy(() -> adapterValidator.validateCreation(adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported adapter source")
                .hasMessageContaining(ADAPTER_NAME);
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenAdapterNameIsUpdated() {
        Adapter adapter = createMinimalAdapter();
        adapter.setName("new_adapter_name");

        assertThatThrownBy(() -> adapterValidator.validateUpdate(ADAPTER_NAME, adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Adapter with name: '" + ADAPTER_NAME + "' can not be renamed. New adapter name: 'new_adapter_name'");
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenDisplayFieldsValidatorThrows() {
        Adapter adapter = createMinimalAdapter();
        doThrow(IllegalArgumentException.class).when(displayFieldsValidator)
                .validateDisplayName(DISPLAY_NAME, "Adapter", ADAPTER_NAME);

        assertThatThrownBy(() -> adapterValidator.validateUpdate(ADAPTER_NAME, adapter))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenEndpointsSourceAndBaseEndpointIsNull() {
        Adapter adapter = createMinimalAdapter();
        adapter.setSource(new AdapterEndpointsSource());
        adapter.setBaseEndpoint(null);

        assertThatThrownBy(() -> adapterValidator.validateUpdate(ADAPTER_NAME, adapter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Base endpoint is required when source type is 'Adapter endpoints'. Adapter: " + ADAPTER_NAME);
    }

    @Test
    void validateUpdate_shouldNotThrowWhenSourceIsNullAndBaseEndpointIsValid() {
        Adapter adapter = createMinimalAdapter();
        adapter.setBaseEndpoint(VALID_BASE_ENDPOINT);

        assertThatNoException().isThrownBy(() -> adapterValidator.validateUpdate(ADAPTER_NAME, adapter));
    }

    private static Adapter createMinimalAdapter() {
        Adapter adapter = new Adapter();
        adapter.setName(ADAPTER_NAME);
        adapter.setDisplayName(DISPLAY_NAME);
        return adapter;
    }

    // Test-only source type to trigger "Unsupported adapter source" path
    private static final class UnsupportedAdapterSource extends AdapterSource {
    }
}
