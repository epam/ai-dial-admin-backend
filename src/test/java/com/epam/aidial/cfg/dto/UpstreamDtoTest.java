package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class UpstreamDtoTest {

    private static final String ENDPOINT = "\"endpoint\":\"http://upstream.test/chat\"";

    private final ObjectMapper mapper = JsonMapperConfiguration.createJsonMapper();

    @ParameterizedTest
    @ValueSource(strings = {"{" + ENDPOINT + "}", "{" + ENDPOINT + ",\"weight\":null}"})
    void deserialize_absentOrNullWeight_defaultsToOne(String json) {
        assertThat(mapper.readValue(json, UpstreamDto.class).getWeight()).isEqualTo(1);
    }

    @Test
    void deserialize_explicitWeight_isKept() {
        String json = "{" + ENDPOINT + ",\"weight\":5}";

        assertThat(mapper.readValue(json, UpstreamDto.class).getWeight()).isEqualTo(5);
    }

    @Test
    void deserialize_explicitZeroWeight_isKept() {
        String json = "{" + ENDPOINT + ",\"weight\":0}";

        assertThat(mapper.readValue(json, UpstreamDto.class).getWeight()).isZero();
    }
}
