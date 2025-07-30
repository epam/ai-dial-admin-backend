package com.epam.aidial.cfg.configuration;

import com.epam.aidial.core.config.validation.CustomApplicationConformToTypeSchemaValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ValidationConfig {

    @Bean
    public CustomApplicationConformToTypeSchemaValidator customApplicationConformToTypeSchemaValidator() {
        return new CustomApplicationConformToTypeSchemaValidator();
    }
}
