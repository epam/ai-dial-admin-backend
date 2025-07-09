package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Role;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Pattern;

import static com.epam.aidial.core.config.CoreRole.DEFAULT_ROLE_NAME;

@Component
@Slf4j
public class RoleValidator {

    @Value("${validation.role.name:}")
    private String roleNameValidationPattern;

    public void validateRoleCreation(Role role) {
        final String roleName = role.getName();

        if (StringUtils.isEmpty(roleNameValidationPattern)) {
            log.debug("Role name validation pattern is empty, skipping validation for role: {}", roleName);
            return;
        }

        if (!Pattern.matches(roleNameValidationPattern, roleName)) {
            throw new IllegalArgumentException("Role name '" + roleName
                + "' does not match the required pattern: " + roleNameValidationPattern);
        }
    }

    public void validateRoleUpdate(String roleName, Role role) {
        if (!Objects.equals(roleName, role.getName())) {
            throw new IllegalArgumentException("Role with name: '" + roleName + "' can not be renamed. New role name: '" + role.getName() + "'");
        }
    }

    public void validateRoleDeletion(String roleName) {
        if (Objects.equals(roleName, DEFAULT_ROLE_NAME)) {
            throw new IllegalArgumentException(DEFAULT_ROLE_NAME + " role can not be deleted");
        }
    }
}
