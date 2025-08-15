package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.CoreShareResourceLimit;
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
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RoleLimitMapper.class, RoleShareResourceLimitMapper.class})
public interface RoleCoreMapper {

    @Mapping(target = "limits", qualifiedByName = "mapToCoreLimits")
    CoreRole mapRole(Role role, @Context Collection<Deployment> deployments);

    @Mapping(target = "keys", ignore = true)
    @Mapping(target = "limits", ignore = true)
    @Mapping(target = "share", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Role mapToRole(CoreRole role, Map<String, Set<String>> deploymentNamesByUserRole);

    @AfterMapping
    default void afterMapping(CoreRole coreRole, Map<String, Set<String>> deploymentNamesByUserRole, @MappingTarget Role role) {
        role.setLimits(mapRoleLimits(coreRole, deploymentNamesByUserRole));
        role.setShare(mapShareResourceLimitsToList(coreRole));
    }

    default List<RoleLimit> mapRoleLimits(CoreRole coreRole, Map<String, Set<String>> deploymentNamesByUserRole) {
        Set<String> deploymentNames = SetUtils.emptyIfNull(deploymentNamesByUserRole.get(coreRole.getName()));

        return MapUtils.emptyIfNull(coreRole.getLimits()).entrySet()
                .stream()
                .map(entry -> {
                    var deploymentName = entry.getKey();
                    boolean enabled = deploymentNames.contains(deploymentName);

                    var roleLimit = toLimit(entry.getValue(), coreRole.getName(), deploymentName, enabled);
                    boolean isDisabledEmptyLimit = !roleLimit.isEnabled() && roleLimit.getLimit().isEmpty();

                    return isDisabledEmptyLimit ? null : roleLimit;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    default List<RoleShareResourceLimit> mapShareResourceLimitsToList(CoreRole coreRole) {
        return MapUtils.emptyIfNull(coreRole.getShare()).entrySet()
                .stream()
                .map(e -> toShareResourceLimit(e.getValue(), coreRole.getName(), e.getKey()))
                .collect(Collectors.toList());
    }

    RoleLimit toLimit(CoreLimit limit, String role, String deploymentName, boolean enabled);

    RoleShareResourceLimit toShareResourceLimit(CoreShareResourceLimit limit, String role, String deploymentName);

}
