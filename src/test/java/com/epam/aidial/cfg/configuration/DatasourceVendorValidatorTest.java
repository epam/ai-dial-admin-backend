package com.epam.aidial.cfg.configuration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatasourceVendorValidatorTest {

    @ParameterizedTest
    @CsvSource({"H2", "POSTGRES", "MS_SQL_SERVER"})
    void whenVendorIsValid_thenValidationSucceeds(String vendor) {
        DatasourceVendorValidator properties = new DatasourceVendorValidator();
        ApplicationEnvironmentPreparedEvent event = createEventWithVendor(vendor);

        assertThatCode(() -> properties.onApplicationEvent(event))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("invalidVendors")
    void whenVendorIsInvalid_thenValidationFails(String vendor, String expectedMessagePart) {
        DatasourceVendorValidator properties = new DatasourceVendorValidator();
        ApplicationEnvironmentPreparedEvent event = createEventWithVendor(vendor);

        assertThatThrownBy(() -> properties.onApplicationEvent(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(expectedMessagePart)
                .hasMessageContaining("Valid values are");
    }

    private static Stream<Arguments> invalidVendors() {
        return Stream.of(
                Arguments.of("INVALID", "Invalid datasource.vendor value: 'INVALID'"),
                Arguments.of("mysql", "Invalid datasource.vendor value: 'mysql'"),
                Arguments.of("postgres", "Invalid datasource.vendor value: 'postgres'"),
                Arguments.of("h2", "Invalid datasource.vendor value: 'h2'"),
                Arguments.of("", "Undefined datasource.vendor value"),
                Arguments.of("   ", "Undefined datasource.vendor value"),
                Arguments.of(null, "Undefined datasource.vendor value")
        );
    }

    private ApplicationEnvironmentPreparedEvent createEventWithVendor(String vendor) {
        ConfigurableEnvironment environment = new StandardEnvironment();
        
        Map<String, Object> properties = new HashMap<>();
        if (vendor != null) {
            properties.put("datasource.vendor", vendor);
        }
        environment.getPropertySources().addFirst(new MapPropertySource("test", properties));
        
        ApplicationEnvironmentPreparedEvent event = mock(ApplicationEnvironmentPreparedEvent.class);
        when(event.getEnvironment()).thenReturn(environment);
        
        return event;
    }
}

