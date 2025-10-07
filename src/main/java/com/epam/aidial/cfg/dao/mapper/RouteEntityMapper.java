package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.DeploymentTypeEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.RouteEntity;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.route.Route;
import org.apache.commons.collections4.ListUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DeploymentEntityMapper.class, UpstreamEntityMapper.class})
public abstract class RouteEntityMapper {

    @Autowired
    private DeploymentEntityMapper deploymentEntityMapper;

    public abstract Route toDomain(RouteEntity entity);

    public RouteEntity toEntity(Route domain, RouteEntity entity) {
        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> rolesForLimits = deploymentEntityMapper.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        List<RoleShareResourceLimit> roleShareResourceLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleShareResourceLimits());
        List<RoleEntity> rolesForResourceShareLimits = deploymentEntityMapper.findRolesByNames(roleShareResourceLimits.stream().map(RoleShareResourceLimit::getRole).toList());

        boolean isMappedDefaultRoleLimitOrShareResourceLimitDiffer = deploymentEntityMapper
                .isMappedDefaultRoleLimitOrShareResourceLimitDiffer(domain.getDeployment(), entity.getDeployment());

        RouteEntity updatedEntity = update(domain, entity);

        if (isMappedDefaultRoleLimitOrShareResourceLimitDiffer) {
            updatedEntity.setUpdatedAt(System.currentTimeMillis());
        }

        deploymentEntityMapper.setRoleLimits(updatedEntity.getDeployment(), rolesForLimits, roleLimits);
        deploymentEntityMapper.setRoleShareResourceLimits(updatedEntity.getDeployment(), rolesForResourceShareLimits, roleShareResourceLimits);
        updatedEntity.getDeployment().setType(DeploymentTypeEntity.ROUTE);

        return updatedEntity;
    }

    @Mapping(target = "deploymentName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract RouteEntity update(Route domain, @MappingTarget RouteEntity entity);
}
