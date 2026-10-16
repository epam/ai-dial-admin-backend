package com.epam.aidial.cfg.client;

import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

public class MessageConversionCoreClientConfiguration {

    /**
     * Replaces the default converters of the Feign decoder with a single Jackson converter that accepts any media type.
     */
    @Bean
    public HttpMessageConverterCustomizer feignMessageConverterCustomizer(JsonMapper jsonMapper) {
        return converters -> {
            JacksonJsonHttpMessageConverter jackson = new JacksonJsonHttpMessageConverter(jsonMapper);
            jackson.setSupportedMediaTypes(List.of(MediaType.ALL));

            converters.clear();
            converters.add(jackson);
        };
    }

}
