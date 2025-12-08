package com.epam.aidial.cfg.service.config.transfer.version;

import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.domain.model.AdminSettings;
import com.epam.aidial.cfg.domain.service.AdminSettingsService;
import com.epam.aidial.cfg.model.CoreConfigVersions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreConfigVersionServiceTest {

    private static final String CORE_VERSION = "2.0.0";

    @Mock
    private AdminSettingsService adminSettingsService;
    @Mock
    private CoreConfigVersionAutoDetectService coreConfigVersionAutoDetectService;
    @Mock
    private CoreConfigVersionProperties coreConfigVersionProperties;

    @InjectMocks
    private CoreConfigVersionService service;

    @Test
    void getVersionForExport_returnsManuallySetVersion() {
        // given
        AdminSettings adminSettings = new AdminSettings();
        adminSettings.setCoreConfigVersion(CORE_VERSION);

        when(adminSettingsService.getAdminSettings()).thenReturn(adminSettings);

        // when
        String actual = service.getVersionForExport();

        // then
        assertThat(actual).isEqualTo(CORE_VERSION);
        verifyNoInteractions(coreConfigVersionAutoDetectService, coreConfigVersionProperties);
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void getVersionForExport_returnsAutoDetectedVersion(String manuallySetVersion) {
        // given
        AdminSettings adminSettings = new AdminSettings();
        adminSettings.setCoreConfigVersion(manuallySetVersion);

        when(adminSettingsService.getAdminSettings()).thenReturn(adminSettings);
        when(coreConfigVersionAutoDetectService.getVersion()).thenReturn(CORE_VERSION);

        // when
        String actual = service.getVersionForExport();

        // then
        assertThat(actual).isEqualTo(CORE_VERSION);
        verifyNoInteractions(coreConfigVersionProperties);
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '", "-1"}, nullValues = "null")
    void getVersionForExport_returnsDefaultVersion(String autoDetectedVersion) {
        // given
        AdminSettings adminSettings = new AdminSettings();
        adminSettings.setCoreConfigVersion(null);

        when(adminSettingsService.getAdminSettings()).thenReturn(adminSettings);
        when(coreConfigVersionAutoDetectService.getVersion()).thenReturn(autoDetectedVersion);
        when(coreConfigVersionProperties.getTarget()).thenReturn(CORE_VERSION);

        // when
        String actual = service.getVersionForExport();

        // then
        assertThat(actual).isEqualTo(CORE_VERSION);
    }

    @Test
    void getVersions_returnsAllVersions() {
        // given
        AdminSettings adminSettings = new AdminSettings();
        adminSettings.setCoreConfigVersion(CORE_VERSION);

        when(adminSettingsService.getAdminSettings()).thenReturn(adminSettings);
        when(coreConfigVersionAutoDetectService.getVersion()).thenReturn(CORE_VERSION);
        when(coreConfigVersionProperties.getTarget()).thenReturn(CORE_VERSION);

        CoreConfigVersions expected = CoreConfigVersions.builder()
                .manuallySetVersion(CORE_VERSION)
                .autoDetectedVersion(CORE_VERSION)
                .defaultVersion(CORE_VERSION)
                .build();

        // when
        CoreConfigVersions actual = service.getVersions();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}