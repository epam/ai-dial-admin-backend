package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.cfg.client.AnonymousCoreConfigClient;
import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void getVersion_autoDetectDisabled_throwsExceptionIfTargetVersionIsBlank(String version) {
        // Given
        when(properties.isAutoDetectEnabled()).thenReturn(false);
        when(properties.getTarget()).thenReturn(version);

        // When
        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.getVersion()
        );

        // Then
        assertEquals(
                "Core version auto-detection is disabled and Core target version is undefined",
                exception.getMessage()
        );
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
    void getVersion_successfulApiCall_returnsCoreVersionWithSuffix() {
        // Given
        when(properties.isAutoDetectEnabled()).thenReturn(true);
        when(coreConfigClient.getVersion()).thenReturn(CORE_VERSION + "-rc");

        // When
        String result = service.getVersion();

        // Then
        assertEquals(CORE_VERSION, result);
        verify(properties).isAutoDetectEnabled();
        verify(coreConfigClient).getVersion();
    }

    @Test
    void getVersion_unsuccessfulApiCall_returnsTargetVersion() {
        // Given
        when(properties.isAutoDetectEnabled()).thenReturn(true);
        when(properties.getTarget()).thenReturn(TARGET_VERSION);
        when(coreConfigClient.getVersion()).thenThrow(new RuntimeException());

        // When
        String result = service.getVersion();

        // Then
        assertEquals(TARGET_VERSION, result);
        verify(properties).isAutoDetectEnabled();
        verify(coreConfigClient).getVersion();
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void getVersion_unsuccessfulApiCall_throwsExceptionIfTargetVersionIsBlank(String version) {
        // Given
        when(properties.isAutoDetectEnabled()).thenReturn(true);
        when(properties.getTarget()).thenReturn(version);
        when(coreConfigClient.getVersion()).thenThrow(new RuntimeException());

        // When
        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.getVersion()
        );

        // Then
        assertEquals(
                "Unable to retrieve Core version and Core target version is undefined",
                exception.getMessage()
        );
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