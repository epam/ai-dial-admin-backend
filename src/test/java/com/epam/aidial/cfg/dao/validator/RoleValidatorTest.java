package com.epam.aidial.cfg.dao.validator;

import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.validator.RoleValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleValidatorTest {

    private RoleValidator roleValidator;

    @BeforeEach
    void setUp() {
        roleValidator = new RoleValidator();
    }

    @Test
    void validateRoleDeletion_shouldThrowExceptionWhenDefaultRoleName() {
        assertThatThrownBy(() -> roleValidator.validateRoleDeletion("default"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("default role can not be deleted");
    }

    @Test
    void validateRoleDeletion_shouldDoNothingWhenNotDefaultRoleName() {
        assertThatNoException().isThrownBy(() -> roleValidator.validateRoleDeletion("role_name"));
    }

    @Test
    void validateRoleUpdate_shouldThrowExceptionWhenRoleNameIsUpdated() {
        Role role = new Role();
        role.setName("new_role_name");

        assertThatThrownBy(() -> roleValidator.validateRoleUpdate("role_name", role))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role with name: 'role_name' can not be renamed. New role name: 'new_role_name'");
    }

    @Test
    void validateRoleUpdate_shouldDoNothingWhenRoleNameIsNotUpdated() {
        Role role = new Role();
        role.setName("role_name");

        assertThatNoException().isThrownBy(() -> roleValidator.validateRoleUpdate("role_name", role));
    }
}