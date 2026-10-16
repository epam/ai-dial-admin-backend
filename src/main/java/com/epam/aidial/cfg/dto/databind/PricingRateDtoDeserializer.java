package com.epam.aidial.cfg.dto.databind;

import com.epam.aidial.cfg.dto.PricingRateConditionDto;
import com.epam.aidial.cfg.dto.PricingRateDto;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.InvalidFormatException;

/**
 * Accepts either a plain rate string (the flat, pre-existing shape) or a decision-tree node
 * object ({@code {test, ifTrue, ifFalse}}), producing a unified {@link PricingRateDto}.
 */
public class PricingRateDtoDeserializer extends ValueDeserializer<PricingRateDto> {

    @Override
    public PricingRateDto deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING || token == JsonToken.VALUE_NUMBER_FLOAT || token == JsonToken.VALUE_NUMBER_INT) {
            String rate = p.getString();
            try {
                Double.parseDouble(rate);
            } catch (NumberFormatException e) {
                throw InvalidFormatException.from(p, "Expected a JSON string with a valid double", rate, PricingRateDto.class);
            }
            return PricingRateDto.flat(rate);
        }

        if (p.currentToken() == JsonToken.START_OBJECT) {
            PricingRateConditionDto test = null;
            PricingRateDto ifTrue = null;
            PricingRateDto ifFalse = null;
            while (p.nextToken() != JsonToken.END_OBJECT) {
                String name = p.currentName();
                p.nextToken();
                switch (name) {
                    case "test" -> test = ctx.readValue(p, PricingRateConditionDto.class);
                    case "ifTrue" -> ifTrue = deserialize(p, ctx);
                    case "ifFalse" -> ifFalse = deserialize(p, ctx);
                    default -> p.skipChildren();
                }
            }
            if (test == null) {
                return ctx.reportInputMismatch(PricingRateDto.class, "A decision-tree node requires a \"test\" field");
            }
            PricingRateDto pricingRate = new PricingRateDto();
            pricingRate.setTest(test);
            pricingRate.setIfTrue(ifTrue);
            pricingRate.setIfFalse(ifFalse);
            return pricingRate;
        }

        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }

        return ctx.reportInputMismatch(PricingRateDto.class,
                "Expected a rate string or a decision-tree node object, got %s", p.currentToken());
    }
}
