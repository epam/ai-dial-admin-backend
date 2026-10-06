package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingDtoTest {

    private final ObjectMapper mapper = JsonMapperConfiguration.createJsonMapper();

    @Test
    void deserialize_tokenFlatRates() throws Exception {
        PricingDto dto = mapper.readValue("""
                {"unit": "token", "prompt": "0.000003", "completion": "0.000015"}
                """, PricingDto.class);

        assertThat(dto.getPrompt().getRate()).isEqualTo("0.000003");
        assertThat(dto.getCompletion().getRate()).isEqualTo("0.000015");
        assertThat(dto.getCacheRead()).isNull();
    }

    @Test
    void deserialize_tokenCacheReadFlatAndCacheWriteTree() throws Exception {
        PricingDto dto = mapper.readValue("""
                {"unit": "token", "prompt": "0.000003", "completion": "0.000015",
                 "cacheRead": "0.0000003",
                 "cacheWrite": {"test": {"field": "ttl", "operator": "==", "value": "1h"},
                                "ifTrue": "0.000006", "ifFalse": "0.00000375"}}
                """, PricingDto.class);

        assertThat(dto.getCacheRead().isLeaf()).isTrue();
        assertThat(dto.getCacheWrite().isLeaf()).isFalse();
        assertThat(dto.getCacheWrite().getIfTrue().getRate()).isEqualTo("0.000006");
    }

    @Test
    void deserialize_tokenPromptDecisionTree() throws Exception {
        PricingDto dto = mapper.readValue("""
                {"unit": "token",
                 "prompt": {"test": {"field": "promptTokens", "operator": ">", "value": "128000"},
                            "ifTrue": "0.000006", "ifFalse": "0.000003"},
                 "completion": "0.000015"}
                """, PricingDto.class);

        assertThat(dto.getPrompt().isLeaf()).isFalse();
        assertThat(dto.getPrompt().getTest().getField()).isEqualTo("promptTokens");
        assertThat(dto.getPrompt().getIfFalse().getRate()).isEqualTo("0.000003");
        assertThat(dto.getCompletion().getRate()).isEqualTo("0.000015");
    }

    @Test
    void deserialize_charUnitFlatRates() throws Exception {
        PricingDto dto = mapper.readValue("""
                {"unit": "char_without_whitespace", "prompt": "0.0000008", "completion": "0.0000016"}
                """, PricingDto.class);

        assertThat(dto.getPrompt().getRate()).isEqualTo("0.0000008");
        assertThat(dto.getCompletion().getRate()).isEqualTo("0.0000016");
    }

    @Test
    void deserialize_unitNoneWithoutRates() throws Exception {
        PricingDto dto = mapper.readValue("{\"unit\": \"none\"}", PricingDto.class);

        assertThat(dto.getUnit()).isEqualTo("none");
        assertThat(dto.getPrompt()).isNull();
        assertThat(dto.getCompletion()).isNull();
    }

    @Test
    void deserialize_nonNumericRate_rejected() {
        assertThatThrownBy(() -> mapper.readValue("{\"unit\": \"token\", \"prompt\": \"abc\"}", PricingDto.class))
                .isInstanceOf(InvalidFormatException.class);
        assertThatThrownBy(() -> mapper.readValue("{\"unit\": \"token\", \"completion\": \"1.2.3\"}", PricingDto.class))
                .isInstanceOf(InvalidFormatException.class);
    }

    @Test
    void serialize_flatRatesStayJsonStrings() throws Exception {
        PricingDto dto = mapper.readValue("""
                {"unit": "token", "prompt": "0.000003", "completion": "0.000015"}
                """, PricingDto.class);

        assertThat(mapper.readTree(mapper.writeValueAsString(dto)).get("prompt").isTextual()).isTrue();
    }
}
