package com.epam.aidial.cfg.dao.validator;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.validator.KeyValidator;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyValidatorTest {

    @Mock
    private TransactionTimestampContext transactionTimestampContext;

    @InjectMocks
    private KeyValidator keyValidator;

    @ParameterizedTest
    @CsvSource({"1", "2"})
    void validateCreation_shouldThrowExceptionWhenExpiresAtIsNotGreaterThanTxTimestamp(long expiresAt) {
        when(transactionTimestampContext.getTimestamp()).thenReturn(2L);

        Key key = new Key();
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
        key.setExpiresAt(expiresAt);

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setCreatedAt(2);

        assertThatThrownBy(() -> keyValidator.validateUpdate("key_name", key, keyEntity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Key expiresAt ms: '" + expiresAt + "' should be greater than key createdAt ms: '2'");
    }

    @ParameterizedTest
    @CsvSource(value = {"3", "null"}, nullValues = "null")
    void validateUpdate_shouldDoNothingWhenKeyNameIsNotUpdatedAndExpiresAtIsGreaterThanKeyCreatedAtOrNull(Long expiresAt) {
        Key key = new Key();
        key.setName("key_name");
        key.setExpiresAt(expiresAt);

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setCreatedAt(2);

        assertThatNoException().isThrownBy(() -> keyValidator.validateUpdate("key_name", key, keyEntity));
    }

}