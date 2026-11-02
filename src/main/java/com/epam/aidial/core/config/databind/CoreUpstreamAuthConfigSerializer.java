package com.epam.aidial.core.config.databind;

import com.epam.aidial.core.config.CoreUpstreamAuthConfig;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializes a {@link CoreUpstreamAuthConfig} as a plain auth type string when it has no header name, and as
 * a {@code {type, headerName}} object otherwise. The type is written by the mapper's own enum handling.
 */
public class CoreUpstreamAuthConfigSerializer extends ValueSerializer<CoreUpstreamAuthConfig> {

    @Override
    public void serialize(CoreUpstreamAuthConfig value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        if (value.getHeaderName() == null) {
            serializers.writeValue(gen, value.getType());
            return;
        }

        gen.writeStartObject();
        serializers.defaultSerializeProperty("type", value.getType(), gen);
        serializers.defaultSerializeProperty("headerName", value.getHeaderName(), gen);
        gen.writeEndObject();
    }
}
