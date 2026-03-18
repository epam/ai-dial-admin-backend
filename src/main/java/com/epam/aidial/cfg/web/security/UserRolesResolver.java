package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class UserRolesResolver {

    private final Map<String, Set<UserRole>> rolesMapping;

    public Collection<? extends GrantedAuthority> resolve(Collection<? extends GrantedAuthority> grantedAuthorities) {
        Set<UserRole> userRoles = new HashSet<>();

        for (var grantedAuthority : grantedAuthorities) {
            String authority = grantedAuthority.getAuthority();
            Set<UserRole> authorityUserRoles = rolesMapping.get(authority);
            if (authorityUserRoles != null) {
                userRoles.addAll(authorityUserRoles);
            }
        }

        List<SimpleGrantedAuthority> result = userRoles.stream()
                .map(UserRole::name)
                .map(SimpleGrantedAuthority::new)
                .toList();

        if (result.isEmpty()) {
            log.warn("Authorization failed - roles mapping: {}, granted authorities: {}", rolesMapping, grantedAuthorities);
        }

        log.trace("Authorization state - roles mapping: {}, granted authorities: {}, user roles: {}", rolesMapping, grantedAuthorities, result);

        return result;
    }
}
