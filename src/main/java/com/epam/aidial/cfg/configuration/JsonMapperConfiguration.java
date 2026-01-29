package com.epam.aidial.cfg.configuration;

import com.epam.aidial.core.config.CoreCostLimit;
import com.epam.aidial.core.config.CoreCostLimitMixinForCoreObjectMapper;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreLimitMixinForCoreObjectMapper;
import com.epam.aidial.core.config.CoreResourceAuthSettings;
import com.epam.aidial.core.config.CoreResourceAuthSettingsMixinForCoreObjectMapper;
import com.epam.aidial.core.config.CoreToolSet;
import com.epam.aidial.core.config.CoreToolSetMixinForCoreObjectMapper;
import com.epam.aidial.core.config.CoreUpstream;
import com.epam.aidial.core.config.CoreUpstreamMixinForCoreObjectMapper;
import com.epam.aidial.core.config.validation.ValidationModule;
import com.epam.aidial.ql.deserializers.json.QueryLanguageModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.EnumFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Created by Aliaksei Kurnosau on 9/9/24.
 */
@Configuration
public class JsonMapperConfiguration {

    @Bean
    @Primary
    public JsonMapper getJsonMapper() {
        return createJsonMapper();
    }

    @Bean
    public JsonMapper coreJsonMapper() {
        return createCoreJsonMapper();
    }

    @Bean
    public JsonMapper nullIncludedJsonMapper() {
        return createNullIncludedJsonMapper();
    }

    public static JsonMapper createJsonMapper() {
        return createDefaultJsonMapperBuilder()
                .build();
    }

    public static JsonMapper createPrettyJsonMapper() {
        return createDefaultJsonMapperBuilder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    public static JsonMapper createNullIncludedJsonMapper() {
        return createDefaultJsonMapperBuilder()
                .serializationInclusion(JsonInclude.Include.ALWAYS)
                .build();
    }

    public static JsonMapper createCoreJsonMapper() {
        return JsonMapper.builder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addMixIn(CoreLimit.class, CoreLimitMixinForCoreObjectMapper.class)
                .addMixIn(CoreCostLimit.class, CoreCostLimitMixinForCoreObjectMapper.class)
                .addMixIn(CoreUpstream.class, CoreUpstreamMixinForCoreObjectMapper.class)
                .addMixIn(CoreToolSet.class, CoreToolSetMixinForCoreObjectMapper.class)
                .addMixIn(CoreResourceAuthSettings.class, CoreResourceAuthSettingsMixinForCoreObjectMapper.class)
                .build();
    }

    private static JsonMapper.Builder createDefaultJsonMapperBuilder() {
        return JsonMapper.builder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .enable(EnumFeature.WRITE_ENUMS_TO_LOWERCASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .addModule(new QueryLanguageModule())
                .addModule(new ValidationModule())
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
}
