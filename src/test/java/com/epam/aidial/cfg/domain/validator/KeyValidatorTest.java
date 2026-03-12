package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";

    @Mock
    private TransactionTimestampContext transactionTimestampContext;
    @Mock
    private IdFieldValidator idFieldValidator;
    @Mock
    private DisplayFieldsValidator displayFieldsValidator;
    @Mock
    private CidrValidator cidrValidator;

    private KeyValidator keyValidator;

    @BeforeEach
    void setUp() {
        keyValidator = new KeyValidator(idFieldValidator, transactionTimestampContext, displayFieldsValidator, cidrValidator, null);
    }

    @ParameterizedTest
    @CsvSource({"1", "2"})
    void validateCreation_shouldThrowExceptionWhenExpiresAtIsNotGreaterThanTxTimestamp(long expiresAt) {
        when(transactionTimestampContext.getTimestamp()).thenReturn(2L);

        Key key = new Key();
        key.setName("key_name");
        key.setProject("project");
        key.setExpiresAt(expiresAt);

        assertThatThrownBy(() -> keyValidator.validateCreation(key))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Key expiresAt ms: '" + expiresAt + "' should be greater than current epoch ms: '2'");
    }

    @ParameterizedTest
    @CsvSource(value = {"3", "null"}, nullValues = "null")
    void validateCreation_shouldDoNothingWhenExpiresAtIsGreaterThanTxTimestampOrNull(Long expiresAt) {
        when(transactionTimestampContext.getTimestamp()).thenReturn(2L);

        Key key = new Key();
        key.setName("key_name");
        key.setProject("project");
        key.setExpiresAt(expiresAt);

        assertThatNoException().isThrownBy(() -> keyValidator.validateCreation(key));
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenKeyNameIsUpdated() {
        Key key = new Key();
        key.setName("new_key_name");

        assertThatThrownBy(() -> keyValidator.validateUpdate("key_name", key, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Key with name: 'key_name' can not be renamed. New key name: 'new_key_name'");
    }

    @ParameterizedTest
    @CsvSource({"1", "2"})
    void validateUpdate_shouldThrowExceptionWhenExpiresAtIsNotGreaterThanKeyCreatedAt(long expiresAt) {
        Key key = new Key();
        key.setName("key_name");
        key.setProject("project");
        key.setExpiresAt(expiresAt);

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setCreatedAt(2L);

        assertThatThrownBy(() -> keyValidator.validateUpdate("key_name", key, keyEntity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Key expiresAt ms: '" + expiresAt + "' should be greater than key createdAt ms: '2'");
    }

    @ParameterizedTest
    @CsvSource(value = {"3", "null"}, nullValues = "null")
    void validateUpdate_shouldDoNothingWhenKeyNameIsNotUpdatedAndExpiresAtIsGreaterThanKeyCreatedAtOrNull(Long expiresAt) {
        Key key = new Key();
        key.setName("key_name");
        key.setProject("project");
        key.setExpiresAt(expiresAt);

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setCreatedAt(2L);

        assertThatNoException().isThrownBy(() -> keyValidator.validateUpdate("key_name", key, keyEntity));
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid-name", "valid_name", "ValidName123", "name-123_456", "name.with.dots"})
    void validateCreation_shouldNotThrowExceptionForValidName(String name) {
        // given
        ReflectionTestUtils.setField(keyValidator, "keyNameValidationPattern", NAME_VALIDATION_PATTERN);
        when(transactionTimestampContext.getTimestamp()).thenReturn(2L);

        Key key = new Key();
        key.setName(name);
        key.setProject("project");
        key.setExpiresAt(3L);

        // when/then
        assertThatNoException().isThrownBy(() -> keyValidator.validateCreation(key));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid name with spaces", "invalid@name", "invalid#name", "invalid$name",
            "name-that-is-way-too-long-for-validation-pattern"})
    void validateCreation_shouldThrowExceptionForInvalidName(String name) {
        // given
        ReflectionTestUtils.setField(keyValidator, "keyNameValidationPattern", NAME_VALIDATION_PATTERN);

        Key key = new Key();
        key.setName(name);
        key.setExpiresAt(3L);

        // when/then
        assertThatThrownBy(() -> keyValidator.validateCreation(key))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenIdFieldValidatorThrows() {
        // given
        Key key = new Key();
        key.setName("key_name");

        doThrow(IllegalArgumentException.class).when(idFieldValidator)
                .validateName("Key", "key_name");

        // when/then
        Assertions.assertThatThrownBy(() -> keyValidator.validateCreation(key))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void validateCreation_shouldThrowExceptionWhenProjectIsBlank(String project) {
        // given
        Key key = new Key();
        key.setName("key_name");
        key.setProject(project);

        // when/then
        assertThatThrownBy(() -> keyValidator.validateCreation(key))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is required. Key name: key_name");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void validateUpdate_shouldThrowExceptionWhenProjectIsBlank(String project) {
        // given
        Key key = new Key();
        key.setName("key_name");
        key.setProject(project);

        KeyEntity keyEntity = new KeyEntity();

        // when/then
        assertThatThrownBy(() -> keyValidator.validateUpdate("key_name", key, keyEntity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is required. Key name: key_name");
    }

    @Test
    void validateCreation_shouldNotThrowExceptionForValidRanges() {
        // given
        Key key = new Key();
        key.setName("key_name");
        key.setProject("project");
        key.setExpiresAt(3L);
        key.setAllowedIpAddressRanges(List.of("198.51.100.14/24", "2002::1234:abcd:ffff:c0a8:101/64"));

        // when/then
        assertThatNoException().isThrownBy(() -> keyValidator.validateCreation(key));
    }

    @Test
    void validateCreation_shouldThrowExceptionForInvalidRanges() {
        // given
        Key key = new Key();
        key.setName("key_name");
        key.setProject("project");
        key.setExpiresAt(3L);
        key.setAllowedIpAddressRanges(List.of("198.51.10031.14/24", "2002::1234:abcd:ffff:c0a8:101"));

        doThrow(new IllegalArgumentException("Invalid CIDR")).when(cidrValidator).validate(anyString());
        // when/then
        assertThatThrownBy(() -> keyValidator.validateCreation(key))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid allowed IP address ranges.");
    }

    @Test
    void validateUpdate_shouldNotThrowExceptionForValidRanges() {
        // given
        Key key = new Key();
        key.setName("key_name");
        key.setProject("project");
        key.setExpiresAt(3L);
        key.setAllowedIpAddressRanges(List.of("198.51.100.14/24", "2002::1234:abcd:ffff:c0a8:101/64"));

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setCreatedAt(1L);
        // when/then
        assertThatNoException().isThrownBy(() -> keyValidator.validateUpdate(key.getName(), key, keyEntity));
    }

    @Test
    void validateUpdate_shouldThrowExceptionForInvalidRanges() {
        // given
        Key key = new Key();
        key.setName("key_name");
        key.setProject("project");
        key.setExpiresAt(3L);
        key.setAllowedIpAddressRanges(List.of("198.51.1002.14/24", "2002::1234:abcd:ffff:c0a8:101"));

        KeyEntity keyEntity = new KeyEntity();

        doThrow(new IllegalArgumentException("Invalid CIDR")).when(cidrValidator).validate(anyString());

        // when/then
        assertThatThrownBy(() -> keyValidator.validateUpdate(key.getName(), key, keyEntity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid allowed IP address ranges.");
    }
}