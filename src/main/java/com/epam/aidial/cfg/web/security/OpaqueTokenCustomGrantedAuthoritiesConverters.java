package com.epam.aidial.cfg.web.security;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.utils.MapExtractionUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
@LogExecution
public class OpaqueTokenCustomGrantedAuthoritiesConverters {

    private static final ParameterizedTypeReference<JsonNode> JSON_NODE = new ParameterizedTypeReference<>() {
    };

    public static final Map<String, Function<OpaqueAuthorityExtractionContext, List<GrantedAuthority>>> CONVERTERS = Map.of(
            "fn:getGoogleWorkspaceGroups", OpaqueTokenCustomGrantedAuthoritiesConverters::getGoogleAuthorities
    );

    private static List<GrantedAuthority> getGoogleAuthorities(OpaqueAuthorityExtractionContext context) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(context.token());

        HttpEntity<JsonNode> entity = new HttpEntity<>(headers);

        var email = MapExtractionUtils.extractFirstNonNullValue(context.attributes(), context.emailClaims()).orElseThrow(
                () -> new OAuth2IntrospectionException("Email claim is missing in token"));

        ResponseEntity<JsonNode> response = makeRequest(() -> context.restTemplate().exchange(
                "https://content-cloudidentity.googleapis.com/v1/groups/-/memberships:searchDirectGroups?query=member_key_id=='{email}'",
                HttpMethod.GET,
                entity,
                JSON_NODE,
                email
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
            ResponseEntity<T> response = request.get();
            log.debug("Response: {}", response);
            return response;
        } catch (Exception ex) {
            log.warn("Failed to retrieve authorities", ex);
            throw new OAuth2IntrospectionException(ex.getMessage(), ex);
        }
    }

    private void checkResponse(ResponseEntity<?> response) {
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to retrieve authorities. Response: {}", response);
            throw new OAuth2IntrospectionException("Failed to retrieve authorities. Status code: " + response.getStatusCode());
        }
    }

}