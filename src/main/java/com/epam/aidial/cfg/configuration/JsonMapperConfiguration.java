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
import com.epam.aidial.core.config.CoreUpstreamInterface;
import com.epam.aidial.core.config.CoreUpstreamInterfaceMixinForCoreObjectMapper;
import com.epam.aidial.core.config.CoreUpstreamMixinForCoreObjectMapper;
import com.epam.aidial.core.config.validation.ValidationModule;
import com.epam.aidial.ql.deserializers.json.QueryLanguageModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

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
    public JsonMapper nullPreservingJsonMapper() {
        return createNullPreservingJsonMapper();
    }

    public static JsonMapper createJsonMapper() {
        return createDefaultJsonMapperBuilder()
                .build();
    }

    /**
     * Same as {@link #createJsonMapper()}, but never drops nulls: neither null properties nor null values inside maps.
     * For payloads where an explicit null is meaningful, e.g. free-form properties.
     */
    public static JsonMapper createNullPreservingJsonMapper() {
        return createDefaultJsonMapperBuilder()
                .changeDefaultPropertyInclusion(inclusion -> JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS))
                .build();
    }

    public static JsonMapper createPrettyJsonMapper() {
        return createDefaultJsonMapperBuilder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    public static JsonMapper createCoreJsonMapper() {
        return withLegacyCompatibility(JsonMapper.builder())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addMixIn(CoreLimit.class, CoreLimitMixinForCoreObjectMapper.class)
                .addMixIn(CoreCostLimit.class, CoreCostLimitMixinForCoreObjectMapper.class)
                .addMixIn(CoreUpstream.class, CoreUpstreamMixinForCoreObjectMapper.class)
                .addMixIn(CoreUpstreamInterface.class, CoreUpstreamInterfaceMixinForCoreObjectMapper.class)
                .addMixIn(CoreToolSet.class, CoreToolSetMixinForCoreObjectMapper.class)
                .addMixIn(CoreResourceAuthSettings.class, CoreResourceAuthSettingsMixinForCoreObjectMapper.class)
                .build();
    }

    /**
     * Keeps the behavior that stored data, exported configs and Core payloads rely on, where Jackson 3 changed its defaults:
     * properties keep their declaration order (constructor properties are not moved first) and null is accepted for primitive properties.
     */
    public static JsonMapper.Builder withLegacyCompatibility(JsonMapper.Builder builder) {
        return builder
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .disable(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
    }

    private static JsonMapper.Builder createDefaultJsonMapperBuilder() {
        return withLegacyCompatibility(JsonMapper.builder())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .enable(EnumFeature.WRITE_ENUMS_TO_LOWERCASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
                .changeDefaultPropertyInclusion(inclusion -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
                .addModule(new QueryLanguageModule())
                .addModule(new ValidationModule())
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
}
