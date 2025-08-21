package com.epam.aidial.cfg.domain.normalizer;

import com.epam.aidial.cfg.domain.model.ToolSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ToolSetNormalizerTest {

    private ToolSetNormalizer toolSetNormalizer;

    @BeforeEach
    void setUp() {
        toolSetNormalizer = new ToolSetNormalizer();
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void normalize_shouldSetDisplayNameToNullWhenDisplayNameIsBlank(String displayName) {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName(displayName);

        // when
        toolSetNormalizer.normalize(toolSet);

        // then
        assertThat(toolSet.getDisplayName()).isNull();
    }

    @Test
    void normalize_shouldLeaveDisplayNameAsIsWhenDisplayNameIsNotBlank() {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setDisplayName("text");

        // when
        toolSetNormalizer.normalize(toolSet);

        // then
        assertThat(toolSet.getDisplayName()).isEqualTo("text");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void normalize_shouldSetEndpointToNullWhenEndpointIsBlank(String endpoint) {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setEndpoint(endpoint);

        // when
        toolSetNormalizer.normalize(toolSet);

        // then
        assertThat(toolSet.getEndpoint()).isNull();
    }

    @Test
    void normalize_shouldLeaveEndpointAsIsWhenEndpointIsNotBlank() {
        // given
        ToolSet toolSet = new ToolSet();
        toolSet.setEndpoint("test");

        // when
        toolSetNormalizer.normalize(toolSet);

        // then
        assertThat(toolSet.getEndpoint()).isEqualTo("test");
    }
}