package com.epam.aidial.cfg.dto.databind;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        ObjectMapper mapper = JsonMapperConfiguration.createJsonMapper();
        SerializerProvider serializerProvider = mapper.getSerializerProvider();
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