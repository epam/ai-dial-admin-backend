package com.epam.aidial.cfg.web.security;

import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public record OpaqueAuthorityExtractionContext(RestTemplate restTemplate, String token, Map<String, Object> attributes,
                                               List<String> emailClaims) {
}