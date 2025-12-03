package com.epam.aidial.cfg.domain.resolver.key;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.ValidityStateEntity;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyValidityStateOnGetResolverTest {

    @Mock
    private TransactionTimestampContext transactionTimestampContext;

    @InjectMocks
    private KeyValidityStateOnGetResolver keyValidityStateOnGetResolver;

    @ParameterizedTest
    @CsvSource(value = {"null", "5"}, nullValues = "null")
    void resolveValidityStateEntity_returnExistingStateIfKeyIsNotExpired(Long keyExpiresAt) {
        // given
        ValidityStateEntity validityStateEntity = new ValidityStateEntity();
        validityStateEntity.setMessage("some reason");
        validityStateEntity.setValid(false);

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setValidityState(validityStateEntity);
        keyEntity.setExpiresAt(keyExpiresAt);

        when(transactionTimestampContext.getTimestamp()).thenReturn(3L);

        // when
        ValidityStateEntity actualValidityStateEntity = keyValidityStateOnGetResolver.resolveValidityStateEntity(keyEntity);

        // then
        assertThat(actualValidityStateEntity).isEqualTo(validityStateEntity);
    }

    @Test
    void resolveValidityStateEntity_returnExistingStateIfKeyIsExpiredAndHasValidState() {
        // given
        ValidityStateEntity validityStateEntity = new ValidityStateEntity();
        validityStateEntity.setValid(true);

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setValidityState(validityStateEntity);
        keyEntity.setExpiresAt(2L);

        ValidityStateEntity expectedValidityStateEntity = new ValidityStateEntity();
        expectedValidityStateEntity.setMessage("Key is expired");
        expectedValidityStateEntity.setValid(false);

        when(transactionTimestampContext.getTimestamp()).thenReturn(3L);

        // when
        ValidityStateEntity actualValidityStateEntity = keyValidityStateOnGetResolver.resolveValidityStateEntity(keyEntity);

        // then
        assertThat(actualValidityStateEntity).isEqualTo(expectedValidityStateEntity);
    }

    @Test
    void resolveValidityStateEntity_returnExistingStateIfKeyIsExpiredAndHasInvalidState() {
        // given
        ValidityStateEntity validityStateEntity = new ValidityStateEntity();
        validityStateEntity.setMessage("No roles assigned, Key value is missing");
        validityStateEntity.setValid(false);

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setValidityState(validityStateEntity);
        keyEntity.setExpiresAt(2L);

        ValidityStateEntity expectedValidityStateEntity = new ValidityStateEntity();
        expectedValidityStateEntity.setMessage("No roles assigned, Key value is missing, Key is expired");
        expectedValidityStateEntity.setValid(false);

        when(transactionTimestampContext.getTimestamp()).thenReturn(3L);

        // when
        ValidityStateEntity actualValidityStateEntity = keyValidityStateOnGetResolver.resolveValidityStateEntity(keyEntity);

        // then
        assertThat(actualValidityStateEntity).isEqualTo(expectedValidityStateEntity);
    }
}