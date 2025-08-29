package com.epam.aidial.cfg.service.hashing;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HashCalculator {
    private final ObjectWriter writer;
    public static final String ANY_HASH = "*";


    public HashCalculator(ObjectMapper mapper) {
        this.writer = mapper.copy()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
        .enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN)
        .writer();
    }

    public String calculateHash(Object body) {
        try {
            byte[] json = writer.withoutAttribute("createAt")
                    .withoutAttribute("updateAt")
                    .writeValueAsBytes(body);
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(json);
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
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

