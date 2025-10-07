package com.epam.aidial.cfg.domain.normalizer;

import com.epam.aidial.cfg.domain.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ModelNormalizerTest {

    private ModelNormalizer modelNormalizer;

    @BeforeEach
    void setUp() {
        modelNormalizer = new ModelNormalizer();
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void normalize_shouldSetDisplayVersionToNullWhenDisplayVersionIsBlank(String displayVersion) {
        // given
        Model model = new Model();
        model.setDisplayVersion(displayVersion);

        // when
        modelNormalizer.normalize(model);

        // then
        assertThat(model.getDisplayVersion()).isNull();
    }

    @Test
    void normalize_shouldLeaveDisplayVersionAsIsWhenDisplayVersionIsNotBlank() {
        // given
        Model model = new Model();
        model.setDisplayVersion("1.0");

        // when
        modelNormalizer.normalize(model);

        // then
        assertThat(model.getDisplayVersion()).isEqualTo("1.0");
    }
}