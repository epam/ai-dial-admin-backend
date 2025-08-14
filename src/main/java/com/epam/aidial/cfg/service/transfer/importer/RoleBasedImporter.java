package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;

import java.util.List;
import java.util.Map;

public abstract class RoleBasedImporter {

    protected void setRoleLimits(String deploymentName,
                                 List<RoleLimit> existingRoleLimits,
                                 Map<String, Role> roles,
                                 Deployment newDeployment,
                                 boolean isPreview) {
        // For the deployment we need to leave only those role limits which role is present in config. It means that:
        // 1. If role limit already exists - we save its new state here during deployment update and then during
        //    role update. If we try to remove it here and re-add it later during role import, we get
        //    org.hibernate.ObjectDeletedException: deleted instance passed to merge.
        // 2. If role limit doesn't exist - we just show its new state on preview, but save it later during role import.
        //    If we try to save it here and during role import, we get
        //    org.hibernate.NonUniqueObjectException: A different object with the same identifier value was already
        //    associated with the session.
        List<RoleLimit> roleLimits = roles.values().stream()
                .flatMap(role -> role.getLimits().stream())
                .filter(roleLimit -> roleLimit.getDeploymentName().equals(deploymentName))
                .filter(roleLimit -> isPreview || !isNewRoleLimit(roleLimit, existingRoleLimits))
                .toList();

        newDeployment.setRoleLimits(roleLimits);
    }

    private boolean isNewRoleLimit(RoleLimit roleLimit, List<RoleLimit> existingRoleLimits) {
        return existingRoleLimits.stream()
                .noneMatch(existingRoleLimit -> isSameRoleLimit(existingRoleLimit, roleLimit));
    }

    private boolean isSameRoleLimit(RoleLimit roleLimit1, RoleLimit roleLimit2) {
        return roleLimit1.getRole().equals(roleLimit2.getRole())
                && roleLimit1.getDeploymentName().equals(roleLimit2.getDeploymentName());
    }

}
