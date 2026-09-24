package com.epam.aidial.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingRateTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserialize_leafRateString() throws Exception {
        PricingRate rate = mapper.readValue("\"0.000003\"", PricingRate.class);

        assertThat(rate.isLeaf()).isTrue();
        assertThat(rate.getRate()).isEqualTo("0.000003");
        assertThat(rate.getTest()).isNull();
    }

    @Test
    void deserialize_leafRateNumber() throws Exception {
        PricingRate rate = mapper.readValue("0.0000000065", PricingRate.class);

        assertThat(rate.isLeaf()).isTrue();
        assertThat(rate.getRate()).isEqualTo("0.0000000065");
        assertThat(rate.getTest()).isNull();
    }

    @Test
    void deserialize_decisionTreeNode() throws Exception {
        String json = """
                {
                  "test": {"field": "ttl", "operator": "==", "value": "1h"},
                  "ifTrue": "0.000006",
                  "ifFalse": "0.00000375"
                }
                """;

        PricingRate rate = mapper.readValue(json, PricingRate.class);

        assertThat(rate.isLeaf()).isFalse();
        assertThat(rate.getTest().getField()).isEqualTo("ttl");
        assertThat(rate.getTest().getOperator()).isEqualTo(PricingRateOperator.EQ);
        assertThat(rate.getTest().getValue()).isEqualTo("1h");
        assertThat(rate.getIfTrue().getRate()).isEqualTo("0.000006");
        assertThat(rate.getIfFalse().getRate()).isEqualTo("0.00000375");
    }

    @Test
    void deserialize_nestedDecisionTree() throws Exception {
        String json = """
                {
                  "test": {"field": "serviceTier", "operator": "==", "value": "flex"},
                  "ifTrue": {
                    "test": {"field": "ttl", "operator": "==", "value": "1h"},
                    "ifTrue": "0.000006"
                  }
                }
                """;

        PricingRate rate = mapper.readValue(json, PricingRate.class);

        assertThat(rate.isLeaf()).isFalse();
        assertThat(rate.getIfTrue().getTest().getField()).isEqualTo("ttl");
        assertThat(rate.getIfTrue().getIfTrue().getRate()).isEqualTo("0.000006");
        assertThat(rate.getIfTrue().getIfFalse()).isNull();
        assertThat(rate.getIfFalse()).isNull();
    }

    @Test
    void deserialize_leafWithNonDoubleRate_throws() {
        assertThatThrownBy(() -> mapper.readValue("\"not-a-double\"", PricingRate.class))
                .isInstanceOf(InvalidFormatException.class);
    }

    @Test
    void deserialize_nodeMissingTest_throws() {
        assertThatThrownBy(() -> mapper.readValue("{\"ifTrue\": \"0.000006\"}", PricingRate.class))
                .isInstanceOf(MismatchedInputException.class);
    }

    @Test
    void deserialize_null_returnsNull() throws Exception {
        assertThat(mapper.readValue("null", PricingRate.class)).isNull();
    }

    @Test
    void serialize_leaf_writesPlainString() throws Exception {
        PricingRate rate = new PricingRate();
        rate.setRate("0.000003");

        assertThat(mapper.writeValueAsString(rate)).isEqualTo("\"0.000003\"");
    }

    @Test
    void serialize_node_roundTrips() throws Exception {
        String json = """
                {"test":{"field":"ttl","operator":"==","value":"1h"},"ifTrue":"0.000006","ifFalse":"0.00000375"}""";

        PricingRate rate = mapper.readValue(json, PricingRate.class);
        PricingRate roundTripped = mapper.readValue(mapper.writeValueAsString(rate), PricingRate.class);

        assertThat(roundTripped.getTest().getField()).isEqualTo("ttl");
        assertThat(roundTripped.getTest().getOperator()).isEqualTo(PricingRateOperator.EQ);
        assertThat(roundTripped.getIfTrue().getRate()).isEqualTo("0.000006");
        assertThat(roundTripped.getIfFalse().getRate()).isEqualTo("0.00000375");
    }
}
