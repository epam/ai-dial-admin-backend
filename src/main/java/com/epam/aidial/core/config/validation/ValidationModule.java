package com.epam.aidial.core.config.validation;

import tools.jackson.databind.module.SimpleModule;

public class ValidationModule extends SimpleModule {

    public ValidationModule() {
        super();
        setDeserializerModifier(new BeanDeserializerModifierWithValidation());
    }
}
