package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class UpstreamTest {

    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();

    @Test
    void deserialize_missingWeight_defaultsToOne() {
        Upstream upstream = objectMapper.readValue("{\"endpoint\":\"http://localhost\"}", Upstream.class);

        assertThat(upstream.getWeight()).isEqualTo(1);
    }

    @Test
    void deserialize_explicitNullWeight_defaultsToOne() {
        Upstream upstream = objectMapper.readValue("{\"endpoint\":\"http://localhost\",\"weight\":null}", Upstream.class);

        assertThat(upstream.getWeight()).isEqualTo(1);
    }

    @Test
    void deserialize_explicitWeight_isKept() {
        Upstream upstream = objectMapper.readValue("{\"endpoint\":\"http://localhost\",\"weight\":5}", Upstream.class);

        assertThat(upstream.getWeight()).isEqualTo(5);
    }

    @Test
    void deserialize_explicitZeroWeight_isKept() {
        Upstream upstream = objectMapper.readValue("{\"endpoint\":\"http://localhost\",\"weight\":0}", Upstream.class);

        assertThat(upstream.getWeight()).isZero();
    }
}
