package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnProperty(value = "config.rest.security.mode", havingValue = "oidc", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class RolesMappingResolver {

    public Map<String, Set<UserRole>> resolve(Map<String, Set<UserRole>> defaultRolesMapping,
                                              Map<String, Set<UserRole>> providerRolesMapping) {
        if (MapUtils.isNotEmpty(providerRolesMapping)) {
            Map<String, Set<UserRole>> result = new HashMap<>(MapUtils.emptyIfNull(defaultRolesMapping));
            result.putAll(providerRolesMapping);
            return result;
        }

        if (MapUtils.isNotEmpty(defaultRolesMapping)) {
            return defaultRolesMapping;
        }

        return Map.of();
    }
}
