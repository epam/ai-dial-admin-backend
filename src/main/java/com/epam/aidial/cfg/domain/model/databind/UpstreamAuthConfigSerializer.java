package com.epam.aidial.cfg.domain.model.databind;

import com.epam.aidial.cfg.domain.model.UpstreamAuthConfig;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializes a {@link UpstreamAuthConfig} as a plain auth type string when it has no header name, and as
 * a {@code {type, headerName}} object otherwise. The type is written by the mapper's own enum handling.
 */
public class UpstreamAuthConfigSerializer extends ValueSerializer<UpstreamAuthConfig> {

    @Override
    public void serialize(UpstreamAuthConfig value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
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
