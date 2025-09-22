package com.epam.aidial.cfg.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.optionals.OptionalDecoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

public class MessageConversionCoreClientConfiguration {
    @Bean
    @Primary
    public Decoder feignDecoder(ObjectMapper objectMapper) {

        MappingJackson2HttpMessageConverter jackson = new MappingJackson2HttpMessageConverter(objectMapper);
        jackson.setSupportedMediaTypes(List.of(MediaType.ALL));

        HttpMessageConverters converters = new HttpMessageConverters(jackson);

        return new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(() -> converters)));
    }

}
