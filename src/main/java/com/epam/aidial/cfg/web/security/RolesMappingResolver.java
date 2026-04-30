package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "config.rest.security.mode", havingValue = "oidc", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class RolesMappingResolver {

    public Map<String, Set<UserRole>> resolve(Set<String> defaultAllowedRoles,
                                              Map<String, Set<UserRole>> defaultRolesMapping,
                                              Set<String> providerAllowedRoles,
                                              Map<String, Set<UserRole>> providerRolesMapping) {
        if (MapUtils.isNotEmpty(providerRolesMapping)) {
            Map<String, Set<UserRole>> result = new HashMap<>(MapUtils.emptyIfNull(defaultRolesMapping));
            result.putAll(providerRolesMapping);
            return result;
        }

        if (CollectionUtils.isNotEmpty(providerAllowedRoles)) {
            Set<String> result = new HashSet<>(providerAllowedRoles);
            result.addAll(CollectionUtils.emptyIfNull(defaultAllowedRoles));
            return mapToFullAdmin(result);
        }

        if (MapUtils.isNotEmpty(defaultRolesMapping)) {
            return defaultRolesMapping;
        }

        return mapToFullAdmin(CollectionUtils.emptyIfNull(defaultAllowedRoles));
    }

    private Map<String, Set<UserRole>> mapToFullAdmin(Collection<String> roles) {
        return roles.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        role -> Set.of(UserRole.FULL_ADMIN)
                ));
    }
}
