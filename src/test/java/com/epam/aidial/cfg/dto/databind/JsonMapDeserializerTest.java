package com.epam.aidial.cfg.dto.databind;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonMapDeserializerTest {

    @Test
    void testDeserialize() throws IOException {
        // given
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Map.class, new JsonMapDeserializer());
        ObjectMapper mapper = JsonMapperConfiguration.createJsonMapper().rebuild().addModule(module).build();
        File file = new File("src/test/resources/defs.json");

        // when
        Map<String, String> result = mapper.readValue(file, new TypeReference<>() {
        });

        // then
        Map<String, String> expected = new HashMap<>();
        expected.put("ToolEndpointInfoMethodType", "{\"enum\":[\"get\",\"post\",\"put\",\"delete\"],\"title\":\"ToolEndpointInfoMethodType\",\"type\":\"string\"}");
        assertEquals(expected, result);
    }
}