package com.epam.aidial.cfg.service.config.transfer.version;

import com.epam.aidial.cfg.client.AnonymousCoreConfigClient;
import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreConfigVersionAutoDetectServiceTest {

    private static final String CORE_VERSION = "2.0.0";

    @Mock
    private AnonymousCoreConfigClient coreConfigClient;
    @Mock
    private CoreConfigVersionProperties properties;

    private CoreConfigVersionAutoDetectService service;

    @BeforeEach
    void setUp() {
        when(properties.getCacheExpirationMs()).thenReturn(300000L);
        service = new CoreConfigVersionAutoDetectService(coreConfigClient, properties);
    }

    @Test
    void getVersion_autoDetectDisabled_returnsNull() {
        // given
        when(properties.isAutoDetectEnabled()).thenReturn(false);

        // when
        String result = service.getVersion();

        // then
        assertNull(result);
        verifyNoMoreInteractions(coreConfigClient);
    }

    @Test
    void getVersion_successfulApiCall_returnsCoreVersion() {
        // given
        when(properties.isAutoDetectEnabled()).thenReturn(true);
        when(coreConfigClient.getVersion()).thenReturn(CORE_VERSION);

        // when
        String result = service.getVersion();

        // then
        assertEquals(CORE_VERSION, result);
    }

    @Test
    void getVersion_successfulApiCallReturnsSuffixedVersion_returnsCoreVersion() {
        // given
        when(properties.isAutoDetectEnabled()).thenReturn(true);
        when(coreConfigClient.getVersion()).thenReturn(CORE_VERSION + "-rc");

        // when
        String result = service.getVersion();

        // then
        assertEquals(CORE_VERSION, result);
    }

    @Test
    void getVersion_unsuccessfulApiCall_returnsAutoDetectFailedVersion() {
        // given
        when(properties.isAutoDetectEnabled()).thenReturn(true);
        when(coreConfigClient.getVersion()).thenThrow(new RuntimeException());

        // when
        String result = service.getVersion();

        // then
        assertEquals("-1", result);
    }

    @Test
    void getVersion_cachedVersion_returnsCachedVersionWithoutApiCall() {
        // given
        when(properties.isAutoDetectEnabled()).thenReturn(true);
        when(coreConfigClient.getVersion()).thenReturn(CORE_VERSION);

        // when
        // First call to populate the cache
        service.getVersion();
        // Second call should use the cache
        String result = service.getVersion();

        // then
        assertEquals(CORE_VERSION, result);
        // Verify getVersion was called only once
        verify(coreConfigClient, times(1)).getVersion();
    }

}