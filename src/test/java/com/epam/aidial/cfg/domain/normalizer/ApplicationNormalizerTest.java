package com.epam.aidial.cfg.domain.normalizer;

import com.epam.aidial.cfg.domain.model.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationNormalizerTest {

    private ApplicationNormalizer applicationNormalizer;

    @BeforeEach
    void setUp() {
        applicationNormalizer = new ApplicationNormalizer();
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void normalize_shouldSetDisplayNameToNullWhenDisplayNameIsBlank(String displayName) {
        // given
        Application application = new Application();
        application.setDisplayName(displayName);

        // when
        applicationNormalizer.normalize(application);

        // then
        assertThat(application.getDisplayName()).isNull();
    }

    @Test
    void normalize_shouldLeaveDisplayNameAsIsWhenDisplayNameIsNotBlank() {
        // given
        Application application = new Application();
        application.setDisplayName("text");

        // when
        applicationNormalizer.normalize(application);

        // then
        assertThat(application.getDisplayName()).isEqualTo("text");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void normalize_shouldSetDisplayVersionToNullWhenDisplayVersionIsBlank(String displayVersion) {
        // given
        Application application = new Application();
        application.setDisplayVersion(displayVersion);

        // when
        applicationNormalizer.normalize(application);

        // then
        assertThat(application.getDisplayVersion()).isNull();
    }

    @Test
    void normalize_shouldLeaveDisplayVersionAsIsWhenDisplayVersionIsNotBlank() {
        // given
        Application application = new Application();
        application.setDisplayVersion("1.0");

        // when
        applicationNormalizer.normalize(application);

        // then
        assertThat(application.getDisplayVersion()).isEqualTo("1.0");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void normalize_shouldSetEndpointToNullWhenEndpointIsBlank(String endpoint) {
        // given
        Application application = new Application();
        application.setEndpoint(endpoint);

        // when
        applicationNormalizer.normalize(application);

        // then
        assertThat(application.getEndpoint()).isNull();
    }

    @Test
    void normalize_shouldLeaveEndpointAsIsWhenEndpointIsNotBlank() {
        // given
        Application application = new Application();
        application.setEndpoint("test");

        // when
        applicationNormalizer.normalize(application);

        // then
        assertThat(application.getEndpoint()).isEqualTo("test");
    }
}