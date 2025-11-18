package com.epam.aidial.cfg.service.config.transfer.importer;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.DeploymentHolder;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import org.apache.commons.collections4.SetUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DeploymentHolderImporter {

    protected List<RoleLimit> getRoleLimits(String deploymentName, Set<String> userRoles) {
        return getRoleLimits(deploymentName, List.of(), userRoles);
    }

    protected List<RoleLimit> getRoleLimits(Deployment existingDeployment, Set<String> userRoles) {
        return getRoleLimits(existingDeployment.getName(), existingDeployment.getRoleLimits(), userRoles);
    }

    private List<RoleLimit> getRoleLimits(String deploymentName,
                                          List<RoleLimit> existingRoleLimits,
                                          Set<String> userRoles) {
        Set<String> safeUserRoles = SetUtils.emptyIfNull(userRoles);
        List<RoleLimit> result = new ArrayList<>(existingRoleLimits.size() + safeUserRoles.size());

        Set<String> existingRoles = existingRoleLimits.stream()
                .map(RoleLimit::getRole)
                .collect(Collectors.toSet());

        for (RoleLimit existingRoleLimit : existingRoleLimits) {
            boolean enabled = safeUserRoles.contains(existingRoleLimit.getRole());
            result.add(existingRoleLimit.toBuilder().enabled(enabled).build());
        }

        for (String userRole : safeUserRoles) {
            if (!existingRoles.contains(userRole)) {
                result.add(createRoleLimitForUserRole(deploymentName, userRole));
            }
        }

        return result;
    }

    private RoleLimit createRoleLimitForUserRole(String deploymentName, String roleName) {
        RoleLimit roleLimit = new RoleLimit();
        roleLimit.setRole(roleName);
        roleLimit.setDeploymentName(deploymentName);
        roleLimit.setEnabled(true);
        roleLimit.setLimit(new Limit());
        return roleLimit;
    }

    protected <T extends DeploymentHolder> List<String> getNextImportComponentNames(Collection<ImportComponent<T>> importComponents) {
        return importComponents.stream()
                .map(ImportComponent::getNext)
                .map(DeploymentHolder::getDeployment)
                .map(Deployment::getName)
                .toList();
    }

}
