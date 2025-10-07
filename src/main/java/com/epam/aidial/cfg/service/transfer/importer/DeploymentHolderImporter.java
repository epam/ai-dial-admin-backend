package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.DeploymentHolder;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class DeploymentHolderImporter {

    protected void setLimits(String deploymentName, Map<String, Role> roles, Deployment newDeployment) {
        setRoleLimits(deploymentName, List.of(), roles, newDeployment);
    }

    protected void setLimits(String deploymentName,
                             Deployment existingDeployment,
                             Map<String, Role> roles,
                             Deployment newDeployment) {
        setRoleLimits(deploymentName, existingDeployment.getRoleLimits(), roles, newDeployment);
    }

    private void setRoleLimits(String deploymentName,
                               List<RoleLimit> existingRoleLimits,
                               Map<String, Role> roles,
                               Deployment newDeployment) {
        // For the deployment we need to leave only those role limits which role is present in config.
        // It means that:
        // 1. If role limit already exists - we save its new state here during deployment update and then during
        //    role update. If we try to remove it here and re-add it later during role import, we get
        //    org.hibernate.ObjectDeletedException: deleted instance passed to merge.
        // 2. If role limit doesn't exist - we save it later during role import.
        //    If we try to save it here and during role import, we get
        //    org.hibernate.NonUniqueObjectException: A different object with the same identifier value was already
        //    associated with the session.
        List<RoleLimit> roleLimits = roles.values().stream()
                .flatMap(role -> role.getLimits().stream())
                .filter(roleLimit -> roleLimit.getDeploymentName().equals(deploymentName))
                .filter(roleLimit -> isExistingRoleLimit(roleLimit, existingRoleLimits))
                .toList();

        newDeployment.setRoleLimits(roleLimits);
    }

    private boolean isExistingRoleLimit(RoleLimit limit, List<RoleLimit> existingLimits) {
        return existingLimits.stream()
                .anyMatch(existingRoleLimit -> isSameRoleLimit(existingRoleLimit, limit));
    }

    private boolean isSameRoleLimit(RoleLimit limit1, RoleLimit limit2) {
        return limit1.getRole().equals(limit2.getRole())
                && limit1.getDeploymentName().equals(limit2.getDeploymentName());
    }


    protected <T extends DeploymentHolder> List<String> getNextImportComponentNames(Collection<ImportComponent<T>> importComponents) {
        return importComponents.stream()
                .map(ImportComponent::getNext)
                .map(DeploymentHolder::getDeployment)
                .map(Deployment::getName)
                .toList();
    }

    protected List<RoleLimit> getImportedLimits(Collection<ImportComponent<Role>> roleImportComponents) {
        Collection<Role> importedRoles = roleImportComponents.stream().map(ImportComponent::getNext).toList();
        return importedRoles.stream().map(Role::getLimits).flatMap(Collection::stream).toList();
    }

    protected void setImportedLimits(DeploymentHolder deploymentHolder, List<RoleLimit> importedRoleLimits) {
        String name = deploymentHolder.getDeployment().getName();

        List<RoleLimit> roleLimits = importedRoleLimits.stream()
                .filter(roleLimit -> roleLimit.getDeploymentName().equals(name))
                .toList();

        deploymentHolder.getDeployment().setRoleLimits(roleLimits);
    }

}
