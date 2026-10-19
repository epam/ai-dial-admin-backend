package com.epam.aidial.cfg.dto.databind;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class JsonMapSerializerTest {

    @Test
    void testSerialize() throws IOException {
        // given
        Map<String, String> map = new HashMap<>();
        map.put("ToolEndpointInfoMethodType", "{\"enum\":[\"get\",\"post\",\"put\",\"delete\"],\"title\":\"ToolEndpointInfoMethodType\",\"type\":\"string\"}");
        Writer jsonWriter = new StringWriter();
        ObjectMapper mapper = JsonMapperConfiguration.createJsonMapper();
        JsonGenerator jsonGenerator = mapper.createGenerator(jsonWriter);
        SerializationContext serializerProvider = mapper._serializationContext();
        JsonMapSerializer jsonMapSerializer = new JsonMapSerializer();
        File file = new File("src/test/resources/defs.json");
        String expected = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        // when
        jsonMapSerializer.serialize(map, jsonGenerator, serializerProvider);
        jsonGenerator.flush();

        // then
        Assertions.assertThat(jsonWriter).hasToString(expected);
    }
}