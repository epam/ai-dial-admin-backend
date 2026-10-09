package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.PricingRate;
import com.epam.aidial.cfg.domain.model.PricingRateCondition;
import com.epam.aidial.cfg.domain.model.PricingRateOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PricingRateEntityMapperTest {

    private final PricingRateEntityMapper mapper = new PricingRateEntityMapper();

    @BeforeEach
    void setUp() {
        mapper.objectMapper = JsonMapperConfiguration.createJsonMapper();
    }

    @Test
    void mapToPricingRate_null_returnsNull() {
        assertThat(mapper.mapToPricingRate(null)).isNull();
    }

    @Test
    void mapFromPricingRate_null_returnsNull() {
        assertThat(mapper.mapFromPricingRate(null)).isNull();
    }

    @Test
    void roundTrips_leafRate() {
        PricingRate rate = new PricingRate();
        rate.setRate("0.000003");

        String stored = mapper.mapFromPricingRate(rate);
        PricingRate restored = mapper.mapToPricingRate(stored);

        assertThat(restored.isLeaf()).isTrue();
        assertThat(restored.getRate()).isEqualTo("0.000003");
    }

    @Test
    void mapToPricingRate_legacyUnquotedNumber_parsesAsLeafRate() {
        // pre-existing rows stored the raw double as plain text, e.g. "0.0000000065", not as a JSON string
        PricingRate restored = mapper.mapToPricingRate("0.0000000065");

        assertThat(restored.isLeaf()).isTrue();
        assertThat(restored.getRate()).isEqualTo("0.0000000065");
    }

    @Test
    void roundTrips_decisionTreeNode() {
        PricingRateCondition condition = new PricingRateCondition();
        condition.setField("ttl");
        condition.setOperator(PricingRateOperator.EQ);
        condition.setValue("1h");

        PricingRate ifTrue = new PricingRate();
        ifTrue.setRate("0.000006");
        PricingRate ifFalse = new PricingRate();
        ifFalse.setRate("0.00000375");

        PricingRate rate = new PricingRate();
        rate.setTest(condition);
        rate.setIfTrue(ifTrue);
        rate.setIfFalse(ifFalse);

        String stored = mapper.mapFromPricingRate(rate);
        PricingRate restored = mapper.mapToPricingRate(stored);

        assertThat(restored.isLeaf()).isFalse();
        assertThat(restored.getTest().getField()).isEqualTo("ttl");
        assertThat(restored.getTest().getOperator()).isEqualTo(PricingRateOperator.EQ);
        assertThat(restored.getTest().getValue()).isEqualTo("1h");
        assertThat(restored.getIfTrue().getRate()).isEqualTo("0.000006");
        assertThat(restored.getIfFalse().getRate()).isEqualTo("0.00000375");
    }
}
