package com.epam.aidial.cfg.domain.validator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CidrValidatorTest {
    private final CidrValidator validator = new CidrValidator();

    @ParameterizedTest
    @ValueSource(strings = {"198.51.100.14/24", "2002::1234:abcd:ffff:c0a8:101/64"})
    void validate_shouldAcceptValidCidr(String cidr) {
        assertThatNoException().isThrownBy(() -> validator.validate(cidr));
    }

    @ParameterizedTest
    @ValueSource(strings = {"198.51.100.14/33", "198.51.100.14/-1", "2001:db8::/129"})
    void validate_shouldFailWhenInvalidPrefix(String cidr) {
        assertThatThrownBy(() -> validator.validate(cidr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid prefix length");
    }

    @ParameterizedTest
    @ValueSource(strings = {"198.51.100.1444/24", "2002::1234::1011/64"})
    void validate_shouldFailWhenIpIsInvalid(String cidr) {
        assertThatThrownBy(() -> validator.validate(cidr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No such host is known");
    }
}