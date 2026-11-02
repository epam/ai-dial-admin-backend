package com.epam.aidial.core.config;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Field initializers are the contract for properties absent from the JSON, so they must survive deserialization.
 */
class CoreUpstreamTest {

    private static final String ENDPOINT = "\"endpoint\":\"http://upstream.test/chat\"";

    private final List<ObjectMapper> mappers = List.of(
            JsonMapperConfiguration.createJsonMapper(), JsonMapperConfiguration.createCoreJsonMapper());

    @ParameterizedTest
    @ValueSource(strings = {"{" + ENDPOINT + "}", "{" + ENDPOINT + ",\"weight\":null}"})
    void deserialize_absentOrNullWeight_defaultsToOne(String json) {
        mappers.forEach(mapper -> assertThat(mapper.readValue(json, CoreUpstream.class).getWeight()).isEqualTo(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{" + ENDPOINT + ",\"weight\":5}", "{" + ENDPOINT + ",\"dial:weight\":5}"})
    void deserialize_explicitWeight_isKept(String json) {
        mappers.forEach(mapper -> assertThat(mapper.readValue(json, CoreUpstream.class).getWeight()).isEqualTo(5));
    }

    @Test
    void deserialize_explicitZeroWeight_isKept() {
        String json = "{" + ENDPOINT + ",\"weight\":0}";

        mappers.forEach(mapper -> assertThat(mapper.readValue(json, CoreUpstream.class).getWeight()).isZero());
    }

    @Test
    void deserialize_absentProperties_keepOtherDefaults() {
        String json = "{" + ENDPOINT + "}";

        mappers.forEach(mapper -> {
            CoreUpstream upstream = mapper.readValue(json, CoreUpstream.class);
            assertThat(upstream.getAuthType()).isEqualTo(CoreUpstreamAuthConfig.of(CoreUpstreamAuthType.API_KEY));
            assertThat(upstream.getInterfaces()).isEmpty();
            assertThat(upstream.getTier()).isZero();
        });
    }
}
