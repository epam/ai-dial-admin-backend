package com.epam.aidial.core.config.validation;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.deser.bean.BeanDeserializer;

import static com.epam.aidial.core.config.validation.ValidationUtil.validate;

public class BeanDeserializerModifierWithValidation extends ValueDeserializerModifier {

    @Override
    public ValueDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
        if (deserializer instanceof BeanDeserializer) {
            return new BeanDeserializerWithValidation((BeanDeserializer) deserializer);
        }
        return deserializer;
    }

    private static class BeanDeserializerWithValidation extends BeanDeserializer {

        protected BeanDeserializerWithValidation(BeanDeserializer src) {
            super(src);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            Object instance = super.deserialize(p, ctxt);
            validate(instance);
            return instance;
        }

    }
}