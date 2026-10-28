package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpstreamAuthConfigDtoTest {

    private static final String ENDPOINT = "\"endpoint\":\"http://upstream.test/chat\"";

    private final ObjectMapper mapper = JsonMapperConfiguration.createJsonMapper();
    private final List<ObjectMapper> mappers = List.of(mapper, JsonMapperConfiguration.createCoreJsonMapper());

    @Test
    void authType_defaultsToApiKey() {
        assertThat(new UpstreamDto().getAuthType()).isEqualTo(UpstreamAuthConfigDto.of(UpstreamAuthTypeDto.API_KEY));
        mappers.forEach(m -> assertThat(m.readValue("{" + ENDPOINT + "}", UpstreamDto.class).getAuthType())
                .isEqualTo(UpstreamAuthConfigDto.of(UpstreamAuthTypeDto.API_KEY)));
    }

    @ParameterizedTest
    @CsvSource({"BEARER, BEARER", "bearer, BEARER", "Bearer, BEARER", "api_key, API_KEY", "API_KEY, API_KEY",
            "' bearer ', BEARER"})
    void deserialize_stringForm_isCaseInsensitiveAndHasNoHeaderName(String value, UpstreamAuthTypeDto expected) {
        String json = mapper.writeValueAsString(value);

        mappers.forEach(m -> {
            UpstreamAuthConfigDto config = m.readValue(json, UpstreamAuthConfigDto.class);

            assertThat(config.getType()).isEqualTo(expected);
            assertThat(config.getHeaderName()).isNull();
        });
    }

    @Test
    void deserialize_authTypeOnUpstream() {
        String json = "{" + ENDPOINT + ",\"authType\":\"bearer\"}";

        mappers.forEach(m -> assertThat(m.readValue(json, UpstreamDto.class).getAuthType())
                .isEqualTo(UpstreamAuthConfigDto.of(UpstreamAuthTypeDto.BEARER)));
    }

    @Test
    void deserialize_objectFormWithCustomHeader() {
        String json = "{" + ENDPOINT + ",\"authType\":{\"type\":\"CUSTOM_HEADER\",\"headerName\":\"X-Api-Token\"}}";

        mappers.forEach(m -> assertThat(m.readValue(json, UpstreamDto.class).getAuthType())
                .isEqualTo(UpstreamAuthConfigDto.customHeader("X-Api-Token")));
    }

    @Test
    void deserialize_objectFormWithPlainType() {
        mappers.forEach(m -> assertThat(m.readValue("{\"type\":\"bearer\"}", UpstreamAuthConfigDto.class))
                .isEqualTo(UpstreamAuthConfigDto.of(UpstreamAuthTypeDto.BEARER)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"CUSTOM_HEADER\"", "{\"type\":\"CUSTOM_HEADER\"}", "{\"type\":\"CUSTOM_HEADER\",\"headerName\":null}",
            "{\"type\":\"CUSTOM_HEADER\",\"headerName\":\"\"}", "{\"type\":\"CUSTOM_HEADER\",\"headerName\":\"X Api\"}",
            "{\"type\":\"CUSTOM_HEADER\",\"headerName\":\"X-Api:\"}", "{\"type\":\"CUSTOM_HEADER\",\"headerName\":\"X-Api\\n\"}",
            "{\"type\":\"CUSTOM_HEADER\",\"headerName\":\"Ключ\"}"})
    void deserialize_customHeaderWithoutValidHeaderName_isRejected(String json) {
        mappers.forEach(m -> assertThatThrownBy(() -> m.readValue(json, UpstreamAuthConfigDto.class))
                .isInstanceOf(JacksonException.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"type\":\"API_KEY\",\"headerName\":\"X-Api-Token\"}", "{\"type\":\"BEARER\",\"headerName\":\"X-Api-Token\"}"})
    void deserialize_headerNameForOtherTypes_isRejected(String json) {
        mappers.forEach(m -> assertThatThrownBy(() -> m.readValue(json, UpstreamAuthConfigDto.class))
                .isInstanceOf(JacksonException.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"type\":null}", "{\"headerName\":\"X-Api-Token\"}", "\"BASIC\"", "\"\"", "{\"type\":\"BASIC\"}",
            "123", "[]", "true"})
    void deserialize_missingOrUnknownType_isRejected(String json) {
        mappers.forEach(m -> assertThatThrownBy(() -> m.readValue(json, UpstreamAuthConfigDto.class))
                .isInstanceOf(JacksonException.class));
    }

    @Test
    void deserialize_explicitNull_isNull() {
        String json = "{" + ENDPOINT + ",\"authType\":null}";

        mappers.forEach(m -> assertThat(m.readValue(json, UpstreamDto.class).getAuthType()).isNull());
    }

    @Test
    void serialize_plainType_isString() {
        assertThat(mapper.writeValueAsString(UpstreamAuthConfigDto.of(UpstreamAuthTypeDto.BEARER))).isEqualTo("\"bearer\"");
        assertThat(JsonMapperConfiguration.createCoreJsonMapper().writeValueAsString(UpstreamAuthConfigDto.of(UpstreamAuthTypeDto.BEARER)))
                .isEqualTo("\"BEARER\"");
    }

    @Test
    void serialize_customHeader_isObject() {
        assertThat(mapper.readTree(mapper.writeValueAsString(UpstreamAuthConfigDto.customHeader("X-Api-Token"))))
                .isEqualTo(mapper.readTree("{\"type\":\"custom_header\",\"headerName\":\"X-Api-Token\"}"));
    }

    @Test
    void roundTrip() {
        for (UpstreamAuthConfigDto config : List.of(UpstreamAuthConfigDto.of(UpstreamAuthTypeDto.API_KEY),
                UpstreamAuthConfigDto.of(UpstreamAuthTypeDto.BEARER), UpstreamAuthConfigDto.customHeader("X-Api-Token"))) {
            mappers.forEach(m -> assertThat(m.readValue(m.writeValueAsString(config), UpstreamAuthConfigDto.class)).isEqualTo(config));
        }
    }

    @Test
    void roundTrip_insideUpstream() {
        UpstreamDto upstream = new UpstreamDto();
        upstream.setEndpoint("http://upstream.test/chat");
        upstream.setKey("secret");
        upstream.setAuthType(UpstreamAuthConfigDto.customHeader("X-Api-Token"));

        UpstreamDto result = mapper.readValue(mapper.writeValueAsString(upstream), UpstreamDto.class);

        assertThat(result.getAuthType()).isEqualTo(UpstreamAuthConfigDto.customHeader("X-Api-Token"));
    }

    @Test
    void deserialize_valuesStoredByPreviousVersions_stillParse() {
        // upstreams column holds the lowercase enum written before the object form existed
        List<UpstreamDto> upstreams = mapper.readValue(
                "[{" + ENDPOINT + ",\"authType\":\"bearer\"},{" + ENDPOINT + ",\"authType\":\"api_key\"},{" + ENDPOINT + "}]",
                mapper.getTypeFactory().constructCollectionType(List.class, UpstreamDto.class));

        assertThat(upstreams).extracting(u -> u.getAuthType().getType())
                .containsExactly(UpstreamAuthTypeDto.BEARER, UpstreamAuthTypeDto.API_KEY, UpstreamAuthTypeDto.API_KEY);
    }
}
