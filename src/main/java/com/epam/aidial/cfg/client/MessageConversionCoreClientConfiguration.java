package com.epam.aidial.cfg.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class MessageConversionCoreClientConfiguration {

    @Bean
    @Primary
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters, ObjectMapper objectMapper) {
        var decoder = new SpringDecoder(messageConverters);
        return (response, type) -> {
            var contentType = response.headers().get("content-type");
            if (CollectionUtils.isEmpty(contentType)) {
                return objectMapper.readValue(response.body().asInputStream(), objectMapper.constructType(type));
            }
            return decoder.decode(response, type);
        };
    }

}
