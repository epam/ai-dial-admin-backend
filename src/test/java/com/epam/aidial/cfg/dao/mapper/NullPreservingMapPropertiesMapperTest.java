package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NullPreservingMapPropertiesMapperTest {

    private final ObjectMapper defaultMapper = JsonMapperConfiguration.createJsonMapper();
    private final NullPreservingMapPropertiesMapper mapper = new NullPreservingMapPropertiesMapper();

    @BeforeEach
    void setUp() {
        mapper.objectMapper = JsonMapperConfiguration.createNullPreservingJsonMapper();
    }

    @Test
    void mapFromObjectMap_nestedNull_isKeptInStoredString() {
        String stored = mapper.mapFromObjectMap(propertiesWithNestedNull());

        assertThat(defaultMapper.readTree(stored).at("/features").has("timestamp")).isTrue();
        assertThat(defaultMapper.readTree(stored).at("/features/timestamp").isNull()).isTrue();
    }

    @Test
    void roundTrip_nestedNull_keyIsPreserved() {
        Map<String, Object> restored = mapper.mapToObjectMap(mapper.mapFromObjectMap(propertiesWithNestedNull()));

        assertThat(restored).isEqualTo(propertiesWithNestedNull());
        assertThat(restored.get("features")).isInstanceOfSatisfying(Map.class,
                features -> assertThat(features.containsKey("timestamp")).isTrue());
    }

    @Test
    void nullMap_mapsToNull() {
        assertThat(mapper.mapFromObjectMap(null)).isNull();
        assertThat(mapper.mapToObjectMap(null)).isNull();
    }

    private Map<String, Object> propertiesWithNestedNull() {
        Map<String, Object> features = new HashMap<>();
        features.put("timestamp", null);
        Map<String, Object> properties = new HashMap<>();
        properties.put("features", features);
        return properties;
    }
}
