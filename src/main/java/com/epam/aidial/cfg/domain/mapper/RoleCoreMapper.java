package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.core.config.CoreLimit;
import com.epam.aidial.core.config.CoreRole;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", uses = {
        RoleLimitMapper.class, ShareResourceLimitCoreMapper.class, CostLimitCoreMapper.class
})
public interface RoleCoreMapper {

    @Mapping(target = "limits", qualifiedByName = "mapToCoreLimits")
    CoreRole mapRole(Role role, @Context Collection<Deployment> deployments);

    Role copy(Role role);

    default Role mapToRole(CoreRole coreRole, String name, List<RoleLimit> roleLimits) {
        Role role = new Role();
        role.setDisplayName(name);
        return mapToRole(coreRole, roleLimits, role);
    }

    @Mapping(target = "limits", source = "roleLimits")
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "keys", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Role mapToRole(CoreRole coreRole, List<RoleLimit> roleLimits, @MappingTarget Role role);

    RoleLimit toLimit(CoreLimit limit, String role, String deploymentName, boolean enabled);

}
