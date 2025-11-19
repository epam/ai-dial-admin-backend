package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class RoleValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";

    @Mock
    private IdFieldValidator idFieldValidator;
    @Mock
    private DisplayFieldsValidator displayFieldsValidator;
    private RoleValidator roleValidator;

    @BeforeEach
    void setUp() {
        roleValidator = new RoleValidator(idFieldValidator, displayFieldsValidator, null);
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

    @ParameterizedTest
    @ValueSource(strings = {"valid-name", "valid_name", "ValidName123", "name-123_456", "name.with.dots"})
    void validateRoleCreation_shouldNotThrowExceptionForValidName(String name) {
        // given
        ReflectionTestUtils.setField(roleValidator, "roleNameValidationPattern", NAME_VALIDATION_PATTERN);

        Role role = new Role();
        role.setName(name);

        // when/then
        assertThatNoException().isThrownBy(() -> roleValidator.validateRoleCreation(role));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid name with spaces", "invalid@name", "invalid#name", "invalid$name", 
            "name-that-is-way-too-long-for-validation-pattern"})
    void validateRoleCreation_shouldThrowExceptionForInvalidName(String name) {
        // given
        ReflectionTestUtils.setField(roleValidator, "roleNameValidationPattern", NAME_VALIDATION_PATTERN);

        Role role = new Role();
        role.setName(name);

        // when/then
        assertThatThrownBy(() -> roleValidator.validateRoleCreation(role))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern");
    }

    @Test
    void validateRoleCreation_shouldThrowExceptionWhenIdFieldValidatorThrows() {
        // given
        Role role = new Role();
        role.setName("role_name");

        doThrow(IllegalArgumentException.class).when(idFieldValidator)
                .validateName("Role", "role_name");

        // when/then
        Assertions.assertThatThrownBy(() -> roleValidator.validateRoleCreation(role))
                .isInstanceOf(IllegalArgumentException.class);
    }
}