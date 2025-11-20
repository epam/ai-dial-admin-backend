package com.epam.aidial.cfg.service.config.export;

import com.epam.aidial.cfg.client.CoreConfigClient;
import feign.FeignException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoreConfigReloadServiceTest {

    private CoreConfigReloadService coreConfigReloadService;
    private ConfigExportFacade configExportFacade;
    private CoreConfigClient coreConfigClient;

    @BeforeEach
    void init() {
        coreConfigClient = mock(CoreConfigClient.class);
        configExportFacade = mock(ConfigExportFacade.class);
        coreConfigReloadService = new CoreConfigReloadService(coreConfigClient, configExportFacade, 5000);
    }

    @Test
    void reloadConfig_Success() throws Exception {
        // given
        // when
        coreConfigReloadService.reloadConfig();
        // then
        verify(configExportFacade).exportCurrentConfig();
        verify(coreConfigClient).reload();
    }

    @Test
    void reloadConfig_Exception() {
        // given
        var exception = mock(FeignException.Unauthorized.class);
        when(coreConfigClient.reload()).thenThrow(exception);
        // when
        Assertions.assertThatThrownBy(() -> coreConfigReloadService.reloadConfig())
                .isInstanceOf(FeignException.Unauthorized.class);
        // then
        verify(configExportFacade).exportCurrentConfig();
        verify(coreConfigClient).reload();
    }
}