package com.epam.aidial.cfg.service.hashing;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

@Component
public class HashCalculator {
    private final ObjectWriter writer;

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
            throw new RuntimeException("Failed to compute Hash", e);
        }
    }
}

