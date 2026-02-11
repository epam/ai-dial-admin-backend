package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class DefaultOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<>() {
    };

    private final RestTemplate restTemplate;
    private final OpaqueTokenProviderConfig config;
    private final MultiPathGrantedAuthoritiesConverter<ClaimAccessor> multiPathGrantedAuthoritiesConverter;

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        Map<String, Object> attributes = introspectToken(token);

        List<GrantedAuthority> grantedAuthorities = extractAuthorities(token, attributes);

        Map<String, Object> newAttributes = new HashMap<>(attributes);
        newAttributes.put(OpaqueTokenProviderConfig.IDP_CLAIM, config.getName());

        return new OAuth2IntrospectionAuthenticatedPrincipal(
                (String) attributes.get(config.getPrincipalClaim()), newAttributes, grantedAuthorities);
    }

    private Map<String, Object> introspectToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        RequestEntity<Void> requestEntity = RequestEntity.get(config.getUserInfoEndpoint())
                .headers(headers)
                .build();

        ResponseEntity<Map<String, Object>> responseEntity = makeRequest(requestEntity);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            log.debug("Introspection endpoint responded with {}. Provider: {}", responseEntity, config);
            throw new OAuth2IntrospectionException("Introspection endpoint responded with " + responseEntity.getStatusCode());
        }

        return responseEntity.getBody();
    }

    private ResponseEntity<Map<String, Object>> makeRequest(RequestEntity<?> requestEntity) {
        try {
            return restTemplate.exchange(requestEntity, STRING_OBJECT_MAP);
        } catch (Exception ex) {
            log.debug("Token introspection request failed for provider {}", config, ex);
            throw new OAuth2IntrospectionException(ex.getMessage(), ex);
        }
    }

    private List<GrantedAuthority> extractAuthorities(String token, Map<String, Object> attributes) {
        List<String> roleClaims = config.getRoleClaims();
        if (isCustomizedRoleClaimsExtraction(roleClaims)) {
            var converterName = roleClaims.get(0);
            var converter = OpaqueTokenCustomGrantedAuthoritiesConverters.CONVERTERS.get(converterName);
            if (converter == null) {
                log.debug("Unable to find custom granted authorities converter for provider {}", config);
                throw new OAuth2IntrospectionException("Unable to find custom granted authorities converter: " + converterName);
            }

            return converter.apply(restTemplate, token, attributes);
        } else {
            return multiPathGrantedAuthoritiesConverter.convert(() -> attributes);
        }
    }

    private boolean isCustomizedRoleClaimsExtraction(List<String> roleClaims) {
        List<String> safeRoleClaims = ListUtils.emptyIfNull(roleClaims);
        return safeRoleClaims.size() == 1 && safeRoleClaims.get(0).startsWith("fn:");
    }
}
