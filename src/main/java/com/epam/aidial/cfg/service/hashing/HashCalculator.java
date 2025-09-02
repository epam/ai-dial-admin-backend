package com.epam.aidial.cfg.service.hashing;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Set;


@Component
@Slf4j
public class HashCalculator {
    public static final String ANY_HASH = "*";
    private static final Set<String> IGNORED_FIELDS = Set.of("createdAt", "updatedAt");
    private final ObjectWriter writer;
    private final Base64.Encoder base64Encoder =  Base64.getUrlEncoder().withoutPadding();

    public HashCalculator(ObjectMapper mapper) {
        var hashingMapper = mapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                .addMixIn(Object.class, HashFilterMixin.class);
        var filters = new SimpleFilterProvider()
                .addFilter("hashFilter", SimpleBeanPropertyFilter.serializeAllExcept(IGNORED_FIELDS))
                .setFailOnUnknownId(false);
        hashingMapper.setFilterProvider(filters);
        this.writer = hashingMapper.writer();
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
