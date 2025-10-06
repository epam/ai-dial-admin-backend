package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreRole;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {
        RoleLimitMapper.class, ShareResourceLimitCoreMapper.class, CostLimitCoreMapper.class
})
public interface RoleCoreMapper {

    @Mapping(target = "limits", qualifiedByName = "mapToCoreLimits")
    CoreRole mapRole(Role role, @Context Collection<Deployment> deployments);

    @Mapping(target = "keys", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "limits", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Role mapToRole(CoreRole role, Map<String, Set<String>> deploymentNamesByUserRole);

    @AfterMapping
    default void afterMapping(CoreRole coreRole, Map<String, Set<String>> deploymentNamesByUserRole, @MappingTarget Role role) {
        role.setLimits(mapRoleLimits(coreRole, deploymentNamesByUserRole));
    }

    default List<RoleLimit> mapRoleLimits(CoreRole coreRole, Map<String, Set<String>> deploymentNamesByUserRole) {
        Set<String> deploymentNames = SetUtils.emptyIfNull(deploymentNamesByUserRole.get(coreRole.getName()));

        return MapUtils.emptyIfNull(coreRole.getLimits()).entrySet()
                .stream()
                .map(entry -> {
                    var deploymentName = entry.getKey();
                    boolean enabled = deploymentNames.contains(deploymentName);

                    return entry.getValue() != null
                            ? toLimit(entry.getValue(), coreRole.getName(), deploymentName, enabled)
                            : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    RoleLimit toLimit(CoreLimit limit, String role, String deploymentName, boolean enabled);

}
