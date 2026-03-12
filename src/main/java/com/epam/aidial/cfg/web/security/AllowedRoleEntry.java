package com.epam.aidial.cfg.web.security;

import lombok.extern.slf4j.Slf4j;

import java.util.EnumSet;
import java.util.Set;

/**
 * Parses a single {@code allowedRoles} entry string into an IdP role name and a set of {@link AdminRole} values.
 *
 * <p>Format: {@code IdpRole:[BACKEND_ROLE1,BACKEND_ROLE2]} or plain {@code IdpRole} (defaults to FULL_ADMIN).
 */
@Slf4j
public record AllowedRoleEntry(String idpRole, Set<AdminRole> adminRoles) {

    /**
     * Parses a single allowedRoles entry string.
     *
     * <p>If the entry contains {@code :[...]} (using lastIndexOf) and ends with {@code ]},
     * the IdP role name is extracted before {@code :[} and backend roles are parsed from within {@code [...]}.
     * Otherwise, the entire string is treated as the IdP role name and defaults to {@code FULL_ADMIN}.
     */
    public static AllowedRoleEntry parse(String entry) {
        if (entry == null || entry.isBlank()) {
            return new AllowedRoleEntry("", Set.of(AdminRole.FULL_ADMIN));
        }

        int colonBracketIndex = entry.lastIndexOf(":[");
        if (colonBracketIndex != -1 && entry.endsWith("]")) {
            String idpRole = entry.substring(0, colonBracketIndex).trim();
            String rolesStr = entry.substring(colonBracketIndex + 2, entry.length() - 1);
            Set<AdminRole> adminRoles = parseAdminRoles(rolesStr, entry);
            return new AllowedRoleEntry(idpRole, adminRoles);
        }

        return new AllowedRoleEntry(entry.trim(), Set.of(AdminRole.FULL_ADMIN));
    }

    private static Set<AdminRole> parseAdminRoles(String rolesStr, String originalEntry) {
        Set<AdminRole> result = EnumSet.noneOf(AdminRole.class);
        for (String role : rolesStr.split(",")) {
            String trimmed = role.trim();
            try {
                result.add(AdminRole.valueOf(trimmed));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown AdminRole '{}' in allowedRoles entry '{}', ignoring", trimmed, originalEntry);
            }
        }
        if (result.isEmpty()) {
            log.warn("No valid AdminRole found in allowedRoles entry '{}', defaulting to FULL_ADMIN", originalEntry);
            result.add(AdminRole.FULL_ADMIN);
        }
        return Set.copyOf(result);
    }
}
