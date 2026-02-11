package com.epam.aidial.cfg.web.security;

import com.epam.aidial.ql.common.deserializer.TriFunction;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
public class OpaqueTokenCustomGrantedAuthoritiesConverters {

    private static final ParameterizedTypeReference<JsonNode> JSON_NODE = new ParameterizedTypeReference<>() {
    };

    public static final Map<String, TriFunction<RestTemplate, String, Map<String, Object>, List<GrantedAuthority>>> CONVERTERS = Map.of(
            "fn:getGoogleWorkspaceGroups", OpaqueTokenCustomGrantedAuthoritiesConverters::getGoogleAuthorities
    );

    private static List<GrantedAuthority> getGoogleAuthorities(RestTemplate restTemplate, String token, Map<String, Object> attributes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<JsonNode> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = makeRequest(() -> restTemplate.exchange(
                "https://content-cloudidentity.googleapis.com/v1/groups/-/memberships:searchDirectGroups?query=member_key_id=='{email}'",
                HttpMethod.GET,
                entity,
                JSON_NODE,
                attributes.get("email")
        ));

        checkResponse(response);

        List<GrantedAuthority> result = new ArrayList<>();

        for (var membership : response.getBody().get("memberships")) {
            String id = membership.get("groupKey").get("id").textValue();
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(id);
            result.add(simpleGrantedAuthority);
        }

        return result;
    }

    private <T> ResponseEntity<T> makeRequest(Supplier<ResponseEntity<T>> request) {
        try {
            return request.get();
        } catch (Exception ex) {
            log.debug("Failed to retrieve authorities", ex);
            throw new OAuth2IntrospectionException(ex.getMessage(), ex);
        }
    }

    private void checkResponse(ResponseEntity<?> response) {
        if (response.getStatusCode() != HttpStatus.OK) {
            log.debug("Failed to retrieve authorities. Response: {}", response);
            throw new OAuth2IntrospectionException("Failed to retrieve authorities. Status code: " + response.getStatusCode());
        }
    }

}
