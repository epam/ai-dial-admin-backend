package com.epam.aidial.cfg.web.security;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Slf4j
public class MultiPathGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String DEFAULT_AUTHORITY_PREFIX = "SCOPE_";
    private String authorityPrefix = DEFAULT_AUTHORITY_PREFIX;

    private List<String> authoritiesPaths;

    @NotNull
    @Override
    public List<GrantedAuthority> convert(@NotNull Jwt token) {
        var authorities = getAuthorities(token);
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authorityPrefix + authority))
                .collect(Collectors.toList());
    }

    private Set<String> getAuthorities(Jwt token) {
        Set<String> authorities = new HashSet<>();
        final Map<String, Object> claims = token.getClaims();
        authoritiesPaths.stream()
                .map(path -> extractPath(path, claims))
                .filter(Objects::nonNull)
                .forEach(c -> extractRoles(c, authorities));
        return authorities;
    }

    private Object extractPath(String path, Map<String, Object> claims) {
        Object current = claims;
        for (String partPath : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                log.warn("Authority path '{}' failed at '{}'", path, partPath);
                return null;
            }
            current = map.get(partPath);
        }

        if (current == null) {
            log.warn("Authority path '{}' extract as null", path);
        }
        return current;
    }

    private void extractRoles(Object value, Set<String> authorities) {
        if (value instanceof String role) {
            authorities.add(role);
        } else if (value instanceof Collection<?> listRoles) {
            for (Object role : listRoles) {
                if (role instanceof String roleString) {
                    authorities.add(roleString);
                } else {
                    log.warn("Unsupported authority value type: {}", role.getClass());
                }
            }
        }
    }
}