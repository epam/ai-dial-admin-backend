package com.epam.aidial.cfg.configuration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatasourceVendorPropertiesTest {

    @ParameterizedTest
    @CsvSource({"H2", "POSTGRES", "MS_SQL_SERVER"})
    void whenVendorIsValid_thenValidationSucceeds(String vendor) throws Exception {
        DatasourceVendorProperties properties = new DatasourceVendorProperties();
        ReflectionTestUtils.setField(properties, "vendor", vendor);

        assertThatCode(() -> properties.validateDatasourceVendor())
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("invalidVendors")
    void whenVendorIsInvalid_thenValidationFails(String vendor, String expectedMessagePart) throws Exception {
        DatasourceVendorProperties properties = new DatasourceVendorProperties();
        ReflectionTestUtils.setField(properties, "vendor", vendor);

        assertThatThrownBy(() -> properties.validateDatasourceVendor())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(expectedMessagePart)
                .hasMessageContaining("Valid values are: "); //valid values order is not guaranteed
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
}

