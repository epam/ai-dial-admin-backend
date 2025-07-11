package com.epam.aidial.cfg.service.transfer;

import com.epam.aidial.cfg.client.AnonymousCoreConfigClient;
import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreConfigVersionAutoDetectServiceTest {

    private static final String CORE_VERSION = "2.0.0";
    private static final String TARGET_VERSION = "1.0.0";
    private static final long CACHE_EXPIRATION_MS = 300000;

    @Mock
    private AnonymousCoreConfigClient coreConfigClient;
    @Mock
    private CoreConfigVersionProperties properties;

    private CoreConfigVersionAutoDetectService service;

    @BeforeEach
    void setUp() {
        when(properties.getCacheExpirationMs()).thenReturn(CACHE_EXPIRATION_MS);
        service = new CoreConfigVersionAutoDetectService(coreConfigClient, properties);
    }

    @Test
    void getVersion_autoDetectDisabled_returnsTargetVersion() {
        // Given
        when(properties.isAutoDetectEnabled()).thenReturn(false);
        when(properties.getTarget()).thenReturn(TARGET_VERSION);

        // When
        String result = service.getVersion();

        // Then
        assertEquals(TARGET_VERSION, result);
        verify(properties).isAutoDetectEnabled();
        verify(properties).getTarget();
        verifyNoMoreInteractions(coreConfigClient);
    }

    @Test
    void getVersion_successfulApiCall_returnsCoreVersion() {
        // Given
        when(properties.isAutoDetectEnabled()).thenReturn(true);
        when(coreConfigClient.getVersion()).thenReturn(CORE_VERSION);

        // When
        String result = service.getVersion();

        // Then
        assertEquals(CORE_VERSION, result);
        verify(properties).isAutoDetectEnabled();
        verify(coreConfigClient).getVersion();
    }

    @Test
    void getVersion_cachedVersion_returnsCachedVersionWithoutApiCall() {
        // Given
        when(properties.isAutoDetectEnabled()).thenReturn(true);
        when(coreConfigClient.getVersion()).thenReturn(CORE_VERSION);

        // When
        // First call to populate the cache
        service.getVersion();
        // Second call should use the cache
        String result = service.getVersion();

        // Then
        assertEquals(CORE_VERSION, result);
        // Verify getVersion was called only once
        verify(coreConfigClient, times(1)).getVersion();
    }
}