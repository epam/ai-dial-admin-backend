package com.epam.aidial.cfg.dao.resolver.key;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.resolver.key.KeyGeneratedAtResolver;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyGeneratedAtResolverTest {

    @Mock
    private TransactionTimestampContext transactionTimestampContext;

    @InjectMocks
    private KeyGeneratedAtResolver keyGeneratedAtResolver;

    @Test
    void resolveKeyGeneratedAtValueDuringCreation_shouldReturnTxTimestamp() {
        // given
        when(transactionTimestampContext.getTimestamp()).thenReturn(1L);

        // when
        long actual = keyGeneratedAtResolver.resolveKeyGeneratedAtValueDuringCreation();

        // then
        assertThat(actual).isEqualTo(1L);
    }

    @Test
    void resolveKeyGeneratedAtValueDuringUpdate_shouldReturnTxTimestampWhenKeyValueIsUpdated() {
        // given
        Key key = new Key();
        key.setKey("new keyValue");

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setKey("keyValue");

        when(transactionTimestampContext.getTimestamp()).thenReturn(1L);

        // when
        long actual = keyGeneratedAtResolver.resolveKeyGeneratedAtValueDuringUpdate(key, keyEntity);

        // then
        assertThat(actual).isEqualTo(1L);
    }

    @Test
    void resolveKeyGeneratedAtValueDuringUpdate_shouldReturnKeyEntityKeyGeneratedAtValueWhenKeyValueIsNotUpdated() {
        // given
        Key key = new Key();
        key.setKey("keyValue");

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setKey("keyValue");
        keyEntity.setKeyGeneratedAt(1L);

        // when
        long actual = keyGeneratedAtResolver.resolveKeyGeneratedAtValueDuringUpdate(key, keyEntity);

        // then
        assertThat(actual).isEqualTo(1L);
    }
}