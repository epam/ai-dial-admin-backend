package com.epam.aidial.cfg.service.hashing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Set;


@Component
@Slf4j
public class HashCalculator {
    public static final String ANY_HASH = "*";
    private static final Set<String> IGNORED_FIELDS = Set.of("createdAt", "updatedAt", "validityState");
    private final ObjectWriter writer;
    private final Base64.Encoder base64Encoder;

    public HashCalculator(JsonMapper mapper) {
        var filters = new SimpleFilterProvider()
                .addFilter("hashFilter", SimpleBeanPropertyFilter.serializeAllExcept(IGNORED_FIELDS))
                .setFailOnUnknownId(false);
        var hashingMapper = mapper.rebuild()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .disable(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                .addMixIn(Object.class, HashFilterMixin.class)
                .filterProvider(filters)
                .build();
        this.writer = hashingMapper.writer();
        this.base64Encoder = Base64.getUrlEncoder().withoutPadding();
    }

    public String calculateHash(Object body) {
        try {
            byte[] json = writer.writeValueAsBytes(body);
            var md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(json);
            return base64Encoder.encodeToString(digest);
        } catch (Exception e) {
            log.warn("Failed to compute hash for body: {}", toLoggableJson(body), e);
            throw new RuntimeException("Failed to compute hash", e);
        }
    }

    private String toLoggableJson(Object body) {
        try {
            return writer.writeValueAsString(body);
        } catch (Exception e) {
            return "Unable to serialize";
        }
    }
}
