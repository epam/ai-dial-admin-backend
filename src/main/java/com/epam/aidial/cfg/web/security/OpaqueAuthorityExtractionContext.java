package com.epam.aidial.cfg.web.security;

import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

public record OpaqueAuthorityExtractionContext(
        RestTemplate restTemplate,
        String token,
        Map<String, Object> attributes,
        Set<String> emailClaims
) {
}