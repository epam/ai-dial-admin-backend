package com.epam.aidial.cfg.web.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AllowedRoleEntryTest {

    @Test
    void parsePlainRole_defaultsToFullAdmin() {
        var entry = AllowedRoleEntry.parse("ConfigAdmin");
        assertThat(entry.idpRole()).isEqualTo("ConfigAdmin");
        assertThat(entry.adminRoles()).containsExactly(AdminRole.FULL_ADMIN);
    }

    @Test
    void parsePlainRole_admin_defaultsToFullAdmin() {
        var entry = AllowedRoleEntry.parse("admin");
        assertThat(entry.idpRole()).isEqualTo("admin");
        assertThat(entry.adminRoles()).containsExactly(AdminRole.FULL_ADMIN);
    }

    @Test
    void parseReadOnlyBracketSyntax() {
        var entry = AllowedRoleEntry.parse("Viewer:[READ_ONLY_ADMIN]");
        assertThat(entry.idpRole()).isEqualTo("Viewer");
        assertThat(entry.adminRoles()).containsExactly(AdminRole.READ_ONLY_ADMIN);
    }

    @Test
    void parseFullAdminBracketSyntax() {
        var entry = AllowedRoleEntry.parse("ConfigAdmin:[FULL_ADMIN]");
        assertThat(entry.idpRole()).isEqualTo("ConfigAdmin");
        assertThat(entry.adminRoles()).containsExactly(AdminRole.FULL_ADMIN);
    }

    @Test
    void parseMultipleBackendRoles() {
        var entry = AllowedRoleEntry.parse("Super:[FULL_ADMIN,READ_ONLY_ADMIN]");
        assertThat(entry.idpRole()).isEqualTo("Super");
        assertThat(entry.adminRoles()).containsExactlyInAnyOrder(AdminRole.FULL_ADMIN, AdminRole.READ_ONLY_ADMIN);
    }

    @Test
    void parseRoleWithColonsInName() {
        var entry = AllowedRoleEntry.parse("role:with:colons:[FULL_ADMIN]");
        assertThat(entry.idpRole()).isEqualTo("role:with:colons");
        assertThat(entry.adminRoles()).containsExactly(AdminRole.FULL_ADMIN);
    }

    @Test
    void parseInvalidBackendRole_defaultsToFullAdmin() {
        var entry = AllowedRoleEntry.parse("SomeRole:[UNKNOWN_ROLE]");
        assertThat(entry.idpRole()).isEqualTo("SomeRole");
        assertThat(entry.adminRoles()).containsExactly(AdminRole.FULL_ADMIN);
    }

    @Test
    void parseNullEntry_returnsEmpty() {
        var entry = AllowedRoleEntry.parse(null);
        assertThat(entry.idpRole()).isEqualTo("");
        assertThat(entry.adminRoles()).containsExactly(AdminRole.FULL_ADMIN);
    }

    @Test
    void parseBlankEntry_returnsEmpty() {
        var entry = AllowedRoleEntry.parse("  ");
        assertThat(entry.idpRole()).isEqualTo("");
        assertThat(entry.adminRoles()).containsExactly(AdminRole.FULL_ADMIN);
    }

    @Test
    void parseWithSpacesAroundBrackets() {
        var entry = AllowedRoleEntry.parse("  Viewer  :[READ_ONLY_ADMIN]");
        assertThat(entry.idpRole()).isEqualTo("Viewer");
        assertThat(entry.adminRoles()).containsExactly(AdminRole.READ_ONLY_ADMIN);
    }

    @Test
    void resolveAdminRoles_returnsMappedRoles() {
        var mapping = java.util.Map.of(
                "ConfigAdmin", Set.of(AdminRole.FULL_ADMIN),
                "Viewer", Set.of(AdminRole.READ_ONLY_ADMIN)
        );
        var result = IdentityProviderUtils.resolveAdminRoles(mapping, Set.of("ConfigAdmin", "Viewer"));
        assertThat(result).containsExactlyInAnyOrder(AdminRole.FULL_ADMIN, AdminRole.READ_ONLY_ADMIN);
    }

    @Test
    void resolveAdminRoles_unknownRole_returnsEmpty() {
        var mapping = java.util.Map.of("ConfigAdmin", Set.of(AdminRole.FULL_ADMIN));
        var result = IdentityProviderUtils.resolveAdminRoles(mapping, Set.of("UnknownRole"));
        assertThat(result).isEmpty();
    }
}
