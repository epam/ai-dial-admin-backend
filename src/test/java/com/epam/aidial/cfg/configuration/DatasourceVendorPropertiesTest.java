package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.exception.InvalidDatasourceVendorException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatasourceVendorPropertiesTest {

    private static Stream<Arguments> validVendors() {
        return Stream.of(
                Arguments.of("H2"),
                Arguments.of("POSTGRES"),
                Arguments.of("MS_SQL_SERVER")
        );
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

    @ParameterizedTest
    @MethodSource("validVendors")
    void whenVendorIsValid_thenValidationSucceeds(String vendor) throws Exception {
        DatasourceVendorProperties properties = new DatasourceVendorProperties();
        setVendorField(properties, vendor);

        assertThatCode(() -> properties.validateDatasourceVendor())
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("invalidVendors")
    void whenVendorIsInvalid_thenValidationFails(String vendor, String expectedMessagePart) throws Exception {
        DatasourceVendorProperties properties = new DatasourceVendorProperties();
        setVendorField(properties, vendor);

        assertThatThrownBy(() -> properties.validateDatasourceVendor())
                .isInstanceOf(InvalidDatasourceVendorException.class)
                .hasMessageContaining(expectedMessagePart)
                .hasMessageContaining("Valid values are: H2, POSTGRES, MS_SQL_SERVER");
    }

    private void setVendorField(DatasourceVendorProperties properties, String value) throws Exception {
        Field vendorField = DatasourceVendorProperties.class.getDeclaredField("vendor");
        vendorField.setAccessible(true);
        vendorField.set(properties, value);
    }
}

