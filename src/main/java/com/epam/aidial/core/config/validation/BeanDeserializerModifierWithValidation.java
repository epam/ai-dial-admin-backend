package com.epam.aidial.core.config.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

import java.io.IOException;

import static com.epam.aidial.core.config.validation.ValidationUtil.validate;

public class BeanDeserializerModifierWithValidation extends BeanDeserializerModifier {

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        if (deserializer instanceof BeanDeserializer) {
            return new BeanDeserializerWithValidation((BeanDeserializerBase) deserializer);
        }
        return deserializer;
    }

    private static class BeanDeserializerWithValidation extends BeanDeserializer {

        protected BeanDeserializerWithValidation(BeanDeserializerBase src) {
            super(src);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Object instance = super.deserialize(p, ctxt);
            validate(instance);
            return instance;
        }

    }
}