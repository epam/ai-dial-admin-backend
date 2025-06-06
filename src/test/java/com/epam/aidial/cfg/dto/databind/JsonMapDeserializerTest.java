package com.epam.aidial.cfg.dto.databind;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonMapDeserializerTest {

    @Test
    void testDeserialize() throws IOException {
        // given
        ObjectMapper mapper = JsonMapperConfiguration.createJsonMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Map.class, new JsonMapDeserializer());
        mapper.registerModule(module);
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