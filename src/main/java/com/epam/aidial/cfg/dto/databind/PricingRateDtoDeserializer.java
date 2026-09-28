package com.epam.aidial.cfg.dto.databind;

import com.epam.aidial.cfg.dto.PricingRateConditionDto;
import com.epam.aidial.cfg.dto.PricingRateDto;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;

/**
 * Accepts either a plain rate string (the flat, pre-existing shape) or a decision-tree node
 * object ({@code {test, ifTrue, ifFalse}}), producing a unified {@link PricingRateDto}.
 */
public class PricingRateDtoDeserializer extends JsonDeserializer<PricingRateDto> {

    @Override
    public PricingRateDto deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        JsonToken token = p.getCurrentToken();
        if (token == JsonToken.VALUE_STRING || token == JsonToken.VALUE_NUMBER_FLOAT || token == JsonToken.VALUE_NUMBER_INT) {
            String rate = p.getText();
            try {
                Double.parseDouble(rate);
            } catch (NumberFormatException e) {
                throw InvalidFormatException.from(p, "Expected a JSON string with a valid double", rate, PricingRateDto.class);
            }
            PricingRateDto pricingRate = new PricingRateDto();
            pricingRate.setRate(rate);
            return pricingRate;
        }

        if (p.getCurrentToken() == JsonToken.START_OBJECT) {
            PricingRateConditionDto test = null;
            PricingRateDto ifTrue = null;
            PricingRateDto ifFalse = null;
            while (p.nextToken() != JsonToken.END_OBJECT) {
                String name = p.getCurrentName();
                p.nextToken();
                switch (name) {
                    case "test" -> test = p.readValueAs(PricingRateConditionDto.class);
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

        if (p.getCurrentToken() == JsonToken.VALUE_NULL) {
            return null;
        }

        return ctx.reportInputMismatch(PricingRateDto.class,
                "Expected a rate string or a decision-tree node object, got %s", p.getCurrentToken());
    }
}
