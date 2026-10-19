package com.epam.aidial.cfg.domain.model.databind;

import com.epam.aidial.cfg.domain.model.PricingRate;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializes a {@link PricingRate} as a plain rate string when it's the flat leaf shape, and as a
 * {@code {test, ifTrue, ifFalse}} decision-tree node object otherwise.
 */
public class PricingRateSerializer extends ValueSerializer<PricingRate> {

    @Override
    public void serialize(PricingRate value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        if (value.isLeaf()) {
            gen.writeString(value.getRate());
            return;
        }

        gen.writeStartObject();
        serializers.defaultSerializeProperty("test", value.getTest(), gen);
        writeBranch(gen, serializers, "ifTrue", value.getIfTrue());
        writeBranch(gen, serializers, "ifFalse", value.getIfFalse());
        gen.writeEndObject();
    }

    private static void writeBranch(JsonGenerator gen, SerializationContext serializers, String name, PricingRate branch)
            throws JacksonException {
        if (branch != null) {
            gen.writeName(name);
            serializers.writeValue(gen, branch);
        }
    }
}
