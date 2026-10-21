package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.Upstream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpstreamEntityMapperTest {

    private final UpstreamEntityMapper mapper = new UpstreamEntityMapper() {
    };

    @BeforeEach
    void setUp() {
        mapper.objectMapper = JsonMapperConfiguration.createJsonMapper();
    }

    @Test
    void map_upstreamWithoutWeight_defaultsToOne() {
        // weight is optional in Core's config, and Core's default for a missing one is 1.
        // Reading it back as 0 would export "weight": 0, which Core reads as "upstream disabled".
        List<Upstream> upstreams = mapper.map("""
                [{"endpoint": "http://adapter/chat/completions"}]
                """);

        assertThat(upstreams).singleElement()
                .extracting(Upstream::getWeight)
                .isEqualTo(1);
    }

    @Test
    void map_upstreamWithNullWeight_defaultsToOne() {
        List<Upstream> upstreams = mapper.map("""
                [{"endpoint": "http://adapter/chat/completions", "weight": null}]
                """);

        assertThat(upstreams).singleElement()
                .extracting(Upstream::getWeight)
                .isEqualTo(1);
    }

    @Test
    void map_upstreamWithExplicitWeight_keepsIt() {
        List<Upstream> upstreams = mapper.map("""
                [{"endpoint": "http://adapter/chat/completions", "weight": 5}]
                """);

        assertThat(upstreams).singleElement()
                .extracting(Upstream::getWeight)
                .isEqualTo(5);
    }

    @Test
    void map_upstreamExplicitlyDisabled_keepsZeroWeight() {
        // zero is a deliberate "disable this upstream" in Core, so it must survive the round trip
        List<Upstream> upstreams = mapper.map("""
                [{"endpoint": "http://adapter/chat/completions", "weight": 0}]
                """);

        assertThat(upstreams).singleElement()
                .extracting(Upstream::getWeight)
                .isEqualTo(0);
    }

    @Test
    void map_roundTripsWithoutWeight_exportsDefaultWeight() {
        List<Upstream> upstreams = mapper.map("""
                [{"endpoint": "http://adapter/chat/completions"}]
                """);

        assertThat(mapper.map(upstreams)).contains("\"weight\":1");
    }
}
