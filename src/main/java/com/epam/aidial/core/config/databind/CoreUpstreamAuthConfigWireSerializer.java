package com.epam.aidial.core.config.databind;

import com.epam.aidial.core.config.CoreUpstreamAuthConfig;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializes a {@link CoreUpstreamAuthConfig} exactly the way DIAL Core writes it: always as a
 * {@code {type, headerName}} object, with {@code headerName} explicitly {@code null} unless the type is
 * {@code CUSTOM_HEADER}. Core's {@code UpstreamAuthConfig} is a record without a custom serializer, so its config
 * reload response never uses the plain string form that {@link CoreUpstreamAuthConfigSerializer} writes.
 * <p>
 * Used by the Core object mapper only (see {@code CoreUpstreamAuthConfigMixinForCoreObjectMapper}), so that the
 * sync state comparison sees the same JSON as the one returned by Core.
 */
public class CoreUpstreamAuthConfigWireSerializer extends ValueSerializer<CoreUpstreamAuthConfig> {

    @Override
    public void serialize(CoreUpstreamAuthConfig value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        gen.writeStartObject();
        serializers.defaultSerializeProperty("type", value.getType(), gen);
        if (value.getHeaderName() == null) {
            gen.writeNullProperty("headerName");
        } else {
            gen.writeStringProperty("headerName", value.getHeaderName());
        }
        gen.writeEndObject();
    }
}
