package com.epam.aidial.core.config.databind;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

public class MapToJsonArraySerializer extends ValueSerializer<Map<URI, String>> {

    @Override
    public void serialize(Map<URI, String> uriStringMap, JsonGenerator jsonGenerator,
                          SerializationContext serializerProvider) throws JacksonException {
        jsonGenerator.writeStartArray();
        Iterator<Map.Entry<URI, String>> iterator = uriStringMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<URI, String> entry = iterator.next();
            jsonGenerator.writeRaw(entry.getValue());
            if (iterator.hasNext()) {
                jsonGenerator.writeRaw(",");
            }
        }
        jsonGenerator.writeEndArray();
    }
}
