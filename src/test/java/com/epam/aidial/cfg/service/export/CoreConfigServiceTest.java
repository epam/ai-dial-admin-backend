package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.client.CoreConfigClient;
import feign.FeignException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoreConfigServiceTest {

    private CoreConfigService coreConfigService;
    private ConfigExportScheduler configExportScheduler;
    private CoreConfigClient coreConfigClient;

    @BeforeEach
    void init() {
        coreConfigClient = mock(CoreConfigClient.class);
        configExportScheduler = mock(ConfigExportScheduler.class);
        coreConfigService = new CoreConfigService(coreConfigClient, configExportScheduler, 5000);
    }

    @Test
    void reloadConfig_Success() throws Exception {
        // given
        // when
        coreConfigService.reloadConfig();
        // then
        verify(configExportScheduler).exportCurrentConfig();
        verify(coreConfigClient).reload();
    }

    @Test
    void reloadConfig_Exception() {
        // given
        var exception = mock(FeignException.Unauthorized.class);
        when(coreConfigClient.reload()).thenThrow(exception);
        // when
        Assertions.assertThatThrownBy(() -> coreConfigService.reloadConfig())
                .isInstanceOf(FeignException.Unauthorized.class);
        // then
        verify(configExportScheduler).exportCurrentConfig();
        verify(coreConfigClient).reload();
    }
}