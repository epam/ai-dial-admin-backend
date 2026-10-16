package com.epam.aidial.cfg.domain.model.databind;

import com.epam.aidial.cfg.domain.model.PricingRate;
import com.epam.aidial.cfg.domain.model.PricingRateCondition;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.InvalidFormatException;

/**
 * Accepts either a plain rate string (the flat, pre-existing shape) or a decision-tree node
 * object ({@code {test, ifTrue, ifFalse}}), producing a unified {@link PricingRate}.
 */
public class PricingRateDeserializer extends ValueDeserializer<PricingRate> {

    @Override
    public PricingRate deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING || token == JsonToken.VALUE_NUMBER_FLOAT || token == JsonToken.VALUE_NUMBER_INT) {
            String rate = p.getString();
            try {
                Double.parseDouble(rate);
            } catch (NumberFormatException e) {
                throw InvalidFormatException.from(p, "Expected a JSON string with a valid double", rate, PricingRate.class);
            }
            return PricingRate.flat(rate);
        }

        if (p.currentToken() == JsonToken.START_OBJECT) {
            PricingRateCondition test = null;
            PricingRate ifTrue = null;
            PricingRate ifFalse = null;
            while (p.nextToken() != JsonToken.END_OBJECT) {
                String name = p.currentName();
                p.nextToken();
                switch (name) {
                    case "test" -> test = ctx.readValue(p, PricingRateCondition.class);
                    case "ifTrue" -> ifTrue = deserialize(p, ctx);
                    case "ifFalse" -> ifFalse = deserialize(p, ctx);
                    default -> p.skipChildren();
                }
            }
            if (test == null) {
                return ctx.reportInputMismatch(PricingRate.class, "A decision-tree node requires a \"test\" field");
            }
            PricingRate pricingRate = new PricingRate();
            pricingRate.setTest(test);
            pricingRate.setIfTrue(ifTrue);
            pricingRate.setIfFalse(ifFalse);
            return pricingRate;
        }

        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }

        return ctx.reportInputMismatch(PricingRate.class,
                "Expected a rate string or a decision-tree node object, got %s", p.currentToken());
    }
}
