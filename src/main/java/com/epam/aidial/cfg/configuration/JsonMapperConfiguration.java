package com.epam.aidial.cfg.configuration;

import com.epam.aidial.core.config.validation.ValidationModule;
import com.epam.aidial.ql.deserializers.json.QueryLanguageModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.cfg.EnumFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Aliaksei Kurnosau on 9/9/24.
 */
@Configuration
public class JsonMapperConfiguration {

    @Bean
    public JsonMapper getJsonMapper() {
        return createJsonMapper();
    }

    public static JsonMapper createJsonMapper() {
        return JsonMapper.builder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .enable(EnumFeature.WRITE_ENUMS_TO_LOWERCASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .addModule(new QueryLanguageModule())
                .addModule(new ValidationModule())
                .build();
    }

}
