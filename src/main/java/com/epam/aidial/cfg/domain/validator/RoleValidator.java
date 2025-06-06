package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Role;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.epam.aidial.core.config.CoreRole.DEFAULT_ROLE_NAME;

@Component
public class RoleValidator {

    public void validateRoleDeletion(String roleName) {
        if (Objects.equals(roleName, DEFAULT_ROLE_NAME)) {
            throw new IllegalArgumentException(DEFAULT_ROLE_NAME + " role can not be deleted");
        }
    }

    public void validateRoleUpdate(String roleName, Role role) {
        if (!Objects.equals(roleName, role.getName())) {
            throw new IllegalArgumentException("Role with name: '" + roleName + "' can not be renamed. New role name: '" + role.getName() + "'");
        }
    }
}
