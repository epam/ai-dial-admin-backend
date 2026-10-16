package com.epam.aidial.metric.util;

import org.springframework.boot.convert.DurationStyle;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.Duration;

public class DurationDeserializer extends ValueDeserializer<Duration> {

    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        return DurationStyle.detectAndParse(p.getString());
    }
}
